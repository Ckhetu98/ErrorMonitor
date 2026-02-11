import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  Mail, 
  MessageCircle, 
  Clock, 
  CheckCircle, 
  AlertCircle,
  Search,
  Filter,
  Send,
  X,
  User,
  Calendar,
  Tag
} from 'lucide-react'
import { contactService } from '../services/contactService'
import toast from 'react-hot-toast'

const ContactManagementPage = () => {
  const [queries, setQueries] = useState([])
  const [stats, setStats] = useState({})
  const [loading, setLoading] = useState(true)
  const [selectedQuery, setSelectedQuery] = useState(null)
  const [showResolveModal, setShowResolveModal] = useState(false)
  const [responseMessage, setResponseMessage] = useState('')
  const [isResolving, setIsResolving] = useState(false)
  const [filters, setFilters] = useState({
    status: '',
    search: '',
    page: 1,
    pageSize: 10
  })
  const [pagination, setPagination] = useState({})

  useEffect(() => {
    fetchQueries()
    fetchStats()
  }, [filters])

  const fetchQueries = async () => {
    try {
      setLoading(true)
      const response = await contactService.getQueries(
        filters.status || null, 
        filters.page, 
        filters.pageSize
      )
      
      // Handle different response formats
      if (Array.isArray(response)) {
        setQueries(response)
        setPagination({})
      } else if (response && response.queries) {
        setQueries(response.queries)
        setPagination({
          totalCount: response.totalCount,
          page: response.page,
          pageSize: response.pageSize,
          totalPages: response.totalPages
        })
      } else {
        setQueries([])
        setPagination({})
      }
    } catch (error) {
      console.error('Error fetching queries:', error)
      setQueries([]) // Ensure queries is always an array
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const statsData = await contactService.getStats()
      setStats(statsData)
    } catch (error) {
      console.error('Error fetching stats:', error)
    }
  }

  const handleResolveQuery = async () => {
    if (!selectedQuery || !responseMessage.trim()) {
      toast.error('Please enter a response message')
      return
    }

    try {
      setIsResolving(true)
      await contactService.resolveQuery(selectedQuery.id, responseMessage)
      
      // Update the query in the list
      setQueries(queries.map(q => 
        q.id === selectedQuery.id 
          ? { ...q, status: 'RESOLVED', responseSentAt: new Date().toISOString() }
          : q
      ))
      
      toast.success('Query resolved successfully! Email sent to customer.')
      setShowResolveModal(false)
      setSelectedQuery(null)
      setResponseMessage('')
      fetchStats() // Refresh stats
    } catch (error) {
      console.error('Error resolving query:', error)
      toast.error('Failed to resolve query. Please try again.')
    } finally {
      setIsResolving(false)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400'
      case 'RESOLVED': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
      case 'CLOSED': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400'
    }
  }

  const getSubjectLabel = (subject) => {
    const labels = {
      'general': 'General Inquiry',
      'support': 'Technical Support',
      'sales': 'Sales Question',
      'partnership': 'Partnership',
      'feedback': 'Feedback'
    }
    return labels[subject] || subject
  }

  const filteredQueries = (queries || []).filter(query => 
    !filters.search || 
    query.fullName?.toLowerCase().includes(filters.search.toLowerCase()) ||
    query.email?.toLowerCase().includes(filters.search.toLowerCase()) ||
    query.message?.toLowerCase().includes(filters.search.toLowerCase())
  )

  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Contact Management
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Manage and respond to customer queries and support requests
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
              <Mail className="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Queries</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.totalQueries || 0}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 dark:bg-yellow-900/30 rounded-lg">
              <Clock className="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Pending</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.pendingQueries || 0}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-lg">
              <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Resolved</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.resolvedQueries || 0}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
              <Calendar className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Today</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.todayQueries || 0}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Search queries..."
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                className="pl-10 input"
              />
            </div>
          </div>
          <div className="md:w-48">
            <select
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value, page: 1 })}
              className="input"
            >
              <option value="">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>
        </div>
      </div>

      {/* Queries List */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div className="p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Contact Queries</h2>
        </div>

        {loading ? (
          <div className="p-8 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-2 text-gray-600 dark:text-gray-400">Loading queries...</p>
          </div>
        ) : filteredQueries.length === 0 ? (
          <div className="p-8 text-center">
            <Mail className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-600 dark:text-gray-400">No queries found</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200 dark:divide-gray-700">
            {filteredQueries.map((query) => (
              <motion.div
                key={query.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="p-6 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-semibold text-gray-900 dark:text-white">
                        {query.fullName}
                      </h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(query.status)}`}>
                        {query.status}
                      </span>
                      <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-full text-xs">
                        {getSubjectLabel(query.subject)}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-2">{query.email}</p>
                    <p className="text-gray-700 dark:text-gray-300 mb-3 line-clamp-2">{query.message}</p>
                    <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-4 h-4" />
                        {new Date(query.createdAt).toLocaleDateString()}
                      </span>
                      {query.responseSentAt && (
                        <span className="flex items-center gap-1 text-green-600 dark:text-green-400">
                          <CheckCircle className="w-4 h-4" />
                          Responded
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2 ml-4">
                    <button
                      onClick={() => setSelectedQuery(query)}
                      className="px-3 py-1 text-sm bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 rounded-lg hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors"
                    >
                      View
                    </button>
                    {query.status !== 'RESOLVED' && (
                      <button
                        onClick={() => {
                          setSelectedQuery(query)
                          setShowResolveModal(true)
                        }}
                        className="px-3 py-1 text-sm bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 rounded-lg hover:bg-green-200 dark:hover:bg-green-900/50 transition-colors"
                      >
                        Resolve
                      </button>
                    )}
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="p-6 border-t border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Showing {((pagination.page - 1) * pagination.pageSize) + 1} to {Math.min(pagination.page * pagination.pageSize, pagination.totalCount)} of {pagination.totalCount} queries
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => setFilters({ ...filters, page: filters.page - 1 })}
                  disabled={filters.page <= 1}
                  className="px-3 py-1 text-sm border border-gray-300 dark:border-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-gray-700"
                >
                  Previous
                </button>
                <button
                  onClick={() => setFilters({ ...filters, page: filters.page + 1 })}
                  disabled={filters.page >= pagination.totalPages}
                  className="px-3 py-1 text-sm border border-gray-300 dark:border-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-gray-700"
                >
                  Next
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Resolve Modal */}
      {showResolveModal && selectedQuery && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
          >
            <div className="p-6 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Resolve Query
                </h3>
                <button
                  onClick={() => {
                    setShowResolveModal(false)
                    setResponseMessage('')
                  }}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <X className="w-6 h-6" />
                </button>
              </div>
            </div>

            <div className="p-6">
              {/* Query Details */}
              <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg mb-6">
                <h4 className="font-medium text-gray-900 dark:text-white mb-2">Original Query</h4>
                <div className="space-y-2 text-sm">
                  <p><span className="font-medium">From:</span> {selectedQuery.fullName} ({selectedQuery.email})</p>
                  <p><span className="font-medium">Subject:</span> {getSubjectLabel(selectedQuery.subject)}</p>
                  <p><span className="font-medium">Message:</span></p>
                  <p className="bg-white dark:bg-gray-800 p-3 rounded border">{selectedQuery.message}</p>
                </div>
              </div>

              {/* Response Form */}
              <div>
                <label className="label">Response Message</label>
                <textarea
                  value={responseMessage}
                  onChange={(e) => setResponseMessage(e.target.value)}
                  rows="8"
                  className="input"
                  placeholder="Type your response message here..."
                  required
                />
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">
                  This message will be sent to the customer via email.
                </p>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 dark:border-gray-700 flex gap-3 justify-end">
              <button
                onClick={() => {
                  setShowResolveModal(false)
                  setResponseMessage('')
                }}
                className="px-4 py-2 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
              >
                Cancel
              </button>
              <button
                onClick={handleResolveQuery}
                disabled={!responseMessage.trim() || isResolving}
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <Send className="w-4 h-4" />
                {isResolving ? 'Sending...' : 'Send Response'}
              </button>
            </div>
          </motion.div>
        </div>
      )}

      {/* Query Detail Modal */}
      {selectedQuery && !showResolveModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
          >
            <div className="p-6 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Query Details
                </h3>
                <button
                  onClick={() => setSelectedQuery(null)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <X className="w-6 h-6" />
                </button>
              </div>
            </div>

            <div className="p-6 space-y-4">
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Name</label>
                <p className="text-gray-900 dark:text-white">{selectedQuery.fullName}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Email</label>
                <p className="text-gray-900 dark:text-white">{selectedQuery.email}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Subject</label>
                <p className="text-gray-900 dark:text-white">{getSubjectLabel(selectedQuery.subject)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Status</label>
                <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedQuery.status)}`}>
                  {selectedQuery.status}
                </span>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Message</label>
                <p className="text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-700 p-3 rounded border">
                  {selectedQuery.message}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Submitted</label>
                <p className="text-gray-900 dark:text-white">{new Date(selectedQuery.createdAt).toLocaleString()}</p>
              </div>
              {selectedQuery.responseSentAt && (
                <div>
                  <label className="text-sm font-medium text-gray-600 dark:text-gray-400">Response Sent</label>
                  <p className="text-gray-900 dark:text-white">{new Date(selectedQuery.responseSentAt).toLocaleString()}</p>
                </div>
              )}
            </div>

            <div className="p-6 border-t border-gray-200 dark:border-gray-700 flex gap-3 justify-end">
              <button
                onClick={() => setSelectedQuery(null)}
                className="px-4 py-2 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
              >
                Close
              </button>
              {selectedQuery.status !== 'RESOLVED' && (
                <button
                  onClick={() => setShowResolveModal(true)}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center gap-2"
                >
                  <Send className="w-4 h-4" />
                  Resolve Query
                </button>
              )}
            </div>
          </motion.div>
        </div>
      )}
    </div>
  )
}

export default ContactManagementPage