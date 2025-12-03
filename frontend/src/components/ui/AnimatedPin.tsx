import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { PinContainer } from './3dPin'
import { SummonersRift } from '@/assets'

interface AnimatedPinProps {
  onGetStarted?: () => void
}

export function AnimatedPin({ onGetStarted }: AnimatedPinProps) {
  const [isDiving, setIsDiving] = useState(false)
  const [isFlashing, setIsFlashing] = useState(false)

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault()
    setIsDiving(true)
    
    // This is so hacky
    setTimeout(() => {
      setIsFlashing(true)
    }, 350)
    
    setTimeout(() => {
      onGetStarted?.()
    }, 800)
    
    setTimeout(() => {
      setIsDiving(false)
      setIsFlashing(false)
    }, 2200)
  }

  return (
    <>
      <motion.div
        animate={isDiving ? {
          scale: 2000,
          z: 1000,
        } : {
          scale: 1,
          z: 0,
        }}
        transition={{
          duration: 0.8,
          ease: [0.43, 0.13, 0.23, 0.96],
        }}
        style={{
          transformStyle: 'preserve-3d',
          position: 'relative',
          zIndex: isDiving ? 9999 : 50,
          transformOrigin: 'center 64%',
        }}
      >
        <PinContainer
            title="Dive In"
            href="#"
            onClick={handleClick}
        >
          <div className="flex basis-full flex-col p-12 tracking-tight text-slate-100/50 w-[56rem] h-[48rem]">
            <h3 className="max-w-s !pb-2 !m-0 font-bold text-3xl text-slate-100">
              Ready to climb the ranks?
            </h3>
            <div className="text-lg !m-0 !p-0 font-normal">
              <span className="text-gray-400">
                Start getting personalized champion recommendations and improve your gameplay.
              </span>
            </div>
            
            {/* Summoners Rift background */}
            <div className="flex flex-1 w-full rounded-lg mt-6 relative overflow-hidden">
              <img src={SummonersRift} alt="Summoners Rift" className="absolute inset-0 w-full h-full object-cover opacity-50"/>
            </div>
          </div>
        </PinContainer>
      </motion.div>

      {/* Full screen cyan flash */}
      <AnimatePresence>
        {isFlashing && (
          <>
            {/* Main cyan flash covering entire screen */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ 
                opacity: [0, 1, 1, 1, 0],
              }}
              exit={{ opacity: 0 }}
              transition={{ 
                duration: 1.4,
                times: [0, 0.15, 0.4, 0.7, 1]
              }}
              className="fixed inset-0 z-[10000] bg-cyan-500 pointer-events-none"
            />
            
            {/* Bright cyan-white flash at the peak */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ 
                opacity: [0, 0.9, 0.9, 0],
              }}
              exit={{ opacity: 0 }}
              transition={{ 
                duration: 1.0,
                times: [0, 0.3, 0.6, 1]
              }}
              className="fixed inset-0 z-[10001] bg-cyan-200 pointer-events-none"
            />
          </>
        )}
      </AnimatePresence>
    </>
  )
}
