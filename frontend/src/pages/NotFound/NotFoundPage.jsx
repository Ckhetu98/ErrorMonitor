import React from 'react'
import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { Home, ArrowLeft, Search } from 'lucide-react'

const NotFoundPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center px-4">
      <div className="text-center max-w-md mx-auto">
        {/* Animated 404 */}
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ 
            duration: 0.8, 
            type: "spring", 
            stiffness: 100 
          }}
          className="mb-8"
        >
          <h1 className="text-9xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
            404
          </h1>
        </motion.div>

        {/* Floating Elements */}
        <motion.div
          animate={{ 
            y: [0, -20, 0],
            rotate: [0, 5, -5, 0]
          }}
          transition={{ 
            duration: 4, 
            repeat: Infinity,
            ease: "easeInOut"
          }}
          className="absolute top-20 left-10 w-16 h-16 bg-blue-200 rounded-full opacity-30"
        />
        
        <motion.div
          animate={{ 
            y: [0, 20, 0],
            rotate: [0, -5, 5, 0]
          }}
          transition={{ 
            duration: 3, 
            repeat: Infinity,
            ease: "easeInOut",
            delay: 1
          }}
          className="absolute top-32 right-20 w-12 h-12 bg-purple-200 rounded-full opacity-30"
        />

        {/* Error Message */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mb-8"
        >
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            Oops! Page Not Found
          </h2>
          <p className="text-lg text-gray-600 mb-2">
            The page you're looking for doesn't exist or has been moved.
          </p>
          <p className="text-gray-500">
            Don't worry, even our error monitoring system couldn't catch this one! 😄
          </p>
        </motion.div>

        {/* Search Suggestion */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="mb-8"
        >
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-center mb-4">
              <Search className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              What were you looking for?
            </h3>
            <ul className="text-sm text-gray-600 space-y-1">
              <li>• <Link to="/dashboard" className="text-blue-600 hover:underline">Dashboard</Link> - Monitor your applications</li>
              <li>• <Link to="/errors" className="text-blue-600 hover:underline">Errors</Link> - View error logs</li>
              <li>• <Link to="/applications" className="text-blue-600 hover:underline">Applications</Link> - Manage your apps</li>
              <li>• <Link to="/reports" className="text-blue-600 hover:underline">Reports</Link> - Generate reports</li>
            </ul>
          </div>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.7 }}
          className="flex flex-col sm:flex-row gap-4 justify-center"
        >
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => window.history.back()}
            className="btn-secondary flex items-center justify-center"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Go Back
          </motion.button>
          
          <Link to="/">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="btn-primary flex items-center justify-center w-full sm:w-auto"
            >
              <Home className="w-4 h-4 mr-2" />
              Go Home
            </motion.button>
          </Link>
        </motion.div>

        {/* Fun Error Code */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.6, delay: 1 }}
          className="mt-12 text-xs text-gray-400 font-mono"
        >
          Error Code: PAGE_NOT_FOUND_404
          <br />
          Timestamp: {new Date().toISOString()}
          <br />
          Severity: Low (Don't worry, it's not critical! 😊)
        </motion.div>

        {/* Animated Background Elements */}
        <motion.div
          animate={{ 
            scale: [1, 1.2, 1],
            opacity: [0.1, 0.3, 0.1]
          }}
          transition={{ 
            duration: 6, 
            repeat: Infinity,
            ease: "easeInOut"
          }}
          className="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-400 rounded-full blur-3xl -z-10"
          style={{ 
            width: '300px', 
            height: '300px',
            left: '50%',
            top: '50%',
            transform: 'translate(-50%, -50%)'
          }}
        />
      </div>
    </div>
  )
}

export default NotFoundPage