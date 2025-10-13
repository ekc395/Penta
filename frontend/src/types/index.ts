export interface Champion {
  id: number
  championId: number
  name: string
  title: string
  role: string
  lane: string
  imageUrl: string
  splashUrl: string
  winRate: number
  pickRate: number
  banRate: number
  tier: number
  tags: string
}

export interface Player {
  id: number
  summonerName: string
  puuid: string
  summonerId: string
  region: string
  summonerLevel: number
  profileIconUrl: string
  lastUpdated: string
  recentChampions: PlayerChampion[]
  recentMatches: PlayerMatch[]
}

export interface PlayerChampion {
  id: number
  champion: Champion
  gamesPlayed: number
  wins: number
  losses: number
  winRate: number
  averageKills: number
  averageDeaths: number
  averageAssists: number
  averageCs: number
  lastPlayed: string
  masteryLevel: number
  masteryPoints: number
}

export interface PlayerMatch {
  id: number
  champion: Champion
  matchId: string
  gameMode: string
  gameType: string
  gameStartTime: string
  gameDuration: number
  won: boolean
  kills: number
  deaths: number
  assists: number
  cs: number
  lane: string
  role: string
  teamId: number
}

export interface ChampionRecommendation {
  champion: Champion
  recommendationScore: number
  reason: string
  strengths: string[]
  weaknesses: string[]
  teamSynergy: Synergy
  opponentMatchup: Matchup
  playerComfort: PlayerComfort
}

export interface Synergy {
  synergizesWith: Champion[]
  conflictsWith: Champion[]
  synergyScore: number
}

export interface Matchup {
  strongAgainst: Champion[]
  weakAgainst: Champion[]
  matchupScore: number
}

export interface PlayerComfort {
  gamesPlayed: number
  winRate: number
  masteryLevel: number
  comfortScore: number
}

export interface TeamRecommendationRequest {
  summonerName: string
  region: string
  teamChampions: string[]
  opponentChampions: string[]
  preferredRole: string
}

// Import types from constants
export type { Role, Region } from '@/constants'
