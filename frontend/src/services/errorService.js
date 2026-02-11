import api from './api'

export const errorService = {
  // Get all errors
  getAllErrors: async () => {
    try {
      const response = await api.get('/errorlogs')
      return response.data
    } catch (error) {
      console.error('Error fetching all errors:', error)
      return []
    }
  },

  // Get error logs with filtering and pagination
  getErrorLogs: async (filters = {}) => {
    try {
      const params = new URLSearchParams()
      
      if (filters.severity) params.append('severity', filters.severity)
      if (filters.status) params.append('status', filters.status)
      if (filters.page) params.append('page', filters.page)
      if (filters.pageSize) params.append('pageSize', filters.pageSize)
      
      const response = await api.get(`/errorlogs?${params.toString()}`)
      return response.data
    } catch (error) {
      console.error('Error fetching error logs:', error)
      return []
    }
  },

  // Log a new error
  logError: async (errorData) => {
    const response = await api.post('/errorlogs/log', errorData)
    return response.data
  },

  // Resolve an error
  resolveError: async (errorId) => {
    const response = await api.put(`/errorlogs/${errorId}/resolve`)
    return response.data
  },

  // Delete an error permanently
  deleteError: async (errorId) => {
    const response = await api.delete(`/errorlogs/${errorId}`)
    return response.data
  }
}