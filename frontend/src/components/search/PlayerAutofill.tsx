import { useState, useEffect, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { summonerApi } from '@/services/api'

export interface PlayerSuggestion {
  summonerName: string
  gameName: string
  tagLine: string
  summonerLevel: number
  profileIconUrl: string
  rank?: {
    tier: string
    rank: string
    leaguePoints: number
  }
}

interface PlayerAutofillProps {
  query: string
  region: string
  onSelect: (player: PlayerSuggestion) => void
  isVisible: boolean
  onClose: () => void
}

export function PlayerAutofill({ query, region, onSelect, isVisible, onClose }: PlayerAutofillProps) {
  const [suggestions, setSuggestions] = useState<PlayerSuggestion[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (query.length >= 2 && isVisible) {
      searchPlayers(query, region)
    } else {
      setSuggestions([])
    }
  }, [query, region, isVisible])

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        onClose()
      }
    }

    if (isVisible) {
      document.addEventListener('mousedown', handleClickOutside)
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isVisible, onClose])

  const searchPlayers = async (searchQuery: string, region: string) => {
    setIsLoading(true)
    try {
      const results = await summonerApi.searchPlayers(searchQuery, region, 5)
      setSuggestions(results)
    } catch (error) {
      console.error('Error searching players:', error)
      setSuggestions([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleSelect = (player: PlayerSuggestion) => {
    onSelect(player)
    onClose()
  }

  if (!isVisible || query.length < 2) {
    return null
  }

  return (
    <AnimatePresence>
      <motion.div
        ref={dropdownRef}
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -10 }}
        transition={{ duration: 0.2 }}
        className="absolute top-full left-0 right-0 z-50 mt-1 bg-white rounded-lg shadow-xl border border-gray-200 max-h-80 overflow-y-auto"
      >
        <div className="py-2">
          <div className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2 px-4">
            Summoner Profiles
          </div>
          
          {isLoading ? (
            <div className="flex items-center justify-center py-6">
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-primary-600"></div>
              <span className="ml-2 text-sm text-gray-600">Searching...</span>
            </div>
          ) : suggestions.length > 0 ? (
            <div className="space-y-0">
              {suggestions.map((player, index) => (
                <motion.div
                  key={`${player.gameName}-${player.tagLine}`}
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.05 }}
                  onClick={() => handleSelect(player)}
                  className="flex items-center px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors border-b border-gray-100 last:border-b-0"
                >
                  {/* Profile Picture */}
                  <div className="flex-shrink-0 w-8 h-8 rounded-full overflow-hidden mr-3">
                    <img
                      src={player.profileIconUrl}
                      alt={`${player.gameName} profile`}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  
                  {/* Player Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2">
                      <span className="font-medium text-gray-900 text-sm truncate">
                        {player.gameName}
                      </span>
                      <span className="text-gray-500 font-medium text-sm">
                        #{player.tagLine}
                      </span>
                    </div>
                    
                    <div className="flex items-center space-x-3 mt-0.5">
                      <span className="text-xs text-gray-600">
                        Level {player.summonerLevel}
                      </span>
                      {player.rank && (
                        <span className="text-xs font-medium text-gray-700">
                          {player.rank.tier} {player.rank.rank} - {player.rank.leaguePoints}LP
                        </span>
                      )}
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          ) : (
            <div className="text-center py-6 text-sm text-gray-500">
              No players found matching "{query}"
            </div>
          )}
        </div>
      </motion.div>
    </AnimatePresence>
  )
}

