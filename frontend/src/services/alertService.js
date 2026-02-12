import api from './api'

export const alertService = {
  // Get all alerts
  getAllAlerts: async () => {
    try {
      const response = await api.get('/alerts')
      return response.data
    } catch (error) {
      console.error('Error fetching alerts:', error)
      return []
    }
  },

  // Get unresolved alerts
  getUnresolvedAlerts: async () => {
    try {
      const response = await api.get('/alerts/unresolved')
      return response.data
    } catch (error) {
      console.error('Error fetching unresolved alerts:', error)
      return []
    }
  },

  // Get alert stats
  getAlertStats: async () => {
    try {
      const response = await api.get('/alerts/stats')
      return response.data
    } catch (error) {
      console.error('Error fetching alert stats:', error)
      return { totalAlerts: 0, unresolvedAlerts: 0, resolvedAlerts: 0, criticalAlerts: 0 }
    }
  },

  // Create alert
  createAlert: async (alertData) => {
    const response = await api.post('/alerts', alertData)
    return response.data
  },

  // Resolve alert
  resolveAlert: async (alertId) => {
    const response = await api.put(`/alerts/${alertId}/resolve`)
    return response.data
  },

  // Delete alert
  deleteAlert: async (alertId) => {
    const response = await api.delete(`/alerts/${alertId}`)
    return response.data
  }
}