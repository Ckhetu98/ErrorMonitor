import api from './api'

export const userService = {
  // Get all users
  getUsers: async () => {
    const response = await api.get('/users')
    return response.data
  },

  // Create new user
  createUser: async (userData) => {
    const response = await api.post('/users', userData)
    return response.data
  },

  // Update user
  updateUser: async (id, userData) => {
    const response = await api.put(`/users/${id}`, userData)
    return response.data
  },

  // Delete user
  deleteUser: async (id) => {
    const response = await api.delete(`/users/${id}`)
    return response.data
  },

  // Toggle user status
  toggleUserStatus: async (id) => {
    const response = await api.put(`/users/${id}/toggle-status`)
    return response.data
  }
}