import axios from 'axios'
import { ChampionRecommendation, TeamRecommendationRequest } from '@/types'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for adding auth tokens if needed
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for handling errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const summonerApi = {
  getProfile: async (summonerName: string, region: string) => {
    const response = await api.get(`/players/${encodeURIComponent(summonerName)}`, { 
      params: { region } 
    })
    return response.data
  }
}

export const championRecommendationApi = {
  /**
   * Get champion recommendations for a player
   */
  getRecommendations: async (
    summonerName: string,
    region: string,
    teamChampions?: string[],
    opponentChampions?: string[],
    preferredRole?: string
  ): Promise<ChampionRecommendation[]> => {
    const params = new URLSearchParams({
      region,
      ...(preferredRole && { preferredRole }),
    })

    if (teamChampions && teamChampions.length > 0) {
      teamChampions.forEach(champion => params.append('teamChampions', champion))
    }

    if (opponentChampions && opponentChampions.length > 0) {
      opponentChampions.forEach(champion => params.append('opponentChampions', champion))
    }

    const response = await api.get(`/recommendations/player/${summonerName}?${params}`)
    return response.data
  },

  /**
   * Get team recommendations
   */
  getTeamRecommendations: async (request: TeamRecommendationRequest): Promise<ChampionRecommendation[]> => {
    const response = await api.post('/recommendations/team', request)
    return response.data
  },

  /**
   * Get counter-pick recommendations
   */
  getCounterRecommendations: async (
    championName: string,
    region: string,
    preferredRole?: string
  ): Promise<ChampionRecommendation[]> => {
    const params = new URLSearchParams({ region })
    if (preferredRole) {
      params.append('preferredRole', preferredRole)
    }

    const response = await api.get(`/recommendations/counter/${championName}?${params}`)
    return response.data
  },
}

export default api
