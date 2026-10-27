import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  CalendarCheck,
  Leaf,
  ShoppingCart,
  Wallet,
  Menu,
} from 'lucide-react'
import { cn } from '@/utils'

interface BottomNavProps {
  onMenuClick: () => void
}

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/attendance', label: 'Attendance', icon: CalendarCheck },
  { to: '/harvest', label: 'Harvest', icon: Leaf },
  { to: '/sales', label: 'Sales', icon: ShoppingCart },
  { to: '/wages', label: 'Wages', icon: Wallet },
]

export default function BottomNav({ onMenuClick }: BottomNavProps) {
  return (
    <nav className="lg:hidden fixed bottom-0 inset-x-0 z-30 bg-white/95 backdrop-blur-md border-t border-gray-200 shadow-lg px-2 py-1 flex items-center justify-around safe-bottom">
      {navItems.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            cn(
              'flex flex-col items-center justify-center py-1 px-2 rounded-lg text-[10px] font-medium transition-colors min-w-[50px]',
              isActive
                ? 'text-primary-600 font-semibold'
                : 'text-gray-500 hover:text-gray-900',
            )
          }
        >
          {({ isActive }) => (
            <>
              <Icon
                className={cn(
                  'h-5 w-5 mb-0.5 transition-transform',
                  isActive ? 'scale-110 text-primary-600' : 'text-gray-400',
                )}
              />
              <span>{label}</span>
            </>
          )}
        </NavLink>
      ))}

      <button
        type="button"
        onClick={onMenuClick}
        className="flex flex-col items-center justify-center py-1 px-2 rounded-lg text-[10px] font-medium text-gray-500 hover:text-gray-900 transition-colors min-w-[50px]"
      >
        <Menu className="h-5 w-5 mb-0.5 text-gray-400" />
        <span>More</span>
      </button>
    </nav>
  )
}
