import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Search, Target, Users, TrendingUp, ArrowRight } from 'lucide-react'
import { REGIONS } from '@/constants'
import { Region } from '@/types'
import { summonerApi } from '@/services/api'

export function HomePage() {
  const [summonerName, setSummonerName] = useState('')
  const [riotTagline, setRiotTagline] = useState('')
  const [region, setRegion] = useState<Region>('NA')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const regions = REGIONS

  const handleSearch = async () => {
    if (!summonerName.trim()) {
      setError('Please enter a summoner name')
      return
    }
    setIsLoading(true)
    setError(null)
    try {
      const fullName = riotTagline.trim() 
        ? `${summonerName.trim()}#${riotTagline.trim()}`
        : summonerName.trim()
      await summonerApi.getProfile(fullName, region)
      window.location.href = `/player/${encodeURIComponent(fullName)}?region=${region}`
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to find summoner. Please check the name and region.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-primary-600 via-primary-700 to-secondary-600 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center"
          >
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              Master Your
              <span className="block text-accent-300">Champion Pool</span>
            </h1>
            <p className="text-xl md:text-2xl text-primary-100 mb-8 max-w-3xl mx-auto">
              Get intelligent champion recommendations based on your playstyle, team composition, 
              and opponent matchups using advanced League of Legends data analysis.
            </p>

            {/* Search Form - OP.GG Style */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="max-w-3xl mx-auto"
            >
              <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-4">
                <div className="flex flex-col lg:flex-row gap-4 items-center">
                  {/* Username Input */}
                  <div className="flex-1 w-full">
                    <label className="block text-sm font-medium text-white/80 mb-2">
                      Summoner Name
                    </label>
                    <input
                      type="text"
                      placeholder="Enter summoner name"
                      value={summonerName}
                      onChange={(e) => setSummonerName(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                      className="w-full px-4 py-3 bg-white/20 border border-white/30 rounded-xl text-white placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-white/50"
                    />
                  </div>
                  
                  {/* Riot Tagline Input */}
                  <div className="flex-1 w-full">
                    <label className="block text-sm font-medium text-white/80 mb-2">
                      Riot Tagline
                    </label>
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-white/70 text-lg font-bold">#</span>
                      <input
                        type="text"
                        placeholder="TAG"
                        value={riotTagline}
                        onChange={(e) => setRiotTagline(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        className="w-full pl-8 pr-4 py-3 bg-white/20 border border-white/30 rounded-xl text-white placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-white/50"
                      />
                    </div>
                  </div>
                  
                  {/* Region Selector */}
                  <div className="w-full lg:w-48">
                    <label className="block text-sm font-medium text-white/80 mb-2">
                      Region
                    </label>
                    <select
                      value={region}
                      onChange={(e) => setRegion(e.target.value as Region)}
                      className="w-full px-4 py-3 bg-white/20 border border-white/30 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-white/50"
                    >
                      {regions.map((region) => (
                        <option key={region.value} value={region.value} className="text-gray-900">
                          {region.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  {/* Search Button */}
                  <div className="w-full lg:w-auto flex flex-col">
                    <label className="block text-sm font-medium text-white/80 mb-2 invisible">
                      Search
                    </label>
                    <button
                      onClick={handleSearch}
                      disabled={isLoading}
                      className="w-full lg:w-auto px-8 py-3 bg-accent-600 hover:bg-accent-700 disabled:bg-accent-400 disabled:cursor-not-allowed rounded-xl font-semibold transition-colors flex items-center justify-center space-x-2"
                    >
                      <Search className="w-5 h-5" />
                      <span>{isLoading ? 'Searching...' : 'Search'}</span>
                    </button>
                  </div>
                </div>
                
                {/* Error Display */}
                {error && (
                  <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="mt-4 p-3 bg-red-500/20 border border-red-500/30 rounded-xl text-red-200 text-sm"
                  >
                    {error}
                  </motion.div>
                )}
              </div>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Why Choose Penta?
            </h2>
            <p className="text-xl text-gray-600 max-w-3xl mx-auto">
              Our advanced recommendation system analyzes multiple data sources to provide 
              the most accurate champion suggestions for your games.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                icon: Target,
                title: 'Personalized Recommendations',
                description: 'Get suggestions based on your champion mastery, win rates, and playstyle preferences.',
              },
              {
                icon: Users,
                title: 'Team Synergy Analysis',
                description: 'Analyze how well champions work together in your team composition.',
              },
              {
                icon: TrendingUp,
                title: 'Meta & Matchup Data',
                description: 'Stay ahead with real-time meta analysis and champion matchup statistics.',
              },
            ].map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: index * 0.2 }}
                viewport={{ once: true }}
                className="text-center p-8 rounded-2xl hover:shadow-lg transition-shadow"
              >
                <div className="w-16 h-16 bg-primary-100 rounded-2xl flex items-center justify-center mx-auto mb-6">
                  <feature.icon className="w-8 h-8 text-primary-600" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-4">
                  {feature.title}
                </h3>
                <p className="text-gray-600">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
          >
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
              Ready to Climb the Ranks?
            </h2>
            <p className="text-xl text-gray-600 mb-8">
              Start getting personalized champion recommendations today and improve your gameplay.
            </p>
            <Link
              to="/recommendations"
              className="inline-flex items-center space-x-2 px-8 py-4 bg-primary-600 hover:bg-primary-700 text-white font-semibold rounded-xl transition-colors"
            >
              <span>Get Started</span>
              <ArrowRight className="w-5 h-5" />
            </Link>
          </motion.div>
        </div>
      </section>
    </div>
  )
}
