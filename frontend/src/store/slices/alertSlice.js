import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  alerts: [],
  activeAlerts: [],
  isLoading: false,
  error: null,
  unreadCount: 0
}

const alertSlice = createSlice({
  name: 'alerts',
  initialState,
  reducers: {
    setLoading: (state, action) => {
      state.isLoading = action.payload
    },
    setAlerts: (state, action) => {
      state.alerts = action.payload
      state.unreadCount = action.payload.filter(alert => !alert.isRead).length
    },
    setActiveAlerts: (state, action) => {
      state.activeAlerts = action.payload
    },
    addAlert: (state, action) => {
      state.alerts.unshift(action.payload)
      if (!action.payload.isRead) {
        state.unreadCount += 1
      }
    },
    updateAlert: (state, action) => {
      const index = state.alerts.findIndex(alert => alert.id === action.payload.id)
      if (index !== -1) {
        const wasUnread = !state.alerts[index].isRead
        state.alerts[index] = { ...state.alerts[index], ...action.payload }
        
        // Update unread count
        if (wasUnread && action.payload.isRead) {
          state.unreadCount -= 1
        } else if (!wasUnread && action.payload.isRead === false) {
          state.unreadCount += 1
        }
      }
    },
    markAsRead: (state, action) => {
      const alert = state.alerts.find(alert => alert.id === action.payload)
      if (alert && !alert.isRead) {
        alert.isRead = true
        state.unreadCount -= 1
      }
    },
    markAllAsRead: (state) => {
      state.alerts.forEach(alert => {
        alert.isRead = true
      })
      state.unreadCount = 0
    },
    removeAlert: (state, action) => {
      const alertIndex = state.alerts.findIndex(alert => alert.id === action.payload)
      if (alertIndex !== -1) {
        if (!state.alerts[alertIndex].isRead) {
          state.unreadCount -= 1
        }
        state.alerts.splice(alertIndex, 1)
      }
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
  setAlerts,
  setActiveAlerts,
  addAlert,
  updateAlert,
  markAsRead,
  markAllAsRead,
  removeAlert,
  setError,
  clearError
} = alertSlice.actions

export default alertSlice.reducer