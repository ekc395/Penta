import { PlayerMatch } from '@/types'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { useState } from 'react'

interface MatchCardProps {
  match: PlayerMatch
}

export function MatchCard({ match }: MatchCardProps) {
  const [expanded, setExpanded] = useState(false)
  const kda = match.deaths > 0 
    ? ((match.kills + match.assists) / match.deaths).toFixed(2)
    : 'Perfect'
  
  const kdaColor = match.deaths === 0 
    ? 'text-amber-500' 
    : parseFloat(kda) >= 3 
    ? 'text-green-600' 
    : parseFloat(kda) >= 2 
    ? 'text-blue-600' 
    : 'text-gray-600'

  return (
    <div className={`card overflow-hidden ${match.won ? 'bg-blue-50/50 border-l-4 border-blue-500' : 'bg-red-50/50 border-l-4 border-red-500'}`}>
      {/* Main Match Info */}
      <div className="p-4">
        <div className="flex items-center gap-4">
          {/* Champion Image */}
          <div className="relative">
            <img 
              src={match.champion.imageUrl} 
              alt={match.champion.name}
              className="w-16 h-16 rounded-lg"
            />
            <div className="absolute -bottom-1 -right-1 bg-gray-900 text-white text-xs px-1.5 py-0.5 rounded">
              {match.champion.name}
            </div>
          </div>

          {/* Game Type & Result */}
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <span className={`font-bold text-sm ${match.won ? 'text-blue-600' : 'text-red-600'}`}>
                {match.won ? 'VICTORY' : 'DEFEAT'}
              </span>
              <span className="text-gray-400">•</span>
              <span className="text-sm text-gray-600">{match.gameMode}</span>
              {match.lane && match.lane !== 'NONE' && (
                <>
                  <span className="text-gray-400">•</span>
                  <span className="text-sm text-gray-600">{match.lane}</span>
                </>
              )}
            </div>
            <div className="text-xs text-gray-500">
              {new Date(match.gameStartTime).toLocaleDateString()} • {Math.floor(match.gameDuration / 60)}m {match.gameDuration % 60}s
            </div>
          </div>

          {/* KDA Stats */}
          <div className="text-center px-4">
            <div className="text-lg font-bold mb-1">
              <span>{match.kills}</span>
              <span className="text-gray-400 mx-1">/</span>
              <span className="text-red-500">{match.deaths}</span>
              <span className="text-gray-400 mx-1">/</span>
              <span>{match.assists}</span>
            </div>
            <div className={`text-sm font-semibold ${kdaColor}`}>
              {kda} KDA
            </div>
          </div>

          {/* CS & Vision */}
          <div className="text-center px-4 border-l border-gray-200">
            <div className="text-sm font-semibold text-gray-900">{match.cs} CS</div>
            <div className="text-xs text-gray-500">
              {(match.cs / (match.gameDuration / 60)).toFixed(1)} / min
            </div>
          </div>

          {/* Expand Button */}
          <button
            onClick={() => setExpanded(!expanded)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            {expanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Expanded Details */}
      {expanded && (
        <div className="border-t border-gray-200 bg-white p-4">
          <div className="grid grid-cols-3 gap-4">
            {/* Damage Stats */}
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-2">Damage</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Damage Dealt:</span>
                  <span className="font-medium">{match.damageDealt?.toLocaleString() || 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Damage Taken:</span>
                  <span className="font-medium">{match.damageTaken?.toLocaleString() || 0}</span>
                </div>
              </div>
            </div>

            {/* Gold & CS */}
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-2">Gold & Farm</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Gold:</span>
                  <span className="font-medium">{match.goldEarned?.toLocaleString() || 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">CS:</span>
                  <span className="font-medium">{match.cs}</span>
                </div>
              </div>
            </div>

            {/* Vision */}
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-2">Vision</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Vision Score:</span>
                  <span className="font-medium">{match.visionScore || 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Wards:</span>
                  <span className="font-medium">{match.wardsPlaced || 0} / {match.wardsKilled || 0}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
