import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Users, MapPin, FileText, UserCheck, ClipboardList,
  Wallet, Leaf, Package, ShoppingCart, Receipt, Truck, Wrench,
  Calendar, BarChart2, Bell, LogOut, ChevronLeft, X,
} from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { cn } from '@/utils'

const nav = [
  { section: 'Overview', items: [
    { to: '/',         icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/schedule', icon: Calendar,        label: 'Schedule'  },
  ]},
  { section: 'Parties', items: [
    { to: '/owners',  icon: Users,       label: 'Land Owners'  },
    { to: '/workers', icon: UserCheck,   label: 'Workers'      },
    { to: '/buyers',  icon: ShoppingCart, label: 'Buyers'      },
  ]},
  { section: 'Estate', items: [
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
    { to: '/sales',    icon: ShoppingCart, label: 'Sales'    },
    { to: '/expenses', icon: Receipt,      label: 'Expenses' },
  ]},
  { section: 'Fleet & Tools', items: [
    { to: '/vehicles',  icon: Truck,   label: 'Vehicles'  },
    { to: '/equipment', icon: Wrench,  label: 'Equipment' },
  ]},
  { section: 'Analytics', items: [
    { to: '/reports',       icon: BarChart2, label: 'Reports'       },
    { to: '/notifications', icon: Bell,      label: 'Notifications' },
  ]},
]

interface Props {
  collapsed: boolean
  onToggle: () => void
  onClose?: () => void
}

export default function Sidebar({ collapsed, onToggle, onClose }: Props) {
  const { user, logout } = useAuth()

  return (
    <aside className={cn(
      'h-full flex flex-col bg-gradient-to-b from-gray-950 via-slate-900 to-gray-950 text-white transition-all duration-300 border-r border-gray-800/80 select-none',
      collapsed ? 'w-16' : 'w-60',
    )}>
      {/* Header / Brand */}
      <div className="flex h-16 items-center justify-between px-4 border-b border-gray-800/80">
        {!collapsed ? (
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-glow">
              <Leaf className="h-5 w-5 text-white" />
            </div>
            <div>
              <span className="text-sm font-bold tracking-tight text-white block leading-tight">ArecaHarvest</span>
              <span className="text-[10px] text-primary-400 font-medium tracking-wide">ENTERPRISE AGRI</span>
            </div>
          </div>
        ) : (
          <div className="flex h-9 w-9 mx-auto items-center justify-center rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-glow">
            <Leaf className="h-5 w-5 text-white" />
          </div>
        )}

        {/* Desktop collapse toggle */}
        {!collapsed && !onClose && (
          <button
            onClick={onToggle}
            className="p-1.5 rounded-lg text-gray-400 hover:text-white hover:bg-gray-800/60 transition-colors"
            title="Collapse Sidebar"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}

        {/* Mobile close button */}
        {onClose && (
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-gray-400 hover:text-white hover:bg-gray-800/60 transition-colors"
            title="Close Drawer"
          >
            <X className="h-5 w-5" />
          </button>
        )}
      </div>

      {/* Navigation List */}
      <nav className="flex-1 overflow-y-auto py-3 space-y-4 px-2.5">
        {nav.map(section => (
          <div key={section.section}>
            {!collapsed && (
              <p className="px-3 mb-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400/80">
                {section.section}
              </p>
            )}
            <ul className="space-y-1">
              {section.items.map(item => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.to === '/'}
                    onClick={() => onClose?.()}
                    className={({ isActive }) => cn(
                      'group flex items-center gap-3 rounded-xl px-3 py-2 text-xs sm:text-sm font-medium transition-all duration-150',
                      isActive
                        ? 'bg-gradient-to-r from-primary-600 to-primary-700 text-white shadow-sm'
                        : 'text-gray-300 hover:bg-gray-800/70 hover:text-white',
                      collapsed && 'justify-center px-0 py-2.5',
                    )}
                    title={collapsed ? item.label : undefined}
                  >
                    <item.icon className={cn(
                      'h-4 w-4 shrink-0 transition-transform group-hover:scale-110',
                      collapsed && 'h-5 w-5',
                    )} />
                    {!collapsed && <span className="truncate">{item.label}</span>}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      {/* User Footer */}
      <div className="border-t border-gray-800/80 p-3 bg-gray-950/60">
        {!collapsed && (
          <div className="px-2.5 py-1.5 mb-1.5">
            <p className="text-xs font-semibold text-white truncate">{user?.fullName || 'User'}</p>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
              <p className="text-[10px] text-gray-400 uppercase tracking-wider font-mono">
                {user?.roles?.[0]?.replace('ROLE_', '') || 'STAFF'}
              </p>
            </div>
          </div>
        )}
        <button
          onClick={logout}
          className={cn(
            'flex w-full items-center gap-3 rounded-xl px-3 py-2 text-xs sm:text-sm text-red-400 hover:bg-red-500/10 hover:text-red-300 transition-colors',
            collapsed && 'justify-center px-0',
          )}
          title="Sign Out"
        >
          <LogOut className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Sign Out</span>}
        </button>
      </div>
    </aside>
  )
}

