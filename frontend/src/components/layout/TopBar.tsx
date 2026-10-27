import { useState, useEffect } from 'react'
import { Bell, Menu, AlignJustify, Sun, Moon } from 'lucide-react'
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
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-3 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-200/70 dark:border-gray-800/80 px-3.5 sm:px-6 transition-colors">
      <div className="flex items-center gap-2.5 sm:gap-3.5">
        {/* Mobile menu toggle (touch target >= 44px) */}
        <button
          onClick={onMenuClick}
          className="flex lg:hidden h-10 w-10 items-center justify-center rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 active:scale-95 transition-all"
          title="Toggle Navigation Menu"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Desktop sidebar toggle */}
        <button
          onClick={onMenuClick}
          className="hidden lg:flex h-9 w-9 items-center justify-center rounded-xl text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition-all"
          title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <AlignJustify className="h-5 w-5" />
        </button>

        <div className="flex flex-col">
          <h1 className="text-base sm:text-lg font-bold text-gray-900 dark:text-white tracking-tight truncate max-w-[200px] sm:max-w-md">
            {title}
          </h1>
          <span className="hidden sm:inline text-[11px] text-gray-400 dark:text-gray-500 font-medium">
            ArecaNut Farm Ops System
          </span>
        </div>
      </div>

      <div className="flex items-center gap-1.5 sm:gap-2.5">
        {/* Dark mode toggle */}
        <button
          type="button"
          onClick={() => setDark(d => !d)}
          className="flex h-10 w-10 items-center justify-center rounded-xl text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {dark ? (
            <Sun className="h-5 w-5 text-amber-400 hover:rotate-90 transition-transform duration-300" />
          ) : (
            <Moon className="h-5 w-5 text-gray-600 hover:-rotate-12 transition-transform duration-300" />
          )}
        </button>

        {/* Notifications */}
        <Link
          to="/notifications"
          className="relative flex h-10 w-10 items-center justify-center rounded-xl text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          title="Notifications"
        >
          <Bell className="h-5 w-5" />
          {unread > 0 && (
            <span className="absolute top-1.5 right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[9px] font-bold text-white shadow-sm ring-2 ring-white dark:ring-gray-900">
              {unread > 9 ? '9+' : unread}
            </span>
          )}
        </Link>

        {/* User avatar badge */}
        <div className="flex items-center gap-2 pl-1.5 border-l border-gray-200 dark:border-gray-800">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-primary-600 to-primary-800 text-xs font-bold text-white shadow-sm ring-2 ring-primary-500/20">
            {user ? getInitials(user.fullName) : 'AH'}
          </div>
          <div className="hidden md:flex flex-col text-left">
            <span className="text-xs font-semibold text-gray-800 dark:text-gray-200 leading-tight truncate max-w-[120px]">
              {user?.fullName?.split(' ')?.[0] || 'User'}
            </span>
            <span className="text-[10px] text-gray-400 uppercase font-mono">
              {user?.roles?.[0]?.replace('ROLE_', '') || 'STAFF'}
            </span>
          </div>
        </div>
      </div>
    </header>
  )
}

