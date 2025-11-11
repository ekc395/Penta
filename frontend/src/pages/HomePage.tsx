import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Search, Target, Users, TrendingUp, ArrowRight } from 'lucide-react'
import { REGIONS } from '@/constants'
import { Region } from '@/types'
import { summonerApi } from '@/services/api'
import { PlayerAutofill, PlayerSuggestion } from '@/components/search/PlayerAutofill'
import TextType from '@/components/ui/TextType'
import LightRays from '@/components/ui/LightRays'
import { useScrollAnimation } from '@/hooks/useScrollAnimation'
import { Iron, Bronze, Silver, Gold, Platinum, Emerald, Diamond, Master, Grandmaster, Challenger } from '@/assets'

export function HomePage() {
  const navigate = useNavigate()
  const [summonerName, setSummonerName] = useState('')
  const [riotTagline, setRiotTagline] = useState('')
  const [region, setRegion] = useState<Region>('na1')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showAutofill, setShowAutofill] = useState(false)
  const regions = REGIONS
  const featureRefs = useScrollAnimation()

  const handleSearch = async () => {
    if (!summonerName.trim()) {
      setError('Please enter a summoner name')
      return
    }
    
    if (isLoading) return
    
    setIsLoading(true)
    setError(null)
    setShowAutofill(false)
    try {
      const fullName = riotTagline.trim() 
        ? `${summonerName.trim()}#${riotTagline.trim()}`
        : summonerName.trim()
      
      const response = await summonerApi.getProfile(fullName, region)
      
      if (response.status === 'collecting') {
        setError('Collecting player data... Please wait 15 seconds.')
        setTimeout(() => {
          navigate(`/player/${encodeURIComponent(fullName)}?region=${region}`)
        }, 15000)
      } else {
        navigate(`/player/${encodeURIComponent(fullName)}?region=${region}`)
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to find summoner.')
      setIsLoading(false)
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
    <div className="min-h-screen bg-zinc-950">
      {/* Fixed Animated Light Rays Background */}
      <div className="fixed inset-0 opacity-20 z-0 pointer-events-none">
        <LightRays
          raysOrigin="top-center"
          raysColor="#3b82f6"
          raysSpeed={0.5}
          lightSpread={2}
          rayLength={1.5}
          fadeDistance={1.2}
          saturation={0.8}
          followMouse={true}
          mouseInfluence={0.15}
          noiseAmount={0.1}
          distortion={0.05}
        />
      </div>

      {/* Hero Section */}
      <section className="bg-transparent text-white min-h-screen flex items-center justify-center relative">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="flex flex-col items-center"
          >
            <div className="flex flex-col items-center mb-12">
              <div className="min-w-[700px] md:min-w-[1000px] text-center">
                <div className="text-6xl md:text-8xl font-bold mb-4">
                  Win the Game
                </div>
                <TextType 
                  text="Before it Starts."
                  as="h2"
                  className="text-6xl md:text-8xl font-bold text-white"
                  typingSpeed={100}
                  initialDelay={500}
                  loop={true}
                  showCursor={true}
                  cursorClassName="text-white"
                />
              </div>
            </div>
            <p className="text-2xl md:text-3xl text-gray-400 mb-12 max-w-4xl mx-auto text-center">
              Get intelligent champion recommendations based on your playstyle, team composition, 
              and opponent matchups.
            </p>

            {/* Search Form - OP.GG Style */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="w-full max-w-5xl mx-auto"
            >
              <div className="bg-zinc-800 border border-zinc-700 rounded-3xl p-6">
                <div className="flex flex-col lg:flex-row gap-6 items-center">
                  {/* Username Input */}
                  <div className="flex-1 w-full relative">
                    <label className="block text-base font-medium text-gray-300 mb-3">
                      Summoner Name
                    </label>
                    <input
                      type="text"
                      placeholder="Enter summoner name"
                      value={summonerName}
                      onChange={handleSummonerNameChange}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                      onFocus={() => summonerName.length >= 2 && setShowAutofill(true)}
                      className="w-full px-5 py-4 text-lg bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
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
                    <label className="block text-base font-medium text-gray-300 mb-3">
                      Riot Tagline
                    </label>
                    <div className="relative">
                      <span className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 text-xl font-bold">#</span>
                      <input
                        type="text"
                        placeholder="TAG"
                        value={riotTagline}
                        onChange={(e) => setRiotTagline(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        className="w-full pl-10 pr-5 py-4 text-lg bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                      />
                    </div>
                  </div>
                  
                  {/* Region Selector */}
                  <div className="w-full lg:w-56">
                    <label className="block text-base font-medium text-gray-300 mb-3">
                      Region
                    </label>
                    <select
                      value={region}
                      onChange={(e) => setRegion(e.target.value as Region)}
                      className="w-full px-5 py-4 text-lg bg-gray-800 border border-gray-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
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
                    <label className="block text-base font-medium text-gray-300 mb-3 invisible">
                      Search
                    </label>
                    <button
                      onClick={handleSearch}
                      disabled={isLoading}
                      className="w-full lg:w-auto px-10 py-4 text-lg bg-green-600 hover:bg-green-700 disabled:bg-green-400 disabled:cursor-not-allowed rounded-xl font-semibold transition-colors flex items-center justify-center space-x-2"
                    >
                      <Search className="w-6 h-6" />
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
      <section className="features-section relative bg-transparent" style={{ height: '400vh' }}>
        {/* Gradient Background Effects */}
        <div className="sticky top-0 h-screen flex items-center justify-center overflow-hidden">
          {/* Animated background gradients */}
          <div className="absolute inset-0 opacity-30">
            <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-500/20 rounded-full blur-3xl"></div>
            <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl"></div>
          </div>

          <div className="w-full max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div 
              className="features-heading text-center mb-12"
              style={{
                opacity: 0,
                willChange: 'opacity, transform',
                transition: 'opacity 0.3s ease-out, transform 0.3s ease-out',
              }}
            >
              <h2 className="text-3xl md:text-4xl font-bold text-gray-100 mb-4">
                Why Choose Penta?
              </h2>
            </div>

            <div className="relative h-[600px] flex items-center justify-center">
              {[
                {
                  icon: Target,
                  title: 'Personalized Recommendations',
                  description: 'Get suggestions based on your champion mastery, win rates, and playstyle preferences.',
                  gradient: 'from-blue-500/10 to-cyan-500/10',
                  iconBg: 'bg-gradient-to-br from-blue-500 to-cyan-500',
                  accentColor: 'border-blue-500/30',
                },
                {
                  icon: Users,
                  title: 'Team Synergy Analysis',
                  description: 'Analyze how well champions work together in your team composition.',
                  gradient: 'from-purple-500/10 to-pink-500/10',
                  iconBg: 'bg-gradient-to-br from-purple-500 to-pink-500',
                  accentColor: 'border-purple-500/30',
                },
                {
                  icon: TrendingUp,
                  title: 'Meta & Matchup Data',
                  description: 'Stay ahead with real-time meta analysis and champion matchup statistics.',
                  gradient: 'from-orange-500/10 to-red-500/10',
                  iconBg: 'bg-gradient-to-br from-orange-500 to-red-500',
                  accentColor: 'border-orange-500/30',
                },
              ].map((feature, index) => (
                <div
                  key={index}
                  ref={(el) => {
                    if (el) featureRefs.current[index] = el
                  }}
                  className="feature-content-item absolute inset-0 flex flex-col items-center justify-center text-center px-6 md:px-12"
                  style={{
                    opacity: 0,
                    willChange: 'opacity, transform',
                    transition: 'opacity 0.3s ease-out, transform 0.3s ease-out',
                  }}
                >
                  {/* Card with gradient background */}
                  <div className={`max-w-5xl w-full bg-gradient-to-br ${feature.gradient} backdrop-blur-sm rounded-[48px] border ${feature.accentColor} p-16 md:p-20 shadow-2xl`}>
                    {/* Icon with gradient and shadow */}
                    <div className="flex justify-center mb-12">
                      <div className={`w-32 h-32 md:w-40 md:h-40 ${feature.iconBg} rounded-3xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform duration-300`}>
                        <feature.icon className="w-16 h-16 md:w-20 md:h-20 text-white" strokeWidth={2.5} />
                      </div>
                    </div>

                    {/* Title */}
                    <h3 className="text-5xl md:text-6xl lg:text-7xl font-bold text-white mb-8">
                      {feature.title}
                    </h3>

                    {/* Description */}
                    <p className="text-2xl md:text-3xl text-gray-300 max-w-3xl mx-auto leading-relaxed">
                      {feature.description}
                    </p>

                    {/* Decorative line */}
                    <div className="mt-12 flex justify-center">
                      <div className={`w-32 h-1.5 ${feature.iconBg} rounded-full`}></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="min-h-screen bg-transparent flex items-center justify-center relative">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center py-20">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
          >
            <h2 className="text-5xl md:text-7xl font-bold text-gray-100 mb-8">
              Ready to Climb the Ranks?
            </h2>
            <p className="text-2xl md:text-3xl text-gray-400 mb-12 max-w-4xl mx-auto">
              Start getting personalized champion recommendations today and improve your gameplay.
            </p>
            <button
              onClick={() => {
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
              className="inline-flex items-center space-x-3 px-12 py-5 text-xl bg-primary-600 hover:bg-primary-700 text-white font-semibold rounded-2xl transition-colors shadow-lg hover:shadow-xl"
            >
              <span>Get Started</span>
              <ArrowRight className="w-6 h-6" />
            </button>
            <img src={Iron} alt="Iron Rank" className="w-full h-full object-contain" />
            <img src={Bronze} alt="Bronze Rank" className="w-full h-full object-contain" />
            <img src={Silver} alt="Silver Rank" className="w-full h-full object-contain" />
            <img src={Gold} alt="Gold Rank" className="w-full h-full object-contain" />
            <img src={Platinum} alt="Platinum Rank" className="w-full h-full object-contain" />
            <img src={Emerald} alt="Emerald Rank" className="w-full h-full object-contain" />
            <img src={Diamond} alt="Diamond Rank" className="w-full h-full object-contain" />
            <img src={Master} alt="Master Rank" className="w-full h-full object-contain" />
            <img src={Grandmaster} alt="Grandmaster Rank" className="w-full h-full object-contain" />
            <img src={Challenger} alt="Challener Rank" className="w-full h-full object-contain" />
          </motion.div>
        </div>
      </section>
    </div>
  )
}
