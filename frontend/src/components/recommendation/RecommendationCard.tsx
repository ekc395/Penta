import { ChampionRecommendation } from '@/types'
import { Star, TrendingUp, Users, Target, AlertTriangle, CheckCircle } from 'lucide-react'
import { ChampionCard } from '@/components/champion/ChampionCard'

interface RecommendationCardProps {
  recommendation: ChampionRecommendation
  rank: number
}

export function RecommendationCard({ recommendation, rank }: RecommendationCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 0.8) return 'text-green-600'
    if (score >= 0.6) return 'text-yellow-600'
    if (score >= 0.4) return 'text-orange-600'
    return 'text-red-600'
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
          <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
            <span className="text-primary-600 font-bold text-sm">#{rank}</span>
          </div>
          <div>
            <h3 className="text-xl font-semibold text-gray-900">
              {recommendation.champion.name}
            </h3>
            <p className="text-gray-600">{recommendation.champion.title}</p>
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
        <p className="text-gray-700 leading-relaxed">
          {recommendation.reason}
        </p>
      </div>

      {/* Score Breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-blue-50 rounded-lg p-4">
          <div className="flex items-center mb-2">
            <Users className="w-4 h-4 text-blue-600 mr-2" />
            <span className="text-sm font-medium text-blue-900">Player Comfort</span>
          </div>
          <div className="text-2xl font-bold text-blue-600">
            {(recommendation.playerComfort.comfortScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-blue-700">
            {recommendation.playerComfort.gamesPlayed} games, {recommendation.playerComfort.winRate.toFixed(1)}% WR
          </div>
        </div>

        <div className="bg-green-50 rounded-lg p-4">
          <div className="flex items-center mb-2">
            <Target className="w-4 h-4 text-green-600 mr-2" />
            <span className="text-sm font-medium text-green-900">Team Synergy</span>
          </div>
          <div className="text-2xl font-bold text-green-600">
            {(recommendation.teamSynergy.synergyScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-green-700">
            {recommendation.teamSynergy.synergizesWith.length} synergies
          </div>
        </div>

        <div className="bg-purple-50 rounded-lg p-4">
          <div className="flex items-center mb-2">
            <TrendingUp className="w-4 h-4 text-purple-600 mr-2" />
            <span className="text-sm font-medium text-purple-900">Matchup</span>
          </div>
          <div className="text-2xl font-bold text-purple-600">
            {(recommendation.opponentMatchup.matchupScore * 100).toFixed(0)}%
          </div>
          <div className="text-xs text-purple-700">
            {recommendation.opponentMatchup.strongAgainst.length} advantages
          </div>
        </div>
      </div>

      {/* Strengths and Weaknesses */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {recommendation.strengths.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-900 mb-2 flex items-center">
              <CheckCircle className="w-4 h-4 text-green-600 mr-2" />
              Strengths
            </h4>
            <ul className="space-y-1">
              {recommendation.strengths.map((strength, index) => (
                <li key={index} className="text-sm text-gray-700 flex items-start">
                  <span className="text-green-500 mr-2">•</span>
                  {strength}
                </li>
              ))}
            </ul>
          </div>
        )}

        {recommendation.weaknesses.length > 0 && (
          <div>
            <h4 className="text-sm font-semibold text-gray-900 mb-2 flex items-center">
              <AlertTriangle className="w-4 h-4 text-orange-600 mr-2" />
              Weaknesses
            </h4>
            <ul className="space-y-1">
              {recommendation.weaknesses.map((weakness, index) => (
                <li key={index} className="text-sm text-gray-700 flex items-start">
                  <span className="text-orange-500 mr-2">•</span>
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
