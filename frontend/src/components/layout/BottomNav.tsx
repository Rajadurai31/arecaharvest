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
  { to: '/',           label: 'Home',       icon: LayoutDashboard },
  { to: '/attendance', label: 'Attendance', icon: CalendarCheck },
  { to: '/harvest',    label: 'Harvest',    icon: Leaf },
  { to: '/sales',      label: 'Sales',      icon: ShoppingCart },
  { to: '/wages',      label: 'Wages',      icon: Wallet },
]

export default function BottomNav({ onMenuClick }: BottomNavProps) {
  return (
    <div className="lg:hidden fixed bottom-3 inset-x-3 z-40 max-w-lg mx-auto">
      <nav className="glass-dock rounded-2xl px-1.5 py-1.5 flex items-center justify-around shadow-dock">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              cn(
                'relative flex flex-col items-center justify-center py-1 px-2.5 rounded-xl transition-all duration-200 min-w-[54px] min-h-[44px]',
                isActive
                  ? 'text-primary-600 dark:text-primary-400 font-semibold'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200',
              )
            }
          >
            {({ isActive }) => (
              <>
                {isActive && (
                  <span className="absolute inset-0 bg-primary-50 dark:bg-primary-950/80 rounded-xl -z-10 animate-fade-in" />
                )}
                <Icon
                  className={cn(
                    'h-5 w-5 mb-0.5 transition-transform duration-150',
                    isActive ? 'scale-110 text-primary-600 dark:text-primary-400' : 'text-gray-400 dark:text-gray-500',
                  )}
                />
                <span className="text-[10px] tracking-tight leading-none">{label}</span>
              </>
            )}
          </NavLink>
        ))}

        <button
          type="button"
          onClick={onMenuClick}
          className="flex flex-col items-center justify-center py-1 px-2.5 rounded-xl text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 transition-colors min-w-[54px] min-h-[44px] active:scale-95"
          title="Open Menu"
        >
          <Menu className="h-5 w-5 mb-0.5 text-gray-400 dark:text-gray-500" />
          <span className="text-[10px] tracking-tight leading-none">More</span>
        </button>
      </nav>
    </div>
  )
}

