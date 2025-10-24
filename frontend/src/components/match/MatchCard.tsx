import { PlayerMatch } from '@/types'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { useState } from 'react'

interface MatchCardProps {
  match: PlayerMatch
}

export function MatchCard({ match }: MatchCardProps) {
  const [expanded, setExpanded] = useState(false)

  const kdaValue = match.deaths > 0 
    ? (match.kills + match.assists) / match.deaths 
    : Infinity

  const kdaDisplay = match.deaths === 0 ? 'Perfect' : kdaValue.toFixed(2)

  const kdaColor =
    kdaValue === Infinity ? 'text-amber-300' :
    kdaValue >= 4 ? 'text-emerald-300' :
    kdaValue >= 3 ? 'text-sky-300' :
    kdaValue >= 2 ? 'text-cyan-300' :
    'text-zinc-400'

  const frame = match.won
    ? 'from-sky-500/15 border-sky-400/40'
    : 'from-rose-500/15 border-rose-400/40'

  return (
    <div
      className={`
        overflow-hidden rounded-xl border
        bg-zinc-900/60 backdrop-blur
        bg-gradient-to-r ${frame}
        shadow-[0_0_0_1px_rgba(255,255,255,0.03)]
        hover:shadow-lg transition-shadow
      `}
    >
      {/* Main Info */}
      <div className="p-4">
        <div className="flex items-center gap-6">
          {/* Champion Image */}
          <div className="relative flex-shrink-0">
            <img
              src={match.champion.imageUrl}
              alt={match.champion.name}
              className={`w-16 h-16 rounded-lg ring-2 ${
                match.won ? 'ring-sky-400/40' : 'ring-rose-400/40'
              }`}
            />
          </div>

          {/* Game Info */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-1">
              <span
                className={`font-semibold text-sm uppercase tracking-wide ${
                  match.won ? 'text-sky-300' : 'text-rose-300'
                }`}
              >
                {match.won ? 'Victory' : 'Defeat'}
              </span>
              <span className="text-zinc-600/60">•</span>
              <span className="text-sm text-zinc-300 uppercase">
                {match.gameMode}
              </span>
              {match.lane && match.lane !== 'NONE' && (
                <>
                  <span className="text-zinc-600/60">•</span>
                  <span className="text-sm text-zinc-300 uppercase">
                    {match.lane}
                  </span>
                </>
              )}
            </div>
            <div className="text-sm text-zinc-400">
              {new Date(match.gameStartTime).toLocaleDateString()} •{' '}
              {Math.floor(match.gameDuration / 60)}m {match.gameDuration % 60}s
            </div>
          </div>

          {/* KDA */}
          <div className="text-right px-6 flex-shrink-0 border-l border-zinc-800">
            <div className="text-xl font-bold text-white mb-1">
              <span>{match.kills}</span>
              <span className="text-zinc-500 mx-1">/</span>
              <span className="text-rose-300">{match.deaths}</span>
              <span className="text-zinc-500 mx-1">/</span>
              <span>{match.assists}</span>
            </div>
            <div className={`text-sm font-semibold ${kdaColor}`}>
              {kdaDisplay} KDA
            </div>
          </div>

          {/* CS */}
          <div className="text-right px-6 border-l border-zinc-800 flex-shrink-0">
            <div className="text-xl font-bold text-white">{match.cs}</div>
            <div className="text-sm text-zinc-400">
              {(match.cs / (match.gameDuration / 60)).toFixed(1)} CS/m
            </div>
          </div>

          {/* Expand Button */}
          <button
            onClick={() => setExpanded(!expanded)}
            className="p-2 hover:bg-zinc-800/60 rounded-lg transition-colors flex-shrink-0"
          >
            {expanded ? (
              <ChevronUp className="w-5 h-5 text-zinc-400" />
            ) : (
              <ChevronDown className="w-5 h-5 text-zinc-400" />
            )}
          </button>
        </div>
      </div>

      {/* Expanded Section */}
      {expanded && (
        <div className="border-t border-zinc-800 bg-zinc-950/60 p-6">
          <div className="grid grid-cols-4 gap-6 mb-6">
            {/* Damage */}
            <div className="bg-zinc-900/60 rounded-lg p-4 ring-1 ring-inset ring-white/5">
              <h4 className="text-sm font-semibold text-zinc-300 mb-3">
                Damage
              </h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-zinc-400">Dealt:</span>
                  <span className="font-medium text-rose-300">
                    {match.damageDealt?.toLocaleString() ?? 0}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-zinc-400">Taken:</span>
                  <span className="font-medium text-sky-300">
                    {match.damageTaken?.toLocaleString() ?? 0}
                  </span>
                </div>
              </div>
            </div>

            {/* Gold & Farm */}
            <div className="bg-zinc-900/60 rounded-lg p-4 ring-1 ring-inset ring-white/5">
              <h4 className="text-sm font-semibold text-zinc-300 mb-3">
                Gold & Farm
              </h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-zinc-400">Gold:</span>
                  <span className="font-medium text-amber-300">
                    {match.goldEarned?.toLocaleString() ?? 0}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-zinc-400">CS:</span>
                  <span className="font-medium text-white">{match.cs}</span>
                </div>
              </div>
            </div>

            {/* Vision */}
            <div className="bg-zinc-900/60 rounded-lg p-4 ring-1 ring-inset ring-white/5">
              <h4 className="text-sm font-semibold text-zinc-300 mb-3">
                Vision
              </h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-zinc-400">Score:</span>
                  <span className="font-medium text-purple-300">
                    {match.visionScore ?? 0}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-zinc-400">Wards:</span>
                  <span className="font-medium text-white">
                    {match.wardsPlaced ?? 0} / {match.wardsKilled ?? 0}
                  </span>
                </div>
              </div>
            </div>

            {/* Performance */}
            <div className="bg-zinc-900/60 rounded-lg p-4 ring-1 ring-inset ring-white/5">
              <h4 className="text-sm font-semibold text-zinc-300 mb-3">
                Performance
              </h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-zinc-400">KDA:</span>
                  <span className={`font-medium ${kdaColor}`}>
                    {kdaDisplay}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-zinc-400">Result:</span>
                  <span
                    className={`font-medium ${
                      match.won ? 'text-sky-300' : 'text-rose-300'
                    }`}
                  >
                    {match.won ? 'Win' : 'Loss'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
