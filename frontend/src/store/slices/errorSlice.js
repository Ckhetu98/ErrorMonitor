import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  errors: [],
  isLoading: false,
  error: null,
  filters: {
    severity: 'all',
    application: 'all',
    dateRange: 'last7days'
  },
  pagination: {
    page: 1,
    limit: 20,
    total: 0
  }
}

const errorSlice = createSlice({
  name: 'errors',
  initialState,
  reducers: {
    setLoading: (state, action) => {
      state.isLoading = action.payload
    },
    setErrors: (state, action) => {
      state.errors = action.payload
    },
    addError: (state, action) => {
      state.errors.unshift(action.payload)
    },
    updateError: (state, action) => {
      const index = state.errors.findIndex(error => error.id === action.payload.id)
      if (index !== -1) {
        state.errors[index] = { ...state.errors[index], ...action.payload }
      }
    },
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload }
    },
    setPagination: (state, action) => {
      state.pagination = { ...state.pagination, ...action.payload }
    },
    clearError: (state) => {
      state.error = null
    }
  }
})

export const {
  setLoading,
  setErrors,
  addError,
  updateError,
  setFilters,
  setPagination,
  clearError
} = errorSlice.actions

export default errorSlice.reducer