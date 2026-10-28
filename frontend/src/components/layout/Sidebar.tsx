import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Users, MapPin, FileText, UserCheck, ClipboardList,
  Wallet, Leaf, Package, ShoppingCart, Receipt, Truck, Wrench,
  Calendar, BarChart2, Bell, LogOut, ChevronLeft, ChevronRight, X,
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
      'h-screen flex flex-col bg-slate-950/95 backdrop-blur-2xl text-white transition-all duration-300 border-r border-slate-800/80 select-none shadow-2xl',
      collapsed ? 'w-20' : 'w-64',
    )}>
      {/* Header / Brand */}
      <div className="flex h-16 items-center justify-between px-4 border-b border-slate-800/80 shrink-0">
        {!collapsed ? (
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 via-primary-600 to-primary-700 shadow-lg shadow-emerald-900/30 ring-1 ring-white/20">
              <Leaf className="h-5 w-5 text-white" />
            </div>
            <div>
              <span className="text-base font-extrabold tracking-tight text-white block leading-tight">
                ArecaHarvest
              </span>
              <span className="text-[10px] text-emerald-400 font-bold tracking-wider uppercase font-mono">
                Enterprise Agri
              </span>
            </div>
          </div>
        ) : (
          <div className="flex h-10 w-10 mx-auto items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 via-primary-600 to-primary-700 shadow-lg shadow-emerald-900/30 ring-1 ring-white/20">
            <Leaf className="h-5 w-5 text-white" />
          </div>
        )}

        {/* Desktop collapse toggle */}
        {!collapsed && !onClose && (
          <button
            onClick={onToggle}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800/80 transition-colors"
            title="Collapse Sidebar"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}

        {/* Mobile close button */}
        {onClose && (
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800/80 transition-colors"
            title="Close Drawer"
          >
            <X className="h-5 w-5" />
          </button>
        )}
      </div>

      {/* If collapsed on desktop, show mini expand button at top */}
      {collapsed && !onClose && (
        <div className="px-2 pt-2 pb-1 shrink-0 flex justify-center">
          <button
            onClick={onToggle}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800/80 transition-colors"
            title="Expand Sidebar"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Navigation List */}
      <nav className="flex-1 overflow-y-auto py-3 space-y-4 px-3 custom-scrollbar">
        {nav.map(section => (
          <div key={section.section}>
            {!collapsed && (
              <p className="px-3 mb-1.5 text-[10px] font-extrabold uppercase tracking-widest text-slate-400/90 font-mono">
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
                      'group flex items-center gap-3 rounded-xl px-3.5 py-2 text-xs sm:text-sm font-semibold transition-all duration-150 relative',
                      isActive
                        ? 'bg-gradient-to-r from-emerald-600 to-primary-700 text-white shadow-md shadow-emerald-950/40'
                        : 'text-slate-300 hover:bg-slate-800/70 hover:text-white',
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

      {/* User & Status Footer */}
      <div className="border-t border-slate-800/80 p-3 bg-slate-950/80 shrink-0">
        {!collapsed && (
          <div className="px-2.5 py-1.5 mb-2 rounded-xl bg-slate-900/60 border border-slate-800/60">
            <div className="flex items-center justify-between">
              <p className="text-xs font-bold text-white truncate">{user?.fullName || 'User'}</p>
              <span className="text-[9px] px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-300 font-mono font-bold border border-emerald-800/50">
                {user?.roles?.[0]?.replace('ROLE_', '') || 'STAFF'}
              </span>
            </div>
            <div className="flex items-center gap-1.5 mt-1">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
              <p className="text-[10px] text-emerald-400 font-mono tracking-wide">
                SYSTEM ONLINE • H2 DEMO
              </p>
            </div>
          </div>
        )}
        <button
          onClick={logout}
          className={cn(
            'flex w-full items-center gap-3 rounded-xl px-3 py-2 text-xs sm:text-sm font-semibold text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 transition-colors',
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
