import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { setCredentials } from '../../store/slices/authSlice'
import LoadingSpinner from '../../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'

// Function to decode JWT token
const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Error decoding JWT:', error)
    return null
  }
}

const LoginSuccess = () => {
  const navigate = useNavigate()
  const dispatch = useDispatch()

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)
    const token = urlParams.get('token')
    
    console.log('Current URL:', window.location.href)
    console.log('Token from URL:', token)
    
    if (token) {
      try {
        // Decode JWT token to get actual user data
        const decodedToken = decodeJWT(token)
        console.log('Decoded token:', decodedToken)
        
        if (!decodedToken) {
          throw new Error('Invalid token')
        }
        
        // Store token
        localStorage.setItem('token', token)
        
        // Create user object from decoded token - .NET backend uses Microsoft claim types
        const user = {
          id: decodedToken["http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"] || decodedToken.userId,
          username: decodedToken["http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"] || decodedToken.sub,
          email: decodedToken["http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"] || decodedToken.email,
          role: decodedToken["http://schemas.microsoft.com/ws/2008/06/identity/claims/role"] || decodedToken.role
        }
        
        console.log('=== USER OBJECT DEBUG ===')
        console.log('Extracted user:', user)
        console.log('User role:', user.role)
        console.log('Role type:', typeof user.role)
        
        localStorage.setItem('user', JSON.stringify(user))
        
        // Update Redux state
        dispatch(setCredentials({
          token: token,
          user: user
        }))
        
        toast.success('Login successful! Welcome to the dashboard.')
        navigate('/dashboard', { replace: true })
      } catch (error) {
        console.error('Token processing error:', error)
        toast.error('Login failed. Please try again.')
        navigate('/login', { replace: true })
      }
    } else {
      console.error('No token found in URL parameters')
      navigate('/login', { replace: true })
    }
  }, [dispatch, navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="text-center">
        <LoadingSpinner size="large" />
        <p className="mt-4 text-gray-600 dark:text-gray-400">Completing sign-in...</p>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-500">Redirecting to dashboard...</p>
      </div>
    </div>
  )
}

export default LoginSuccess