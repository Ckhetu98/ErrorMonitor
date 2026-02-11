import { HubConnectionBuilder, LogLevel } from '@microsoft/signalr'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

class WebSocketService {
  constructor() {
    this.signalRConnection = null
    this.stompClient = null
    this.isSpringBoot = false
    this.callbacks = new Map()
  }

  // Initialize connection based on backend type
  async connect(apiBaseUrl) {
    this.isSpringBoot = apiBaseUrl?.includes('8080') || false
    
    if (this.isSpringBoot) {
      console.log('WebSocket enabled - using Spring Boot backend')
      return this.connectSpringBoot(apiBaseUrl)
    } else {
      console.log('WebSocket disabled - using .NET backend with SignalR')
      return false
    }
  }

  // SignalR connection for .NET backend
  async connectSignalR(apiBaseUrl) {
    try {
      const token = localStorage.getItem('token')
      this.signalRConnection = new HubConnectionBuilder()
        .withUrl(`${apiBaseUrl.replace('/api', '')}/monitoringHub`, {
          accessTokenFactory: () => token
        })
        .withAutomaticReconnect()
        .configureLogging(LogLevel.Information)
        .build()

      await this.signalRConnection.start()
      console.log('SignalR Connected')
      return true
    } catch (error) {
      console.error('SignalR connection failed:', error)
      return false
    }
  }

  // WebSocket connection for Spring Boot backend
  async connectSpringBoot(apiBaseUrl) {
    try {
      const socket = new SockJS(`${apiBaseUrl.replace('/api', '')}/ws`)
      this.stompClient = Stomp.over(socket)
      
      return new Promise((resolve) => {
        this.stompClient.connect({}, () => {
          console.log('Spring Boot WebSocket Connected')
          resolve(true)
        }, (error) => {
          console.error('Spring Boot WebSocket connection failed:', error)
          resolve(false)
        })
      })
    } catch (error) {
      console.error('Spring Boot WebSocket connection failed:', error)
      return false
    }
  }

  // Subscribe to application errors
  subscribeToErrors(applicationId, callback) {
    if (this.isSpringBoot && this.stompClient) {
      this.stompClient.subscribe(`/topic/errors/${applicationId}`, (message) => {
        callback(JSON.parse(message.body))
      })
    } else if (this.signalRConnection) {
      this.signalRConnection.invoke('JoinApplicationGroup', applicationId)
      this.signalRConnection.on('ErrorReceived', callback)
    }
  }

  // Send error notification
  sendError(applicationId, errorData) {
    if (this.isSpringBoot && this.stompClient) {
      this.stompClient.send('/app/error', {}, JSON.stringify(errorData))
    } else if (this.signalRConnection) {
      this.signalRConnection.invoke('SendErrorToGroup', `app_${applicationId}`, errorData)
    }
  }

  // Disconnect
  disconnect() {
    if (this.signalRConnection) {
      this.signalRConnection.stop()
    }
    if (this.stompClient) {
      this.stompClient.disconnect()
    }
  }
}

export const websocketService = new WebSocketService()