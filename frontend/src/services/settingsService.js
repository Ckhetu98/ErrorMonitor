import api from './api'

export const settingsService = {
  // Security settings
  getSecuritySettings: async () => {
    const response = await api.get('/settings/security')
    return response.data
  },

  saveSecuritySettings: async (settings) => {
    const response = await api.post('/settings/security', {
      sessionTimeoutMinutes: parseInt(settings.sessionTimeout),
      maxLoginAttempts: parseInt(settings.maxLoginAttempts)
    })
    return response.data
  },

  // 2FA settings - individual user
  enable2FA: async (userId) => {
    const response = await api.post('/auth/enable-2fa', { userId })
    return response.data
  },

  disable2FA: async (userId) => {
    const response = await api.post('/auth/disable-2fa', { userId })
    return response.data
  },

  // Global 2FA control (Admin only)
  toggleGlobal2FA: async (enabled) => {
    console.log('Calling toggleGlobal2FA with enabled:', enabled)
    const response = await api.post('/auth/toggle-global-2fa', { enabled })
    console.log('toggleGlobal2FA response:', response.data)
    return response.data
  },

  get2FAStatus: async () => {
    console.log('Calling get2FAStatus')
    const response = await api.get('/auth/2fa-status')
    console.log('get2FAStatus response:', response.data)
    return response.data
  }
}