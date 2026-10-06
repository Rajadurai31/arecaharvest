import { type LucideIcon } from 'lucide-react'
import { cn } from '@/utils'

interface Props {
  label: string
  value: string | number
  icon: LucideIcon
  trend?: string
  trendUp?: boolean
  color?: 'green' | 'blue' | 'amber' | 'red' | 'purple'
  subtitle?: string
}

const colors = {
  green:  { bg: 'bg-green-50',  icon: 'text-green-600',  iconBg: 'bg-green-100' },
  blue:   { bg: 'bg-blue-50',   icon: 'text-blue-600',   iconBg: 'bg-blue-100'  },
  amber:  { bg: 'bg-amber-50',  icon: 'text-amber-600',  iconBg: 'bg-amber-100' },
  red:    { bg: 'bg-red-50',    icon: 'text-red-600',    iconBg: 'bg-red-100'   },
  purple: { bg: 'bg-purple-50', icon: 'text-purple-600', iconBg: 'bg-purple-100'},
}

export default function StatCard({ label, value, icon: Icon, trend, trendUp, color = 'green', subtitle }: Props) {
  const c = colors[color]
  return (
    <div className="card flex items-start gap-4">
      <div className={cn('flex h-11 w-11 shrink-0 items-center justify-center rounded-xl', c.iconBg)}>
        <Icon className={cn('h-5 w-5', c.icon)} />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs font-medium text-gray-500 uppercase tracking-wide truncate">{label}</p>
        <p className="mt-1 text-2xl font-bold text-gray-900 tabular-nums">{value}</p>
        {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
        {trend && (
          <p className={cn('text-xs font-medium mt-1', trendUp ? 'text-green-600' : 'text-red-500')}>
            {trendUp ? '↑' : '↓'} {trend}
          </p>
        )}
      </div>
    </div>
  )
}
