import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_CHIPLESS_SERVER_BASE_URL
})

export { apiClient }
