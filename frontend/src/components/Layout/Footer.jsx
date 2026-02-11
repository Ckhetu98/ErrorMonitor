import React from 'react'
import { Link } from 'react-router-dom'
import { Shield } from 'lucide-react'

const Footer = () => {
  return (
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
  )
}

export default Footer