import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  stats: {
    totalErrors: 0,
    criticalErrors: 0,
    resolvedErrors: 0,
    activeApplications: 0,
    avgResponseTime: 0,
    uptime: 0
  },
  errorTrends: [],
  severityBreakdown: [],
  applicationErrors: [],
  recentErrors: [],
  isLoading: false,
  error: null,
  lastUpdated: null
}

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    setLoading: (state, action) => {
      state.isLoading = action.payload
    },
    setStats: (state, action) => {
      state.stats = action.payload
      state.lastUpdated = new Date().toISOString()
    },
    setErrorTrends: (state, action) => {
      state.errorTrends = action.payload
    },
    setSeverityBreakdown: (state, action) => {
      state.severityBreakdown = action.payload
    },
    setApplicationErrors: (state, action) => {
      state.applicationErrors = action.payload
    },
    setRecentErrors: (state, action) => {
      state.recentErrors = action.payload
    },
    addRecentError: (state, action) => {
      state.recentErrors.unshift(action.payload)
      if (state.recentErrors.length > 10) {
        state.recentErrors = state.recentErrors.slice(0, 10)
      }
    },
    updateStats: (state, action) => {
      state.stats = { ...state.stats, ...action.payload }
      state.lastUpdated = new Date().toISOString()
    },
    setError: (state, action) => {
      state.error = action.payload
    },
    clearError: (state) => {
      state.error = null
    }
  }
})

export const {
  setLoading,
  setStats,
  setErrorTrends,
  setSeverityBreakdown,
  setApplicationErrors,
  setRecentErrors,
  addRecentError,
  updateStats,
  setError,
  clearError
} = dashboardSlice.actions

export default dashboardSlice.reducer