import { Link } from 'react-router-dom'
import { User } from 'lucide-react'
import PentaLogo from '@/assets/PentaLogo.png'

export function Header() {

  return (
    <header className="bg-zinc-950 shadow-sm border-b border-zinc-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center">
              <img src={PentaLogo} alt="Penta Logo" className="w-full h-full object-contain" />
            </div>
            <span className="text-2xl font-bold">Penta</span>
          </Link>

          {/* User Actions */}
          <div className="flex items-center space-x-4">
            <button className="p-2 text-gray-400 hover:text-gray-300 transition-colors">
              <User className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}
