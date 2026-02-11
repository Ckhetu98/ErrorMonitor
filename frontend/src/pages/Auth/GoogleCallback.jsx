import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LoadingSpinner from '../../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'

const GoogleCallback = () => {
  const navigate = useNavigate()

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)
    const code = urlParams.get('code')
    const error = urlParams.get('error')
    
    if (error) {
      // Handle OAuth errors
      console.error('Google OAuth error:', error)
      let errorMessage = 'Google authentication failed'
      
      switch (error) {
        case 'access_denied':
          errorMessage = 'Access denied. You cancelled the Google sign-in.'
          break
        case 'invalid_request':
          errorMessage = 'Invalid request. Please try again.'
          break
        default:
          errorMessage = 'Google authentication failed. Please try again.'
      }
      
      toast.error(errorMessage)
      navigate('/login?error=google_auth_failed')
      return
    }
    
    if (code) {
      try {
        // Redirect to backend - it will handle OAuth and redirect back to /login/success
        const backendUrl = `http://localhost:8080/api/auth/google/callback?code=${code}`
        window.location.href = backendUrl
      } catch (error) {
        console.error('Error redirecting to backend:', error)
        toast.error('Failed to complete Google sign-in')
        navigate('/login?error=google_auth_failed')
      }
    } else {
      console.error('No authorization code received from Google')
      toast.error('No authorization code received from Google')
      navigate('/login?error=no_code')
    }
  }, [navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="text-center">
        <LoadingSpinner size="large" />
        <p className="mt-4 text-gray-600 dark:text-gray-400">Completing Google sign-in...</p>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-500">Please wait while we verify your account.</p>
      </div>
    </div>
  )
}

export default GoogleCallback
