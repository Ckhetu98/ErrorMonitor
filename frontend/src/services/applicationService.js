import api from './api'

export const applicationService = {
  // Get all applications
  getApplications: async () => {
    const response = await api.get('/applications')
    return response.data
  },

  // Create new application
  createApplication: async (applicationData) => {
    // Map frontend fields to backend fields
    const backendData = {
      name: applicationData.appName,
      description: applicationData.appDescription,
      technology: applicationData.technology,
      version: applicationData.version,
      baseUrl: applicationData.baseUrl,
      healthCheckUrl: applicationData.healthCheckUrl
    }
    const response = await api.post('/applications', backendData)
    return response.data
  },

  // Update application
  updateApplication: async (id, applicationData) => {
    // Map frontend fields to backend fields
    const backendData = {
      name: applicationData.appName,
      description: applicationData.appDescription,
      technology: applicationData.technology,
      version: applicationData.version,
      baseUrl: applicationData.baseUrl,
      healthCheckUrl: applicationData.healthCheckUrl,
      isActive: applicationData.isActive
    }
    const response = await api.put(`/applications/${id}`, backendData)
    return response.data
  },

  // Pause application
  pauseApplication: async (id) => {
    const response = await api.put(`/applications/${id}/pause`)
    return response.data
  },

  // Resume application
  resumeApplication: async (id) => {
    const response = await api.put(`/applications/${id}/resume`)
    return response.data
  },

  // Delete application
  deleteApplication: async (id) => {
    const response = await api.delete(`/applications/${id}`)
    return response.data
  }
}