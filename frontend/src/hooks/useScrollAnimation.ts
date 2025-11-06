import { useEffect, useCallback, useRef } from 'react'

const HEADING_FADE_THRESHOLD = 0.1
const ITEM_FADE_IN_DURATION = 0.2
const ITEM_VISIBLE_END = 0.8
const ITEM_FADE_OUT_DURATION = 0.2
const TRANSLATE_DISTANCE = 20
const THROTTLE_DELAY = 16

/**
 * Throttle utility for performance optimization
 */
const useThrottle = (callback: Function, delay: number) => {
  const lastRun = useRef(Date.now())
  const timeoutRef = useRef<NodeJS.Timeout>()

  return useCallback((...args: any[]) => {
    const now = Date.now()
    const timeSinceLastRun = now - lastRun.current

    if (timeSinceLastRun >= delay) {
      callback(...args)
      lastRun.current = now
    } else {
      clearTimeout(timeoutRef.current)
      timeoutRef.current = setTimeout(() => {
        callback(...args)
        lastRun.current = Date.now()
      }, delay - timeSinceLastRun)
    }
  }, [callback, delay])
}

/**
 * Hook for scroll-based animations with sticky sections
 */
export const useScrollAnimation = () => {
  const featureRefs = useRef<HTMLElement[]>([])

  const handleScrollAnimation = useCallback(() => {
    const section = document.querySelector('.features-section') as HTMLElement
    const contentItems = featureRefs.current
    const heading = document.querySelector('.features-heading') as HTMLElement
    
    if (!section || !contentItems.length) return

    const sectionTop = section.offsetTop
    const sectionHeight = section.offsetHeight
    const scrollY = window.scrollY
    const viewportHeight = window.innerHeight
    const scrollProgress = (scrollY - sectionTop) / (sectionHeight - viewportHeight)
    const clampedProgress = Math.max(0, Math.min(1, scrollProgress))

    if (heading) {
      const headingProgress = Math.min(clampedProgress / HEADING_FADE_THRESHOLD, 1)
      heading.style.opacity = `${headingProgress}`
      heading.style.transform = `translateY(${TRANSLATE_DISTANCE * (1 - headingProgress)}px)`
    }

    const totalItems = contentItems.length
    const progressPerItem = 1 / totalItems

    contentItems.forEach((item, index) => {
      if (!item) return

      const itemStartProgress = index * progressPerItem
      const itemEndProgress = (index + 1) * progressPerItem
      
      let opacity = 0
      let translateY = TRANSLATE_DISTANCE
      
      if (clampedProgress >= itemStartProgress && clampedProgress <= itemEndProgress) {
        const itemProgress = (clampedProgress - itemStartProgress) / progressPerItem
        
        if (itemProgress < ITEM_FADE_IN_DURATION) {
          opacity = itemProgress / ITEM_FADE_IN_DURATION
          translateY = TRANSLATE_DISTANCE * (1 - itemProgress / ITEM_FADE_IN_DURATION)
        } else if (itemProgress < ITEM_VISIBLE_END) {
          opacity = 1
          translateY = 0
        } else {
          opacity = (1 - itemProgress) / ITEM_FADE_OUT_DURATION
          translateY = -TRANSLATE_DISTANCE * ((itemProgress - ITEM_VISIBLE_END) / ITEM_FADE_OUT_DURATION)
        }
      } else if (clampedProgress > itemEndProgress) {
        opacity = 0
        translateY = -TRANSLATE_DISTANCE
      }
      
      item.style.opacity = `${opacity}`
      item.style.transform = `translateY(${translateY}px)`
    })
  }, [])

  const throttledScroll = useThrottle(handleScrollAnimation, THROTTLE_DELAY)

  useEffect(() => {
    window.addEventListener('scroll', throttledScroll, { passive: true })
    handleScrollAnimation()

    return () => window.removeEventListener('scroll', throttledScroll)
  }, [throttledScroll, handleScrollAnimation])

  return featureRefs
}
