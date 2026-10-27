import { useEffect, useState } from 'react'
import {
  Tractor, Users, Leaf, Wallet, Receipt, ShoppingCart,
  Package, TrendingUp, Calendar, ArrowRight,
  Plus, CalendarCheck, BarChart3, AlertCircle, Sparkles,
} from 'lucide-react'
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend, Cell,
} from 'recharts'
import { Link } from 'react-router-dom'
import { dashboardApi } from '@/api'
import type { DashboardKpis } from '@/types'
import StatCard from '@/components/common/StatCard'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import { formatCurrency, formatKg, todayISO } from '@/utils'

const FIELD_COLORS = ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#14b8a6', '#f97316']

export default function DashboardPage() {
  const [kpis, setKpis] = useState<DashboardKpis | null>(null)
  const [harvestTrend, setHarvestTrend] = useState<{ date: string; kg: number }[]>([])
  const [revenueExpenses, setRevenueExpenses] = useState<{ month: string; revenue: number; expenses: number; profit: number }[]>([])
  const [fieldWiseHarvest, setFieldWiseHarvest] = useState<{ field: string; kg: number }[]>([])
  const [salesByBuyer, setSalesByBuyer] = useState<{ buyer: string; amount: number }[]>([])
  const [wageTrend, setWageTrend] = useState<{ date: string; amount: number }[]>([])
  const [pendingPayments, setPendingPayments] = useState<{ workerOutstanding: number; buyerOutstanding: number } | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const today = todayISO()
        const thirtyDaysAgo = new Date(Date.now() - 30 * 864e5).toISOString().split('T')[0]
        const sixMonthsAgo = new Date(Date.now() - 180 * 864e5).toISOString().split('T')[0]
        const sixtyDaysAgo = new Date(Date.now() - 60 * 864e5).toISOString().split('T')[0]

        const [kpisData, trend, rev, fieldData, buyerData, wagesData, pendingData] = await Promise.all([
          dashboardApi.getKpis().catch(() => null),
          dashboardApi.getHarvestTrend(thirtyDaysAgo, today).catch(() => ({})),
          dashboardApi.getRevenueVsExpenses(sixMonthsAgo, today).catch(() => ({})),
          dashboardApi.getFieldWiseHarvest(sixtyDaysAgo, today).catch(() => ({})),
          dashboardApi.getSalesByBuyer(sixtyDaysAgo, today).catch(() => ({})),
          dashboardApi.getWageTrend(sixtyDaysAgo, today).catch(() => ({})),
          dashboardApi.getPendingPayments().catch(() => null),
        ])

        if (kpisData) {
          setKpis(kpisData)
        }

        setHarvestTrend(
          Object.entries(trend ?? {}).map(([date, kg]) => ({
            date: date.slice(5),
            kg: Number(kg ?? 0),
          }))
        )

        setRevenueExpenses(
          Object.entries(rev ?? {}).map(([month, vals]) => ({
            month,
            revenue: Number(vals?.revenue ?? 0),
            expenses: Number(vals?.expenses ?? 0),
            profit: Number(vals?.profit ?? 0),
          }))
        )

        setFieldWiseHarvest(
          Object.entries(fieldData ?? {})
            .map(([field, kg]) => ({ field, kg: Number(kg ?? 0) }))
            .sort((a, b) => b.kg - a.kg)
        )

        setSalesByBuyer(
          Object.entries(buyerData ?? {})
            .map(([buyer, amount]) => ({ buyer, amount: Number(amount ?? 0) }))
            .sort((a, b) => b.amount - a.amount)
        )

        setWageTrend(
          Object.entries(wagesData ?? {}).map(([date, amount]) => ({
            date: date.slice(5),
            amount: Number(amount ?? 0),
          }))
        )

        setPendingPayments({
          workerOutstanding: Number(pendingData?.workerOutstanding ?? 0),
          buyerOutstanding: Number(pendingData?.buyerOutstanding ?? 0),
        })
      } catch (err) {
        console.error('Failed to load dashboard data:', err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <LoadingSpinner center size="lg" />
  if (!kpis) return (
    <div className="card text-center py-16 my-8 max-w-md mx-auto">
      <AlertCircle className="h-10 w-10 text-amber-500 mx-auto mb-3" />
      <h3 className="text-base font-bold text-gray-800 dark:text-gray-100">Connecting to Farm Data</h3>
      <p className="text-xs text-gray-500 mt-1">Please ensure backend server is operational.</p>
    </div>
  )

  const monthProfit = (kpis.monthRevenue ?? 0) - (kpis.monthExpenses ?? 0)

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Welcome & Quick Actions Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 p-4 sm:p-5 rounded-2xl bg-gradient-to-r from-primary-900 via-primary-800 to-slate-900 text-white shadow-lg relative overflow-hidden">
        <div className="relative z-10">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary-700/60 text-primary-200 text-xs font-medium mb-2 border border-primary-500/30">
            <Sparkles className="h-3 w-3 text-emerald-300" />
            <span>Areca Operations 2026</span>
          </div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-white">Harvest Central Command</h2>
          <p className="text-xs sm:text-sm text-primary-200/90 mt-0.5 flex items-center gap-1.5">
            <Calendar className="h-3.5 w-3.5 text-primary-300" />
            {new Date().toLocaleDateString('en-IN', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}
            <span className="hidden sm:inline">• {kpis.activeFields} Active Plantation Fields</span>
          </p>
        </div>

        {/* Quick action pill shortcuts */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0 scrollbar-none z-10">
          <Link
            to="/harvest"
            className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-white text-xs font-semibold shadow-sm active:scale-95 transition-all whitespace-nowrap"
          >
            <Leaf className="h-3.5 w-3.5" />
            <span>+ Harvest</span>
          </Link>
          <Link
            to="/attendance"
            className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-white/15 hover:bg-white/25 text-white text-xs font-semibold border border-white/20 active:scale-95 transition-all whitespace-nowrap"
          >
            <CalendarCheck className="h-3.5 w-3.5" />
            <span>+ Attendance</span>
          </Link>
          <Link
            to="/sales"
            className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-white/15 hover:bg-white/25 text-white text-xs font-semibold border border-white/20 active:scale-95 transition-all whitespace-nowrap"
          >
            <ShoppingCart className="h-3.5 w-3.5" />
            <span>+ Sale</span>
          </Link>
          <Link
            to="/expenses"
            className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-white/15 hover:bg-white/25 text-white text-xs font-semibold border border-white/20 active:scale-95 transition-all whitespace-nowrap"
          >
            <Receipt className="h-3.5 w-3.5" />
            <span>+ Expense</span>
          </Link>
        </div>
      </div>

      {/* Section 1: Today's KPIs */}
      <section>
        <div className="flex items-center justify-between mb-3 px-1">
          <h3 className="text-xs sm:text-sm font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
            Daily Operations
          </h3>
          <span className="text-[11px] font-medium text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" /> Live Metrics
          </span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-5 gap-3 sm:gap-4">
          <StatCard label="Active Fields"    value={kpis.activeFields}                  icon={Tractor}  color="green" />
          <StatCard label="Working Today"    value={kpis.workersWorkingToday}           icon={Users}    color="blue"  />
          <StatCard label="Today's Harvest"  value={formatKg(kpis.todayHarvestKg)}     icon={Leaf}     color="green" />
          <StatCard label="Today's Wages"    value={formatCurrency(kpis.todayWorkerWage)} icon={Wallet} color="amber" />
          <StatCard label="Today's Expenses" value={formatCurrency(kpis.todayExpenses)}   icon={Receipt} color="red"   />
        </div>
      </section>

      {/* Section 2: Financial & Stock Position */}
      <section>
        <div className="flex items-center justify-between mb-3 px-1">
          <h3 className="text-xs sm:text-sm font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
            Financial & Stock Overview
          </h3>
          <Link to="/reports" className="text-xs text-primary-600 dark:text-primary-400 hover:underline flex items-center gap-1">
            Full Reports <ArrowRight className="h-3 w-3" />
          </Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-5 gap-3 sm:gap-4">
          <StatCard label="Pending Wages"     value={formatCurrency(kpis.pendingWorkerWages)} icon={AlertCircle}  color="red"   />
          <StatCard label="Buyer Outstanding" value={formatCurrency(kpis.buyerOutstanding)}   icon={ShoppingCart} color="amber" />
          <StatCard label="In Inventory"      value={formatKg(kpis.currentInventoryKg)}        icon={Package}      color="blue"  />
          <StatCard label="Month Revenue"     value={formatCurrency(kpis.monthRevenue)}         icon={TrendingUp}   color="green" />
          <StatCard
            label="Month Profit"
            value={formatCurrency(monthProfit)}
            icon={TrendingUp}
            color={monthProfit >= 0 ? 'green' : 'red'}
            subtitle="Rev − Exp"
          />
        </div>
      </section>

      {/* Section 3: Visual Analytics Charts Row 1 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6">
        {/* Harvest Trend */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">
                <Leaf className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                Daily Harvest Volume (Last 30 Days)
              </h3>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">Quantity harvested across all fields</p>
            </div>
            <span className="badge badge-green text-xs">KG Total</span>
          </div>

          {harvestTrend.length > 0 ? (
            <ResponsiveContainer width="100%" height={210}>
              <AreaChart data={harvestTrend} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
                <defs>
                  <linearGradient id="harvestGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" opacity={0.5} />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} interval="preserveStartEnd" />
                <YAxis tick={{ fontSize: 10 }} />
                <Tooltip
                  formatter={(v: number) => [`${v.toFixed(2)} KG`, 'Harvest']}
                  contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="kg" stroke="#10b981" strokeWidth={2.5} fill="url(#harvestGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400 text-sm">
              <Leaf className="h-8 w-8 text-gray-300 dark:text-gray-600 mb-2" />
              <span>No harvest data in this period</span>
            </div>
          )}
        </div>

        {/* Revenue vs Expenses */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">
                <BarChart3 className="h-4 w-4 text-amber-500" />
                Revenue vs Expenses (Monthly)
              </h3>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">Operating profit margin comparison</p>
            </div>
            <span className="badge badge-amber text-xs">P&L (₹)</span>
          </div>

          {revenueExpenses.length > 0 ? (
            <ResponsiveContainer width="100%" height={210}>
              <BarChart data={revenueExpenses} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" opacity={0.5} />
                <XAxis dataKey="month" tick={{ fontSize: 10 }} />
                <YAxis tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <Tooltip
                  formatter={(v: number) => [`₹${v.toLocaleString('en-IN')}`, '']}
                  contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Legend wrapperStyle={{ fontSize: 11 }} />
                <Bar dataKey="revenue"  name="Revenue"  fill="#10b981" radius={[6, 6, 0, 0]} />
                <Bar dataKey="expenses" name="Expenses" fill="#f59e0b" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400 text-sm">
              <BarChart3 className="h-8 w-8 text-gray-300 dark:text-gray-600 mb-2" />
              <span>No financial history recorded yet</span>
            </div>
          )}
        </div>
      </div>

      {/* Section 4: Visual Analytics Charts Row 2 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6">
        {/* Field-wise Harvest */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">
                <Tractor className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                Field Yield Distribution
              </h3>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">Total yield per plantation (Last 60 Days)</p>
            </div>
            <span className="badge badge-gray text-xs">{fieldWiseHarvest.length} Fields</span>
          </div>

          {fieldWiseHarvest.length > 0 ? (
            <ResponsiveContainer width="100%" height={210}>
              <BarChart data={fieldWiseHarvest} margin={{ top: 5, right: 15, left: -10, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" opacity={0.5} />
                <XAxis dataKey="field" tick={{ fontSize: 10 }} angle={-15} textAnchor="end" interval={0} />
                <YAxis tick={{ fontSize: 10 }} />
                <Tooltip
                  formatter={(v: number) => [`${v.toFixed(2)} KG`, 'Total Yield']}
                  contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Bar dataKey="kg" name="Harvested (KG)" radius={[6, 6, 0, 0]}>
                  {fieldWiseHarvest.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={FIELD_COLORS[index % FIELD_COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400 text-sm">
              <Tractor className="h-8 w-8 text-gray-300 dark:text-gray-600 mb-2" />
              <span>No field harvest data in period</span>
            </div>
          )}
        </div>

        {/* Sales by Buyer */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">
                <ShoppingCart className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                Buyer Volume & Balances
              </h3>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">Top trader transactions & receivables</p>
            </div>
            <span className="badge badge-blue text-xs">Buyers</span>
          </div>

          {salesByBuyer.length > 0 ? (
            <ResponsiveContainer width="100%" height={210}>
              <BarChart data={salesByBuyer} layout="vertical" margin={{ top: 5, right: 20, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" opacity={0.5} />
                <XAxis type="number" tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <YAxis dataKey="buyer" type="category" tick={{ fontSize: 10 }} width={90} />
                <Tooltip
                  formatter={(v: number) => [formatCurrency(v), 'Outstanding']}
                  contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Bar dataKey="amount" fill="#3b82f6" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400 text-sm">
              <ShoppingCart className="h-8 w-8 text-gray-300 dark:text-gray-600 mb-2" />
              <span>No buyer volume recorded</span>
            </div>
          )}
        </div>
      </div>

      {/* Section 5: Pending Balances & Action Hub */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">
        {/* Weekly Wage Trend */}
        <div className="card xl:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">
                <Wallet className="h-4 w-4 text-amber-500" />
                Weekly Wage Outlay
              </h3>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">Harvester labour cost trend</p>
            </div>
            <span className="badge badge-amber text-xs">Labour</span>
          </div>

          {wageTrend.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <AreaChart data={wageTrend} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="wageGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" opacity={0.5} />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} />
                <YAxis tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <Tooltip
                  formatter={(v: number) => [formatCurrency(v), 'Wages']}
                  contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="amount" stroke="#f59e0b" strokeWidth={2.5} fill="url(#wageGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex flex-col items-center justify-center h-44 text-gray-400 text-sm">
              <Wallet className="h-8 w-8 text-gray-300 dark:text-gray-600 mb-2" />
              <span>No wage disbursements recorded</span>
            </div>
          )}
        </div>

        {/* Pending Actions Balance Card */}
        <div className="card flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-bold text-gray-900 dark:text-white">Actionable Balances</h3>
              <span className="badge badge-amber text-xs">Action Needed</span>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 bg-red-50/80 dark:bg-red-950/30 border border-red-100 dark:border-red-900/40 rounded-xl">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-semibold text-red-700 dark:text-red-400 flex items-center gap-1.5">
                    <Users className="h-3.5 w-3.5" /> Worker Wages Due
                  </span>
                  <Link to="/wages" className="text-xs font-bold text-red-600 dark:text-red-300 hover:underline flex items-center gap-0.5">
                    Pay Now <ArrowRight className="h-3 w-3" />
                  </Link>
                </div>
                <div className="text-xl font-extrabold text-red-800 dark:text-red-300 tabular-nums">
                  {formatCurrency(pendingPayments?.workerOutstanding ?? kpis.pendingWorkerWages)}
                </div>
                <p className="text-[11px] text-red-600/80 dark:text-red-400/80 mt-0.5">Unsettled harvester labour balance</p>
              </div>

              <div className="p-3.5 bg-amber-50/80 dark:bg-amber-950/30 border border-amber-100 dark:border-amber-900/40 rounded-xl">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-semibold text-amber-700 dark:text-amber-400 flex items-center gap-1.5">
                    <ShoppingCart className="h-3.5 w-3.5" /> Buyer Receivables
                  </span>
                  <Link to="/sales" className="text-xs font-bold text-amber-600 dark:text-amber-300 hover:underline flex items-center gap-0.5">
                    Collect <ArrowRight className="h-3 w-3" />
                  </Link>
                </div>
                <div className="text-xl font-extrabold text-amber-800 dark:text-amber-300 tabular-nums">
                  {formatCurrency(pendingPayments?.buyerOutstanding ?? kpis.buyerOutstanding)}
                </div>
                <p className="text-[11px] text-amber-600/80 dark:text-amber-400/80 mt-0.5">Due from nut buyers & merchants</p>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-gray-100 dark:border-gray-800 flex justify-between items-center text-xs">
            <span className="text-gray-500 dark:text-gray-400 font-medium">Net Working Delta:</span>
            <span className={`font-bold tabular-nums text-sm ${
              (pendingPayments?.buyerOutstanding ?? 0) >= (pendingPayments?.workerOutstanding ?? 0)
                ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'
            }`}>
              {formatCurrency((pendingPayments?.buyerOutstanding ?? 0) - (pendingPayments?.workerOutstanding ?? 0))}
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}


