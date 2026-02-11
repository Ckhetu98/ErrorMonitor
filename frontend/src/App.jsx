import React, { useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { motion, AnimatePresence } from 'framer-motion'
import { useLocation } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'

// Theme Provider
import { ThemeProvider } from './context/ThemeContext'

// Components
import Layout from './components/Layout/Layout'
import ProtectedRoute from './components/Auth/ProtectedRoute'
import LoadingSpinner from './components/UI/LoadingSpinner'

// Pages
import LandingPage from './pages/Public/LandingPage'
import AboutPage from './pages/Public/AboutPage'
import ContactPage from './pages/Public/ContactPage'
import LoginPage from './pages/Auth/LoginPage'
import RegisterPage from './pages/Auth/RegisterPage'
import LoginSuccess from './pages/Auth/LoginSuccess'
import GoogleCallback from './pages/Auth/GoogleCallback'
import AuthCallback from './pages/Auth/AuthCallback'
import OTPVerificationPage from './pages/Auth/OTPVerificationPage'
import ErrorsPage from './pages/Errors/ErrorsPage'
import ApplicationsPage from './pages/Applications/ApplicationsPage'
import ReportsPage from './pages/Reports/ReportsPage'
import AlertsPage from './pages/Alerts/AlertsPage'
import SettingsPage from './pages/Settings/SettingsPage'
import UsersPage from './pages/UsersPage'
import AuditPage from './pages/AuditPage'
import ContactManagementPage from './pages/ContactManagementPage'
import DashboardPage from './pages/Dashboard/DashboardPage'
import NotFoundPage from './pages/NotFound/NotFoundPage'

// Services
import { signalRService } from './services/signalRService'
import { errorMonitor } from './utils/errorMonitor'

// Page transition variants
const pageVariants = {
  initial: {
    opacity: 0,
    x: -20,
  },
  in: {
    opacity: 1,
    x: 0,
  },
  out: {
    opacity: 0,
    x: 20,
  },
}

const pageTransition = {
  type: 'tween',
  ease: 'anticipate',
  duration: 0.4,
}

function App() {
  const dispatch = useDispatch()
  const location = useLocation()
  const { isAuthenticated, isLoading } = useSelector((state) => state.auth)

  useEffect(() => {
    // Initialize SignalR connection when authenticated
    if (isAuthenticated) {
      signalRService.startConnection()
    }

    return () => {
      signalRService.stopConnection()
    }
  }, [isAuthenticated])

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white dark:bg-gray-900">
        <LoadingSpinner size="large" />
      </div>
    )
  }

  return (
    <ThemeProvider>
      <div className="App bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 min-h-screen transition-colors duration-300">
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#ffffff',
              color: '#374151',
              border: '1px solid #e5e7eb',
              borderRadius: '8px',
              boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
              fontSize: '14px',
              fontWeight: '500',
            },
            success: {
              style: {
                background: '#10b981',
                color: '#ffffff',
                border: '1px solid #059669',
              },
            },
            error: {
              style: {
                background: '#ef4444',
                color: '#ffffff',
                border: '1px solid #dc2626',
              },
            },
          }}
        />
        <AnimatePresence mode="wait">
            <Routes location={location} key={location.pathname}>
            {/* Public Routes - Protected after login */}
            <Route
              path="/"
              element={
                isAuthenticated ? (
                  <Navigate to="/dashboard" replace />
                ) : (
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <LandingPage />
                  </motion.div>
                )
              }
            />
            <Route
              path="/about"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <AboutPage />
                </motion.div>
              }
            />
            <Route
              path="/contact"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <ContactPage />
                </motion.div>
              }
            />

            {/* Auth Routes */}
            <Route
              path="/login"
              element={
                isAuthenticated ? (
                  <Navigate to="/dashboard" replace />
                ) : (
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <LoginPage />
                  </motion.div>
                )
              }
            />
            <Route
              path="/register"
              element={
                isAuthenticated ? (
                  <Navigate to="/dashboard" replace />
                ) : (
                  <motion.div
                    initial="initial"
                    animate="in"
                    exit="out"
                    variants={pageVariants}
                    transition={pageTransition}
                  >
                    <RegisterPage />
                  </motion.div>
                )
              }
            />
            <Route
              path="/login/success"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <LoginSuccess />
                </motion.div>
              }
            />
            <Route
              path="/auth/google/callback"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <GoogleCallback />
                </motion.div>
              }
            />
            <Route
              path="/auth/callback"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <AuthCallback />
                </motion.div>
              }
            />
            <Route
              path="/auth/verify-otp"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <OTPVerificationPage />
                </motion.div>
              }
            />

            {/* Protected Routes */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <DashboardPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/errors"
              element={
                <ProtectedRoute roles={['ADMIN', 'DEVELOPER']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <ErrorsPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/applications"
              element={
                <ProtectedRoute roles={['ADMIN', 'DEVELOPER']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <ApplicationsPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/reports"
              element={
                <ProtectedRoute>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <ReportsPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/alerts"
              element={
                <ProtectedRoute roles={['ADMIN', 'DEVELOPER']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <AlertsPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/settings"
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <SettingsPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/users"
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <UsersPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/contact-management"
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <ContactManagementPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/audit"
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <Layout>
                    <motion.div
                      initial="initial"
                      animate="in"
                      exit="out"
                      variants={pageVariants}
                      transition={pageTransition}
                    >
                      <AuditPage />
                    </motion.div>
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="*"
              element={
                <motion.div
                  initial="initial"
                  animate="in"
                  exit="out"
                  variants={pageVariants}
                  transition={pageTransition}
                >
                  <NotFoundPage />
                </motion.div>
              }
            />
          </Routes>
        </AnimatePresence>
      </div>
    </ThemeProvider>
  )
}

export default App