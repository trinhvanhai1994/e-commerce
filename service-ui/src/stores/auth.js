import { defineStore } from 'pinia'
import { customerService } from '@/services/customer.service.js'

// Customer session (task-003). Kept apart from the admin token (localStorage 'authToken', sent by httpClient on
// every request): the two never overwrite each other, and the customer token goes only to customer APIs.
const STORAGE_KEY = 'customerSession'
// Refresh a little before the access token really expires
const EXPIRY_MARGIN_MS = 30 * 1000

// Not reactive state: a function to run once the customer has logged in (e.g. open the review form)
let pendingAction = null
let refreshInFlight = null

function readStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function writeStorage(session) {
  try {
    if (session) localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Private mode or blocked storage: the session lives only in memory for this tab
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    customer: null,
    accessToken: null,
    refreshToken: null,
    accessExpiresAt: 0,
    modalOpen: false,
    // 'login' | 'register' | 'forgot'
    modalView: 'login',
    // Short line shown on top of the popup, e.g. "Đăng nhập để viết đánh giá"
    modalReason: '',
  }),

  getters: {
    isLoggedIn: (state) => !!state.customer && !!state.refreshToken,
    displayName: (state) => state.customer?.fullName || state.customer?.phone || '',
  },

  actions: {
    /** Call once at app start: restore the session and follow logins/logouts made in other tabs. */
    init() {
      this.loadFromStorage()
      window.addEventListener('storage', (event) => {
        if (event.key === STORAGE_KEY) this.loadFromStorage()
      })
    },

    loadFromStorage() {
      const s = readStorage()
      this.customer = s?.customer || null
      this.accessToken = s?.accessToken || null
      this.refreshToken = s?.refreshToken || null
      this.accessExpiresAt = s?.accessExpiresAt || 0
    },

    applySession(res) {
      this.customer = res.customer
      this.accessToken = res.accessToken
      this.refreshToken = res.refreshToken
      this.accessExpiresAt = Date.now() + (res.expiresIn || 0) * 1000 - EXPIRY_MARGIN_MS
      this.persist()
    },

    persist() {
      writeStorage(this.refreshToken ? {
        customer: this.customer,
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        accessExpiresAt: this.accessExpiresAt,
      } : null)
    },

    clearSession() {
      this.customer = null
      this.accessToken = null
      this.refreshToken = null
      this.accessExpiresAt = 0
      writeStorage(null)
    },

    setCustomer(profile) {
      this.customer = profile
      this.persist()
    },

    async login(form) {
      this.applySession(await customerService.login(form))
      this.finishModal()
    },

    async register(form) {
      this.applySession(await customerService.register(form))
      this.finishModal()
    },

    async logout() {
      const refreshToken = this.refreshToken
      this.clearSession()
      pendingAction = null
      if (refreshToken) {
        try {
          await customerService.logout(refreshToken)
        } catch {
          // Already logged out locally; the server session expires by itself
        }
      }
    },

    /** Valid access token, refreshed when needed. Throws (status 401) when the session is over. */
    async getAccessToken() {
      if (!this.refreshToken) {
        const error = new Error('Vui lòng đăng nhập để tiếp tục.')
        error.status = 401
        throw error
      }
      if (this.accessToken && Date.now() < this.accessExpiresAt) return this.accessToken
      return this.refreshSession()
    },

    async refreshSession() {
      // Several calls may hit an expired token at once: share one refresh
      if (!refreshInFlight) {
        const token = this.refreshToken
        refreshInFlight = customerService.refresh(token)
          .then((res) => {
            this.applySession(res)
            return res.accessToken
          })
          .catch((e) => {
            // Another tab may have rotated the token already: use what it stored
            const stored = readStorage()
            if (stored?.refreshToken && stored.refreshToken !== token) {
              this.loadFromStorage()
              return this.accessToken
            }
            if (e.status === 401) this.clearSession()
            throw e
          })
          .finally(() => {
            refreshInFlight = null
          })
      }
      return refreshInFlight
    },

    /**
     * Runs fn(accessToken). On 401 refreshes once and retries; if the session is over, clears it, opens the login
     * popup and rethrows (callers keep what the customer typed).
     */
    async authorized(fn, reason = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.') {
      try {
        return await fn(await this.getAccessToken())
      } catch (e) {
        if (e.status !== 401) throw e
        try {
          return await fn(await this.refreshSession())
        } catch (retryError) {
          if (retryError.status === 401) {
            this.clearSession()
            this.openModal('login', reason)
          }
          throw retryError
        }
      }
    },

    /** Runs action now when logged in; otherwise opens the popup and runs it right after login or sign-up. */
    requireLogin(action, reason = '') {
      if (this.isLoggedIn) {
        action?.()
        return
      }
      pendingAction = action || null
      this.openModal('login', reason)
    },

    openModal(view = 'login', reason = '') {
      this.modalView = view
      this.modalReason = reason
      this.modalOpen = true
    },

    /** Closed by the customer: the pending action is dropped. */
    closeModal() {
      this.modalOpen = false
      pendingAction = null
    },

    finishModal() {
      this.modalOpen = false
      const action = pendingAction
      pendingAction = null
      action?.()
    },
  },
})
