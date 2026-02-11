import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { 
  Shield, 
  Zap, 
  BarChart3, 
  Bell, 
  Users, 
  ArrowRight,
  CheckCircle,
  Star,
  Sun,
  Moon,
  Menu,
  X
} from 'lucide-react'
import { useTheme } from '../../context/ThemeContext'

const LandingPage = () => {
  const { isDark, toggleTheme } = useTheme()
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  
  const features = [
    {
      icon: Zap,
      title: 'Real-Time Monitoring',
      description: 'Monitor errors as they happen with instant notifications and live dashboards.'
    },
    {
      icon: BarChart3,
      title: 'Advanced Analytics',
      description: 'Comprehensive error analytics with trends, patterns, and insights.'
    },
    {
      icon: Shield,
      title: 'Secure & Reliable',
      description: 'Enterprise-grade security with role-based access control.'
    },
    {
      icon: Bell,
      title: 'Smart Alerts',
      description: 'Intelligent alerting system that notifies you when it matters most.'
    }
  ]

  const stats = [
    { label: 'Errors Detected', value: '10,000+' },
    { label: 'Applications Monitored', value: '50+' },
    { label: 'Response Time', value: '<50ms' },
    { label: 'Uptime', value: '99.9%' }
  ]

  return (
    <div className="min-h-screen bg-white dark:bg-gray-900 text-gray-900 dark:text-white">
      {/* Navigation */}
      <nav className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-b border-gray-200 dark:border-gray-700 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Shield className="w-8 h-8 text-blue-600 mr-2" />
              <span className="text-xl font-bold text-gray-900 dark:text-white">ErrorMonitor</span>
            </div>
            
            {/* Desktop Menu */}
            <div className="hidden md:flex items-center space-x-8">
              <Link to="/about" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors">About</Link>
              <Link to="/contact" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors">Contact</Link>
              <button
                onClick={toggleTheme}
                className="p-2 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
                aria-label="Toggle theme"
              >
                {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
              </button>
              <Link to="/login" className="btn-secondary">Login</Link>
              <Link to="/register" className="btn-primary">Sign Up</Link>
            </div>
            
            {/* Mobile Menu Button */}
            <div className="md:hidden flex items-center space-x-2">
              <button
                onClick={toggleTheme}
                className="p-2 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300"
                aria-label="Toggle theme"
              >
                {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
              </button>
              <button
                onClick={() => setIsMenuOpen(!isMenuOpen)}
                className="p-2 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300"
                aria-label="Toggle menu"
              >
                {isMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
              </button>
            </div>
          </div>
          
          {/* Mobile Menu */}
          {isMenuOpen && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              className="md:hidden py-4 border-t border-gray-200 dark:border-gray-700"
            >
              <div className="flex flex-col space-y-4">
                <Link 
                  to="/about" 
                  className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  About
                </Link>
                <Link 
                  to="/contact" 
                  className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  Contact
                </Link>
                <div className="flex flex-col space-y-2 pt-2">
                  <Link 
                    to="/login" 
                    className="btn-secondary text-center"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Login
                  </Link>
                  <Link 
                    to="/register" 
                    className="btn-primary text-center"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Sign Up
                  </Link>
                </div>
              </div>
            </motion.div>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="text-left">
              <motion.h1
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8 }}
                className="text-5xl md:text-6xl font-bold text-gray-900 dark:text-white mb-6"
              >
                Monitor Errors in
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                  {' '}Real-Time
                </span>
              </motion.h1>
              
              <motion.p
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.2 }}
                className="text-xl text-gray-600 dark:text-gray-300 mb-8 max-w-2xl"
              >
                Detect, analyze, and resolve application errors faster with our comprehensive 
                monitoring system. Get instant alerts and detailed insights.
              </motion.p>
              
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.4 }}
                className="flex flex-col sm:flex-row gap-4"
              >
                <Link to="/register" className="btn-primary text-lg px-8 py-3">
                  Get Started Free
                  <ArrowRight className="w-5 h-5 ml-2" />
                </Link>
                <Link to="/about" className="btn-secondary text-lg px-8 py-3">
                  Learn More
                </Link>
              </motion.div>
            </div>

            {/* Hero Video */}
            <motion.div
              initial={{ opacity: 0, x: 50 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 1, delay: 0.3 }}
              className="relative"
            >
              <motion.video
                src="/assets/asset-63e44766.mp4"
                autoPlay
                loop
                muted
                playsInline
                className="w-full h-auto rounded-2xl shadow-2xl"
                animate={{ 
                  y: [0, -10, 0]
                }}
                transition={{ 
                  duration: 6, 
                  repeat: Infinity, 
                  ease: "easeInOut" 
                }}
              />
              
              {/* Floating elements around the video */}
              <motion.div
                animate={{ 
                  y: [0, -20, 0],
                  rotate: [0, 10, 0]
                }}
                transition={{ 
                  duration: 4, 
                  repeat: Infinity,
                  ease: "easeInOut"
                }}
                className="absolute -top-4 -right-4 w-20 h-20 bg-blue-500 rounded-full opacity-20 blur-xl"
              />
              
              <motion.div
                animate={{ 
                  y: [0, 15, 0],
                  rotate: [0, -10, 0]
                }}
                transition={{ 
                  duration: 5, 
                  repeat: Infinity,
                  ease: "easeInOut",
                  delay: 1
                }}
                className="absolute -bottom-6 -left-6 w-16 h-16 bg-purple-500 rounded-full opacity-20 blur-xl"
              />
            </motion.div>
          </div>
        </div>

        {/* Background floating elements */}
        <motion.div
          animate={{ y: [0, -20, 0] }}
          transition={{ duration: 4, repeat: Infinity }}
          className="absolute top-20 left-10 w-20 h-20 bg-blue-200 rounded-full opacity-20"
        />
        <motion.div
          animate={{ y: [0, 20, 0] }}
          transition={{ duration: 3, repeat: Infinity }}
          className="absolute top-40 right-20 w-16 h-16 bg-purple-200 rounded-full opacity-20"
        />
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-gray-100 dark:bg-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map((stat, index) => (
              <motion.div
                key={stat.label}
                initial={{ opacity: 0, scale: 0.5 }}
                whileInView={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="text-center"
              >
                <div className="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-2">{stat.value}</div>
                <div className="text-gray-600 dark:text-gray-300">{stat.label}</div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-white dark:bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Powerful Features
            </h2>
            <p className="text-xl text-gray-600 dark:text-gray-300">
              Everything you need to monitor and manage application errors
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <motion.div
                key={feature.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="card-hover text-center bg-gray-50 dark:bg-gray-800/50 p-6 rounded-xl border border-gray-200 dark:border-gray-700"
              >
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-600/20 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <feature.icon className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600 dark:text-gray-300">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-blue-600 to-purple-600">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <h2 className="text-4xl font-bold text-white mb-4">
              Ready to Get Started?
            </h2>
            <p className="text-xl text-blue-100 mb-8">
              Join thousands of developers who trust our error monitoring system
            </p>
            <Link to="/register" className="btn bg-white text-blue-600 hover:bg-gray-100 text-lg px-8 py-3">
              Start Free Trial
            </Link>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 dark:bg-gray-950 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center mb-4">
                <Shield className="w-8 h-8 text-blue-400 mr-2" />
                <span className="text-xl font-bold">ErrorMonitor</span>
              </div>
              <p className="text-gray-400">
                Real-time error monitoring and detection system for modern applications.
              </p>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link to="/" className="hover:text-white">Features</Link></li>
                <li><Link to="/" className="hover:text-white">Pricing</Link></li>
                <li><Link to="/" className="hover:text-white">Documentation</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Company</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link to="/about" className="hover:text-white">About</Link></li>
                <li><Link to="/contact" className="hover:text-white">Contact</Link></li>
                <li><Link to="/" className="hover:text-white">Careers</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link to="/" className="hover:text-white">Help Center</Link></li>
                <li><Link to="/" className="hover:text-white">Status</Link></li>
                <li><Link to="/" className="hover:text-white">Security</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2025 ErrorMonitor. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default LandingPage