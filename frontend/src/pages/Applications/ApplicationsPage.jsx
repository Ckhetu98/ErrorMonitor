import React, { useState, useEffect, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Plus, Edit, Trash2, Pause, Play, X, AlertTriangle, Copy, Check } from 'lucide-react'
import { applicationService } from '../../services/applicationService'
import { auditService } from '../../services/auditService'
import { useSelector } from 'react-redux'
import { errorMonitor } from '../../utils/errorMonitor'
import toast from 'react-hot-toast'

const ApplicationsPage = () => {
  const { user } = useSelector(state => state.auth)
  const [applications, setApplications] = useState([])
  const [showModal, setShowModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [editingApp, setEditingApp] = useState(null)
  const [deletingApp, setDeletingApp] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [errors, setErrors] = useState({})
  const [copiedKey, setCopiedKey] = useState(null)

  const [formData, setFormData] = useState({
    appName: '',
    appDescription: '',
    technology: '',
    version: '',
    baseUrl: '',
    healthCheckUrl: '',
    status: 'ACTIVE'
  })

  const validateForm = () => {
    const newErrors = {}
    
    if (!formData.appName.trim()) {
      newErrors.appName = 'Application name is required'
    } else if (formData.appName.length < 3) {
      newErrors.appName = 'Application name must be at least 3 characters'
    } else if (!/^[a-zA-Z0-9\s_-]+$/.test(formData.appName)) {
      newErrors.appName = 'Application name can only contain letters, numbers, spaces, hyphens, and underscores'
    }
    
    if (formData.baseUrl && !/^https?:\/\/.+/.test(formData.baseUrl)) {
      newErrors.baseUrl = 'Base URL must start with http:// or https://'
    }
    
    if (formData.healthCheckUrl && !/^https?:\/\/.+/.test(formData.healthCheckUrl)) {
      newErrors.healthCheckUrl = 'Health check URL must start with http:// or https://'
    }
    
    if (formData.version && !/^\d+(\.\d+)*(-[a-zA-Z0-9]+)?$/.test(formData.version)) {
      newErrors.version = 'Version must follow semantic versioning (e.g., 1.0.0, 2.1.0-beta)'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const fetchApplications = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await applicationService.getApplications()
      setApplications(data || [])
    } catch (error) {
      console.error('Failed to fetch applications:', error)
      if (error.code === 'ECONNREFUSED' || error.message.includes('Network Error')) {
        setError('Backend server is not running. Please start the backend server on port 52583.')
      } else if (error.response?.status === 401) {
        setError('Authentication failed. Please login again.')
      } else {
        setError(`Connection failed: ${error.message}. Check if backend is running on port 52583.`)
      }
      setApplications([])
      toast.error('Failed to connect to backend server')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchApplications()
  }, [])

  const resetForm = () => {
    setFormData({
      appName: '',
      appDescription: '',
      technology: '',
      version: '',
      baseUrl: '',
      healthCheckUrl: '',
      status: 'ACTIVE'
    })
    setErrors({})
  }

  const handleInputChange = useCallback((e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }, [errors])

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!validateForm()) {
      toast.error('Please fix the validation errors')
      return
    }
    
    try {
      if (editingApp) {
        await applicationService.updateApplication(editingApp.id, formData)
        toast.success('Application updated successfully!')
      } else {
        const result = await applicationService.createApplication(formData)
        toast.success(`Application created successfully!`)
        toast('API Key: ' + result.apiKey + ' - Save this key!', {
          icon: '🔑',
          duration: 8000,
        })
      }

      setShowModal(false)
      setEditingApp(null)
      resetForm()
      fetchApplications()
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Failed to save application'
      toast.error(errorMessage)
    }
  }

  const handleAddApplication = () => {
    setEditingApp(null)
    resetForm()
    setShowModal(true)
  }

  const handleEditApplication = (app) => {
    setEditingApp(app)
    setFormData({
      appName: app.name || '',
      appDescription: app.description || '',
      technology: app.technology || '',
      version: app.version || '',
      baseUrl: app.baseUrl || '',
      healthCheckUrl: app.healthCheckUrl || '',
      status: app.isActive ? 'ACTIVE' : 'INACTIVE'
    })
    setErrors({})
    setShowModal(true)
  }

  const handleDeleteApplication = (app) => {
    setDeletingApp(app)
    setShowDeleteModal(true)
  }

  const confirmDelete = async () => {
    if (!deletingApp) return
    
    try {
      await applicationService.deleteApplication(deletingApp.id)
      setApplications(applications.filter(app => app.id !== deletingApp.id))
      setShowDeleteModal(false)
      setDeletingApp(null)
      toast.success('Application deleted successfully!')
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message
      toast.error(errorMessage)
    }
  }

  const handleToggleStatus = async (app) => {
    try {
      if (app.isPaused === true) {
        await applicationService.resumeApplication(app.id)
        toast.success('Application resumed successfully!')
      } else {
        await applicationService.pauseApplication(app.id)
        toast.success('Application paused successfully!')
      }
      fetchApplications()
    } catch (error) {
      toast.error('Failed to update application status.')
    }
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setEditingApp(null)
    resetForm()
  }


  const handleCopyApiKey = async (apiKey) => {
    try {
      await navigator.clipboard.writeText(apiKey)
      setCopiedKey(apiKey)
      toast.success('API Key copied to clipboard!')
      setTimeout(() => setCopiedKey(null), 2000)
    } catch (error) {
      toast.error('Failed to copy API key')
    }
  }

  const calculateHealthPercentage = (app) => {
    // Simple health calculation - default to 100% if no error data
    if (!app.errorCount || app.errorCount === 0) return 100
    const criticalWeight = 20
    const openWeight = 5
    const healthReduction = ((app.criticalErrors || 0) * criticalWeight) + ((app.openErrors || 0) * openWeight)
    return Math.max(0, 100 - healthReduction)
  }

  const getStatusText = (app) => {
    if (app.isPaused === true) return 'PAUSED'
    if (app.isActive === true) return 'ACTIVE'
    if (app.isActive === false) return 'INACTIVE'
    return 'UNKNOWN'
  }

  const getHealthColor = (percentage) => {
    if (percentage >= 80) return 'text-green-600 bg-green-100 dark:bg-green-900/30 dark:text-green-400'
    if (percentage >= 60) return 'text-blue-600 bg-blue-100 dark:bg-blue-900/30 dark:text-blue-400'
    if (percentage >= 40) return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/30 dark:text-yellow-400'
    return 'text-red-600 bg-red-100 dark:bg-red-900/30 dark:text-red-400'
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
      case 'PAUSED': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-400'
      case 'INACTIVE': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
      case 'MAINTENANCE': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400'
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin h-10 w-10 border-b-2 border-blue-600 rounded-full" />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="p-6 pb-20">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">🧩 Applications</h1>
            <p className="text-gray-600 dark:text-gray-400">Monitor your registered applications</p>
          </div>
          <button
            onClick={handleAddApplication}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors"
          >
            <Plus size={18} />
            Add Application
          </button>
        </div>

        {error && (
          <div className="text-center py-12">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
              Backend Connection Issue
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-4">{error}</p>
            <div className="space-y-2">
              <button
                onClick={fetchApplications}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors mr-2"
              >
                Retry Connection
              </button>
              <div className="text-sm text-gray-500 dark:text-gray-400 mt-4">
                <p>Troubleshooting steps:</p>
                <ul className="list-disc list-inside mt-2 space-y-1">
                  <li>Ensure the backend server is running</li>
                  <li>Check if port 52583 is available</li>
                  <li>Verify the backend URL: http://localhost:52583/api</li>
                  <li>Check if authentication token is valid</li>
                </ul>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {applications.map(app => {
            const healthPercentage = calculateHealthPercentage(app)
            return (
              <motion.div
                key={app.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow border border-gray-200 dark:border-gray-700"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-1">
                      {app.name}
                    </h3>
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-xs text-gray-500 dark:text-gray-400">API Key:</span>
                      <code className="text-xs bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded font-mono text-gray-800 dark:text-gray-200">
                        {app.apiKey || 'Create new app to get API key'}
                      </code>
                      {app.apiKey && (
                        <button
                          onClick={() => handleCopyApiKey(app.apiKey)}
                          className="p-1 text-gray-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                          title="Copy API Key"
                        >
                          {copiedKey === app.apiKey ? <Check size={12} /> : <Copy size={12} />}
                        </button>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-1">
                    <button
                      onClick={() => handleToggleStatus(app)}
                      className={`p-1 rounded transition-colors ${
                        app.isPaused === true ? 'text-orange-600 hover:bg-orange-100 dark:hover:bg-orange-900/30' 
                          : 'text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30'
                      }`}
                      title={app.isPaused === true ? 'Resume' : 'Pause'}
                    >
                      {app.isPaused === true ? <Play size={16} /> : <Pause size={16} />}
                    </button>
                    <button
                      onClick={() => handleEditApplication(app)}
                      className="p-1 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded transition-colors"
                    >
                      <Edit size={16} />
                    </button>
                    {user?.role === 'ADMIN' && (
                      <button
                        onClick={() => handleDeleteApplication(app)}
                        className="p-1 text-red-600 hover:bg-red-100 dark:hover:bg-red-900/30 rounded transition-colors"
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </div>
                </div>

                <p className="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
                  {app.description || 'No description available'}
                </p>

                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Application ID:</span>
                    <code className="text-xs bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 px-2 py-1 rounded font-mono">
                      {app.id}
                    </code>
                  </div>

                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Config Property:</span>
                    <div className="flex items-center gap-2">
                      <code className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 px-2 py-1 rounded font-mono">
                        error.monitoring.application.id={app.id}
                      </code>
                      <button
                        onClick={() => handleCopyApiKey(`error.monitoring.application.id=${app.id}`)}
                        className="p-1 text-gray-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                        title="Copy Config Property"
                      >
                        {copiedKey === `error.monitoring.application.id=${app.id}` ? <Check size={12} /> : <Copy size={12} />}
                      </button>
                    </div>
                  </div>

                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">API Key:</span>
                    <div className="flex items-center gap-2">
                      <code className="text-xs bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 px-2 py-1 rounded font-mono">
                        {app.apiKey || 'Create new app to get API key'}
                      </code>
                      {app.apiKey && (
                        <button
                          onClick={() => handleCopyApiKey(app.apiKey)}
                          className="p-1 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded transition-colors"
                          title="Copy API Key"
                        >
                          {copiedKey === app.apiKey ? <Check size={14} /> : <Copy size={14} />}
                        </button>
                      )}
                    </div>
                  </div>

                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Tech Stack:</span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {app.technology || 'N/A'}
                    </span>
                  </div>

                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Health:</span>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getHealthColor(healthPercentage)}`}>
                      {isNaN(healthPercentage) ? '100' : healthPercentage}%
                    </span>
                  </div>

                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Status:</span>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(getStatusText(app))}`}>
                      {getStatusText(app)}
                    </span>
                  </div>
                </div>
              </motion.div>
            )
          })}
        </div>

        {applications.length === 0 && !loading && !error && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">🧩</div>
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
              No Applications Yet
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-4">
              Get started by adding your first application to monitor.
            </p>
            <button
              onClick={handleAddApplication}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Add Your First Application
            </button>
          </div>
        )}

        {/* Add/Edit Modal */}
        <AnimatePresence>
          {showModal && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
              onClick={handleCloseModal}
            >
              <motion.div
                initial={{ scale: 0.95, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.95, opacity: 0 }}
                className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {editingApp ? 'Edit Application' : 'Add Application'}
                  </h3>
                  <button
                    onClick={handleCloseModal}
                    className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                  >
                    <X size={20} />
                  </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                  <div>
                    <input
                      name="appName"
                      required
                      placeholder="Application Name *"
                      value={formData.appName}
                      onChange={handleInputChange}
                      className={`w-full px-3 py-2 border rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                        errors.appName ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'
                      }`}
                    />
                    {errors.appName && (
                      <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.appName}</p>
                    )}
                  </div>

                  <textarea
                    name="appDescription"
                    placeholder="Description"
                    value={formData.appDescription}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                    rows={3}
                  />

                  <div className="grid grid-cols-2 gap-3">
                    <input
                      name="technology"
                      placeholder="Technology"
                      value={formData.technology}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                    <div>
                      <input
                        name="version"
                        placeholder="Version (e.g., 1.0.0)"
                        value={formData.version}
                        onChange={handleInputChange}
                        className={`w-full px-3 py-2 border rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                          errors.version ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'
                        }`}
                      />
                      {errors.version && (
                        <p className="mt-1 text-xs text-red-600 dark:text-red-400">{errors.version}</p>
                      )}
                    </div>
                  </div>

                  <div>
                    <input
                      name="baseUrl"
                      placeholder="Base URL (e.g., https://api.example.com)"
                      value={formData.baseUrl}
                      onChange={handleInputChange}
                      className={`w-full px-3 py-2 border rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                        errors.baseUrl ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'
                      }`}
                    />
                    {errors.baseUrl && (
                      <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.baseUrl}</p>
                    )}
                  </div>

                  <div>
                    <input
                      name="healthCheckUrl"
                      placeholder="Health Check URL (e.g., https://api.example.com/health)"
                      value={formData.healthCheckUrl}
                      onChange={handleInputChange}
                      className={`w-full px-3 py-2 border rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                        errors.healthCheckUrl ? 'border-red-500' : 'border-gray-300 dark:border-gray-600'
                      }`}
                    />
                    {errors.healthCheckUrl && (
                      <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.healthCheckUrl}</p>
                    )}
                  </div>

                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                    <option value="MAINTENANCE">Maintenance</option>
                  </select>

                  <div className="flex justify-end gap-3 pt-4">
                    <button
                      type="button"
                      onClick={handleCloseModal}
                      className="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                    >
                      {editingApp ? 'Update' : 'Create'}
                    </button>
                  </div>
                </form>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Delete Confirmation Modal */}
        <AnimatePresence>
          {showDeleteModal && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
              onClick={() => setShowDeleteModal(false)}
            >
              <motion.div
                initial={{ scale: 0.95, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.95, opacity: 0 }}
                className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="p-6">
                  <div className="text-center mb-6">
                    <div className="w-12 h-12 mx-auto bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center mb-4">
                      <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                      Delete Application
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      Are you sure you want to delete "{deletingApp?.name}"? This will also delete all related error logs and alerts. This action cannot be undone.
                    </p>
                  </div>
                  
                  <div className="flex justify-center gap-3">
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
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}

export default ApplicationsPage