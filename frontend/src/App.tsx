import { Routes, Route } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { HomePage } from '@/pages/HomePage'
import { PlayerPage } from '@/pages/PlayerPage'
import { LiveDraftPage } from '@/pages/LiveDraftPage'
import { Footer } from '@/components/layout/Footer'
import { ChampionDetailPage } from '@/pages/ChampionDetailPage'

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/player/:summonerName/champion" element={<ChampionDetailPage />} />
        <Route path="/player/:summonerName" element={<PlayerPage />} />
        <Route path="/live-draft" element={<LiveDraftPage />} />
      </Routes>
      </main>
      <Footer />
    </div>
  )
}

export default App
