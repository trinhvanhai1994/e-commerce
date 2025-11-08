// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://debase.vn'

export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  enableMock: import.meta.env.VITE_ENABLE_MOCK === 'true',
  enableLogging: import.meta.env.MODE === 'development',
}

export default apiConfig

