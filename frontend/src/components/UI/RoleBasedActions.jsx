import React from 'react'
import { useSelector } from 'react-redux'
import { Plus, Edit, Trash2, Eye, Settings } from 'lucide-react'

const RoleBasedActions = ({ 
  onAdd, 
  onEdit, 
  onDelete, 
  onView, 
  onSettings,
  context = 'general', // 'applications', 'errors', 'users', etc.
  itemOwnerId = null, // For checking ownership
  disabled = false 
}) => {
  const { user } = useSelector((state) => state.auth)
  
  const canPerformAction = (action, context) => {
    const userRole = user?.role
    const isOwner = itemOwnerId ? itemOwnerId === user?.id : true
    
    // Admin can do everything
    if (userRole === 'ADMIN') return true
    
    // Role-based permissions
    switch (context) {
      case 'applications':
        if (action === 'add' || action === 'edit' || action === 'delete') {
          return userRole === 'DEVELOPER' && isOwner
        }
        if (action === 'view') return true
        break
        
      case 'errors':
        if (action === 'resolve') return userRole !== 'VIEWER' && isOwner
        if (action === 'delete') return userRole === 'DEVELOPER' && isOwner
        if (action === 'view') return isOwner
        break
        
      case 'users':
        return userRole === 'ADMIN'
        
      case 'settings':
        return userRole === 'ADMIN'
        
      default:
        if (action === 'view') return true
        return userRole !== 'VIEWER' && isOwner
    }
    
    return false
  }

  return (
    <div className="flex items-center space-x-2">
      {/* Add Button */}
      {onAdd && canPerformAction('add', context) && (
        <button
          onClick={onAdd}
          disabled={disabled}
          className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Add new item"
        >
          <Plus className="h-4 w-4 mr-1" />
          Add
        </button>
      )}

      {/* Edit Button */}
      {onEdit && canPerformAction('edit', context) && (
        <button
          onClick={onEdit}
          disabled={disabled}
          className="inline-flex items-center px-3 py-2 border border-gray-300 dark:border-gray-600 text-sm leading-4 font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Edit item"
        >
          <Edit className="h-4 w-4 mr-1" />
          Edit
        </button>
      )}

      {/* Delete Button */}
      {onDelete && canPerformAction('delete', context) && (
        <button
          onClick={onDelete}
          disabled={disabled}
          className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Delete item"
        >
          <Trash2 className="h-4 w-4 mr-1" />
          Delete
        </button>
      )}

      {/* View Button */}
      {onView && canPerformAction('view', context) && (
        <button
          onClick={onView}
          disabled={disabled}
          className="inline-flex items-center px-3 py-2 border border-gray-300 dark:border-gray-600 text-sm leading-4 font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          title="View details"
        >
          <Eye className="h-4 w-4 mr-1" />
          View
        </button>
      )}

      {/* Settings Button */}
      {onSettings && canPerformAction('settings', context) && (
        <button
          onClick={onSettings}
          disabled={disabled}
          className="inline-flex items-center px-3 py-2 border border-gray-300 dark:border-gray-600 text-sm leading-4 font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Settings"
        >
          <Settings className="h-4 w-4 mr-1" />
          Settings
        </button>
      )}

      {/* Role-based message for restricted actions */}
      {user?.role === 'VIEWER' && (context === 'applications' || context === 'errors') && (
        <span className="text-xs text-gray-500 dark:text-gray-400 italic">
          View-only access
        </span>
      )}
    </div>
  )
}

export default RoleBasedActions