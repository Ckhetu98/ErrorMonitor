import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { useNavigate, useLocation } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { Shield, ArrowLeft } from 'lucide-react'
import { authService } from '../../services/authService'
import toast from 'react-hot-toast'
import { setCredentials } from '../../store/slices/authSlice'
import LoadingSpinner from '../../components/UI/LoadingSpinner'

const OTPVerificationPage = () => {
  const [otp, setOtp] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [timeLeft, setTimeLeft] = useState(300) // 5 minutes
  const navigate = useNavigate()
  const location = useLocation()
  const dispatch = useDispatch()
  
  const userId = location.state?.userId || new URLSearchParams(window.location.search).get('userId')
  const userEmail = location.state?.userEmail || new URLSearchParams(window.location.search).get('email')

  useEffect(() => {
    if (!userId) {
      navigate('/login')
      return
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer)
          toast.error('OTP expired. Please login again.')
          navigate('/login')
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [userId, navigate])

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (otp.length !== 6) {
      toast.error('Please enter a valid 6-digit OTP')
      return
    }

    setIsLoading(true)
    try {
      const response = await authService.verifyOTP(userId, otp)
      
      console.log('=== OTP VERIFICATION RESPONSE ====')
      console.log('Full response:', response)
      console.log('User data:', response.user)
      console.log('User role:', response.user?.role)
      console.log('Token:', response.token)
      
      // Update Redux state with user and token
      dispatch(setCredentials({
        user: response.user,
        token: response.token
      }))
      
      // Also check what's in localStorage after setting
      setTimeout(() => {
        console.log('=== LOCALSTORAGE CHECK ====')
        console.log('Stored user:', JSON.parse(localStorage.getItem('user') || '{}'))
        console.log('Stored token:', localStorage.getItem('token'))
      }, 100)
      
      toast.success('Login successful! Welcome to your dashboard.')
      navigate('/dashboard')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid or expired OTP')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResendOTP = async () => {
    try {
      await authService.resendOTP(userId)
      toast.success('New OTP sent to your email')
      setTimeLeft(300) // Reset timer
    } catch (err) {
      toast.error('Failed to resend OTP')
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center py-12 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-md w-full space-y-8"
      >
        <div className="text-center">
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            className="mx-auto h-12 w-12 bg-blue-600 rounded-xl flex items-center justify-center"
          >
            <Shield className="h-8 w-8 text-white" />
          </motion.div>
          <h2 className="mt-6 text-3xl font-extrabold text-gray-900 dark:text-white">
            Enter Verification Code
          </h2>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            We've sent a 6-digit code to {userEmail}
          </p>
          <p className="mt-1 text-xs text-gray-500 dark:text-gray-500">
            Code expires in: <span className="font-mono font-bold text-red-600">{formatTime(timeLeft)}</span>
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
          <div>
            <label htmlFor="otp" className="sr-only">
              Verification Code
            </label>
            <input
              id="otp"
              name="otp"
              type="text"
              maxLength="6"
              required
              className="appearance-none relative block w-full px-3 py-4 text-center text-2xl font-mono tracking-widest border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="000000"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              autoComplete="one-time-code"
            />
          </div>

          <div>
            <button
              type="submit"
              disabled={isLoading || otp.length !== 6}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <div className="flex items-center">
                  <LoadingSpinner size="small" color="white" />
                  <span className="ml-2">Verifying...</span>
                </div>
              ) : (
                'Verify Code'
              )}
            </button>
          </div>

          <div className="flex items-center justify-between">
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            >
              <ArrowLeft className="w-4 h-4 mr-1" />
              Back to login
            </button>
            
            <button
              type="button"
              onClick={handleResendOTP}
              className="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-500"
            >
              Resend code
            </button>
          </div>
        </form>

        <div className="bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
          <p className="text-xs text-blue-700 dark:text-blue-300">
            <strong>Note:</strong> Check your spam folder if you don't see the email. 
            The verification code is valid for 5 minutes.
          </p>
        </div>
      </motion.div>
    </div>
  )
}

export default OTPVerificationPage