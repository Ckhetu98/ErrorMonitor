import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Search, Filter, AlertTriangle, Clock, CheckCircle, XCircle, Eye, Check } from 'lucide-react'
import { errorService } from '../../services/errorService'
import { auditService } from '../../services/auditService'
import { createPortal } from 'react-dom'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

import { useSelector } from 'react-redux'

const ErrorsPage = () => {
  const { user } = useSelector(state => state.auth)
  const navigate = useNavigate()
  const [errors, setErrors] = useState([])
  const [filteredErrors, setFilteredErrors] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedSeverity, setSelectedSeverity] = useState('all')
  const [selectedStatus, setSelectedStatus] = useState('all')
  const [selectedApp, setSelectedApp] = useState('all')

  const [resolving, setResolving] = useState(new Set())
  const [viewingError, setViewingError] = useState(null)
  const [deleting, setDeleting] = useState(new Set())
  const [showResolveModal, setShowResolveModal] = useState(false)
  const [resolvingError, setResolvingError] = useState(null)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [deletingError, setDeletingError] = useState(null)

  useEffect(() => {
    fetchErrors()
  }, [])

  useEffect(() => {
    filterErrors()
  }, [errors, searchTerm, selectedSeverity, selectedStatus, selectedApp])

  const fetchErrors = async () => {
    try {
      setLoading(true)
      console.log('Fetching errors from:', '/errorlogs')
      
      // Test backend connectivity first
      try {
        const testResponse = await errorService.getAllErrors()
        console.log('Backend test successful')
      } catch (testError) {
        console.error('Backend connectivity test failed:', testError)
        if (testError.response?.status === 404) {
          console.error('404 Error - Check if backend is running on correct port')
        }
      }
      
      const response = await errorService.getAllErrors()
      console.log('Response received:', response)
      setErrors(response)
    } catch (error) {
      console.error('Error fetching errors:', error)
      console.error('Error details:', error.response?.data)
      setErrors([])
    } finally {
      setLoading(false)
    }
  }

  const filterErrors = () => {
    let filtered = errors

    if (searchTerm) {
      filtered = filtered.filter(error =>
        error.errorMessage?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (error.application?.name || error.application || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (error.endpoint || '').toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (selectedSeverity !== 'all') {
      filtered = filtered.filter(error => error.severity === selectedSeverity)
    }

    if (selectedStatus !== 'all') {
      filtered = filtered.filter(error => error.status === selectedStatus)
    }

    if (selectedApp !== 'all') {
      filtered = filtered.filter(error => (error.application?.name || error.application) === selectedApp)
    }

    setFilteredErrors(filtered)
  }

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'Critical': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-200'
      case 'High': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-200'
      case 'Medium': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-200'
      case 'Low': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-200'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-200'
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'Open': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-200'
      case 'In Progress': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-200'
      case 'Resolved': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-200'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-200'
    }
  }

  const formatTime = (timestamp) => {
    const now = new Date()
    const errorTime = new Date(timestamp)
    const diffMs = now - errorTime
    const diffMins = Math.floor(diffMs / 60000)
    
    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins} min ago`
    const diffHours = Math.floor(diffMins / 60)
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`
    const diffDays = Math.floor(diffHours / 24)
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`
  }

  const resolveError = (error) => {
    setResolvingError(error)
    setShowResolveModal(true)
  }

  const confirmResolve = async () => {
    if (!resolvingError) return
    
    try {
      setResolving(prev => new Set([...prev, resolvingError.id]))
      await errorService.resolveError(resolvingError.id)
      
      setErrors(errors.map(error => 
        error.id === resolvingError.id ? { ...error, status: 'Resolved' } : error
      ))
      
      toast.success('Error resolved successfully! Email notification sent to administrator.')
      setShowResolveModal(false)
      setResolvingError(null)
      navigate('/alerts')
    } catch (error) {
      console.error('Error resolving error:', error)
      toast.error('Failed to resolve error')
    } finally {
      setResolving(prev => {
        const newSet = new Set(prev)
        newSet.delete(resolvingError.id)
        return newSet
      })
    }
  }

  const deleteError = (error) => {
    setDeletingError(error)
    setShowDeleteModal(true)
  }

  const confirmDelete = async () => {
    if (!deletingError) return
    
    try {
      setDeleting(prev => new Set([...prev, deletingError.id]))
      await errorService.deleteError(deletingError.id)
      
      setErrors(errors.filter(error => error.id !== deletingError.id))
      toast.success('Error deleted successfully!')
      setShowDeleteModal(false)
      setDeletingError(null)
    } catch (error) {
      console.error('Error deleting error:', error)
      toast.error('Failed to delete error')
    } finally {
      setDeleting(prev => {
        const newSet = new Set(prev)
        newSet.delete(deletingError.id)
        return newSet
      })
    }
  }

  const viewError = (error) => {
    setViewingError(error)
  }

  const uniqueApplications = [...new Set(errors.map(error => error.application?.name || error.application).filter(Boolean))]

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Error Logs</h1>
        <p className="text-gray-600 dark:text-gray-400">Monitor and manage application errors</p>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
          <div>
            <label className="label">Search</label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Search errors..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div>
            <label className="label">Severity</label>
            <select
              className="input"
              value={selectedSeverity}
              onChange={(e) => setSelectedSeverity(e.target.value)}
            >
              <option value="all">All Severities</option>
              <option value="Critical">Critical</option>
              <option value="High">High</option>
              <option value="Medium">Medium</option>
              <option value="Low">Low</option>
            </select>
          </div>

          <div>
            <label className="label">Status</label>
            <select
              className="input"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="Open">Open</option>
              <option value="In Progress">In Progress</option>
              <option value="Resolved">Resolved</option>
            </select>
          </div>

          <div>
            <label className="label">Application</label>
            <select
              className="input"
              value={selectedApp}
              onChange={(e) => setSelectedApp(e.target.value)}
            >
              <option value="all">All Applications</option>
              {uniqueApplications.map(app => (
                <option key={app} value={app}>{app}</option>
              ))}
            </select>
          </div>

          <div className="flex items-end">
            <button
              onClick={() => {
                setSearchTerm('')
                setSelectedSeverity('all')
                setSelectedStatus('all')
                setSelectedApp('all')
              }}
              className="btn-secondary w-full"
            >
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* Error List */}
      <div className="space-y-4">
        {filteredErrors.map((error) => (
          <motion.div
            key={error.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, x: -100 }}
            transition={{ duration: 0.3 }}
            className="card hover:shadow-md transition-shadow"
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-3 mb-2">
                  <AlertTriangle className="w-5 h-5 text-red-500" />
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {error.application?.name || error.application || 'Unknown App'}
                  </h3>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${getSeverityColor(error.severity)}`}>
                    {error.severity}
                  </span>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(error.status)}`}>
                    {error.status}
                  </span>
                </div>

                <div className="space-y-2">
                  <div>
                    <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Endpoint:</span>
                    <span className="ml-2 text-sm text-gray-900 dark:text-white font-mono">
                      {error.endpoint || 'N/A'}
                    </span>
                  </div>

                  <div>
                    <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Error:</span>
                    <span className="ml-2 text-sm text-gray-900 dark:text-white">
                      {error.errorMessage || 'No message'}
                    </span>
                  </div>

                  <div>
                    <span className="text-sm font-medium text-gray-500 dark:text-gray-400">Type:</span>
                    <span className="ml-2 text-sm text-gray-600 dark:text-gray-400 font-mono">
                      {error.errorType || 'Unknown'}
                    </span>
                  </div>

                  <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                    <Clock className="w-4 h-4 mr-1" />
                    {formatTime(error.createdAt)}
                  </div>
                </div>
              </div>

              <div className="flex space-x-2 ml-4">
                {error.status !== 'Resolved' && !resolving.has(error.id) && !deleting.has(error.id) && user?.role !== 'VIEWER' && (
                  <button 
                    onClick={() => resolveError(error)}
                    className="p-2 text-gray-400 hover:text-green-600 dark:hover:text-green-400 transition-colors"
                    title="Mark as resolved"
                  >
                    <Check className="w-4 h-4" />
                  </button>
                )}
                {(error.status === 'Resolved' || resolving.has(error.id)) && !deleting.has(error.id) && (
                  <button 
                    onClick={() => deleteError(error)}
                    className="p-2 text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                    title="Delete permanently"
                    disabled={resolving.has(error.id)}
                  >
                    <XCircle className="w-4 h-4" />
                  </button>
                )}
                {resolving.has(error.id) && (
                  <div className="p-2 text-green-600">
                    <div className="w-4 h-4 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
                  </div>
                )}
                {deleting.has(error.id) && (
                  <div className="p-2 text-red-600">
                    <div className="w-4 h-4 border-2 border-red-600 border-t-transparent rounded-full animate-spin"></div>
                  </div>
                )}
                <button 
                  onClick={() => viewError(error)}
                  className="p-2 text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                  title="View details"
                >
                  <Eye className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Stack Trace Preview */}
            <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <details className="group">
                <summary className="cursor-pointer text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white">
                  Stack Trace
                </summary>
                <div className="mt-2 p-3 bg-gray-50 dark:bg-gray-900/50 rounded text-xs font-mono text-gray-600 dark:text-gray-400 overflow-x-auto">
                  {error.stackTrace || 'No stack trace available'}
                </div>
              </details>
            </div>
          </motion.div>
        ))}

        {filteredErrors.length === 0 && (
          <div className="text-center py-12">
            <AlertTriangle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No errors found</h3>
            <p className="text-gray-500 dark:text-gray-400">
              {searchTerm || selectedSeverity !== 'all' || selectedStatus !== 'all' || selectedApp !== 'all'
                ? 'Try adjusting your filters'
                : 'No errors have been logged yet'
              }
            </p>
          </div>
        )}
      </div>

      {/* Resolve Confirmation Modal */}
      {showResolveModal && createPortal(
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="text-center mb-6">
                <div className="w-12 h-12 mx-auto bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center mb-4">
                  <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                  Resolve Error
                </h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Are you sure you want to resolve this error? This action cannot be undone.
                </p>
              </div>
              
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setShowResolveModal(false)}
                  className="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmResolve}
                  className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
                >
                  Resolve
                </button>
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && createPortal(
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="text-center mb-6">
                <div className="w-12 h-12 mx-auto bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center mb-4">
                  <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                  Delete Error
                </h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Are you sure you want to delete this error? This action cannot be undone.
                </p>
              </div>
              
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setShowDeleteModal(false)}
                  className="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmDelete}
                  className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}

      {/* Error Details Modal */}
      {viewingError && createPortal(
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">Error Details</h2>
                <button 
                  onClick={() => setViewingError(null)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <XCircle className="w-6 h-6" />
                </button>
              </div>
              
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Application</label>
                    <p className="text-gray-900 dark:text-white">{viewingError.application?.name || viewingError.application || 'Unknown'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Severity</label>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getSeverityColor(viewingError.severity)}`}>
                      {viewingError.severity}
                    </span>
                  </div>
                </div>
                
                <div>
                  <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Endpoint</label>
                  <p className="text-gray-900 dark:text-white font-mono">{viewingError.endpoint || 'N/A'}</p>
                </div>
                
                <div>
                  <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Error Message</label>
                  <p className="text-gray-900 dark:text-white">{viewingError.errorMessage || 'No message'}</p>
                </div>
                
                <div>
                  <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Stack Trace</label>
                  <pre className="bg-gray-50 dark:bg-gray-900 p-3 rounded text-xs font-mono text-gray-600 dark:text-gray-400 overflow-x-auto max-h-60">
                    {viewingError.stackTrace || 'No stack trace available'}
                  </pre>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Created At</label>
                    <p className="text-gray-900 dark:text-white">{formatTime(viewingError.createdAt)}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Type</label>
                    <p className="text-gray-900 dark:text-white">{viewingError.errorType || 'Unknown'}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  )
}

export default ErrorsPage