import { useState, useEffect } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowLeft, User, Calendar, Trophy, Target } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Player } from '@/types'
import { summonerApi } from '@/services/api'
import { ChampionCard } from '@/components/champion/ChampionCard'

export function PlayerPage() {
  const { summonerName } = useParams<{ summonerName: string }>()
  const [searchParams] = useSearchParams()
  const region = searchParams.get('region') || 'na1'
  
  const [player, setPlayer] = useState<Player | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (summonerName) {
      fetchPlayerData(summonerName, region)
    }
  }, [summonerName, region])

  const fetchPlayerData = async (name: string, region: string) => {
    try {
      setLoading(true)
      setError(null)
      
      const response = await fetch(`http://localhost:8080/api/players/${encodeURIComponent(name)}?region=${region}`)
      if (!response.ok) {
        throw new Error('Player not found')
      }
      const data = await response.json()
      setPlayer(data)
    } catch (err) {
      setError('Failed to load player data')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading player data...</p>
        </div>
      </div>
    )
  }

  if (error || !player) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <User className="w-8 h-8 text-red-600" />
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Player Not Found
          </h2>
          <p className="text-gray-600 mb-4">
            {error || 'The summoner name you entered could not be found.'}
          </p>
          <Link
            to="/"
            className="btn btn-primary"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Home
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-6"
        >
          <Link
            to="/"
            className="inline-flex items-center text-gray-600 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Home
          </Link>
        </motion.div>

        {/* Player Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="card mb-8"
        >
          <div className="flex items-center space-x-6">
            <div className="w-20 h-20 bg-gradient-primary rounded-full flex items-center justify-center">
              <User className="w-10 h-10 text-white" />
            </div>
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">
                {player.summonerName}
              </h1>
              <div className="flex items-center space-x-6 text-gray-600">
                <div className="flex items-center">
                  <Calendar className="w-4 h-4 mr-2" />
                  Level {player.summonerLevel}
                </div>
                <div className="flex items-center">
                  <Target className="w-4 h-4 mr-2" />
                  {player.region}
                </div>
                <div className="flex items-center">
                  <Trophy className="w-4 h-4 mr-2" />
                  Last updated: {new Date(player.lastUpdated).toLocaleDateString()}
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Recent Champions */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="mb-8"
        >
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">
            Recent Champions
          </h2>
          
          {player.recentChampions && player.recentChampions.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {player.recentChampions.map((playerChampion) => (
                <ChampionCard
                  key={playerChampion.id}
                  champion={playerChampion.champion}
                  showStats={true}
                />
              ))}
            </div>
          ) : (
            <div className="card text-center py-12">
              <Target className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                No champion data available
              </h3>
              <p className="text-gray-600">
                Champion mastery and recent game data could not be loaded.
              </p>
            </div>
          )}
        </motion.div>

        {/* Get Recommendations CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="card bg-gradient-to-r from-primary-50 to-secondary-50 border-primary-200"
        >
          <div className="text-center">
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Ready for champion recommendations?
            </h3>
            <p className="text-gray-600 mb-6">
              Get personalized champion suggestions based on your playstyle and current game context.
            </p>
            <Link
              to={`/recommendations?summonerName=${encodeURIComponent(player.summonerName)}&region=${player.region}`}
              className="btn btn-primary"
            >
              Get Recommendations
            </Link>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
