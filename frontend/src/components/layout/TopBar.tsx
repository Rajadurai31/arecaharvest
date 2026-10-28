import { useState, useEffect } from 'react'
import { Bell, Menu, AlignJustify, Sun, Moon, TrendingUp, Sparkles } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { notificationsApi } from '@/api'
import { useAuth } from '@/context/AuthContext'
import { getInitials } from '@/utils'

interface Props {
  onMenuClick: () => void
  sidebarCollapsed: boolean
}

const breadcrumbMap: Record<string, string> = {
  '/': 'Dashboard Overview',
  '/schedule': 'Harvest Schedule',
  '/owners': 'Land Owners',
  '/fields': 'Plantation Fields',
  '/contracts': 'Harvest Contracts',
  '/workers': 'Workers & Harvesters',
  '/worker-assignments': 'Worker Assignments',
  '/attendance': 'Daily Attendance',
  '/wages': 'Wages & Advances',
  '/harvest': 'Harvest Sessions',
  '/inventory': 'Stock Inventory',
  '/buyers': 'Areca Nut Buyers',
  '/sales': 'Sales & Invoices',
  '/expenses': 'Operational Expenses',
  '/vehicles': 'Fleet Vehicles',
  '/equipment': 'Machinery & Equipment',
  '/reports': 'Analytics & Reports',
  '/notifications': 'System Notifications',
  '/users': 'User Management',
}

export default function TopBar({ onMenuClick, sidebarCollapsed }: Props) {
  const { user } = useAuth()
  const location = useLocation()
  const [unread, setUnread] = useState(0)
  const [dark, setDark] = useState(() => localStorage.getItem('theme') === 'dark')

  const title = breadcrumbMap[location.pathname] ?? 'ArecaHarvest'

  useEffect(() => {
    notificationsApi.getUnreadCount().then(setUnread).catch(() => {})
  }, [location.pathname])

  useEffect(() => {
    if (dark) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }, [dark])

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-3 bg-white/85 dark:bg-slate-900/85 backdrop-blur-xl border-b border-slate-200/80 dark:border-slate-800/80 px-4 sm:px-6 transition-colors shadow-sm">
      <div className="flex items-center gap-3">
        {/* Mobile menu toggle (touch target >= 44px) */}
        <button
          onClick={onMenuClick}
          className="flex lg:hidden h-10 w-10 items-center justify-center rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 active:scale-95 transition-all"
          title="Toggle Navigation Menu"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Desktop sidebar toggle */}
        <button
          onClick={onMenuClick}
          className="hidden lg:flex h-9 w-9 items-center justify-center rounded-xl text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-all"
          title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <AlignJustify className="h-5 w-5" />
        </button>

        <div className="flex flex-col">
          <h1 className="text-base sm:text-lg font-extrabold text-slate-900 dark:text-white tracking-tight truncate max-w-[200px] sm:max-w-md">
            {title}
          </h1>
          <span className="hidden sm:inline text-[11px] text-slate-500 dark:text-slate-400 font-medium">
            Arecanut Plantation Operations Management
          </span>
        </div>
      </div>

      {/* Middle Desktop Season & APMC Ticker */}
      <div className="hidden xl:flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-50/80 dark:bg-emerald-950/40 border border-emerald-200/70 dark:border-emerald-800/50 text-xs font-semibold text-emerald-800 dark:text-emerald-300">
        <Sparkles className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400 animate-pulse" />
        <span>Karnataka Areca Season 2025–26</span>
        <span className="text-emerald-400 dark:text-emerald-600">•</span>
        <TrendingUp className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400" />
        <span className="font-mono">APMC Shimoga Avg: ₹58,500/Qtl</span>
      </div>

      <div className="flex items-center gap-2 sm:gap-3">
        {/* Dark mode toggle */}
        <button
          type="button"
          onClick={() => setDark(d => !d)}
          className="flex h-10 w-10 items-center justify-center rounded-xl text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {dark ? (
            <Sun className="h-5 w-5 text-amber-400 hover:rotate-90 transition-transform duration-300" />
          ) : (
            <Moon className="h-5 w-5 text-slate-600 hover:-rotate-12 transition-transform duration-300" />
          )}
        </button>

        {/* Notifications */}
        <Link
          to="/notifications"
          className="relative flex h-10 w-10 items-center justify-center rounded-xl text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          title="Notifications"
        >
          <Bell className="h-5 w-5" />
          {unread > 0 && (
            <span className="absolute top-1.5 right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-rose-500 text-[9px] font-bold text-white shadow-sm ring-2 ring-white dark:ring-slate-900 animate-pulse">
              {unread > 9 ? '9+' : unread}
            </span>
          )}
        </Link>

        {/* User profile avatar & badge */}
        <div className="flex items-center gap-2.5 pl-2 border-l border-slate-200 dark:border-slate-800">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-600 to-primary-700 text-xs font-bold text-white shadow-md ring-2 ring-emerald-500/30">
            {user ? getInitials(user.fullName) : 'AH'}
          </div>
          <div className="hidden md:flex flex-col text-left">
            <span className="text-xs font-bold text-slate-800 dark:text-slate-200 leading-tight truncate max-w-[120px]">
              {user?.fullName?.split(' ')?.[0] || 'User'}
            </span>
            <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-mono font-bold tracking-wider">
              {user?.roles?.[0]?.replace('ROLE_', '') || 'STAFF'}
            </span>
          </div>
        </div>
      </div>
    </header>
  )
}
