import { useState, useEffect } from 'react'
import { useParams, useSearchParams, Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowLeft, TrendingUp, Target, Trophy } from 'lucide-react'
import { Champion, PlayerMatch } from '@/types'
import { MatchCard } from '@/components/match/MatchCard'

export function ChampionDetailPage() {
  const { summonerName } = useParams<{ summonerName: string }>()
  const [searchParams] = useSearchParams()
  const championId = searchParams.get('championId')
  const region = searchParams.get('region') || 'na1'

  const [champion, setChampion] = useState<Champion | null>(null)
  const [matches, setMatches] = useState<PlayerMatch[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (summonerName && championId) {
      fetchChampionData()
    }
  }, [summonerName, championId, region])

  const fetchChampionData = async () => {
    try {
      setLoading(true)
      // Fetch player data to get matches filtered by champion
      const response = await fetch(
        `http://localhost:8080/api/players/${encodeURIComponent(summonerName!)}?region=${region}`
      )
      const playerData = await response.json()

      // Filter matches for this champion
      const championMatches = playerData.recentMatches?.filter(
        (m: PlayerMatch) => m.champion.id === parseInt(championId!)
      ) || []

      if (championMatches.length > 0) {
        setChampion(championMatches[0].champion)
        setMatches(championMatches)
      }
    } catch (error) {
      console.error('Error fetching champion data:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading champion data...</p>
        </div>
      </div>
    )
  }

  if (!champion) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">Champion not found</p>
          <Link to={`/player/${summonerName}?region=${region}`} className="btn btn-primary mt-4">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Profile
          </Link>
        </div>
      </div>
    )
  }

  // Calculate stats
  const wins = matches.filter(m => m.won).length
  const losses = matches.length - wins
  const winRate = matches.length > 0 ? (wins / matches.length) * 100 : 0
  const totalKills = matches.reduce((sum, m) => sum + m.kills, 0)
  const totalDeaths = matches.reduce((sum, m) => sum + m.deaths, 0)
  const totalAssists = matches.reduce((sum, m) => sum + m.assists, 0)
  const avgKDA = totalDeaths > 0 
    ? ((totalKills + totalAssists) / totalDeaths).toFixed(2) 
    : 'Perfect'
  const avgKills = (totalKills / matches.length).toFixed(1)
  const avgDeaths = (totalDeaths / matches.length).toFixed(1)
  const avgAssists = (totalAssists / matches.length).toFixed(1)
  const totalCS = matches.reduce((sum, m) => sum + m.cs, 0)
  const avgCS = (totalCS / matches.length).toFixed(1)

  return (
    <div className="min-h-screen bg-zinc-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6"
        >
          <Link
            to={`/player/${summonerName}?region=${region}`}
            className="inline-flex items-center text-gray-400 hover:text-gray-100 transition-colors"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Profile
          </Link>
        </motion.div>

        {/* Champion Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="card mb-8 overflow-hidden"
        >
          <div 
            className="h-48 bg-cover bg-center relative"
            style={{ backgroundImage: `url(${champion.splashUrl})` }}
          >
            <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent"></div>
            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
              <div className="flex items-end gap-4">
                <img 
                  src={champion.imageUrl} 
                  alt={champion.name}
                  className="w-24 h-24 rounded-lg border-4 border-white shadow-lg"
                />
                <div className="flex-1">
                  <h1 className="text-4xl font-bold mb-1">{champion.name}</h1>
                  <p className="text-white/90 text-lg">{champion.title}</p>
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Stats Overview */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8"
        >
          <div className="card text-center">
            <div className="text-3xl font-bold text-primary-500">{matches.length}</div>
            <div className="text-sm text-gray-400 mt-1">Games Played</div>
          </div>    
          <div className="card text-center">
            <div className={`text-3xl font-bold ${winRate >= 50 ? 'text-green-600' : 'text-red-600'}`}>
              {winRate.toFixed(1)}%
            </div>
            <div className="text-sm text-gray-400 mt-1">Win Rate</div>
            <div className="text-xs text-gray-400 mt-1">{wins}W {losses}L</div>
          </div>
          <div className="card text-center">
            <div className="text-3xl font-bold text-blue-600">{avgKDA}</div>
            <div className="text-sm text-gray-400 mt-1">Average KDA</div>
            <div className="text-xs text-gray-400 mt-1">
              {avgKills} / {avgDeaths} / {avgAssists}
            </div>
          </div>
          <div className="card text-center">
            <div className="text-3xl font-bold text-yellow-600">{avgCS}</div>
            <div className="text-sm text-gray-400 mt-1">Avg CS</div>
            <div className="text-xs text-gray-400 mt-1">per game</div>
          </div>
        </motion.div>

        {/* Match History */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <h2 className="text-2xl font-semibold text-gray-400 mb-6">
            Match History with {champion.name}
          </h2>
          <div className="space-y-3">
            {matches.map((match) => (
              <MatchCard key={match.id} match={match} />
            ))}
          </div>
        </motion.div>
      </div>
    </div>
  )
}
