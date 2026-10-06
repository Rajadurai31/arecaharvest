import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Users, MapPin, FileText, UserCheck, ClipboardList,
  Wallet, Leaf, Package, ShoppingCart, Receipt, Truck, Wrench,
  Calendar, BarChart2, Bell, Settings, LogOut, ChevronLeft,
} from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { cn } from '@/utils'

const nav = [
  { section: 'Main', items: [
    { to: '/',         icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/schedule', icon: Calendar,        label: 'Schedule'  },
  ]},
  { section: 'People', items: [
    { to: '/owners',  icon: Users,       label: 'Owners'  },
    { to: '/workers', icon: UserCheck,   label: 'Workers' },
  ]},
  { section: 'Fields & Contracts', items: [
    { to: '/fields',    icon: MapPin,    label: 'Fields'    },
    { to: '/contracts', icon: FileText,  label: 'Contracts' },
  ]},
  { section: 'Operations', items: [
    { to: '/attendance', icon: ClipboardList, label: 'Attendance'  },
    { to: '/harvest',    icon: Leaf,          label: 'Harvest'     },
    { to: '/inventory',  icon: Package,       label: 'Inventory'   },
  ]},
  { section: 'Finance', items: [
    { to: '/wages',    icon: Wallet,       label: 'Wages'    },
    { to: '/buyers',   icon: Users,        label: 'Buyers'   },
    { to: '/sales',    icon: ShoppingCart, label: 'Sales'    },
    { to: '/expenses', icon: Receipt,      label: 'Expenses' },
  ]},
  { section: 'Assets', items: [
    { to: '/vehicles',  icon: Truck,   label: 'Vehicles'  },
    { to: '/equipment', icon: Wrench,  label: 'Equipment' },
  ]},
  { section: 'Reports', items: [
    { to: '/reports',       icon: BarChart2, label: 'Reports'       },
    { to: '/notifications', icon: Bell,      label: 'Notifications' },
  ]},
]

interface Props {
  collapsed: boolean
  onToggle: () => void
}

export default function Sidebar({ collapsed, onToggle }: Props) {
  const { user, logout } = useAuth()

  return (
    <aside className={cn(
      'fixed inset-y-0 left-0 z-30 flex flex-col bg-gray-900 text-white transition-all duration-300',
      collapsed ? 'w-16' : 'w-60',
    )}>
      {/* Logo */}
      <div className="flex h-14 items-center justify-between px-4 border-b border-gray-800">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary-600">
              <Leaf className="h-4 w-4 text-white" />
            </div>
            <span className="text-sm font-bold text-white">ArecaHarvest</span>
          </div>
        )}
        {collapsed && (
          <div className="flex h-7 w-7 mx-auto items-center justify-center rounded-lg bg-primary-600">
            <Leaf className="h-4 w-4 text-white" />
          </div>
        )}
        {!collapsed && (
          <button onClick={onToggle} className="p-1 rounded text-gray-400 hover:text-white hover:bg-gray-800">
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-3 space-y-5 px-2">
        {nav.map(section => (
          <div key={section.section}>
            {!collapsed && (
              <p className="px-3 mb-1 text-[10px] font-semibold uppercase tracking-widest text-gray-500">
                {section.section}
              </p>
            )}
            <ul className="space-y-0.5">
              {section.items.map(item => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.to === '/'}
                    className={({ isActive }) => cn(
                      'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-primary-600 text-white'
                        : 'text-gray-300 hover:bg-gray-800 hover:text-white',
                      collapsed && 'justify-center',
                    )}
                    title={collapsed ? item.label : undefined}
                  >
                    <item.icon className="h-4 w-4 shrink-0" />
                    {!collapsed && <span>{item.label}</span>}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      {/* Footer */}
      <div className="border-t border-gray-800 p-3 space-y-1">
        {!collapsed && (
          <div className="px-3 py-2">
            <p className="text-xs font-medium text-white truncate">{user?.fullName}</p>
            <p className="text-[11px] text-gray-400 truncate">{user?.roles?.[0]?.replace('ROLE_', '')}</p>
          </div>
        )}
        <button
          onClick={logout}
          className={cn(
            'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-gray-300 hover:bg-gray-800 hover:text-white transition-colors',
            collapsed && 'justify-center',
          )}
          title="Logout"
        >
          <LogOut className="h-4 w-4 shrink-0" />
          {!collapsed && 'Logout'}
        </button>
      </div>
    </aside>
  )
}
