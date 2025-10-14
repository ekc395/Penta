import { useState } from 'react'
import { useQuery } from 'react-query'
import { motion } from 'framer-motion'
import { Search, Target } from 'lucide-react'
import { championRecommendationApi } from '@/services/api'
import { Role, Region } from '@/types'
import { REGIONS, ROLES } from '@/constants'
import { RecommendationCard } from '@/components/recommendation/RecommendationCard'

export function RecommendationsPage() {
  const [summonerName, setSummonerName] = useState('')
  const [riotTagline, setRiotTagline] = useState('')
  const [region, setRegion] = useState<Region>('na1')
  const [preferredRole, setPreferredRole] = useState<Role>('MID')
  const [teamChampions, setTeamChampions] = useState<string[]>([])
  const [opponentChampions, setOpponentChampions] = useState<string[]>([])
  const [isSearching, setIsSearching] = useState(false)
  const regions = REGIONS
  const roles = ROLES

  const { data: recommendations, isLoading, error } = useQuery(
    ['recommendations', summonerName, riotTagline, region, preferredRole, teamChampions, opponentChampions],
    () => {
      const fullName = riotTagline.trim() 
        ? `${summonerName.trim()}#${riotTagline.trim()}`
        : summonerName.trim()
      return championRecommendationApi.getRecommendations(
        fullName,
        region,
        teamChampions,
        opponentChampions,
        preferredRole
      )
    },
    {
      enabled: isSearching && summonerName.trim() !== '',
      retry: 1,
    }
  )

  const handleSearch = () => {
    if (summonerName.trim()) {
      setIsSearching(true)
    }
  }

  const addTeamChampion = (champion: string) => {
    if (!teamChampions.includes(champion)) {
      setTeamChampions([...teamChampions, champion])
    }
  }

  const removeTeamChampion = (champion: string) => {
    setTeamChampions(teamChampions.filter(c => c !== champion))
  }

  const addOpponentChampion = (champion: string) => {
    if (!opponentChampions.includes(champion)) {
      setOpponentChampions([...opponentChampions, champion])
    }
  }

  const removeOpponentChampion = (champion: string) => {
    setOpponentChampions(opponentChampions.filter(c => c !== champion))
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-center mb-12"
        >
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Champion Recommendations
          </h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Get personalized champion suggestions based on your playstyle, team composition, and opponent matchups.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Search Form */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="lg:col-span-1"
          >
            <div className="card sticky top-8">
              <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
                <Search className="w-5 h-5 mr-2" />
                Search Parameters
              </h2>

              <div className="space-y-6">
                {/* Summoner Name */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Summoner Name
                  </label>
                  <input
                    type="text"
                    value={summonerName}
                    onChange={(e) => setSummonerName(e.target.value)}
                    placeholder="Enter summoner name..."
                    className="input"
                  />
                </div>

                {/* Riot Tagline */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Riot Tagline
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 text-lg font-bold">#</span>
                    <input
                      type="text"
                      placeholder="TAG"
                      value={riotTagline}
                      onChange={(e) => setRiotTagline(e.target.value)}
                      className="w-full pl-8 pr-4 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    />
                  </div>
                </div>

                {/* Region */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Region
                  </label>
                  <select
                    value={region}
                    onChange={(e) => setRegion(e.target.value as Region)}
                    className="input"
                  >
                    {regions.map((region) => (
                      <option key={region.value} value={region.value}>
                        {region.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Preferred Role */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Preferred Role
                  </label>
                  <select
                    value={preferredRole}
                    onChange={(e) => setPreferredRole(e.target.value as Role)}
                    className="input"
                  >
                    {roles.map((role) => (
                      <option key={role.value} value={role.value}>
                        {role.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Team Champions */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Team Champions
                  </label>
                  <div className="space-y-2">
                    {teamChampions.map((champion) => (
                      <div key={champion} className="flex items-center justify-between bg-primary-50 px-3 py-2 rounded-lg">
                        <span className="text-sm font-medium text-primary-700">{champion}</span>
                        <button
                          onClick={() => removeTeamChampion(champion)}
                          className="text-primary-500 hover:text-primary-700"
                        >
                          ×
                        </button>
                      </div>
                    ))}
                    <input
                      type="text"
                      placeholder="Add team champion..."
                      className="input text-sm"
                      onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                          const champion = e.currentTarget.value.trim()
                          if (champion) {
                            addTeamChampion(champion)
                            e.currentTarget.value = ''
                          }
                        }
                      }}
                    />
                  </div>
                </div>

                {/* Opponent Champions */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Opponent Champions
                  </label>
                  <div className="space-y-2">
                    {opponentChampions.map((champion) => (
                      <div key={champion} className="flex items-center justify-between bg-red-50 px-3 py-2 rounded-lg">
                        <span className="text-sm font-medium text-red-700">{champion}</span>
                        <button
                          onClick={() => removeOpponentChampion(champion)}
                          className="text-red-500 hover:text-red-700"
                        >
                          ×
                        </button>
                      </div>
                    ))}
                    <input
                      type="text"
                      placeholder="Add opponent champion..."
                      className="input text-sm"
                      onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                          const champion = e.currentTarget.value.trim()
                          if (champion) {
                            addOpponentChampion(champion)
                            e.currentTarget.value = ''
                          }
                        }
                      }}
                    />
                  </div>
                </div>

                {/* Search Button */}
                <button
                  onClick={handleSearch}
                  disabled={!summonerName.trim() || isLoading}
                  className="w-full btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isLoading ? 'Searching...' : 'Get Recommendations'}
                </button>
              </div>
            </div>
          </motion.div>

          {/* Results */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, delay: 0.4 }}
            className="lg:col-span-2"
          >
            {!!error && (
              <div className="card bg-red-50 border-red-200">
                <p className="text-red-700">
                  Error loading recommendations. Please try again.
                </p>
              </div>
            )}

            {isLoading && (
              <div className="space-y-4">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="card animate-pulse">
                    <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                ))}
              </div>
            )}

            {recommendations && recommendations.length > 0 && (
              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <h2 className="text-2xl font-semibold text-gray-900">
                    Recommendations
                  </h2>
                  <span className="text-sm text-gray-500">
                    {recommendations.length} champions found
                  </span>
                </div>

                {recommendations.map((recommendation, index) => (
                  <motion.div
                    key={recommendation.champion.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: index * 0.1 }}
                  >
                    <RecommendationCard recommendation={recommendation} rank={index + 1} />
                  </motion.div>
                ))}
              </div>
            )}

            {isSearching && !isLoading && (!recommendations || recommendations.length === 0) && (
              <div className="card text-center py-12">
                <Target className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No recommendations found
                </h3>
                <p className="text-gray-600">
                  Try adjusting your search parameters or check if the summoner name is correct.
                </p>
              </div>
            )}

            {!isSearching && (
              <div className="card text-center py-12">
                <Search className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  Ready to get recommendations?
                </h3>
                <p className="text-gray-600">
                  Enter your summoner name and preferences to get personalized champion suggestions.
                </p>
              </div>
            )}
          </motion.div>
        </div>
      </div>
    </div>
  )
}
