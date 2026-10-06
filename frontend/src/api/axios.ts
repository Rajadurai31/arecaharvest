import axios from 'axios'
import toast from 'react-hot-toast'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30_000,
})

// ── Request interceptor: attach JWT ──────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ── Response interceptor: uniform error handling ─────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    const message =
      error.response?.data?.message ||
      error.message ||
      'An unexpected error occurred'

    // Show validation errors as a summary
    if (error.response?.data?.data && typeof error.response.data.data === 'object') {
      const validationErrors = Object.values(error.response.data.data).join(', ')
      toast.error(`Validation: ${validationErrors}`)
    } else {
      toast.error(message)
    }

    return Promise.reject(error)
  },
)

export default api
