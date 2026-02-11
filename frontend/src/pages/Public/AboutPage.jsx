import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { 
  Shield, 
  Zap, 
  BarChart3, 
  Bell, 
  CheckCircle,
  Target,
  Lightbulb,
  Heart,
  Code,
  Database,
  Cloud,
  Award,
  Sun,
  Moon,
  Menu,
  X
} from 'lucide-react'
import { useTheme } from '../../context/ThemeContext'
import Footer from '../../components/Layout/Footer'

const AboutPage = () => {
  const { isDark, toggleTheme } = useTheme()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const team = [
    {
      name: 'Khetesh Choudhary',
      description: 'Project Leader',
      image: '/images/khetesh.jpg'
    },
    {
      name: 'Pranjali Rane',
      image: '/images/pranjali.jpeg'
    },
    {
      name: 'Swapna Saste',
      image: '/images/Swapna Sanjay Saste.jpg'
    },
    {
      name: 'Aditya Adhikari',
      image: '/images/aditya.jpeg'
    },
     {
      name: 'Pratik Avhad',
      image: '/images/Pratik Avhad.jpeg'
    }
  ]

  const stats = [
    { label: 'Errors Detected', value: '10M+', icon: Target },
    { label: 'Applications Monitored', value: '5,000+', icon: Code },
    { label: 'Happy Users', value: '1,200+', icon: Heart },
    { label: 'Uptime', value: '99.9%', icon: Award }
  ]

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

  return (
    <div className="min-h-screen bg-white dark:bg-gray-900 text-gray-900 dark:text-white transition-colors duration-300">
      {/* Navigation */}
      <nav className="bg-white/80 dark:bg-gray-900/80 backdrop-blur-sm border-b border-gray-200 dark:border-gray-700 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <Link to="/" className="flex items-center">
              <Shield className="w-8 h-8 text-blue-600 dark:text-blue-400 mr-2" />
              <span className="text-xl font-bold text-gray-900 dark:text-white">ErrorMonitor</span>
            </Link>
            
            {/* Desktop Menu */}
            <div className="hidden md:flex items-center space-x-8">
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
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center mb-16"
          >
            <h1 className="text-5xl md:text-6xl font-bold text-gray-900 dark:text-white mb-6">
              About 
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                ErrorMonitor
              </span>
            </h1>
            <p className="text-xl text-gray-600 dark:text-gray-400 max-w-3xl mx-auto">
              We're building the future of application monitoring with real-time error detection, 
              intelligent analytics, and seamless developer experience.
            </p>
          </motion.div>
        </div>
      </section>

      {/* Mission Section */}
      <section className="py-16 bg-gray-50 dark:bg-gray-800 transition-colors duration-300">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.8 }}
            >
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-6">Our Mission</h2>
              <p className="text-lg text-gray-600 dark:text-gray-400 mb-6">
                To empower developers and teams with the most advanced error monitoring platform, 
                enabling them to build more reliable applications and deliver exceptional user experiences.
              </p>
              <div className="space-y-4">
                <div className="flex items-center">
                  <CheckCircle className="w-6 h-6 text-green-500 mr-3" />
                  <span className="text-gray-700 dark:text-gray-300">Real-time error detection and alerting</span>
                </div>
                <div className="flex items-center">
                  <CheckCircle className="w-6 h-6 text-green-500 mr-3" />
                  <span className="text-gray-700 dark:text-gray-300">Comprehensive analytics and insights</span>
                </div>
                <div className="flex items-center">
                  <CheckCircle className="w-6 h-6 text-green-500 mr-3" />
                  <span className="text-gray-700 dark:text-gray-300">Seamless integration with existing workflows</span>
                </div>
              </div>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, x: 50 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.8 }}
              className="relative"
            >
              <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl p-8 text-white">
                <Lightbulb className="w-16 h-16 mb-6" />
                <h3 className="text-2xl font-bold mb-4">Innovation First</h3>
                <p className="text-blue-100">
                  We believe in pushing the boundaries of what's possible in application monitoring, 
                  constantly innovating to stay ahead of the curve.
                </p>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16">
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
                <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center mx-auto mb-4">
                  <stat.icon className="w-8 h-8 text-blue-600 dark:text-blue-400" />
                </div>
                <div className="text-3xl font-bold text-gray-900 dark:text-white mb-2">{stat.value}</div>
                <div className="text-gray-600 dark:text-gray-400">{stat.label}</div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gray-50 dark:bg-gray-800 transition-colors duration-300">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Why Choose ErrorMonitor?
            </h2>
            <p className="text-xl text-gray-600 dark:text-gray-400">
              Built by developers, for developers
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <motion.div
                key={feature.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="bg-white dark:bg-gray-700 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-600 hover:shadow-lg transition-all duration-200"
              >
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center mb-4">
                  <feature.icon className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600 dark:text-gray-400">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Team Section */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Meet Our Team
            </h2>
            <p className="text-xl text-gray-600 dark:text-gray-400">
              The passionate people behind ErrorMonitor
            </p>
          </div>
          
          {/* Top row - 3 members */}
          <div className="flex justify-center mb-12">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-16 max-w-4xl">
              {team.slice(0, 3).map((member, index) => (
                <motion.div
                  key={member.name}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: index * 0.1 }}
                  className="text-center"
                >
                  <div className="w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full border-4 border-gray-200 dark:border-gray-700">
                    <img 
                      src={member.image} 
                      alt={member.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-1">
                    {member.name}
                  </h3>
                  {member.description && (
                    <p className="text-blue-600 dark:text-blue-400 font-medium">
                      {member.description}
                    </p>
                  )}
                </motion.div>
              ))}
            </div>
          </div>
          
          {/* Bottom row - 2 members */}
          <div className="flex justify-center">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-16 max-w-2xl">
              {team.slice(3, 5).map((member, index) => (
                <motion.div
                  key={member.name}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: (index + 3) * 0.1 }}
                  className="text-center"
                >
                  <div className="w-32 h-32 mx-auto mb-4 overflow-hidden rounded-full border-4 border-gray-200 dark:border-gray-700">
                    <img 
                      src={member.image} 
                      alt={member.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-1">
                    {member.name}
                  </h3>
                  {member.description && (
                    <p className="text-blue-600 dark:text-blue-400 font-medium">
                      {member.description}
                    </p>
                  )}
                </motion.div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Technology Stack */}
      <section className="py-20 bg-gray-50 dark:bg-gray-800 transition-colors duration-300">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Built with Modern Technology
            </h2>
            <p className="text-xl text-gray-600 dark:text-gray-400">
              Leveraging the latest technologies for optimal performance
            </p>
          </div>
          
          <div className="grid md:grid-cols-3 gap-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="text-center"
            >
              <Code className="w-16 h-16 text-blue-600 dark:text-blue-400 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Frontend</h3>
              <p className="text-gray-600 dark:text-gray-400">React 18, Tailwind CSS, Framer Motion</p>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
              className="text-center"
            >
              <Database className="w-16 h-16 text-green-600 dark:text-green-400 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Backend</h3>
              <p className="text-gray-600 dark:text-gray-400">.NET 8, SignalR, SQL Server</p>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="text-center"
            >
              <Cloud className="w-16 h-16 text-purple-600 dark:text-purple-400 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Architecture</h3>
              <p className="text-gray-600 dark:text-gray-400">MVC Web API, RESTful Services</p>
            </motion.div>
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
              Join thousands of developers who trust ErrorMonitor for their application monitoring needs
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/register" className="btn bg-white text-blue-600 hover:bg-gray-100 text-lg px-8 py-3">
                Start Free Trial
              </Link>
              <Link to="/contact" className="btn border-2 border-white text-white hover:bg-white hover:text-blue-600 text-lg px-8 py-3">
                Contact Sales
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      <Footer />
    </div>
  )
}

export default AboutPage