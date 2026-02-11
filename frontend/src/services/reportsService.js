import api from './api'

export const reportsService = {
  getErrorSummary: async (dateRange = '7d') => {
    const response = await api.get(`/reports/error-summary?dateRange=${dateRange}`)
    return response.data
  },

  getApplicationPerformance: async (dateRange = '7d') => {
    const response = await api.get(`/reports/application-performance?dateRange=${dateRange}`)
    return response.data
  },

  getSeverityBreakdown: async (dateRange = '7d') => {
    const response = await api.get(`/reports/severity-breakdown?dateRange=${dateRange}`)
    return response.data
  },

  getTrendAnalysis: async (dateRange = '7d') => {
    const response = await api.get(`/reports/trend-analysis?dateRange=${dateRange}`)
    return response.data
  }
}