import React, { useState, useEffect, useCallback } from 'react'
import { motion } from 'framer-motion'
import { Save, Shield, Check, AlertCircle, Loader } from 'lucide-react'
import { useSelector } from 'react-redux'
import toast from 'react-hot-toast'
import { settingsService } from '../../services/settingsService'

const SettingsPage = () => {
  const { user } = useSelector((state) => state.auth)
  const [saveStatus, setSaveStatus] = useState('')
  const [loading, setLoading] = useState(false)
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(user?.twoFactorEnabled || false)
  const [twoFactorLoading, setTwoFactorLoading] = useState(false)
  const [globalTwoFactorEnabled, setGlobalTwoFactorEnabled] = useState(false)
  const [globalTwoFactorLoading, setGlobalTwoFactorLoading] = useState(false)
  const [statusLoaded, setStatusLoaded] = useState(false)

  // Load 2FA settings
  useEffect(() => {
    loadGlobal2FAStatus()
    loadSecuritySettings()
  }, [])

  const loadSecuritySettings = async () => {
    try {
      const securitySettings = await settingsService.getSecuritySettings()
      setSettings(prev => ({
        ...prev,
        security: {
          sessionTimeout: securitySettings.sessionTimeoutMinutes ? securitySettings.sessionTimeoutMinutes.toString() : '30',
          maxLoginAttempts: securitySettings.maxLoginAttempts ? securitySettings.maxLoginAttempts.toString() : '5'
        }
      }))
    } catch (err) {
      console.error('Failed to load security settings:', err)
    }
  }

  // Sync 2FA state with user context
  useEffect(() => {
    setTwoFactorEnabled(user?.twoFactorEnabled || false)
  }, [user?.twoFactorEnabled])

  const loadGlobal2FAStatus = async () => {
    try {
      console.log('Loading 2FA status...')
      const response = await settingsService.get2FAStatus()
      console.log('2FA Status response:', response)
      
      const globalEnabled = response.globalEnabled || response.globalTwoFactorEnabled || false
      const individualEnabled = response.enabled || false
      
      setGlobalTwoFactorEnabled(globalEnabled)
      setTwoFactorEnabled(individualEnabled || globalEnabled) // If global is enabled, show individual as enabled too
      setStatusLoaded(true)
      
      console.log('Updated states - Global:', globalEnabled, 'Individual:', individualEnabled)
    } catch (err) {
      console.error('Failed to load 2FA status:', err)
      setStatusLoaded(true)
    }
  }
  const [settings, setSettings] = useState({
    security: {
      sessionTimeout: '30',
      maxLoginAttempts: '5'
    }
  })

  const handleEnable2FA = async () => {
    if (!user?.id) {
      toast.error('User not found. Please refresh and try again.')
      return
    }

    setTwoFactorLoading(true)
    try {
      await settingsService.enable2FA(user.id)
      setTwoFactorEnabled(true)
      // Note: In a real app, you'd dispatch an action to update Redux state
      // For now, we'll rely on the backend state
      toast.success('Two-Factor Authentication enabled successfully')
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Failed to enable 2FA')
      setTwoFactorEnabled(user?.twoFactorEnabled || false)
    } finally {
      setTwoFactorLoading(false)
    }
  }

  const handleDisable2FA = async () => {
    if (!user?.id) {
      toast.error('User not found. Please refresh and try again.')
      return
    }

    setTwoFactorLoading(true)
    try {
      await settingsService.disable2FA(user.id)
      setTwoFactorEnabled(false)
      // Note: In a real app, you'd dispatch an action to update Redux state
      // For now, we'll rely on the backend state
      toast.success('Two-Factor Authentication disabled successfully')
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Failed to disable 2FA')
      setTwoFactorEnabled(user?.twoFactorEnabled || false)
    } finally {
      setTwoFactorLoading(false)
    }
  }

  const handleToggleGlobal2FA = async () => {
    setGlobalTwoFactorLoading(true)
    try {
      const newState = !globalTwoFactorEnabled
      console.log('Toggling Global 2FA to:', newState)
      
      const response = await settingsService.toggleGlobal2FA(newState)
      console.log('Toggle response:', response)
      
      // Update local state
      setGlobalTwoFactorEnabled(newState)
      
      // Reload 2FA status to get updated values
      await loadGlobal2FAStatus()
      
      toast.success(`Global 2FA ${newState ? 'enabled' : 'disabled'} for all users`)
    } catch (err) {
      console.error('Toggle Global 2FA error:', err)
      toast.error(err.response?.data?.message || err.message || 'Failed to toggle global 2FA')
      // Reload status on error to ensure UI is in sync
      await loadGlobal2FAStatus()
    } finally {
      setGlobalTwoFactorLoading(false)
    }
  }

  const handleSave = useCallback(async (section) => {
    setSaveStatus('saving')
    try {
      if (section === 'security') {
        await settingsService.saveSecuritySettings(settings.security)
      }
      setSaveStatus('saved')
      setTimeout(() => setSaveStatus(''), 2000)
    } catch (err) {
      setSaveStatus('')
      toast.error('Failed to save settings: ' + (err.response?.data?.message || err.message))
    }
  }, [settings.security])

  const updateSetting = useCallback((section, key, value) => {
    setSettings(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [key]: value
      }
    }))
  }, [])

  const SecuritySettings = () => (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      <div>
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Security Settings</h3>
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label className="label">Session Timeout (minutes)</label>
            <select
              className="input"
              value={settings.security.sessionTimeout}
              onChange={(e) => updateSetting('security', 'sessionTimeout', e.target.value)}
            >
              <option value="15">15 minutes</option>
              <option value="30">30 minutes</option>
              <option value="60">1 hour</option>
              <option value="120">2 hours</option>
            </select>
          </div>
          <div>
            <label className="label">Max Login Attempts</label>
            <select
              className="input"
              value={settings.security.maxLoginAttempts}
              onChange={(e) => updateSetting('security', 'maxLoginAttempts', e.target.value)}
            >
              <option value="3">3 attempts</option>
              <option value="5">5 attempts</option>
              <option value="10">10 attempts</option>
            </select>
          </div>
        </div>
      </div>

      <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Two-Factor Authentication (2FA)</h3>
        
        {user?.role?.toUpperCase() === 'ADMIN' && statusLoaded && (
          <>
            <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 mb-4">
              <h4 className="font-medium text-yellow-900 dark:text-yellow-400 mb-2">🔐 Global 2FA Control (Admin Only)</h4>
              <p className="text-sm text-yellow-800 dark:text-yellow-300 mb-3">
                Control 2FA for ALL users in the system. When enabled, all users (Admin, Developer, Viewer) must complete OTP verification to login.
              </p>
              <div className="flex items-center justify-between p-3 bg-white dark:bg-gray-800 border border-yellow-200 dark:border-yellow-700 rounded-lg">
                <div>
                  <h5 className="font-medium text-gray-900 dark:text-white">
                    Global 2FA for All Users
                  </h5>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {globalTwoFactorEnabled 
                      ? 'All users must complete 2FA to login'
                      : 'Users can login without 2FA'}
                  </p>
                </div>
                <button
                  onClick={handleToggleGlobal2FA}
                  disabled={globalTwoFactorLoading}
                  className={`px-4 py-2 rounded-lg font-medium flex items-center transition-colors ${
                    globalTwoFactorEnabled
                      ? 'bg-red-600 hover:bg-red-700 text-white'
                      : 'bg-green-600 hover:bg-green-700 text-white'
                  } disabled:opacity-50 disabled:cursor-not-allowed`}
                >
                  {globalTwoFactorLoading ? (
                    <>
                      <Loader className="w-4 h-4 mr-2 animate-spin" />
                      Processing...
                    </>
                  ) : globalTwoFactorEnabled ? (
                    <>
                      <AlertCircle className="w-4 h-4 mr-2" />
                      Disable Global 2FA
                    </>
                  ) : (
                    <>
                      <Shield className="w-4 h-4 mr-2" />
                      Enable Global 2FA
                    </>
                  )}
                </button>
              </div>
            </div>
            
            <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-4">
              <h4 className="font-medium text-blue-900 dark:text-blue-400 mb-2">How Global 2FA Works:</h4>
              <ul className="text-sm text-blue-800 dark:text-blue-300 space-y-1">
                <li>✓ When enabled, ALL users receive an OTP via email after entering username/password</li>
                <li>✓ OTP is valid for 5 minutes</li>
                <li>✓ Users must enter this code to complete the login process</li>
                <li>✓ Applies to Admin, Developer, and Viewer roles</li>
                <li>✓ When disabled, users login normally without OTP</li>
              </ul>
            </div>
          </>
        )}
        
        {user?.role?.toUpperCase() !== 'ADMIN' && statusLoaded && (
          <div className="bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
            <h4 className="font-medium text-gray-900 dark:text-white mb-2">
              Current 2FA Status: {globalTwoFactorEnabled ? 'Enabled' : 'Disabled'}
            </h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              {globalTwoFactorEnabled 
                ? 'Two-factor authentication is currently enabled for all users. You will need to enter an OTP code when logging in.'
                : 'Two-factor authentication is currently disabled. You can login with just your username and password.'}
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">
              Only administrators can change the global 2FA setting.
            </p>
          </div>
        )}
        
        {!statusLoaded && (
          <div className="bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
            <div className="flex items-center">
              <Loader className="w-4 h-4 mr-2 animate-spin" />
              <span className="text-sm text-gray-600 dark:text-gray-400">Loading 2FA status...</span>
            </div>
          </div>
        )}
      </div>

      <div className="pt-4">
        <button
          onClick={() => handleSave('security')}
          disabled={saveStatus === 'saving'}
          className="btn-primary flex items-center"
        >
          {saveStatus === 'saving' ? (
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          ) : saveStatus === 'saved' ? (
            <Check className="w-4 h-4 mr-2" />
          ) : (
            <Save className="w-4 h-4 mr-2" />
          )}
          {saveStatus === 'saving' ? 'Saving...' : saveStatus === 'saved' ? 'Saved!' : 'Save Settings'}
        </button>
      </div>
    </motion.div>
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Security Settings</h1>
        <p className="text-gray-600 dark:text-gray-400">Configure security settings and two-factor authentication</p>
      </div>

      <div className="card">
        <SecuritySettings />
      </div>
    </div>
  )
}

export default SettingsPage