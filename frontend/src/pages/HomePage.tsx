import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Search, Target, Users, TrendingUp, ArrowRight } from 'lucide-react'
import { REGIONS } from '@/constants'
import { Region } from '@/types'
import { summonerApi } from '@/services/api'
import { PlayerAutofill, PlayerSuggestion } from '@/components/search/PlayerAutofill'
import TextType from '@/components/ui/TextType'

export function HomePage() {
  const [summonerName, setSummonerName] = useState('')
  const [riotTagline, setRiotTagline] = useState('')
  const [region, setRegion] = useState<Region>('na1')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showAutofill, setShowAutofill] = useState(false)
  const regions = REGIONS

  const handleSearch = async () => {
    if (!summonerName.trim()) {
      setError('Please enter a summoner name')
      return
    }
    
    if (isLoading) return; // Add this extra guard
    
    setIsLoading(true)
    setError(null)
    setShowAutofill(false) // Close autofill on search
    try {
      const fullName = riotTagline.trim() 
        ? `${summonerName.trim()}#${riotTagline.trim()}`
        : summonerName.trim()
      
      const response = await summonerApi.getProfile(fullName, region)
      
      if (response.status === 'collecting') {
        setError('Collecting player data... Please wait 15 seconds.')
        setTimeout(() => {
          window.location.href = `/player/${encodeURIComponent(fullName)}?region=${region}`
        }, 15000)
      } else {
        window.location.href = `/player/${encodeURIComponent(fullName)}?region=${region}`
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to find summoner.')
      setIsLoading(false) // Only re-enable on error
    }
  }

  const handlePlayerSelect = (player: PlayerSuggestion) => {
    setSummonerName(player.gameName)
    setRiotTagline(player.tagLine)
    setShowAutofill(false)
  }

  const handleSummonerNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    setSummonerName(value)
    setShowAutofill(value.length >= 2)
  }

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-zinc-950 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="flex flex-col items-center"
          >
            <div className="flex flex-col items-center mb-6">
              <div className="min-w-[600px] md:min-w-[900px] text-center">
                <div className="text-4xl md:text-6xl font-bold mb-2">
                  Win the Game
                </div>
                <TextType 
                  text="Before it Starts."
                  as="h2"
                  className="text-4xl md:text-6xl font-bold text-white"
                  typingSpeed={100}
                  initialDelay={500}
                  loop={true}
                  showCursor={true}
                  cursorClassName="text-white"
                />
              </div>
            </div>
            <p className="text-xl md:text-2xl text-gray-300 mb-8 max-w-3xl mx-auto text-center">
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
              <div className="bg-zinc-800 border border-zinc-700 rounded-2xl p-4">
                <div className="flex flex-col lg:flex-row gap-4 items-center">
                  {/* Username Input */}
                  <div className="flex-1 w-full relative">
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Summoner Name
                    </label>
                    <input
                      type="text"
                      placeholder="Enter summoner name"
                      value={summonerName}
                      onChange={handleSummonerNameChange}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                      onFocus={() => summonerName.length >= 2 && setShowAutofill(true)}
                      className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    />
                    <PlayerAutofill
                      query={summonerName}
                      region={region}
                      onSelect={handlePlayerSelect}
                      isVisible={showAutofill}
                      onClose={() => setShowAutofill(false)}
                    />
                  </div>
                  
                  {/* Riot Tagline Input */}
                  <div className="flex-1 w-full">
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Riot Tagline
                    </label>
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg font-bold">#</span>
                      <input
                        type="text"
                        placeholder="TAG"
                        value={riotTagline}
                        onChange={(e) => setRiotTagline(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        className="w-full pl-8 pr-4 py-3 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      />
                    </div>
                  </div>
                  
                  {/* Region Selector */}
                  <div className="w-full lg:w-48">
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Region
                    </label>
                    <select
                      value={region}
                      onChange={(e) => setRegion(e.target.value as Region)}
                      className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
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
                    <label className="block text-sm font-medium text-gray-300 mb-2 invisible">
                      Search
                    </label>
                    <button
                      onClick={handleSearch}
                      disabled={isLoading}
                      className="w-full lg:w-auto px-8 py-3 bg-green-600 hover:bg-green-700 disabled:bg-green-400 disabled:cursor-not-allowed rounded-xl font-semibold transition-colors flex items-center justify-center space-x-2"
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
                    className="mt-4 p-3 bg-red-900/30 border border-red-700 rounded-xl text-red-300 text-sm"
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
      <section className="py-24 bg-zinc-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-3xl md:text-4xl font-bold text-gray-100 mb-4">
              Why Choose Penta?
            </h2>
            <p className="text-xl text-gray-400 max-w-3xl mx-auto">
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
                className="text-center p-8 rounded-2xl bg-zinc-800 border border-zinc-700 hover:shadow-lg transition-shadow"
              >
                <div className="w-16 h-16 bg-primary-900 rounded-2xl flex items-center justify-center mx-auto mb-6">
                  <feature.icon className="w-8 h-8 text-primary-400" />
                </div>
                <h3 className="text-xl font-semibold text-gray-100 mb-4">
                  {feature.title}
                </h3>
                <p className="text-gray-400">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 bg-zinc-950">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
          >
            <h2 className="text-3xl md:text-4xl font-bold text-gray-100 mb-6">
              Ready to Climb the Ranks?
            </h2>
            <p className="text-xl text-gray-400 mb-8">
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
