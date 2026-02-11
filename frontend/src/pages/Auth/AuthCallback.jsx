import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { setCredentials } from '../../store/slices/authSlice'
import LoadingSpinner from '../../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'

const AuthCallback = () => {
  const navigate = useNavigate()
  const dispatch = useDispatch()

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)
    const token = urlParams.get('token')
    const error = urlParams.get('error')
    
    if (error) {
      toast.error('Authentication failed')
      navigate('/login')
      return
    }
    
    if (token) {
      try {
        // Decode JWT to get user info
        const payload = JSON.parse(atob(token.split('.')[1]))
        
        const user = {
          id: payload.userId,
          username: payload.sub,
          role: payload.role,
          email: payload.email
        }
        
        // Store token and user in Redux
        dispatch(setCredentials({ token, user }))
        
        // Store in localStorage
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        
        toast.success('Successfully signed in with Google!')
        navigate('/dashboard')
      } catch (error) {
        console.error('Error processing token:', error)
        toast.error('Failed to process authentication token')
        navigate('/login')
      }
    } else {
      toast.error('No authentication token received')
      navigate('/login')
    }
  }, [navigate, dispatch])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="text-center">
        <LoadingSpinner size="large" />
        <p className="mt-4 text-gray-600 dark:text-gray-400">Completing sign-in...</p>
      </div>
    </div>
  )
}

export default AuthCallback