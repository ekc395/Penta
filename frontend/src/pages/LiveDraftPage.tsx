import { useState } from 'react'

export function LiveDraftPage() {
  const [summonerName, setSummonerName] = useState('')
  const [region, setRegion] = useState('na1')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleAnalyzeLiveDraft = async () => {
    if (!summonerName.trim()) {
      setError('Please enter a summoner name')
      return
    }

    setLoading(true)
    setError(null)

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 2000))
      setError('Live draft analysis feature coming soon!')
    } catch (err: any) {
      setError('Failed to analyze live draft')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-zinc-950 text-white p-6">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-center">Live Draft Analysis</h1>
        
        {/* Input Section */}
        <div className="bg-zinc-800 rounded-lg p-6 mb-8">
          <div className="flex gap-4 items-end">
            <div className="flex-1">
              <label className="block text-sm font-medium mb-2">Summoner Name</label>
              <input
                type="text"
                value={summonerName}
                onChange={(e) => setSummonerName(e.target.value)}
                placeholder="Enter summoner name (e.g., PlayerName#TAG)"
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="w-32">
              <label className="block text-sm font-medium mb-2">Region</label>
              <select
                value={region}
                onChange={(e) => setRegion(e.target.value)}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="na1">NA</option>
                <option value="euw1">EUW</option>
                <option value="eun1">EUNE</option>
                <option value="kr">KR</option>
                <option value="br1">BR</option>
                <option value="jp1">JP</option>
                <option value="ru">RU</option>
                <option value="oc1">OCE</option>
                <option value="tr1">TR</option>
                <option value="la1">LAN</option>
                <option value="la2">LAS</option>
              </select>
            </div>
            <div>
              <button
                onClick={handleAnalyzeLiveDraft}
                disabled={loading}
                className="px-6 py-2 bg-green-700 hover:bg-blue-700 disabled:bg-gray-600 rounded-md transition-colors font-medium"
              >
                {loading ? 'Analyzing...' : 'Analyze Live Draft'}
              </button>
            </div>
          </div>
        </div>

        {error && (
          <div className="bg-red-600 text-white p-4 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* Coming Soon Section */}
        <div className="bg-zinc-800 rounded-lg p-8 text-center">
          <div className="mb-6">
            <div className="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold mb-2">Live Draft Analysis</h2>
            <p className="text-gray-400 mb-6">
              Analyze your live League of Legends draft and get champion recommendations for your team
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-left">
            <div className="bg-gray-800 rounded-lg p-4">
              <h3 className="font-semibold mb-2 text-blue-400">Team Analysis</h3>
              <p className="text-sm text-gray-400">
                Get detailed information about all players on your team including their champion mastery and recent performance.
              </p>
            </div>
            
            <div className="bg-gray-800 rounded-lg p-4">
              <h3 className="font-semibold mb-2 text-green-400">Champion Recommendations</h3>
              <p className="text-sm text-gray-400">
                Receive personalized champion suggestions based on team synergy and player proficiency.
              </p>
            </div>
            
            <div className="bg-gray-800 rounded-lg p-4">
              <h3 className="font-semibold mb-2 text-purple-400">Counter Picks</h3>
              <p className="text-sm text-gray-400">
                Identify the best champions to counter your opponents' picks and gain strategic advantages.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
