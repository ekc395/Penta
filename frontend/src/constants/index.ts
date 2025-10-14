export const REGIONS = [
  { value: 'na1', label: 'North America' },
  { value: 'euw1', label: 'Europe West' },
  { value: 'eune1', label: 'Europe Nordic & East' },
  { value: 'kr', label: 'Korea' },
  { value: 'br1', label: 'Brazil' },
  { value: 'jp1', label: 'Japan' },
  { value: 'oc1', label: 'Oceania' },
  { value: 'tr1', label: 'Turkey' },
  { value: 'ru', label: 'Russia' },
  { value: 'la1', label: 'Latin America North' },
  { value: 'la2', label: 'Latin America South' },
  { value: 'tw2', label: 'Taiwan' },
  { value: 'sg2', label: 'Singapore, Malaysia, Indonesia' },
  { value: 'th2', label: 'Thailand' },
  { value: 'ph2', label: 'Philippines' },
  { value: 'me1', label: 'Middle East' }
] as const

export const ROLES = [
  { value: 'TOP', label: 'Top Lane' },
  { value: 'JUNGLE', label: 'Jungle' },
  { value: 'MID', label: 'Mid Lane' },
  { value: 'ADC', label: 'Bot Lane' },
  { value: 'SUPPORT', label: 'Support' }
] as const

export type Region = typeof REGIONS[number]['value']
export type Role = typeof ROLES[number]['value']