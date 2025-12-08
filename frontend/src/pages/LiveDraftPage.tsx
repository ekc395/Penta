import { useState } from 'react'
import { ScreenCapture } from '@/components/ui/ScreenCapture'

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

        {/* Screen Capture Section */}
        <div className="bg-zinc-800 rounded-lg p-8 text-center">
          <ScreenCapture />
        </div>
      </div>
    </div>
  )
}
