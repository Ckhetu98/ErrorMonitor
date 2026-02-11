import React, { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { logoutUser } from '../../store/slices/authSlice'

const ProtectedRoute = ({ children, roles = [] }) => {
  const { isAuthenticated, user } = useSelector((state) => state.auth)
  const dispatch = useDispatch()

  useEffect(() => {
    // Auto-logout if user tries to access unauthorized role-based pages
    if (isAuthenticated && roles.length > 0 && !roles.includes(user?.role?.toUpperCase())) {
      dispatch(logoutUser())
    }
  }, [isAuthenticated, user?.role, roles, dispatch])

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (roles.length > 0 && !roles.includes(user?.role?.toUpperCase())) {
    return <Navigate to="/login" replace />
  }

  return children
}

export default ProtectedRoute