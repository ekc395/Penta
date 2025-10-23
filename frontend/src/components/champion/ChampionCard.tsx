import { Champion, PlayerChampion } from '@/types'
import { Star, TrendingUp, Users } from 'lucide-react'

interface ChampionCardProps {
  champion: Champion
  onClick?: () => void
  selected?: boolean
  showStats?: boolean
  playerStats?: {
    winRate: number
    gamesPlayed: number
    totalGames: number // Total games in dataset (20)
  }
}

export function ChampionCard({ champion, onClick, selected, showStats = false, playerStats }: ChampionCardProps) {
  const getTierClass = (tier: number) => {
    switch (tier) {
      case 5: return 'tier-s'
      case 4: return 'tier-a'
      case 3: return 'tier-b'
      case 2: return 'tier-c'
      case 1: return 'tier-d'
      default: return 'tier-b'
    }
  }

  const getTierLabel = (tier: number) => {
    switch (tier) {
      case 5: return 'S'
      case 4: return 'A'
      case 3: return 'B'
      case 2: return 'C'
      case 1: return 'D'
      default: return 'B'
    }
  }

  // Calculate play rate: (games with this champ / total games) * 100
  const playRate = playerStats 
    ? ((playerStats.gamesPlayed / playerStats.totalGames) * 100).toFixed(1)
    : '0.0'

  return (
    <div
      className={`champion-card ${selected ? 'selected' : ''}`}
      onClick={onClick}
    >
      <div className="flex items-start space-x-4">
        {/* Champion Image */}
        <div className="flex-shrink-0">
          <div className="w-16 h-16 rounded-lg overflow-hidden bg-gray-200">
            {champion.imageUrl ? (
              <img
                src={champion.imageUrl}
                alt={champion.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full bg-gradient-to-br from-primary-400 to-secondary-400 flex items-center justify-center">
                <span className="text-white font-bold text-lg">
                  {champion.name.charAt(0)}
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Champion Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-lg font-semibold text-gray-100 truncate">
              {champion.name}
            </h3>
            {champion.tier && (
              <span className={`tier-badge ${getTierClass(champion.tier)}`}>
                Tier {getTierLabel(champion.tier)}
              </span>
            )}
          </div>
          
          <p className="text-sm text-gray-400 mb-2">
            {champion.title}
          </p>
          
          <div className="flex items-center space-x-4 text-xs text-gray-500">
            <span className="flex items-center">
              <Users className="w-3 h-3 mr-1" />
              {champion.role}
            </span>
            <span className="flex items-center">
              <TrendingUp className="w-3 h-3 mr-1" />
              {champion.lane}
            </span>
          </div>

          {showStats && playerStats && (
            <div className="mt-3 grid grid-cols-2 gap-2 text-xs">
              <div className="text-center">
                <div className="font-semibold text-green-400">
                  {playerStats.winRate.toFixed(1)}%
                </div>
                <div className="text-gray-500">Win Rate</div>
              </div>
              <div className="text-center">
                <div className="font-semibold text-blue-400">
                  {playRate}%
                </div>
                <div className="text-gray-500">Play Rate</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}