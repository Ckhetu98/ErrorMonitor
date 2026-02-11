import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Shield, User, Activity, Clock, Search, Filter, Download, Eye, AlertTriangle, CheckCircle, XCircle, Settings } from 'lucide-react'
import { auditService } from '../services/auditService'
import { useSelector } from 'react-redux'
import { createPortal } from 'react-dom'

const AuditPage = () => {
  const { user } = useSelector(state => state.auth)
  const [auditLogs, setAuditLogs] = useState([])
  const [filteredLogs, setFilteredLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterAction, setFilterAction] = useState('all')
  const [filterUser, setFilterUser] = useState('all')
  const [dateRange, setDateRange] = useState('7d')
  const [viewingLog, setViewingLog] = useState(null)

  useEffect(() => {
    fetchAuditLogs()
  }, [])

  useEffect(() => {
    filterLogs()
  }, [auditLogs, searchTerm, filterAction, filterUser, dateRange])

  const fetchAuditLogs = async () => {
    try {
      setLoading(true)
      
      // Check if user is logged in
      const token = localStorage.getItem('token')
      const userData = localStorage.getItem('user')
      
      console.log('Token exists:', !!token)
      console.log('User data exists:', !!userData)
      
      if (!token || !userData) {
        console.warn('No token or user data found, using mock data')
        setAuditLogs(getMockAuditLogs())
        return
      }
      
      const data = await auditService.getAuditLogs()
      setAuditLogs(data)
    } catch (error) {
      console.error('Error fetching audit logs:', error)
      // Use mock data as fallback
      setAuditLogs(getMockAuditLogs())
    } finally {
      setLoading(false)
    }
  }
  
  const getMockAuditLogs = () => {
    return [
      {
        id: 1,
        userId: 1,
        action: 'LOGIN',
        entityType: 'User',
        entityId: '1',
        userName: user?.username || 'Admin',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        ipAddress: '127.0.0.1'
      },
      {
        id: 2,
        userId: 1,
        action: 'CREATE_APPLICATION',
        entityType: 'Application',
        entityId: '1',
        userName: user?.username || 'Admin',
        createdAt: new Date(Date.now() - 1800000).toISOString(),
        ipAddress: '127.0.0.1'
      }
    ]
  }

  const filterLogs = () => {
    let filtered = auditLogs

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(log =>
        log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.entityType.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (log.userName && log.userName.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (log.entityId && log.entityId.toLowerCase().includes(searchTerm.toLowerCase()))
      )
    }

    // Action filter
    if (filterAction !== 'all') {
      filtered = filtered.filter(log => log.action === filterAction)
    }

    // User filter
    if (filterUser !== 'all') {
      filtered = filtered.filter(log => log.userName === filterUser)
    }

    // Date range filter
    if (dateRange !== 'all') {
      const now = new Date()
      const days = parseInt(dateRange.replace('d', ''))
      const cutoffDate = new Date(now.getTime() - (days * 24 * 60 * 60 * 1000))
      
      filtered = filtered.filter(log => {
        const logDate = new Date(log.createdAt)
        return logDate >= cutoffDate
      })
    }

    setFilteredLogs(filtered)
  }

  const getActionIcon = (action) => {
    switch (action) {
      case 'LOGIN': return <User className="w-4 h-4 text-green-500" />
      case 'LOGOUT': return <User className="w-4 h-4 text-gray-500" />
      case 'CREATE_APPLICATION': return <Settings className="w-4 h-4 text-blue-500" />
      case 'DELETE_APPLICATION': return <XCircle className="w-4 h-4 text-red-500" />
      case 'CREATE_ALERT': return <Shield className="w-4 h-4 text-blue-500" />
      case 'RESOLVE_ALERT': return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'DELETE_ALERT': return <XCircle className="w-4 h-4 text-red-500" />
      case 'RESOLVE_ERROR': return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'DELETE_ERROR': return <XCircle className="w-4 h-4 text-red-500" />
      case 'CREATE_ERROR': return <AlertTriangle className="w-4 h-4 text-orange-500" />
      default: return <Activity className="w-4 h-4 text-gray-500" />
    }
  }

  const getActionColor = (action) => {
    switch (action) {
      case 'LOGIN': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-200'
      case 'LOGOUT': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-200'
      case 'CREATE_APPLICATION':
      case 'CREATE_ALERT':
      case 'CREATE_ERROR': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-200'
      case 'RESOLVE_ALERT':
      case 'RESOLVE_ERROR': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-200'
      case 'DELETE_APPLICATION':
      case 'DELETE_ALERT':
      case 'DELETE_ERROR': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-200'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-200'
    }
  }

  const formatTime = (timestamp) => {
    const now = new Date()
    const logTime = new Date(timestamp)
    const diffMs = now - logTime
    const diffMins = Math.floor(diffMs / 60000)
    
    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins} min ago`
    const diffHours = Math.floor(diffMins / 60)
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`
    const diffDays = Math.floor(diffHours / 24)
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`
  }

  const exportLogs = () => {
    const csvContent = [
      ['Timestamp', 'User', 'Action', 'Entity Type', 'Entity ID', 'IP Address'].join(','),
      ...filteredLogs.map(log => [
        new Date(log.createdAt).toLocaleString(),
        log.userName || 'System',
        log.action,
        log.entityType,
        log.entityId || '',
        log.ipAddress || ''
      ].join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  }

  const uniqueUsers = [...new Set(auditLogs.map(log => log.userName).filter(Boolean))]
  const uniqueActions = [...new Set(auditLogs.map(log => log.action))]

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Audit & Activity Logs</h1>
          <p className="text-gray-600 dark:text-gray-400">
            Track system activities and user actions for governance and accountability
          </p>
        </div>
        <button
          onClick={exportLogs}
          className="btn-secondary flex items-center"
        >
          <Download className="w-4 h-4 mr-2" />
          Export CSV
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="card"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Activities</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{auditLogs.length}</p>
            </div>
            <Activity className="w-8 h-8 text-blue-500" />
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="card"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Active Users</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{uniqueUsers.length}</p>
            </div>
            <User className="w-8 h-8 text-green-500" />
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="card"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Today's Activities</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">
                {auditLogs.filter(log => {
                  const today = new Date().toDateString()
                  const logDate = new Date(log.createdAt).toDateString()
                  return today === logDate
                }).length}
              </p>
            </div>
            <Clock className="w-8 h-8 text-orange-500" />
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="card"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Action Types</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{uniqueActions.length}</p>
            </div>
            <Shield className="w-8 h-8 text-purple-500" />
          </div>
        </motion.div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="grid gap-4 md:grid-cols-5">
          <div className="md:col-span-2">
            <label className="label">Search</label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Search logs..."
                className="input pl-10 w-full"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div>
            <label className="label">Action</label>
            <select
              className="input w-full"
              value={filterAction}
              onChange={(e) => setFilterAction(e.target.value)}
            >
              <option value="all">All Actions</option>
              {uniqueActions.map(action => (
                <option key={action} value={action}>{action.replace('_', ' ')}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">User</label>
            <select
              className="input w-full"
              value={filterUser}
              onChange={(e) => setFilterUser(e.target.value)}
            >
              <option value="all">All Users</option>
              {uniqueUsers.map(userName => (
                <option key={userName} value={userName}>{userName}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">Date Range</label>
            <select
              className="input w-full"
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value)}
            >
              <option value="all">All Time</option>
              <option value="1d">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
            </select>
          </div>
        </div>
        
        <div className="mt-4">
          <button
            onClick={() => {
              setSearchTerm('')
              setFilterAction('all')
              setFilterUser('all')
              setDateRange('7d')
            }}
            className="btn-secondary"
          >
            Clear Filters
          </button>
        </div>
      </div>

      {/* Audit Logs */}
      <div className="card">
        <div className="space-y-4">
          {filteredLogs.map((log) => (
            <motion.div
              key={log.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              <div className="flex items-center space-x-4">
                {getActionIcon(log.action)}
                <div>
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getActionColor(log.action)}`}>
                      {log.action.replace('_', ' ')}
                    </span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {log.userName || 'System'}
                    </span>
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    {log.entityType} {log.entityId && `(ID: ${log.entityId})`}
                  </div>
                </div>
              </div>
              
              <div className="flex items-center space-x-3">
                <div className="text-right">
                  <div className="text-sm text-gray-900 dark:text-white">
                    {formatTime(log.createdAt)}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    {new Date(log.createdAt).toLocaleDateString()}
                  </div>
                </div>
                <button
                  onClick={() => setViewingLog(log)}
                  className="p-2 text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                  title="View details"
                >
                  <Eye className="w-4 h-4" />
                </button>
              </div>
            </motion.div>
          ))}

          {filteredLogs.length === 0 && (
            <div className="text-center py-12">
              <Activity className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No audit logs found</h3>
              <p className="text-gray-500 dark:text-gray-400">
                {searchTerm || filterAction !== 'all' || filterUser !== 'all' || dateRange !== 'all'
                  ? 'Try adjusting your filters'
                  : 'Audit logs will appear here as users perform actions'
                }
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Log Details Modal */}
      {viewingLog && createPortal(
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">Audit Log Details</h2>
                <button 
                  onClick={() => setViewingLog(null)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <XCircle className="w-6 h-6" />
                </button>
              </div>
              
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Action</label>
                    <div className="flex items-center space-x-2 mt-1">
                      {getActionIcon(viewingLog.action)}
                      <span className={`px-2 py-1 rounded text-xs font-medium ${getActionColor(viewingLog.action)}`}>
                        {viewingLog.action.replace('_', ' ')}
                      </span>
                    </div>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">User</label>
                    <p className="text-gray-900 dark:text-white mt-1">{viewingLog.userName || 'System'}</p>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Entity Type</label>
                    <p className="text-gray-900 dark:text-white mt-1">{viewingLog.entityType}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Entity ID</label>
                    <p className="text-gray-900 dark:text-white mt-1">{viewingLog.entityId || 'N/A'}</p>
                  </div>
                </div>
                
                <div>
                  <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Timestamp</label>
                  <p className="text-gray-900 dark:text-white mt-1">
                    {new Date(viewingLog.createdAt).toLocaleString()}
                  </p>
                </div>
                
                {viewingLog.ipAddress && (
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">IP Address</label>
                    <p className="text-gray-900 dark:text-white mt-1 font-mono">{viewingLog.ipAddress}</p>
                  </div>
                )}
                
                {viewingLog.userAgent && (
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">User Agent</label>
                    <p className="text-gray-900 dark:text-white mt-1 text-sm break-all">{viewingLog.userAgent}</p>
                  </div>
                )}
                
                {viewingLog.oldValues && (
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">Old Values</label>
                    <pre className="bg-gray-50 dark:bg-gray-900 p-3 rounded text-xs font-mono text-gray-600 dark:text-gray-400 overflow-x-auto mt-1">
                      {viewingLog.oldValues}
                    </pre>
                  </div>
                )}
                
                {viewingLog.newValues && (
                  <div>
                    <label className="text-sm font-medium text-gray-500 dark:text-gray-400">New Values</label>
                    <pre className="bg-gray-50 dark:bg-gray-900 p-3 rounded text-xs font-mono text-gray-600 dark:text-gray-400 overflow-x-auto mt-1">
                      {viewingLog.newValues}
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  )
}

export default AuditPage