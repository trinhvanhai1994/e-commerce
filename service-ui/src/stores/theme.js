import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => {
    // Load theme preference from localStorage
    const savedTheme = localStorage.getItem('theme-preference')
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    // Default to saved preference, or system preference, or light
    const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light')
    
    // Apply theme to document
    if (initialTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    
    return {
      isDark: initialTheme === 'dark'
    }
  },
  actions: {
    // Toggle theme
    toggleTheme() {
      this.isDark = !this.isDark
      this.applyTheme()
    },
    // Set theme explicitly
    setTheme(theme) {
      this.isDark = theme === 'dark'
      this.applyTheme()
    },
    // Apply theme to DOM and save to localStorage
    applyTheme() {
      if (this.isDark) {
        document.documentElement.classList.add('dark')
        localStorage.setItem('theme-preference', 'dark')
      } else {
        document.documentElement.classList.remove('dark')
        localStorage.setItem('theme-preference', 'light')
      }
    }
  },
  getters: {
    currentTheme: (state) => state.isDark ? 'dark' : 'light'
  }
})

