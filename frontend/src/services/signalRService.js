import * as signalR from '@microsoft/signalr'

class SignalRService {
  constructor() {
    this.connection = null
    this.isConnected = false
    this.callbacks = new Map()
    this.isDotNetBackend = import.meta.env.VITE_API_BASE_URL?.includes('5000') || false
  }

  async startConnection() {
    if (!this.isDotNetBackend) {
      console.log('SignalR disabled - using Spring Boot backend')
      return
    }

    try {
      const token = localStorage.getItem('token')
      const hubUrl = import.meta.env.VITE_SIGNALR_HUB_URL || 'http://localhost:5000'
      
      this.connection = new signalR.HubConnectionBuilder()
        .withUrl(`${hubUrl}/monitoringHub`, {
          accessTokenFactory: () => token
        })
        .withAutomaticReconnect()
        .build()

      await this.connection.start()
      this.isConnected = true
      console.log('SignalR connected successfully')
    } catch (error) {
      console.error('SignalR connection failed:', error)
    }
  }

  async stopConnection() {
    if (this.connection) {
      await this.connection.stop()
      this.isConnected = false
      this.callbacks.clear()
    }
  }

  // Real-time error notifications
  onNewError(callback) {
    if (this.connection && this.isConnected) {
      this.connection.on('NewError', callback)
    }
  }

  // Real-time alert notifications  
  onNewAlert(callback) {
    if (this.connection && this.isConnected) {
      this.connection.on('NewAlert', callback)
    }
  }

  // Real-time dashboard updates
  onDashboardUpdate(callback) {
    if (this.connection && this.isConnected) {
      this.connection.on('DashboardUpdate', callback)
    }
  }

  // Clean up listeners
  off(eventName) {
    if (this.connection) {
      this.connection.off(eventName)
    }
  }

  // Check connection status
  get connectionState() {
    return this.connection?.state || 'Disconnected'
  }
}

export const signalRService = new SignalRService()