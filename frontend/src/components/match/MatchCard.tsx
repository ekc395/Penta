import { PlayerMatch, MatchParticipant } from '@/types'
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
    ? 'text-yellow-400' 
    : parseFloat(kda) >= 3 
    ? 'text-green-400' 
    : parseFloat(kda) >= 2 
    ? 'text-blue-400' 
    : 'text-gray-400'

  // Split participants into teams
  // Split participants into teams
  const team1 = match.participants?.filter(p => p.teamId === 100) || []
  const team2 = match.participants?.filter(p => p.teamId === 200) || []

  // Add this debug logging
  console.log('Total participants:', match.participants?.length)
  console.log('Team 100 (Blue):', team1.length, team1.map(p => p.summonerName))
  console.log('Team 200 (Red):', team2.length, team2.map(p => p.summonerName))
  
  // Calculate team stats
  const team1Kills = team1.reduce((sum, p) => sum + p.kills, 0)
  const team2Kills = team2.reduce((sum, p) => sum + p.kills, 0)
  const team1Gold = team1.reduce((sum, p) => sum + p.goldEarned, 0)
  const team2Gold = team2.reduce((sum, p) => sum + p.goldEarned, 0)

  const getItemUrl = (itemId: number) => {
    if (!itemId || itemId === 0) return null
    return `https://ddragon.leagueoflegends.com/cdn/15.21.1/img/item/${itemId}.png`
  }

  const renderParticipant = (participant: MatchParticipant, isPlayerTeam: boolean, index: number) => {
    const participantKda = participant.deaths > 0
      ? ((participant.kills + participant.assists) / participant.deaths).toFixed(2)
      : 'Perfect'
  
    return (
      <div 
        // Remove the key from here
        className={`flex items-center gap-3 py-2 px-3 ${isPlayerTeam ? 'bg-blue-900/10' : 'bg-red-900/10'} rounded`}
      >
        {/* Champion */}
        <div className="flex items-center gap-2 w-48">
          <img 
            src={participant.champion.imageUrl}
            alt={participant.champion.name}
            className="w-10 h-10 rounded"
          />
          <div className="flex-1 min-w-0">
            <div className="text-sm font-medium text-gray-200 truncate">
              {participant.summonerName}
            </div>
            <div className="text-xs text-gray-300">
              {participant.champion.name}
            </div>
          </div>
        </div>

        {/* KDA */}
        <div className="w-32 text-center">
          <div className="text-sm text-gray-200">
            {participant.kills} / {participant.deaths} / {participant.assists}
          </div>
          <div className="text-xs text-gray-500">{participantKda} KDA</div>
        </div>

        {/* Damage */}
        <div className="w-28 text-center">
          <div className="text-sm text-red-400">
            {(participant.damageDealt / 1000).toFixed(1)}k
          </div>
          <div className="text-xs text-gray-500">Damage</div>
        </div>

        {/* Gold */}
        <div className="w-24 text-center">
          <div className="text-sm text-yellow-400">
            {(participant.goldEarned / 1000).toFixed(1)}k
          </div>
          <div className="text-xs text-gray-500">Gold</div>
        </div>

        {/* CS */}
        <div className="w-20 text-center">
          <div className="text-sm text-gray-200">{participant.cs}</div>
          <div className="text-xs text-gray-500">CS</div>
        </div>

        {/* Vision */}
        <div className="w-24 text-center">
          <div className="text-sm text-purple-400">{participant.visionScore}</div>
          <div className="text-xs text-gray-500">Vision</div>
        </div>

        {/* Items */}
        <div className="flex gap-1 ml-auto">
          {[participant.item0, participant.item1, participant.item2, 
            participant.item3, participant.item4, participant.item5, 
            participant.item6].map((itemId, idx) => (
            <div key={idx} className="w-8 h-8 bg-gray-800 rounded">
              {itemId > 0 && getItemUrl(itemId) && (
                <img 
                  src={getItemUrl(itemId)!}
                  alt={`Item ${itemId}`}
                  className="w-full h-full rounded"
                />
              )}
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className={`overflow-hidden rounded-lg ${match.won ? 'bg-blue-900/20 border-l-4 border-blue-500' : 'bg-red-900/20 border-l-4 border-red-500'}`}>
      {/* Main Match Info - Collapsed View */}
      <div className="p-4">
        <div className="flex items-center gap-6">
          <div className="relative flex-shrink-0">
            <img 
              src={match.champion.imageUrl} 
              alt={match.champion.name}
              className="w-16 h-16 rounded-lg"
            />
          </div>

          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-1">
              <span className={`font-bold text-sm uppercase ${match.won ? 'text-blue-400' : 'text-red-400'}`}>
                {match.won ? 'Victory' : 'Defeat'}
              </span>
              <span className="text-gray-600">•</span>
              <span className="text-sm text-gray-300 uppercase">{match.gameMode}</span>
              {match.lane && match.lane !== 'NONE' && (
                <>
                  <span className="text-gray-600">•</span>
                  <span className="text-sm text-gray-300 uppercase">{match.lane}</span>
                </>
              )}
            </div>
            <div className="text-sm text-gray-400">
              {new Date(match.gameStartTime).toLocaleDateString()} • {Math.floor(match.gameDuration / 60)}m {match.gameDuration % 60}s
            </div>
          </div>

          <div className="text-right px-6 flex-shrink-0 border-l border-gray-700">
            <div className="text-xl font-bold text-white mb-1">
              <span className="text-white">{match.kills}</span>
              <span className="text-gray-500 mx-1">/</span>
              <span className="text-red-400">{match.deaths}</span>
              <span className="text-gray-500 mx-1">/</span>
              <span className="text-white">{match.assists}</span>
            </div>
            <div className={`text-sm font-semibold ${kdaColor}`}>
              {kda} KDA
            </div>
          </div>

          <div className="text-right px-6 border-l border-gray-700 flex-shrink-0">
            <div className="text-xl font-bold text-white">{match.cs}</div>
            <div className="text-sm text-gray-400">
              {(match.cs / (match.gameDuration / 60)).toFixed(1)} CS/m
            </div>
          </div>

          <button
            onClick={() => setExpanded(!expanded)}
            className="p-2 hover:bg-gray-800 rounded-lg transition-colors flex-shrink-0"
          >
            {expanded ? (
              <ChevronUp className="w-5 h-5 text-gray-400" />
            ) : (
              <ChevronDown className="w-5 h-5 text-gray-400" />
            )}
          </button>
        </div>
      </div>

      {/* Expanded Details - All Players */}
      {expanded && match.participants && match.participants.length > 0 && (
        <div className="border-t border-gray-800 bg-gray-900/50">
          {/* Blue Team */}
          <div className="p-4">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-blue-400">
                {match.won && match.teamId === 100 ? 'Victory' : match.teamId === 100 ? 'Your Team' : 'Enemy Team'} (Blue Team)
              </h3>
              <div className="flex gap-6 text-sm">
                <span className="text-gray-400">
                  <span className="text-white font-semibold">{team1Kills}</span> Kills
                </span>
                <span className="text-gray-400">
                  <span className="text-yellow-400 font-semibold">{(team1Gold / 1000).toFixed(1)}k</span> Gold
                </span>
              </div>
            </div>
            <div className="space-y-1">
              {team1.map((p, idx) => (
                <div key={`team1-${p.summonerName}-${idx}`}>
                  {renderParticipant(p, match.teamId === 100, idx)}
                </div>
              ))}
            </div>
          </div>

          {/* Red Team */}
          <div className="p-4 border-t border-gray-800">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-red-400">
                {!match.won && match.teamId === 200 ? 'Defeat' : match.teamId === 200 ? 'Your Team' : 'Enemy Team'} (Red Team)
              </h3>
              <div className="flex gap-6 text-sm">
                <span className="text-gray-400">
                  <span className="text-white font-semibold">{team2Kills}</span> Kills
                </span>
                <span className="text-gray-400">
                  <span className="text-yellow-400 font-semibold">{(team2Gold / 1000).toFixed(1)}k</span> Gold
                </span>
              </div>
            </div>
            <div className="space-y-1">
            {team2.map((p, idx) => (
              <div key={`team2-${p.summonerName}-${idx}`}>
                {renderParticipant(p, match.teamId === 200, idx)}
              </div>
            ))}
          </div>
          </div>
        </div>
      )}
    </div>
  )
}