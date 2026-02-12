import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Bell, AlertTriangle, CheckCircle, X, Trash2 } from 'lucide-react';
import { alertService } from '../../services/alertService';
import { useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { createPortal } from 'react-dom';

const AlertsPage = () => {
  const { user } = useSelector(state => state.auth);
  const [alerts, setAlerts] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [stats, setStats] = useState({});
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingAlert, setDeletingAlert] = useState(null);
  const [showResolveModal, setShowResolveModal] = useState(false);
  const [resolvingAlert, setResolvingAlert] = useState(null);
  const [errors, setErrors] = useState({});
  const [newAlert, setNewAlert] = useState({
    applicationId: '',
    errorLogId: '',
    alertType: 'EMAIL',
    alertLevel: 'HIGH',
    alertMessage: ''
  });

  useEffect(() => {
    fetchAlerts();
    fetchStats();
    fetchApplications();
  }, [filter]);

  const validateForm = () => {
    const newErrors = {};
    
    if (!newAlert.applicationId) {
      newErrors.applicationId = 'Application is required';
    }
    
    if (!newAlert.errorLogId.trim()) {
      newErrors.errorLogId = 'Error Log ID is required';
    } else if (!/^\d+$/.test(newAlert.errorLogId)) {
      newErrors.errorLogId = 'Error Log ID must be a valid number';
    }
    
    if (!newAlert.alertMessage.trim()) {
      newErrors.alertMessage = 'Alert message is required';
    } else if (newAlert.alertMessage.length < 10) {
      newErrors.alertMessage = 'Alert message must be at least 10 characters';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const data = filter === 'unresolved' 
        ? await alertService.getUnresolvedAlerts()
        : await alertService.getAllAlerts();
      setAlerts(data);
    } catch (error) {
      console.error('Error fetching alerts:', error);
      toast.error('Failed to fetch alerts');
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const statsData = await alertService.getAlertStats();
      setStats(statsData);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const fetchApplications = async () => {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/applications`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        const data = await response.json();
        setApplications(data);
        console.log('Fetched applications:', data);
      } else {
        console.error('Failed to fetch applications:', response.status);
      }
    } catch (error) {
      console.error('Error fetching applications:', error);
    }
  };

  const getAlertRecipients = (alert) => {
    console.log('Current user object:', user)
    const currentUserEmail = user?.email || user?.username || 'No email available'
    
    if (user?.role === 'DEVELOPER') {
      // Developer only sees their own email
      return currentUserEmail
    } else if (user?.role === 'ADMIN') {
      // Admin logic: if app was created by developer, show both emails
      // If admin created the app, show only admin email
      if (alert.applicationCreatedBy && alert.applicationCreatedBy !== currentUserEmail) {
        // App created by developer, show both
        return `${currentUserEmail}, ${alert.applicationCreatedBy}`
      } else {
        // App created by admin, show only admin
        return currentUserEmail
      }
    }
    
    return currentUserEmail
  };

  const handleResolveAlert = (alert) => {
    setResolvingAlert(alert);
    setShowResolveModal(true);
  };

  const confirmResolve = async () => {
    if (!resolvingAlert) return;
    
    try {
      await alertService.resolveAlert(resolvingAlert.id);
      toast.success('Error resolved successfully!');
      setShowResolveModal(false);
      setResolvingAlert(null);
      fetchAlerts();
      fetchStats();
    } catch (error) {
      console.error('Error resolving alert:', error);
      toast.error('Failed to resolve alert: ' + error.message);
    }
  };

  const handleDeleteAlert = (alert) => {
    setDeletingAlert(alert);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    if (!deletingAlert) return;
    
    try {
      await alertService.deleteAlert(deletingAlert.id);
      toast.success('Alert deleted successfully!');
      setShowDeleteModal(false);
      setDeletingAlert(null);
      fetchAlerts();
      fetchStats();
    } catch (error) {
      console.error('Error deleting alert:', error);
      toast.error('Failed to delete alert');
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewAlert(prev => ({ ...prev, [name]: value }));
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleCreateAlert = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      toast.error('Please fix the validation errors');
      return;
    }
    
    try {
      await alertService.createAlert(newAlert);
      toast.success('Alert rule created successfully!');
      setShowCreateModal(false);
      resetForm();
      fetchAlerts();
      fetchStats();
    } catch (error) {
      console.error('Error creating alert:', error);
      toast.error('Failed to create alert rule');
    }
  };

  const resetForm = () => {
    setNewAlert({
      applicationId: '',
      errorLogId: '',
      alertType: 'EMAIL',
      alertLevel: 'HIGH',
      alertMessage: ''
    });
    setErrors({});
  };

  const getSeverityColor = (level) => {
    switch (level?.toLowerCase()) {
      case 'critical': return 'text-red-600 bg-red-100 dark:bg-red-900/30 dark:text-red-400';
      case 'high': return 'text-orange-600 bg-orange-100 dark:bg-orange-900/30 dark:text-orange-400';
      case 'medium': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'low': return 'text-green-600 bg-green-100 dark:bg-green-900/30 dark:text-green-400';
      default: return 'text-gray-600 bg-gray-100 dark:bg-gray-900/30 dark:text-gray-400';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex justify-between items-center"
      >
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">🚨 Alert Management</h1>
          <p className="text-gray-600 dark:text-gray-400">Monitor and manage system alerts</p>
        </div>
        {user?.role === 'ADMIN' && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="btn-primary flex items-center"
          >
            <Plus className="w-4 h-4 mr-2" />
            Create Alert Rule
          </button>
        )}
      </motion.div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="card"
        >
          <div className="flex items-center">
            <Bell className="w-8 h-8 text-blue-600 dark:text-blue-400 mr-3" />
            <div>
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Total Alerts</h3>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.totalAlerts || 0}</p>
            </div>
          </div>
        </motion.div>
        
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.1 }}
          className="card"
        >
          <div className="flex items-center">
            <AlertTriangle className="w-8 h-8 text-red-600 dark:text-red-400 mr-3" />
            <div>
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Unresolved</h3>
              <p className="text-2xl font-bold text-red-600 dark:text-red-400">{stats.unresolvedAlerts || 0}</p>
            </div>
          </div>
        </motion.div>
        
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2 }}
          className="card"
        >
          <div className="flex items-center">
            <CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400 mr-3" />
            <div>
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Resolved</h3>
              <p className="text-2xl font-bold text-green-600 dark:text-green-400">{stats.resolvedAlerts || 0}</p>
            </div>
          </div>
        </motion.div>
        
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.3 }}
          className="card"
        >
          <div className="flex items-center">
            <AlertTriangle className="w-8 h-8 text-red-600 dark:text-red-400 mr-3" />
            <div>
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Critical</h3>
              <p className="text-2xl font-bold text-red-600 dark:text-red-400">{stats.criticalAlerts || 0}</p>
            </div>
          </div>
        </motion.div>
      </div>

      {/* Filter Tabs */}
      <div className="mb-6">
        <div className="border-b border-gray-200 dark:border-gray-700">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setFilter('all')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                filter === 'all'
                  ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              All Alerts
            </button>
            <button
              onClick={() => setFilter('unresolved')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                filter === 'unresolved'
                  ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              Unresolved
            </button>
          </nav>
        </div>
      </div>

      {/* Alerts List */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="card overflow-hidden"
      >
        {alerts.length === 0 ? (
          <div className="p-8 text-center text-gray-500 dark:text-gray-400">
            <AlertTriangle className="w-12 h-12 mx-auto mb-4 text-gray-400 dark:text-gray-500" />
            <p>No alerts found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Alert
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Application
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Level
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Created
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                {alerts.map((alert) => (
                  <motion.tr
                    key={alert.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="hover:bg-gray-50 dark:hover:bg-gray-800"
                  >
                    <td className="px-6 py-4">
                      <div>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">
                          {alert.alertMessage}
                        </p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                          Recipients: {getAlertRecipients(alert)}
                        </p>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 dark:text-white">
                      {alert.applicationName || 'Unknown'}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getSeverityColor(alert.alertLevel)}`}>
                        {alert.alertLevel}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 dark:text-white">
                      {alert.alertType}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                      {formatDate(alert.createdAt)}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        alert.isResolved 
                          ? 'text-green-800 bg-green-100 dark:bg-green-900/30 dark:text-green-400' 
                          : 'text-red-800 bg-red-100 dark:bg-red-900/30 dark:text-red-400'
                      }`}>
                        {alert.isResolved ? 'Resolved' : 'Open'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium">
                      <div className="flex space-x-2">
                        {!alert.isResolved && user?.role !== 'VIEWER' && (
                          <button
                            onClick={() => handleResolveAlert(alert)}
                            className="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 px-3 py-1 bg-blue-100 dark:bg-blue-900/30 rounded transition-colors"
                          >
                            Resolve
                          </button>
                        )}
                        {user?.role === 'ADMIN' && (
                          <button
                            onClick={() => handleDeleteAlert(alert)}
                            className="text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300 px-3 py-1 bg-red-100 dark:bg-red-900/30 rounded transition-colors"
                          >
                            <Trash2 size={14} />
                          </button>
                        )}
                      </div>
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </motion.div>

      {/* Create Alert Modal */}
      {showCreateModal && createPortal(
        <AnimatePresence>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
            onClick={() => setShowCreateModal(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  Create Alert Rule
                </h3>
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <form onSubmit={handleCreateAlert} className="space-y-4">
                <div>
                  <label className="label">Application</label>
                  <select
                    name="applicationId"
                    className={`input ${errors.applicationId ? 'border-red-500' : ''}`}
                    value={newAlert.applicationId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select an application</option>
                    {applications.map((app) => (
                      <option key={app.id} value={app.id}>
                        {app.name}
                      </option>
                    ))}
                  </select>
                  {errors.applicationId && (
                    <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.applicationId}</p>
                  )}
                </div>
                <div>
                  <label className="label">Error Log ID</label>
                  <input
                    type="text"
                    name="errorLogId"
                    className={`input ${errors.errorLogId ? 'border-red-500' : ''}`}
                    placeholder="Enter error log ID"
                    value={newAlert.errorLogId}
                    onChange={handleInputChange}
                    required
                  />
                  {errors.errorLogId && (
                    <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.errorLogId}</p>
                  )}
                </div>
                <div>
                  <label className="label">Alert Type</label>
                  <select
                    name="alertType"
                    className="input"
                    value={newAlert.alertType}
                    onChange={handleInputChange}
                  >
                    <option value="EMAIL">Email</option>
                    <option value="SMS">SMS</option>
                    <option value="SLACK">Slack</option>
                    <option value="WEBHOOK">Webhook</option>
                  </select>
                </div>
                <div>
                  <label className="label">Alert Level</label>
                  <select
                    name="alertLevel"
                    className="input"
                    value={newAlert.alertLevel}
                    onChange={handleInputChange}
                  >
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                </div>
                <div>
                  <label className="label">Alert Message</label>
                  <textarea
                    name="alertMessage"
                    className={`input ${errors.alertMessage ? 'border-red-500' : ''}`}
                    rows="3"
                    placeholder="Enter alert message (minimum 10 characters)"
                    value={newAlert.alertMessage}
                    onChange={handleInputChange}
                    required
                  />
                  {errors.alertMessage && (
                    <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.alertMessage}</p>
                  )}
                </div>
                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="btn-secondary"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                  >
                    Create Alert
                  </button>
                </div>
              </form>
              </div>
            </motion.div>
          </motion.div>
        </AnimatePresence>,
        document.body
      )}

      {/* Resolve Confirmation Modal */}
      {showResolveModal && createPortal(
        <AnimatePresence>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
            onClick={() => setShowResolveModal(false)}
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
                  <div className="w-12 h-12 mx-auto bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center mb-4">
                    <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Resolve Alert
                  </h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Are you sure you want to resolve this alert? This action cannot be undone.
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
            </motion.div>
          </motion.div>
        </AnimatePresence>,
        document.body
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && createPortal(
        <AnimatePresence>
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
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Delete Alert
                  </h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Are you sure you want to delete this alert? This action cannot be undone.
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
            </motion.div>
          </motion.div>
        </AnimatePresence>,
        document.body
      )}
    </div>
  );
};

export default AlertsPage;