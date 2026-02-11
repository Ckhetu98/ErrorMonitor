import { configureStore } from '@reduxjs/toolkit'
import authSlice from './slices/authSlice'
import errorSlice from './slices/errorSlice'
import applicationSlice from './slices/applicationSlice'
import dashboardSlice from './slices/dashboardSlice'
import alertSlice from './slices/alertSlice'

export const store = configureStore({
  reducer: {
    auth: authSlice,
    errors: errorSlice,
    applications: applicationSlice,
    dashboard: dashboardSlice,
    alerts: alertSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
  devTools: process.env.NODE_ENV !== 'production',
})

// Export types for TypeScript (if needed)
// export type RootState = ReturnType<typeof store.getState>
// export type AppDispatch = typeof store.dispatch