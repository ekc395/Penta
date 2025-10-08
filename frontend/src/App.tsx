import { Routes, Route } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { HomePage } from '@/pages/HomePage'
import { RecommendationsPage } from '@/pages/RecommendationsPage'
import { PlayerPage } from '@/pages/PlayerPage'
import { Footer } from '@/components/layout/Footer'

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/recommendations" element={<RecommendationsPage />} />
          <Route path="/player/:summonerName" element={<PlayerPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}

export default App
