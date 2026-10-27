import { useState, useEffect } from 'react'
import { Bell, Menu, AlignJustify } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { notificationsApi } from '@/api'
import { useAuth } from '@/context/AuthContext'
import { getInitials } from '@/utils'

interface Props {
  onMenuClick: () => void
  sidebarCollapsed: boolean
}

const breadcrumbMap: Record<string, string> = {
  '/': 'Dashboard', '/owners': 'Owners', '/fields': 'Fields', '/contracts': 'Contracts',
  '/workers': 'Workers', '/attendance': 'Attendance', '/wages': 'Wages',
  '/harvest': 'Harvest Sessions', '/inventory': 'Inventory', '/buyers': 'Buyers',
  '/sales': 'Sales', '/expenses': 'Expenses', '/vehicles': 'Vehicles',
  '/equipment': 'Equipment', '/schedule': 'Schedule', '/reports': 'Reports',
  '/notifications': 'Notifications',
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
    <header className="sticky top-0 z-20 flex h-14 items-center justify-between gap-4 bg-white border-b border-gray-200 px-4 transition-colors">
      <div className="flex items-center gap-3">
        {/* Mobile menu toggle */}
        <button
          onClick={onMenuClick}
          className="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 lg:hidden"
        >
          <Menu className="h-5 w-5" />
        </button>
        {/* Desktop sidebar toggle */}
        <button
          onClick={onMenuClick}
          className="hidden lg:block p-1.5 rounded-lg text-gray-500 hover:bg-gray-100"
        >
          <AlignJustify className="h-5 w-5" />
        </button>
        <h1 className="text-base font-semibold text-gray-900">{title}</h1>
      </div>

      <div className="flex items-center gap-2">
        {/* Dark mode toggle */}
        <button
          type="button"
          onClick={() => setDark(d => !d)}
          className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {dark ? (
            <span className="text-base">☀️</span>
          ) : (
            <span className="text-base">🌙</span>
          )}
        </button>

        <Link
          to="/notifications"
          className="relative p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700"
        >
          <Bell className="h-5 w-5" />
          {unread > 0 && (
            <span className="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[9px] font-bold text-white">
              {unread > 9 ? '9+' : unread}
            </span>
          )}
        </Link>

        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-600 text-xs font-semibold text-white">
          {user ? getInitials(user.fullName) : '?'}
        </div>
      </div>
    </header>
  )
}
