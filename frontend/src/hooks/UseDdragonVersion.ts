import { useState, useEffect } from 'react'
import { configApi } from '@/services/api'

let cachedVersion: string | null = null

export function useDdragonVersion() {
  const [version, setVersion] = useState<string>(cachedVersion || '15.23.1')

  useEffect(() => {
    if (!cachedVersion) {
      configApi.getDdragonVersion().then((v) => {
        cachedVersion = v
        setVersion(v)
      }).catch(() => {
        // Fallback to default if API fails
        setVersion('15.23.1')
      })
    }
  }, [])

  return version
}