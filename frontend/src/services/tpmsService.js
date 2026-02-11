import axios from 'axios'

// TPMS API Service for Training and Placement Management System
const TPMS_BASE_URL = import.meta.env.VITE_TPMS_API_URL || 'http://localhost:8080/api'

const tpmsApi = axios.create({
  baseURL: TPMS_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

// Request interceptor to add auth token
tpmsApi.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('tpms_user') || 'null')
  if (user && user.token) {
    config.headers.Authorization = `Bearer ${user.token}`
  }
  return config
}, (error) => {
  return Promise.reject(error)
})

// Response interceptor for error handling
tpmsApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('tpms_user')
      if (window.location.pathname.includes('/tpms')) {
        window.location.href = '/tpms/login'
      }
    }
    return Promise.reject(error)
  }
)

export const tpmsService = {
  // Authentication
  auth: {
    login: async (email, password) => {
      const response = await tpmsApi.post('/auth/signin', { email, password })
      const auth = response.data?.data
      
      if (auth && auth.user && auth.token) {
        const userObj = {
          id: auth.user.id,
          fullName: auth.user.fullName,
          email: auth.user.email,
          role: auth.user.role,
          phone: auth.user.phone,
          token: auth.token,
        }
        localStorage.setItem('tpms_user', JSON.stringify(userObj))
        return userObj
      }
      throw new Error('Invalid login response')
    },

    signup: async (userData) => {
      const response = await tpmsApi.post('/auth/signup', userData)
      return response.data?.data
    },

    logout: () => {
      localStorage.removeItem('tpms_user')
    },

    getCurrentUser: () => {
      const user = localStorage.getItem('tpms_user')
      return user ? JSON.parse(user) : null
    },

    isAuthenticated: () => {
      return !!localStorage.getItem('tpms_user')
    }
  },

  // Students
  students: {
    getProfile: (id) => tpmsApi.get(`/users/${id}`).then(r => r.data.data),
    updateProfile: (id, data) => tpmsApi.put(`/users/${id}`, data).then(r => r.data.data),
    deleteAccount: (id) => tpmsApi.delete(`/users/${id}`).then(r => r.data.data),
    getAll: () => tpmsApi.get('/admin/students').then(r => r.data.data),
  },

  // Jobs
  jobs: {
    getAll: () => tpmsApi.get('/jobs').then(r => r.data.data),
    create: (data) => tpmsApi.post('/jobs', data).then(r => r.data.data),
    update: (id, data) => tpmsApi.put(`/jobs/${id}`, data).then(r => r.data.data),
    delete: (id) => tpmsApi.delete(`/jobs/${id}`).then(r => r.data.data),
    updateStatus: (id, status) => tpmsApi.put(`/admin/jobs/${id}/status`, null, { params: { status } }).then(r => r.data.data),
  },

  // Applications
  applications: {
    getByStudent: (studentId) => tpmsApi.get(`/applications/student/${studentId}`).then(r => r.data.data),
    getByRecruiter: (recruiterId) => tpmsApi.get(`/applications/recruiter/${recruiterId}`).then(r => r.data.data),
    create: (studentId, jobId) => tpmsApi.post('/applications/apply', null, { 
      params: { studentId, jobId } 
    }).then(r => r.data.data),
    updateStatus: (id, status) => tpmsApi.put(`/applications/${id}/status/${status}`).then(r => r.data.data),
    delete: (id) => tpmsApi.delete(`/applications/${id}`).then(r => r.data.data),
  },

  // Training
  training: {
    getAll: () => tpmsApi.get('/trainings').then(r => r.data.data),
    getByStudent: (studentId) => tpmsApi.get(`/trainings/student/${studentId}`).then(r => r.data.data),
    create: (data) => tpmsApi.post('/trainings', data).then(r => r.data.data),
    update: (id, data) => tpmsApi.put(`/trainings/${id}`, data).then(r => r.data.data),
    delete: (id) => tpmsApi.delete(`/trainings/${id}`).then(r => r.data.data),
    enroll: (studentId, trainingId) => tpmsApi.post('/trainings/enroll', null, { 
      params: { studentId, trainingId } 
    }).then(r => r.data.data),
    getStudents: (trainingId) => tpmsApi.get(`/trainings/${trainingId}/students`).then(r => r.data.data),
  },

  // Companies
  companies: {
    getAll: () => tpmsApi.get('/companies').then(r => r.data.data),
    getById: (id) => tpmsApi.get(`/companies/${id}`).then(r => r.data.data),
    create: (data) => tpmsApi.post('/companies', data).then(r => r.data.data),
    update: (id, data) => tpmsApi.put(`/companies/${id}`, data).then(r => r.data.data),
    delete: (id) => tpmsApi.delete(`/companies/${id}`).then(r => r.data.data),
  },

  // Contact/Inquiries
  contact: {
    create: (data) => tpmsApi.post('/contacts', data).then(r => r.data.data),
    getAll: () => tpmsApi.get('/contacts').then(r => r.data.data),
    getByStatus: (status) => tpmsApi.get(`/contacts/status/${status}`).then(r => r.data.data),
    updateStatus: (id, status) => tpmsApi.put(`/contacts/${id}/status`, null, { 
      params: { status } 
    }).then(r => r.data.data),
    delete: (id) => tpmsApi.delete(`/contacts/${id}`).then(r => r.data.data),
  },

  // Admin Dashboard
  admin: {
    getDashboardStats: () => tpmsApi.get('/admin/dashboard/stats').then(r => r.data.data),
    getAllStudents: () => tpmsApi.get('/admin/students').then(r => r.data.data),
    getAllRecruiters: () => tpmsApi.get('/admin/recruiters').then(r => r.data.data),
    updateUserStatus: (userId, status) => tpmsApi.put(`/admin/users/${userId}/status`, null, { 
      params: { status } 
    }).then(r => r.data.data),
    deleteUser: (userId) => tpmsApi.delete(`/admin/users/${userId}`).then(r => r.data.data),
    getRecentActivities: () => tpmsApi.get('/admin/activities/recent').then(r => r.data.data),
  },

  // Dashboard Stats (no fallback data)
  getApplicationsStats: () => tpmsApi.get('/applications/stats').then(r => r.data.data),
  getJobStats: () => tpmsApi.get('/jobs/stats').then(r => r.data.data),
  getTrainingStats: () => tpmsApi.get('/trainings/stats').then(r => r.data.data),
  getUserStats: () => tpmsApi.get('/users/stats').then(r => r.data.data),
  getRecentApplications: () => tpmsApi.get('/applications/recent').then(r => r.data.data),
  checkHealth: () => tpmsApi.get('/error-monitoring/health').then(r => r.data)
}

export default tpmsService