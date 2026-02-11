import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Shield, AlertCircle, Loader, ArrowLeft } from 'lucide-react'
import { useNavigate, useLocation } from 'react-router-dom'

const VerifyOTPPage = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [otpCode, setOtpCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [timeLeft, setTimeLeft] = useState(300) // 5 minutes
  const [userId, setUserId] = useState('')

  useEffect(() => {
    // Get userId from location state
    const state = location.state || {}
    setUserId(state.userId || '')

    if (!state.userId) {
      navigate('/login')
      return
    }
  }, [location, navigate])

  useEffect(() => {
    // Timer countdown
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000)
      return () => clearTimeout(timer)
    } else if (timeLeft === 0) {
      setError('OTP has expired. Please try logging in again.')
    }
  }, [timeLeft])

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`
  }

  const handleVerify = async (e) => {
    e.preventDefault()
    
    if (!otpCode || otpCode.length !== 6) {
      setError('Please enter a valid 6-digit OTP')
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await fetch('http://localhost:5000/api/auth/verify-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userId: userId,
          code: otpCode
        })
      })

      const data = await response.json()

      if (response.ok && data.success) {
        // Store token and redirect
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data.user))
        
        // Redirect to dashboard
        navigate('/dashboard')
      } else {
        setError(data.message || 'Invalid or expired OTP. Please try again.')
        setOtpCode('')
      }
    } catch (err) {
      setError('Error verifying OTP. Please try again.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleResendOTP = async () => {
    // Implement resend OTP logic
    setError('Please contact support to resend OTP')
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md"
      >
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-xl p-8 space-y-6">
          {/* Header */}
          <div className="text-center">
            <div className="flex justify-center mb-4">
              <div className="bg-blue-100 dark:bg-blue-900 p-3 rounded-full">
                <Shield className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              Verify Your Identity
            </h1>
            <p className="text-gray-600 dark:text-gray-400">
              Enter the OTP sent to your email
            </p>
          </div>

          {/* Timer */}
          <div className={`text-center p-3 rounded-lg ${
            timeLeft > 60 
              ? 'bg-blue-50 dark:bg-blue-900/20' 
              : 'bg-red-50 dark:bg-red-900/20'
          }`}>
            <p className={`text-sm font-medium ${
              timeLeft > 60
                ? 'text-blue-700 dark:text-blue-400'
                : 'text-red-700 dark:text-red-400'
            }`}>
              Time remaining: {formatTime(timeLeft)}
            </p>
          </div>

          {/* Error Message */}
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-3 flex items-center text-red-700 dark:text-red-400"
            >
              <AlertCircle className="w-5 h-5 mr-3 flex-shrink-0" />
              <span className="text-sm">{error}</span>
            </motion.div>
          )}

          {/* Form */}
          <form onSubmit={handleVerify} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                OTP Code
              </label>
              <input
                type="text"
                value={otpCode}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, '').slice(0, 6)
                  setOtpCode(value)
                  setError('')
                }}
                placeholder="Enter 6-digit code"
                maxLength="6"
                className="w-full px-4 py-3 text-center text-2xl tracking-widest font-mono border-2 border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:border-blue-500 focus:outline-none transition-colors"
                disabled={loading}
              />
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                Enter the 6-digit code sent to your email
              </p>
            </div>

            <button
              type="submit"
              disabled={loading || otpCode.length !== 6 || timeLeft === 0}
              className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-lg transition-colors flex items-center justify-center"
            >
              {loading ? (
                <>
                  <Loader className="w-5 h-5 mr-2 animate-spin" />
                  Verifying...
                </>
              ) : (
                'Verify OTP'
              )}
            </button>
          </form>

          {/* Footer */}
          <div className="space-y-3 border-t border-gray-200 dark:border-gray-700 pt-4">
            <p className="text-center text-sm text-gray-600 dark:text-gray-400">
              Didn't receive the code?
            </p>
            <button
              onClick={handleResendOTP}
              disabled={loading}
              className="w-full text-blue-600 hover:text-blue-700 font-medium text-sm py-2 transition-colors disabled:opacity-50"
            >
              Resend OTP
            </button>
          </div>

          {/* Back to Login */}
          <button
            onClick={() => navigate('/login')}
            className="w-full flex items-center justify-center text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white py-2 transition-colors"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Login
          </button>
        </div>

        {/* Info Box */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mt-6 bg-white dark:bg-gray-800 rounded-lg shadow p-4"
        >
          <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-2">Security Info:</h3>
          <ul className="text-xs text-gray-600 dark:text-gray-400 space-y-1">
            <li>✓ OTP is valid for 5 minutes only</li>
            <li>✓ Never share your OTP with anyone</li>
            <li>✓ Check your email inbox or spam folder</li>
          </ul>
        </motion.div>
      </motion.div>
    </div>
  )
}

export default VerifyOTPPage