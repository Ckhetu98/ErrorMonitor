import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  applications: [],
  isLoading: false,
  error: null,
  selectedApplication: null
}

const applicationSlice = createSlice({
  name: 'applications',
  initialState,
  reducers: {
    setLoading: (state, action) => {
      state.isLoading = action.payload
    },
    setApplications: (state, action) => {
      state.applications = action.payload
    },
    addApplication: (state, action) => {
      state.applications.push(action.payload)
    },
    updateApplication: (state, action) => {
      const index = state.applications.findIndex(app => app.id === action.payload.id)
      if (index !== -1) {
        state.applications[index] = { ...state.applications[index], ...action.payload }
      }
    },
    removeApplication: (state, action) => {
      state.applications = state.applications.filter(app => app.id !== action.payload)
    },
    setSelectedApplication: (state, action) => {
      state.selectedApplication = action.payload
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
  setApplications,
  addApplication,
  updateApplication,
  removeApplication,
  setSelectedApplication,
  setError,
  clearError
} = applicationSlice.actions

export default applicationSlice.reducer