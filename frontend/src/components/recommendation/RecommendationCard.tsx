import { ChampionRecommendation } from '@/types'
import { Star, TrendingUp, Users, Target, AlertTriangle, CheckCircle } from 'lucide-react'
import { ChampionCard } from '@/components/champion/ChampionCard'

interface RecommendationCardProps {
  recommendation: ChampionRecommendation
  rank: number
}

export function RecommendationCard({ recommendation, rank }: RecommendationCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 0.8) return 'text-green-400'
    if (score >= 0.6) return 'text-yellow-400'
    if (score >= 0.4) return 'text-orange-400'
    return 'text-red-400'
  }

  const getScoreLabel = (score: number) => {
    if (score >= 0.8) return 'Excellent'
    if (score >= 0.6) return 'Good'
    if (score >= 0.4) return 'Fair'
    return 'Poor'
  }

  return (
    <div className="recommendation-card">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-primary-900 rounded-full flex items-center justify-center">
            <span className="text-primary-400 font-bold text-sm">#{rank}</span>
          </div>
          <div>
            <h3 className="text-xl font-semibold text-gray-100">
              {recommendation.champion.name}
            </h3>
            <p className="text-gray-400">{recommendation.champion.title}</p>
          </div>
        </div>
        
        <div className="text-right">
          <div className={`text-2xl font-bold ${getScoreColor(recommendation.recommendationScore)}`}>
            {(recommendation.recommendationScore * 100).toFixed(0)}%
          </div>
          <div className="text-sm text-gray-500">
            {getScoreLabel(recommendation.recommendationScore)}
          </div>
        </div>
      </div>

      {/* Recommendation Reason */}
      <div className="mb-6">
        <p className="text-gray-300 leading-relaxed">
          {recommendation.reason}
        </p>
      </div>

      {/* Score Breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-blue-950 rounded-lg p-4 border border-blue-900">
          <div className="flex items-center mb-2">
            <Users className="w-4 h-4 text-blue-400 mr-2" />
            <span className="text-sm font-medium text-blue-300">Player Comfort</span>
          </div>
          <div className="text-2xl font-bold text-blue-400">
            {(recommendation.playerComfort.comfortScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-blue-300">
            {recommendation.playerComfort.gamesPlayed} games, {recommendation.playerComfort.winRate.toFixed(1)}% WR
          </div>
        </div>

        <div className="bg-green-950 rounded-lg p-4 border border-green-900">
          <div className="flex items-center mb-2">
            <Target className="w-4 h-4 text-green-400 mr-2" />
            <span className="text-sm font-medium text-green-300">Team Synergy</span>
          </div>
          <div className="text-2xl font-bold text-green-400">
            {(recommendation.teamSynergy.synergyScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-green-300">
            {recommendation.teamSynergy.synergizesWith.length} synergies
          </div>
        </div>

        <div className="bg-purple-950 rounded-lg p-4 border border-purple-900">
          <div className="flex items-center mb-2">
            <TrendingUp className="w-4 h-4 text-purple-400 mr-2" />
            <span className="text-sm font-medium text-purple-300">Matchup</span>
          </div>
          <div className="text-2xl font-bold text-purple-400">
            {(recommendation.opponentMatchup.matchupScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-purple-300">
            {recommendation.opponentMatchup.strongAgainst.length} advantages
          </div>
        </div>
      </div>

      {/* Strengths and Weaknesses */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {recommendation.strengths.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-100 mb-2 flex items-center">
              <CheckCircle className="w-4 h-4 text-green-400 mr-2" />
              Strengths
            </h4>
            <ul className="space-y-1">
              {recommendation.strengths.map((strength, index) => (
                <li key={index} className="text-sm text-gray-300 flex items-start">
                  <span className="text-green-400 mr-2">•</span>
                  {strength}
                </li>
              ))}
            </ul>
          </div>
        )}

        {recommendation.weaknesses.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-100 mb-2 flex items-center">
              <AlertTriangle className="w-4 h-4 text-orange-400 mr-2" />
              Weaknesses
            </h4>
            <ul className="space-y-1">
              {recommendation.weaknesses.map((weakness, index) => (
                <li key={index} className="text-sm text-gray-300 flex items-start">
                  <span className="text-orange-400 mr-2">•</span>
                  {weakness}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}
