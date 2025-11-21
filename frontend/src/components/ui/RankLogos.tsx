import { motion } from 'framer-motion'
import { Iron, Bronze, Silver, Gold, Platinum, Emerald, Diamond, Master, Grandmaster, Challenger } from '@/assets'

const RANK_POSITIONS = [
  { img: Iron, top: '8%', left: '8%', size: 'w-28 h-28' },
  { img: Bronze, top: '4%', left: '35%', size: 'w-28 h-28' },
  { img: Silver, top: '6%', right: '5%', size: 'w-28 h-28' },
  { img: Gold, top: '30%', left: '3%', size: 'w-28 h-28' },
  { img: Platinum, top: '15%', right: '27%', size: 'w-28 h-28' },
  { img: Emerald, top: '58%', left: '5%', size: 'w-28 h-28' },
  { img: Diamond, top: '50%', right: '7%', size: 'w-28 h-28' },
  { img: Master, top: '78%', left: '18%', size: 'w-28 h-28' },
  { img: Grandmaster, top: '85%', left: '45%', size: 'w-28 h-28' },
  { img: Challenger, top: '82%', right: '10%', size: 'w-28 h-28' },
]

export function RankLogos() {
  return (
    <>
      {RANK_POSITIONS.map((rank, index) => (
        <motion.img
          key={index}
          src={rank.img}
          alt={`Rank ${index + 1}`}
          className={`${rank.size} absolute opacity-40 hover:opacity-70 transition-opacity duration-300 pointer-events-none`}
          style={{
            top: rank.top,
            left: rank.left,
            right: rank.right,
          }}
          initial={{ opacity: 0.65, scale: 1 }}
          whileInView={{ opacity: 0.65, scale: 1 }}
          animate={{
            y: [0, -20, 0],
          }}
          transition={{
            duration: 3 + (index * 0.2),
            delay: index * 0.1,
            repeat: Infinity,
            ease: "easeInOut"
          }}
          viewport={{ once: true }}
        />
      ))}
    </>
  )
}
