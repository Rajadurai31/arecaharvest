import { useState, useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import TopBar from './TopBar'
import BottomNav from './BottomNav'
import { cn } from '@/utils'

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()

  // Automatically close mobile menu on route change
  useEffect(() => {
    setMobileOpen(false)
  }, [location.pathname])

  return (
    <div className="min-h-screen bg-slate-50/70 dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col selection:bg-primary-500 selection:text-white">
      {/* Desktop sidebar - fixed to viewport left */}
      <div className="hidden lg:block fixed inset-y-0 left-0 z-40">
        <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(c => !c)} />
      </div>

      {/* Mobile drawer sidebar with smooth backdrop and slide-in */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden flex">
          <div
            className="fixed inset-0 bg-slate-950/70 backdrop-blur-sm transition-opacity animate-fade-in"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
          <div className="relative z-50 w-72 max-w-[85vw] h-full shadow-2xl animate-slide-up">
            <Sidebar collapsed={false} onToggle={() => setMobileOpen(false)} onClose={() => setMobileOpen(false)} />
          </div>
        </div>
      )}

      {/* Main Content Area - dynamically adjusts to desktop sidebar state & width */}
      <div className={cn(
        'transition-all duration-300 min-h-screen flex flex-col flex-1',
        collapsed ? 'lg:pl-20' : 'lg:pl-64',
      )}>
        <TopBar
          onMenuClick={() => {
            if (window.innerWidth < 1024) setMobileOpen(o => !o)
            else setCollapsed(c => !c)
          }}
          sidebarCollapsed={collapsed}
        />
        <main className="flex-1 p-3.5 sm:p-5 md:p-6 lg:p-8 safe-bottom-padding">
          <div className="w-full max-w-[1720px] mx-auto space-y-6">
            <Outlet />
          </div>
        </main>
      </div>

      {/* Floating Island Mobile Bottom Navigation */}
      <BottomNav onMenuClick={() => setMobileOpen(true)} />
    </div>
  )
}
