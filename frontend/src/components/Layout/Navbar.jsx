import React from 'react'
import { NavLink } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { logoutUser } from '../../store/slices/authSlice'
import { alertService } from '../../services/alertService'
import { Bell, User, LogOut, Shield, LayoutDashboard, AlertTriangle, Smartphone, FileText, Settings, Mail, Menu, X } from 'lucide-react'
import toast from 'react-hot-toast'

const Navbar = () => {
  const dispatch = useDispatch()
  const { user } = useSelector((state) => state.auth)
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false)
  const [showNotifications, setShowNotifications] = React.useState(false)
  const [alerts, setAlerts] = React.useState([])
  const [alertCount, setAlertCount] = React.useState(0)

  React.useEffect(() => {
    fetchUnresolvedAlerts()
  }, [])

  const fetchUnresolvedAlerts = async () => {
    try {
      const unresolvedAlerts = await alertService.getUnresolvedAlerts()
      setAlerts(unresolvedAlerts.slice(0, 5)) // Show only first 5
      setAlertCount(unresolvedAlerts.length)
    } catch (error) {
      console.error('Error fetching alerts:', error)
    }
  }

  const handleLogout = () => {
    toast.success('Logged out successfully!')
    dispatch(logoutUser())
  }

  const navigation = [
    { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'DEVELOPER', 'VIEWER'] },
    { name: 'Errors', href: '/errors', icon: AlertTriangle, roles: ['ADMIN', 'DEVELOPER'] },
    { name: 'Applications', href: '/applications', icon: Smartphone, roles: ['ADMIN', 'DEVELOPER'] },
    { name: 'Reports', href: '/reports', icon: FileText, roles: ['ADMIN', 'DEVELOPER', 'VIEWER'] },
    { name: 'Alerts', href: '/alerts', icon: Bell, roles: ['ADMIN', 'DEVELOPER'] },
    { name: 'Contact', href: '/contact-management', icon: Mail, roles: ['ADMIN'] },
    { name: 'Users', href: '/users', icon: User, roles: ['ADMIN'] },
    { name: 'Audit', href: '/audit', icon: Shield, roles: ['ADMIN'] },
    { name: 'Settings', href: '/settings', icon: Settings, roles: ['ADMIN'] },
  ]

  const filteredNavigation = navigation.filter(item => 
    item.roles.includes(user?.role?.toUpperCase())
  )

  return (
    <nav className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700 transition-colors duration-300">
      <div className="w-full px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center space-x-2 flex-shrink-0">
            <Shield className="w-6 h-6 sm:w-8 sm:h-8 text-blue-600" />
            <span className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white whitespace-nowrap">Error Monitor</span>
          </div>
          
          {/* Desktop Navigation Menu */}
          <div className="hidden lg:flex items-center space-x-4 xl:space-x-6 flex-1 justify-center">
            {filteredNavigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                className={({ isActive }) =>
                  `flex items-center px-2 xl:px-3 py-2 rounded-md text-sm font-medium transition-colors whitespace-nowrap ${
                    isActive
                      ? 'text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                      : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-700'
                  }`
                }
              >
                <item.icon className="w-4 h-4 mr-1 xl:mr-2" />
                <span className="hidden xl:inline">{item.name}</span>
                <span className="xl:hidden">{item.name.split(' ')[0]}</span>
              </NavLink>
            ))}
          </div>
          
          {/* Right side actions */}
          <div className="relative flex items-center space-x-2 sm:space-x-4 flex-shrink-0">
            <button 
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative p-2 text-gray-400 dark:text-gray-300 hover:text-gray-500 dark:hover:text-gray-200 transition-colors"
            >
              <Bell className="w-4 h-4 sm:w-5 sm:h-5" />
              {/* Alert badge */}
              {alertCount > 0 && (
                <span className="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full flex items-center justify-center">
                  <span className="text-xs text-white font-bold">{alertCount > 9 ? '9+' : alertCount}</span>
                </span>
              )}
            </button>
            
            {/* Notifications dropdown */}
            {showNotifications && (
              <div className="absolute right-0 top-12 w-80 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50">
                <div className="p-4 border-b border-gray-200 dark:border-gray-700">
                  <h3 className="text-sm font-semibold text-gray-900 dark:text-white">Active Alerts</h3>
                </div>
                <div className="max-h-64 overflow-y-auto">
                  {alerts.length === 0 ? (
                    <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                      <p className="text-sm">No active alerts</p>
                    </div>
                  ) : (
                    alerts.map((alert) => (
                      <div key={alert.id} className="p-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700">
                        <div className="flex items-start space-x-3">
                          <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                            alert.alertLevel === 'Critical' ? 'bg-red-500' :
                            alert.alertLevel === 'High' ? 'bg-orange-500' :
                            alert.alertLevel === 'Medium' ? 'bg-yellow-500' : 'bg-green-500'
                          }`}></div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-900 dark:text-white">{alert.alertType}</p>
                            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 truncate">{alert.alertMessage}</p>
                            <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                              {new Date(alert.createdAt).toLocaleString()}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
                <div className="p-3 border-t border-gray-200 dark:border-gray-700">
                  <NavLink 
                    to="/alerts" 
                    onClick={() => setShowNotifications(false)}
                    className="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-500 dark:hover:text-blue-300 font-medium"
                  >
                    View all alerts
                  </NavLink>
                </div>
              </div>
            )}
            
            {/* Desktop User Info */}
            <div className="hidden sm:flex items-center space-x-2">
              <User className="w-4 h-4 sm:w-5 sm:h-5 text-gray-400 dark:text-gray-300" />
              <span className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 max-w-20 truncate">{user?.username}</span>
              <span className="px-1.5 py-0.5 sm:px-2 sm:py-1 text-xs bg-blue-100 dark:bg-blue-900/50 text-blue-800 dark:text-blue-200 rounded-full whitespace-nowrap">
                {user?.role}
              </span>
            </div>
            
            <button
              onClick={handleLogout}
              className="p-2 text-gray-400 dark:text-gray-300 hover:text-red-500 dark:hover:text-red-400 transition-colors"
            >
              <LogOut className="w-4 h-4 sm:w-5 sm:h-5" />
            </button>
            
            {/* Mobile menu button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="lg:hidden p-2 text-gray-400 dark:text-gray-300 hover:text-gray-500 dark:hover:text-gray-200 transition-colors"
            >
              {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
        
        {/* Mobile Navigation Menu */}
        {isMobileMenuOpen && (
          <div className="lg:hidden border-t border-gray-200 dark:border-gray-700 py-4">
            <div className="space-y-2">
              {filteredNavigation.map((item) => (
                <NavLink
                  key={item.name}
                  to={item.href}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive
                        ? 'text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20'
                        : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-700'
                    }`
                  }
                >
                  <item.icon className="w-4 h-4 mr-2" />
                  {item.name}
                </NavLink>
              ))}
            </div>
            
            {/* Mobile User Info */}
            <div className="sm:hidden mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <div className="flex items-center px-3 py-2">
                <User className="w-5 h-5 text-gray-400 dark:text-gray-300 mr-2" />
                <span className="text-sm text-gray-700 dark:text-gray-300 mr-2">{user?.username}</span>
                <span className="px-2 py-1 text-xs bg-blue-100 dark:bg-blue-900/50 text-blue-800 dark:text-blue-200 rounded-full">
                  {user?.role}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  )
}

export default Navbar