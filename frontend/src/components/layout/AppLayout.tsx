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
    <div className="min-h-screen bg-slate-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100 flex flex-col selection:bg-primary-500 selection:text-white">
      {/* Desktop sidebar */}
      <div className="hidden lg:block">
        <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(c => !c)} />
      </div>

      {/* Mobile drawer sidebar with smooth backdrop and slide-in */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden flex">
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm transition-opacity animate-fade-in"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
          <div className="relative z-50 w-72 max-w-[85vw] h-full shadow-2xl animate-slide-up">
            <Sidebar collapsed={false} onToggle={() => setMobileOpen(false)} onClose={() => setMobileOpen(false)} />
          </div>
        </div>
      )}

      {/* Main Content Area */}
      <div className={cn(
        'transition-all duration-300 min-h-screen flex flex-col flex-1',
        collapsed ? 'lg:pl-16' : 'lg:pl-60',
      )}>
        <TopBar
          onMenuClick={() => {
            if (window.innerWidth < 1024) setMobileOpen(o => !o)
            else setCollapsed(c => !c)
          }}
          sidebarCollapsed={collapsed}
        />
        <main className="flex-1 p-3.5 sm:p-5 md:p-6 lg:p-8 safe-bottom-padding">
          <div className="max-w-7xl mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>

      {/* Floating Island Mobile Bottom Navigation */}
      <BottomNav onMenuClick={() => setMobileOpen(true)} />
    </div>
  )
}

