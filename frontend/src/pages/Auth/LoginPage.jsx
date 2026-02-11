import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { loginUser, googleLogin } from '../../store/slices/authSlice'
import { Shield, Eye, EyeOff } from 'lucide-react'
import LoadingSpinner from '../../components/UI/LoadingSpinner'
import Footer from '../../components/Layout/Footer'
import toast from 'react-hot-toast'
import { errorMonitor } from '../../utils/errorMonitor'

// SSO Icons
const GoogleIcon = () => (
  <svg className="w-5 h-5" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
  </svg>
)

const LoginPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState({})
  const [searchParams] = useSearchParams()
  
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { isLoading, error, requiresTwoFactor, pendingUserId, pendingUserEmail } = useSelector((state) => state.auth)

  // Handle URL parameters for error messages
  useEffect(() => {
    const errorParam = searchParams.get('error')
    if (errorParam) {
      switch (errorParam) {
        case 'google_auth_failed':
          toast.error('Google authentication failed. Please try again or use email login.')
          break
        case 'no_code':
          toast.error('Google authentication was cancelled or failed.')
          break
        case 'access_denied':
          toast.error('Access denied. Please check your permissions.')
          break
        default:
          toast.error('Authentication failed. Please try again.')
      }
      // Clear the error parameter from URL
      const newUrl = window.location.pathname
      window.history.replaceState({}, '', newUrl)
    }
  }, [searchParams])

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.username.trim()) {
      newErrors.username = 'Username is required'
    } else if (formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters'
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: value
    })
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
  }

  const handleSSOLogin = async (provider) => {
    if (provider === 'Google') {
      try {
        toast.success('Redirecting to Google Sign-In...')
        const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
        
        if (!clientId) {
          toast.error('Google authentication is not configured. Please contact administrator.')
          return
        }
        
        const redirectUri = encodeURIComponent('http://localhost:8080/api/auth/google/callback')
        
        const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
          `client_id=${clientId}&` +
          `redirect_uri=${redirectUri}&` +
          `response_type=code&` +
          `scope=email profile&` +
          `access_type=offline&` +
          `prompt=consent`
        
        window.location.href = googleAuthUrl
      } catch (error) {
        console.error('Google SSO error:', error)
        toast.error('Failed to initiate Google sign-in. Please try again.')
      }
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!validateForm()) {
      toast.error('Please fix the validation errors')
      return
    }
    
    try {
      const result = await dispatch(loginUser(formData))
      
      if (result.type === 'auth/login/fulfilled') {
        if (result.payload.requiresTwoFactor) {
          toast.success('Two-factor authentication required. Check your email for OTP.')
          navigate('/auth/verify-otp', {
            state: {
              userId: result.payload.userId,
              userEmail: result.payload.userEmail
            }
          })
        } else {
          toast.success('Welcome back! Login successful.')
          navigate('/dashboard')
        }
      } else if (result.type === 'auth/login/rejected') {
        const errorMessage = result.payload || 'Invalid username or password'
        toast.error(errorMessage)
        errorMonitor.logManualError(`Authentication failed: ${errorMessage}`, 'High', '/login')
      }
    } catch (error) {
      console.error('Login error:', error)
      if (error.code === 'ERR_NETWORK' || error.message.includes('fetch')) {
        toast.error('Unable to connect to server. Please check if the backend is running.')
      } else {
        toast.error('Login failed. Please try again.')
      }
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex flex-col transition-colors duration-300">
      <div className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="max-w-md w-full space-y-8"
        >
          <div>
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="mx-auto h-12 w-12 bg-blue-600 dark:bg-blue-500 rounded-xl flex items-center justify-center"
            >
              <Shield className="h-8 w-8 text-white" />
            </motion.div>
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
              Sign in to your account
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
              Or{' '}
              <Link
                to="/register"
                className="font-medium text-blue-600 dark:text-blue-400 hover:text-blue-500 dark:hover:text-blue-300"
              >
                create a new account
              </Link>
            </p>
          </div>
          
          <motion.form
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.8, delay: 0.4 }}
            className="mt-8 space-y-6"
            onSubmit={handleSubmit}
          >
            {/* SSO Buttons */}
            <div className="space-y-3">
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                type="button"
                onClick={() => handleSSOLogin('Google')}
                className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <GoogleIcon />
                <span className="ml-3 font-medium">Sign in with Google</span>
              </motion.button>
            </div>

            {/* Divider */}
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300 dark:border-gray-600" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400">Or continue with email</span>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <label htmlFor="username" className="label">
                  Username
                </label>
                <input
                  id="username"
                  name="username"
                  type="text"
                  required
                  className={`input ${errors.username ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''}`}
                  placeholder="Enter your username"
                  value={formData.username}
                  onChange={handleChange}
                />
                {errors.username && (
                  <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.username}</p>
                )}
              </div>
              
              <div>
                <label htmlFor="password" className="label">
                  Password
                </label>
                <div className="relative">
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    required
                    className={`input pr-10 ${errors.password ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="Enter your password"
                    value={formData.password}
                    onChange={handleChange}
                  />
                  <button
                    type="button"
                    className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5 text-gray-400 dark:text-gray-500" />
                    ) : (
                      <Eye className="h-5 w-5 text-gray-400 dark:text-gray-500" />
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.password}</p>
                )}
              </div>
            </div>

            <div>
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                type="submit"
                disabled={isLoading}
                className="w-full btn-primary py-3 text-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <div className="flex items-center justify-center">
                    <LoadingSpinner size="small" color="white" />
                    <span className="ml-2">Signing in...</span>
                  </div>
                ) : (
                  'Sign in'
                )}
              </motion.button>
            </div>

            <div className="text-center">
              <Link
                to="/"
                className="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-500 dark:hover:text-blue-300"
              >
                ← Back to home
              </Link>
            </div>
          </motion.form>
        </motion.div>
      </div>
      <Footer />
    </div>
  )
}

export default LoginPage