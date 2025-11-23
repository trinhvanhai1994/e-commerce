// Visitor Tracking Service - Track website visitors

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'
const VISITOR_SESSION_COOKIE = 'visitor_session_id'
const VISITOR_TRACKED_KEY = 'visitor_tracked_session' // Key để check đã track trong session này chưa

/**
 * Get session ID from cookie
 */
function getSessionIdFromCookie() {
  const cookies = document.cookie.split(';')
  for (let cookie of cookies) {
    const [name, value] = cookie.trim().split('=')
    if (name === VISITOR_SESSION_COOKIE) {
      return decodeURIComponent(value)
    }
  }
  return null
}

/**
 * Set session ID in cookie
 */
function setSessionIdCookie(sessionId) {
  const maxAge = 365 * 24 * 60 * 60 // 1 year in seconds
  document.cookie = `${VISITOR_SESSION_COOKIE}=${encodeURIComponent(sessionId)}; max-age=${maxAge}; path=/; SameSite=Lax`
}

/**
 * Generate a new session ID
 */
function generateSessionId() {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`
}

/**
 * Check if visitor has been tracked in current session
 * Sử dụng sessionStorage để check trong cùng browser session
 */
function hasTrackedInSession() {
  try {
    const tracked = sessionStorage.getItem(VISITOR_TRACKED_KEY)
    if (tracked) {
      const trackedData = JSON.parse(tracked)
      const now = Date.now()
      // Check if tracked within last 5 minutes (tránh refresh page nhiều lần)
      if (now - trackedData.timestamp < 5 * 60 * 1000) {
        return true
      }
    }
    return false
  } catch (e) {
    return false
  }
}

/**
 * Mark visitor as tracked in current session
 */
function markAsTracked(sessionId) {
  try {
    sessionStorage.setItem(VISITOR_TRACKED_KEY, JSON.stringify({
      sessionId: sessionId,
      timestamp: Date.now()
    }))
  } catch (e) {
    // Ignore if sessionStorage is not available
  }
}

/**
 * Track a visitor visit
 * This should be called when the page loads
 * Chỉ track 1 lần mỗi session để tránh duplicate calls
 */
export async function trackVisit() {
  try {
    // Check if already tracked in this session
    if (hasTrackedInSession()) {
      return null // Đã track rồi, không cần track lại
    }
    
    // Get or create session ID
    let sessionId = getSessionIdFromCookie()
    if (!sessionId) {
      sessionId = generateSessionId()
      setSessionIdCookie(sessionId)
    }
    
    // Get current page path
    const pagePath = window.location.pathname
    
    // Call tracking API
    const response = await fetch(`${API_BASE_URL}/api/public/visitors/track`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        sessionId: sessionId,
        pagePath: pagePath
      }),
      credentials: 'include' // Include cookies in request
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()
    
    // Update cookie if server returned a new session ID
    if (data.data && data.data !== sessionId) {
      setSessionIdCookie(data.data)
      sessionId = data.data
    }
    
    // Mark as tracked in session
    markAsTracked(sessionId)
    
    return data
  } catch (error) {
    // Silently fail - don't interrupt user experience
    console.error('Failed to track visit:', error)
    return null
  }
}

/**
 * Track visit on page load
 * Call this in App.vue or main.js
 */
export function initVisitorTracking() {
  // Track visit when app loads
  trackVisit()
  
  // Track visit on route changes (for SPA)
  if (typeof window !== 'undefined' && window.addEventListener) {
    // This will be handled by router navigation guards if needed
  }
}

