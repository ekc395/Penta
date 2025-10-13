export const REGIONS = [
  { value: 'NA', label: 'North America' },
  { value: 'EUW', label: 'Europe West' },
  { value: 'EUNE', label: 'Europe Nordic & East' },
  { value: 'KR', label: 'Korea' },
  { value: 'BR', label: 'Brazil' },
  { value: 'JP', label: 'Japan' },
  { value: 'OCE', label: 'Oceania' },
  { value: 'TR', label: 'Turkey' },
  { value: 'RU', label: 'Russia' },
  { value: 'LAN', label: 'Latin America North' },
  { value: 'LAS', label: 'Latin America South' },
  { value: 'TW', label: 'Taiwan' },
  { value: 'ASEAN', label: 'Singapore, Malaysia, Indonesia' },
  { value: 'TH', label: 'Thailand' },
  { value: 'PH', label: 'Philippines' },
  { value: 'MENA', label: 'Middle East' }
] as const

export const ROLES = [
  { value: 'TOP', label: 'Top Lane' },
  { value: 'JUNGLE', label: 'Jungle' },
  { value: 'MID', label: 'Mid Lane' },
  { value: 'ADC', label: 'Bot Lane' },
  { value: 'SUPPORT', label: 'Support' }
] as const

// Extract the union types for TypeScript
export type Region = typeof REGIONS[number]['value']
export type Role = typeof ROLES[number]['value']
