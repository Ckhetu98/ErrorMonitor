import api from './api'

export const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password })
    return response.data
  },

  verifyOTP: async (userId, code) => {
    const response = await api.post('/auth/verify-otp', { userId, code })
    const { token, user } = response.data
    
    // Store auth data after successful OTP verification
    if (token && user) {
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    }
    
    return response.data
  },

  resendOTP: async (userId) => {
    const response = await api.post('/auth/resend-otp', { userId })
    return response.data
  },

  googleLogin: async (googleUser) => {
    const response = await api.post('/auth/google', {
      token: googleUser.credential || googleUser.access_token,
      email: googleUser.email,
      name: googleUser.name
    })
    
    // Google auth returns requiresTwoFactor, userId, message
    // Token and user data come after OTP verification
    return response.data
  },

  register: async (userData) => {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  logout: async () => {
    try {
      // Clear all auth-related data
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('tpms_user') // Also clear TPMS data if exists
      
      // Clear any cached API data
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } catch (error) {
      console.error('Logout error:', error)
    }
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user')
    return user ? JSON.parse(user) : null
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('token')
  },

  // 2FA methods
  enable2FA: async (userId) => {
    const response = await api.post('/auth/enable-2fa', { userId })
    return response.data
  },

  disable2FA: async (userId) => {
    const response = await api.post('/auth/disable-2fa', { userId })
    return response.data
  }
}