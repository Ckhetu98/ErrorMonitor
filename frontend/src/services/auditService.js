import api from './api'

export const auditService = {
  // Get all audit logs
  getAuditLogs: async () => {
    try {
      const response = await api.get('/audit')
      return response.data.logs || []
    } catch (error) {
      console.error('Error fetching audit logs:', error)
      if (error.response?.status === 401) {
        console.warn('Unauthorized access to audit logs - user may not have permission')
      } else if (error.response?.status === 403) {
        console.warn('Forbidden access to audit logs - insufficient permissions')
      }
      return []
    }
  },

  // Create audit log entry
  createAuditLog: async (auditData) => {
    try {
      const response = await api.post('/audit/log', auditData)
      return response.data
    } catch (error) {
      console.error('Error creating audit log:', error)
      throw error
    }
  }
}