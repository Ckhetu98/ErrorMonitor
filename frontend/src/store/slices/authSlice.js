import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { authService } from '../../services/authService'

// Async thunks
export const loginUser = createAsyncThunk(
  'auth/login',
  async ({ username, password }, { rejectWithValue }) => {
    try {
      console.log('Attempting login for:', username)
      const response = await authService.login(username, password)
      console.log('Login response:', response)
      
      // Check if 2FA is required
      if (response.requiresTwoFactor) {
        return {
          requiresTwoFactor: true,
          userId: response.userId,
          userEmail: response.userEmail
        }
      }
      
      // Normal login - store credentials
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return response
    } catch (error) {
      console.error('Login error in slice:', error)
      
      if (error.code === 'ERR_NETWORK' || !error.response) {
        return rejectWithValue('Unable to connect to server. Please check if the backend is running.')
      }
      
      const message = error.response?.data?.message || 'Login failed'
      if (message.includes('inactive') || message.includes('disabled')) {
        return rejectWithValue('Your account has been disabled. Please contact administrator.')
      }
      return rejectWithValue(message)
    }
  }
)

export const registerUser = createAsyncThunk(
  'auth/register',
  async (userData, { rejectWithValue }) => {
    try {
      console.log('Attempting registration with:', userData)
      const response = await authService.register(userData)
      console.log('Registration response:', response)
      return response
    } catch (error) {
      console.error('Registration error in slice:', error)
      const message = error.response?.data?.message || error.message || 'Registration failed'
      return rejectWithValue(message)
    }
  }
)

export const googleLogin = createAsyncThunk(
  'auth/googleLogin',
  async (googleUser, { rejectWithValue }) => {
    try {
      const response = await authService.googleLogin(googleUser)
      return response
    } catch (error) {
      return rejectWithValue(error.message || 'Google login failed')
    }
  }
)

export const logoutUser = createAsyncThunk(
  'auth/logout',
  async (_, { dispatch }) => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return null
  }
)

// Initial state
const initialState = {
  user: JSON.parse(localStorage.getItem('user')) || null,
  token: localStorage.getItem('token') || null,
  isLoading: false,
  error: null,
  isAuthenticated: !!localStorage.getItem('token'),
  requiresTwoFactor: false,
  pendingUserId: null,
  pendingUserEmail: null,
}

// Slice
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null
    },
    setCredentials: (state, action) => {
      state.user = action.payload.user
      state.token = action.payload.token
      state.isAuthenticated = true
      state.requiresTwoFactor = false
      state.pendingUserId = null
      state.pendingUserEmail = null
    },
    clearCredentials: (state) => {
      state.user = null
      state.token = null
      state.isAuthenticated = false
      state.requiresTwoFactor = false
      state.pendingUserId = null
      state.pendingUserEmail = null
    },
    clearTwoFactorState: (state) => {
      state.requiresTwoFactor = false
      state.pendingUserId = null
      state.pendingUserEmail = null
    },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(loginUser.pending, (state) => {
        state.isLoading = true
        state.error = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.isLoading = false
        
        if (action.payload.requiresTwoFactor) {
          // 2FA required - don't authenticate yet
          state.requiresTwoFactor = true
          state.pendingUserId = action.payload.userId
          state.pendingUserEmail = action.payload.userEmail
          state.error = null
        } else {
          // Normal login
          state.user = action.payload.user
          state.token = action.payload.token
          state.isAuthenticated = true
          state.requiresTwoFactor = false
          state.error = null
        }
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.isLoading = false
        state.error = action.payload
        state.isAuthenticated = false
        state.requiresTwoFactor = false
      })
      // Register
      .addCase(registerUser.pending, (state) => {
        state.isLoading = true
        state.error = null
      })
      .addCase(registerUser.fulfilled, (state) => {
        state.isLoading = false
        state.error = null
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.isLoading = false
        state.error = action.payload
      })
      // Google Login
      .addCase(googleLogin.pending, (state) => {
        state.isLoading = true
        state.error = null
      })
      .addCase(googleLogin.fulfilled, (state, action) => {
        state.isLoading = false
        
        if (action.payload.requiresTwoFactor) {
          // Google auth requires 2FA - don't authenticate yet
          state.requiresTwoFactor = true
          state.pendingUserId = action.payload.userId
          state.pendingUserEmail = action.payload.userEmail
          state.error = null
        } else {
          // Direct Google login (if no 2FA required)
          state.user = action.payload.user
          state.token = action.payload.token
          state.isAuthenticated = true
          state.error = null
          localStorage.setItem('token', action.payload.token)
          localStorage.setItem('user', JSON.stringify(action.payload.user))
        }
      })
      .addCase(googleLogin.rejected, (state, action) => {
        state.isLoading = false
        state.error = action.payload
        state.isAuthenticated = false
      })
      // Logout
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null
        state.token = null
        state.isAuthenticated = false
        state.error = null
        state.requiresTwoFactor = false
        state.pendingUserId = null
        state.pendingUserEmail = null
      })
  },
})

export const { clearError, setCredentials, clearCredentials, clearTwoFactorState } = authSlice.actions
export default authSlice.reducer