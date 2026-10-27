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

const colorStyles = {
  green: {
    bg: 'bg-emerald-50/50 dark:bg-emerald-950/20',
    border: 'border-emerald-100/60 dark:border-emerald-800/40',
    iconBg: 'bg-gradient-to-br from-emerald-500 to-emerald-700 text-white shadow-sm',
    badge: 'text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/60',
  },
  blue: {
    bg: 'bg-blue-50/50 dark:bg-blue-950/20',
    border: 'border-blue-100/60 dark:border-blue-800/40',
    iconBg: 'bg-gradient-to-br from-blue-500 to-blue-700 text-white shadow-sm',
    badge: 'text-blue-700 dark:text-blue-300 bg-blue-50 dark:bg-blue-950/60',
  },
  amber: {
    bg: 'bg-amber-50/50 dark:bg-amber-950/20',
    border: 'border-amber-100/60 dark:border-amber-800/40',
    iconBg: 'bg-gradient-to-br from-amber-500 to-amber-700 text-white shadow-sm',
    badge: 'text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/60',
  },
  red: {
    bg: 'bg-red-50/50 dark:bg-red-950/20',
    border: 'border-red-100/60 dark:border-red-800/40',
    iconBg: 'bg-gradient-to-br from-red-500 to-red-700 text-white shadow-sm',
    badge: 'text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-950/60',
  },
  purple: {
    bg: 'bg-purple-50/50 dark:bg-purple-950/20',
    border: 'border-purple-100/60 dark:border-purple-800/40',
    iconBg: 'bg-gradient-to-br from-purple-500 to-purple-700 text-white shadow-sm',
    badge: 'text-purple-700 dark:text-purple-300 bg-purple-50 dark:bg-purple-950/60',
  },
}

export default function StatCard({ label, value, icon: Icon, trend, trendUp, color = 'green', subtitle }: Props) {
  const c = colorStyles[color]

  return (
    <div className={cn(
      'card relative overflow-hidden flex flex-col justify-between p-3.5 sm:p-5 border transition-all duration-200 hover:-translate-y-0.5 hover:shadow-card-hover',
      c.border,
    )}>
      <div className="flex items-start justify-between gap-2.5">
        <div className="min-w-0 flex-1">
          <p className="text-[11px] sm:text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider truncate">
            {label}
          </p>
          <p className="mt-1 sm:mt-1.5 text-xl sm:text-2xl font-bold tracking-tight text-gray-900 dark:text-white tabular-nums truncate">
            {value}
          </p>
        </div>
        <div className={cn('flex h-10 w-10 sm:h-11 sm:w-11 shrink-0 items-center justify-center rounded-xl', c.iconBg)}>
          <Icon className="h-5 w-5" />
        </div>
      </div>

      {(subtitle || trend) && (
        <div className="mt-2.5 pt-2 border-t border-gray-100 dark:border-gray-800/60 flex items-center justify-between text-xs">
          {subtitle && (
            <span className="text-[11px] text-gray-400 dark:text-gray-500 truncate">{subtitle}</span>
          )}
          {trend && (
            <span className={cn('inline-flex items-center gap-0.5 font-semibold text-[11px] rounded-md px-1.5 py-0.5', c.badge)}>
              {trendUp ? '↑' : '↓'} {trend}
            </span>
          )}
        </div>
      )}
    </div>
  )
}

