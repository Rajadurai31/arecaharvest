import { useEffect, useState } from 'react'
import {
  Tractor, Users, Leaf, Wallet, Receipt, ShoppingCart,
  AlertCircle, Package, TrendingUp, Calendar,
} from 'lucide-react'
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import { dashboardApi } from '@/api'
import type { DashboardKpis } from '@/types'
import StatCard from '@/components/common/StatCard'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import { formatCurrency, formatKg, todayISO, monthStartISO } from '@/utils'

export default function DashboardPage() {
  const [kpis, setKpis] = useState<DashboardKpis | null>(null)
  const [harvestTrend, setHarvestTrend] = useState<{ date: string; kg: number }[]>([])
  const [revenueExpenses, setRevenueExpenses] = useState<{ month: string; revenue: number; expenses: number; profit: number }[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const today = todayISO()
        const month = monthStartISO()
        const thirtyDaysAgo = new Date(Date.now() - 30 * 864e5).toISOString().split('T')[0]
        const sixMonthsAgo = new Date(Date.now() - 180 * 864e5).toISOString().split('T')[0]

        const [kpisData, trend, rev] = await Promise.all([
          dashboardApi.getKpis(),
          dashboardApi.getHarvestTrend(thirtyDaysAgo, today),
          dashboardApi.getRevenueVsExpenses(sixMonthsAgo, today),
        ])

        setKpis(kpisData)

        setHarvestTrend(
          Object.entries(trend).map(([date, kg]) => ({
            date: date.slice(5),
            kg: Number(kg),
          }))
        )

        setRevenueExpenses(
          Object.entries(rev).map(([month, vals]) => ({
            month,
            revenue: Number(vals.revenue ?? 0),
            expenses: Number(vals.expenses ?? 0),
            profit: Number(vals.profit ?? 0),
          }))
        )
      } catch { /* handled by interceptor */ }
      finally { setLoading(false) }
    }
    load()
  }, [])

  if (loading) return <LoadingSpinner center size="lg" />
  if (!kpis) return <p className="text-gray-500 text-center py-16">Could not load dashboard data.</p>

  const monthProfit = (kpis.monthRevenue ?? 0) - (kpis.monthExpenses ?? 0)

  return (
    <div className="space-y-6">
      {/* Date badge */}
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-gray-400" />
        <span className="text-sm text-gray-500">
          {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </span>
      </div>

      {/* Today's KPIs */}
      <section>
        <h2 className="text-base font-semibold text-gray-700 mb-3">Today's Overview</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-5 gap-4">
          <StatCard label="Active Fields"      value={kpis.activeFields}                icon={Tractor}      color="green" />
          <StatCard label="Working Today"      value={kpis.workersWorkingToday}         icon={Users}        color="blue" />
          <StatCard label="Today's Harvest"    value={formatKg(kpis.todayHarvestKg)}   icon={Leaf}         color="green" />
          <StatCard label="Today's Wages"      value={formatCurrency(kpis.todayWorkerWage)}  icon={Wallet}  color="amber" />
          <StatCard label="Today's Expenses"   value={formatCurrency(kpis.todayExpenses)}     icon={Receipt} color="red" />
        </div>
      </section>

      {/* Financial KPIs */}
      <section>
        <h2 className="text-base font-semibold text-gray-700 mb-3">Financial Position</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-5 gap-4">
          <StatCard label="Pending Wages"    value={formatCurrency(kpis.pendingWorkerWages)} icon={AlertCircle}  color="red"   />
          <StatCard label="Buyer Outstanding" value={formatCurrency(kpis.buyerOutstanding)}  icon={ShoppingCart} color="amber" />
          <StatCard label="In Inventory"     value={formatKg(kpis.currentInventoryKg)}       icon={Package}      color="blue"  />
          <StatCard label="Month Revenue"    value={formatCurrency(kpis.monthRevenue)}        icon={TrendingUp}   color="green" />
          <StatCard
            label="Month Profit"
            value={formatCurrency(monthProfit)}
            icon={TrendingUp}
            color={monthProfit >= 0 ? 'green' : 'red'}
            subtitle={`Rev − Exp`}
          />
        </div>
      </section>

      {/* Charts */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Harvest Trend */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Daily Harvest — Last 30 Days (KG)</h3>
          {harvestTrend.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={harvestTrend} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="harvestGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#16a34a" stopOpacity={0.15} />
                    <stop offset="95%" stopColor="#16a34a" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} interval={4} />
                <YAxis tick={{ fontSize: 10 }} />
                <Tooltip formatter={(v: number) => [`${v.toFixed(2)} KG`, 'Harvest']} />
                <Area type="monotone" dataKey="kg" stroke="#16a34a" strokeWidth={2} fill="url(#harvestGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No harvest data for this period</div>
          )}
        </div>

        {/* Revenue vs Expenses */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Revenue vs Expenses — Monthly (₹)</h3>
          {revenueExpenses.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={revenueExpenses} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="month" tick={{ fontSize: 10 }} />
                <YAxis tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v: number) => [`₹${v.toLocaleString('en-IN')}`, '']} />
                <Legend wrapperStyle={{ fontSize: 11 }} />
                <Bar dataKey="revenue"  name="Revenue"  fill="#16a34a" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expenses" name="Expenses" fill="#f59e0b" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No data for this period</div>
          )}
        </div>
      </div>

      {/* Quick links */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">Quick Actions</h3>
        <div className="flex flex-wrap gap-2">
          {[
            { label: '+ Mark Attendance', to: '/attendance' },
            { label: '+ Record Harvest',  to: '/harvest'    },
            { label: '+ Add Expense',     to: '/expenses'   },
            { label: '+ Record Sale',     to: '/sales'      },
            { label: '+ Pay Worker',      to: '/wages'      },
          ].map(q => (
            <a key={q.to} href={q.to} className="btn-primary btn btn-sm">{q.label}</a>
          ))}
        </div>
      </div>
    </div>
  )
}
