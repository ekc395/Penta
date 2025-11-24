import { MatchParticipant } from '@/types'

export interface PentaScoreResult {
  score: number
  rank: number
  badge?: 'MVP' | 'ACE' | null
}

export function calculatePentaScore(
  participant: MatchParticipant,
  allParticipants: MatchParticipant[],
  gameDuration: number
): PentaScoreResult {
  const minutes = gameDuration / 60
  
  // Get team and game averages for comparison
  const team = allParticipants.filter(p => p.teamId === participant.teamId)
  const teamKills = team.reduce((sum, p) => sum + p.kills, 0)
  const gameKills = allParticipants.reduce((sum, p) => sum + p.kills, 0)
  const avgDamage = allParticipants.reduce((sum, p) => sum + p.damageDealt, 0) / 10
  const avgGold = allParticipants.reduce((sum, p) => sum + p.goldEarned, 0) / 10
  const avgCs = allParticipants.reduce((sum, p) => sum + p.cs, 0) / 10
  
  // KILL PARTICIPATION - THE MOST IMPORTANT STAT
  // Being involved in fights matters more than anything
  const killParticipation = teamKills > 0 
    ? (participant.kills + participant.assists) / teamKills 
    : 0
  
  // KP is king - MASSIVELY weighted
  // 80% KP = 4.0 points, 50% KP = 2.5 points, 15% KP = 0.75 points
  const kpScore = Math.min(5.0, killParticipation * 5.0)
  
  // LOW KP PENALTY - punish players avoiding fights
  // If KP < 30%, apply penalty
  const lowKpPenalty = killParticipation < 0.3 
    ? Math.min(1.5, (0.3 - killParticipation) * 3.0)  // Max -1.5 points at 0% KP
    : 0
  
  // KDA with STRONG diminishing returns - don't over-reward safe play
  let kdaScore = 0
  if (participant.deaths === 0) {
    const perfectKda = (participant.kills + participant.assists)
    // Perfect KDA bonus but capped low - safe play shouldn't dominate
    kdaScore = Math.min(1.0, Math.log(perfectKda + 1) * 0.45)
  } else {
    const kda = (participant.kills + participant.assists) / participant.deaths
    // Normal KDA with diminishing returns
    kdaScore = Math.min(1.0, Math.log(kda + 1) * 0.4)
  }
  
  // Raw kills bonus - fighting and getting kills matters
  const killsBonus = Math.min(1.3, participant.kills * 0.08)
  
  // Death penalty - but not overwhelming for fighters
  // 3 deaths = -1.2, 5 deaths = -2.0, 10 deaths = -4.0
  const deathPenalty = Math.min(4, participant.deaths * 0.4)
  
  // DAMAGE - REDUCED weight since it's role-dependent
  const damageRatio = avgDamage > 0 ? participant.damageDealt / avgDamage : 1
  const damageScore = Math.min(1.0, Math.max(0, (damageRatio - 0.6) * 0.7))
  
  // Gold contribution - moderate weight
  const goldRatio = avgGold > 0 ? participant.goldEarned / avgGold : 1
  const goldScore = Math.min(0.7, Math.max(0, (goldRatio - 0.75) * 0.7))
  
  // CS score - minor weight (role-dependent)
  const csPerMin = participant.cs / minutes
  const csScore = Math.min(0.4, csPerMin / 12)
  
  // Vision score - minor weight
  const visionPerMin = participant.visionScore / minutes
  const visionScoreVal = Math.min(0.3, visionPerMin * 0.07)
  
  // Win/loss modifier - FURTHER REDUCED from ±0.7 to ±0.4
  // This creates only a 0.8 point swing
  // High-impact losers should definitively outscore low-impact winners
  const winLossMod = participant.won ? 0.4 : -0.4
  
  // Calculate raw score
  let rawScore = kpScore + kdaScore + killsBonus + damageScore + goldScore + 
                 csScore + visionScoreVal - deathPenalty - lowKpPenalty + winLossMod
  
  // Normalize to 0-10 scale with LOWER base to prevent inflation
  // Reduced from 3.5 to 2.8
  let finalScore = 2.8 + rawScore
  
  // Cap at 10, floor at 0
  finalScore = Math.max(0, Math.min(10, finalScore))
  
  // Calculate rank among all participants
  const allScores = allParticipants.map(p => {
    const pTeam = allParticipants.filter(x => x.teamId === p.teamId)
    const pTeamKills = pTeam.reduce((sum, x) => sum + x.kills, 0)
    const pKp = pTeamKills > 0 ? (p.kills + p.assists) / pTeamKills : 0
    
    const pKpScore = Math.min(5.0, pKp * 5.0)
    
    const pLowKpPenalty = pKp < 0.3 
      ? Math.min(1.5, (0.3 - pKp) * 3.0)
      : 0
    
    let pKdaScore = 0
    if (p.deaths === 0) {
      const perfectKda = (p.kills + p.assists)
      pKdaScore = Math.min(1.0, Math.log(perfectKda + 1) * 0.45)
    } else {
      const kda = (p.kills + p.assists) / p.deaths
      pKdaScore = Math.min(1.0, Math.log(kda + 1) * 0.4)
    }
    
    const pKillsBonus = Math.min(1.3, p.kills * 0.08)
    const pDeathPenalty = Math.min(4, p.deaths * 0.4)
    
    const pDamageRatio = avgDamage > 0 ? p.damageDealt / avgDamage : 1
    const pDamageScore = Math.min(1.0, Math.max(0, (pDamageRatio - 0.6) * 0.7))
    
    const pGoldRatio = avgGold > 0 ? p.goldEarned / avgGold : 1
    const pGoldScore = Math.min(0.7, Math.max(0, (pGoldRatio - 0.75) * 0.7))
    
    const pCsScore = Math.min(0.4, (p.cs / minutes) / 12)
    const pVisionScore = Math.min(0.3, (p.visionScore / minutes) * 0.07)
    
    const pWinLossMod = p.won ? 0.4 : -0.4
    
    const pRawScore = pKpScore + pKdaScore + pKillsBonus + pDamageScore + 
                      pGoldScore + pCsScore + pVisionScore - pDeathPenalty - pLowKpPenalty + pWinLossMod
    
    return {
      summonerName: p.summonerName,
      teamId: p.teamId,
      won: p.won,
      score: Math.max(0, Math.min(10, 2.8 + pRawScore))
    }
  }).sort((a, b) => b.score - a.score)
  
  const rank = allScores.findIndex(s => s.summonerName === participant.summonerName) + 1
  
  // Determine badge
  let badge: 'MVP' | 'ACE' | null = null
  if (rank === 1) {
    badge = 'MVP'
  } else {
    // ACE = best on losing team
    const winningTeamId = allParticipants.find(p => p.won)?.teamId
    const losingTeamScores = allScores.filter(s => s.teamId !== winningTeamId)
    if (losingTeamScores.length > 0 && losingTeamScores[0].summonerName === participant.summonerName) {
      badge = 'ACE'
    }
  }
  
  return {
    score: Math.round(finalScore * 10) / 10,
    rank,
    badge
  }
}