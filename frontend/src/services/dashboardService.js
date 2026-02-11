import api from './api'
import { tpmsService } from './tpmsService'

export const dashboardService = {
  // Get dashboard statistics
  getDashboardStats: async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      
      // Use backend for role-based dashboard stats
      console.log('🔍 Dashboard stats for role:', user.role)
      const response = await fetch('http://localhost:8080/api/dashboard/stats', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        console.log('📊 Dashboard stats from backend:', data)
        return {
          totalErrors: data.totalErrors || 0,
          criticalErrors: data.criticalErrors || 0,
          resolvedErrors: data.resolvedErrors || 0,
          activeApplications: data.activeApplications || 0,
          activeUsers: data.activeUsers || 1,
          avgResponseTime: data.avgResponseTime || 85,
          errorRate: data.errorRate || 0
        }
      }
      
      return {
        totalErrors: 0,
        criticalErrors: 0,
        resolvedErrors: 0,
        activeApplications: 0,
        activeUsers: 0,
        avgResponseTime: 85,
        errorRate: 0
      }
    } catch (error) {
      console.error('Dashboard API error:', error)
      return {
        totalErrors: 0,
        criticalErrors: 0,
        resolvedErrors: 0,
        activeApplications: 0,
        activeUsers: 0,
        avgResponseTime: 85,
        errorRate: 0
      }
    }
  },

  // Get recent errors
  getRecentErrors: async () => {
    try {
      const response = await fetch('http://localhost:8080/api/dashboard/recent-errors', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const errors = await response.json()
        return errors.slice(0, 5).map(error => ({
          id: error.id,
          message: error.error,
          severity: error.severity,
          timestamp: error.timestamp,
          application: error.application
        }))
      }
      return []
    } catch (error) {
      console.error('Recent errors API error:', error)
      return []
    }
  },

  // Get active alerts
  getActiveAlerts: async () => {
    try {
      const response = await api.get('/dashboard/active-alerts')
      return response.data
    } catch (error) {
      console.error('Active alerts API error:', error)
      return []
    }
  },

  // Get application health
  getApplicationHealth: async () => {
    try {
      console.log('Fetching application health data...')
      const response = await api.get('/dashboard/application-health')
      console.log('Application health response:', response.data)
      return response.data
    } catch (error) {
      console.error('Application health API error:', error)
      return []
    }
  },

  // Get recent TPMS activity
  getRecentTPMSActivity: async () => {
    try {
      const applications = await tpmsService.getRecentApplications()
      return applications || []
    } catch (error) {
      console.error('TPMS activity error:', error)
      return []
    }
  }
}