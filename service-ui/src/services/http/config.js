// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'

export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  enableLogging: import.meta.env.MODE === 'development',
}

export default apiConfig

