import api from './api'

class ContactService {
  async submitQuery(queryData) {
    const response = await api.post('/contact/submit', queryData)
    return response.data
  }

  async getQueries(status = null, page = 1, pageSize = 10) {
    let url = `/contact?page=${page}&pageSize=${pageSize}`
    
    if (status) {
      url += `&status=${status}`
    }

    const response = await api.get(url)
    return response.data
  }

  async getQuery(id) {
    try {
      const response = await api.get(`/contact/${id}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch contact query')
    }
  }

  async resolveQuery(id, responseMessage) {
    if (!id || (!Number.isInteger(id) && !/^\d+$/.test(String(id)))) {
      throw new Error('Invalid query ID')
    }
    const response = await api.post(`/contact/${id}/resolve`, { responseMessage })
    return response.data
  }

  async getStats() {
    const response = await api.get('/contact/stats')
    return response.data
  }
}

export const contactService = new ContactService()