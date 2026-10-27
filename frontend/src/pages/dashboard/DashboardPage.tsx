import { useEffect, useState } from 'react'
import {
  Tractor, Users, Leaf, Wallet, Receipt, ShoppingCart,
  AlertCircle, Package, TrendingUp, Calendar, ArrowRight,
  PieChart as PieIcon, BarChart3, Clock,
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

const FIELD_COLORS = ['#16a34a', '#2563eb', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#10b981', '#f97316']

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
          dashboardApi.getKpis(),
          dashboardApi.getHarvestTrend(thirtyDaysAgo, today),
          dashboardApi.getRevenueVsExpenses(sixMonthsAgo, today),
          dashboardApi.getFieldWiseHarvest(sixtyDaysAgo, today),
          dashboardApi.getSalesByBuyer(sixtyDaysAgo, today),
          dashboardApi.getWageTrend(sixtyDaysAgo, today),
          dashboardApi.getPendingPayments(),
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

        setFieldWiseHarvest(
          Object.entries(fieldData)
            .map(([field, kg]) => ({ field, kg: Number(kg) }))
            .sort((a, b) => b.kg - a.kg)
        )

        setSalesByBuyer(
          Object.entries(buyerData)
            .map(([buyer, amount]) => ({ buyer, amount: Number(amount) }))
            .sort((a, b) => b.amount - a.amount)
        )

        setWageTrend(
          Object.entries(wagesData).map(([date, amount]) => ({
            date: date.slice(5),
            amount: Number(amount),
          }))
        )

        setPendingPayments({
          workerOutstanding: Number(pendingData?.workerOutstanding ?? 0),
          buyerOutstanding: Number(pendingData?.buyerOutstanding ?? 0),
        })
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

      {/* Row 1 Charts */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Harvest Trend */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-700">Daily Harvest — Last 30 Days (KG)</h3>
            <span className="text-xs text-gray-400">Total volume</span>
          </div>
          {harvestTrend.length > 0 ? (
            <ResponsiveContainer width="100%" height={230}>
              <AreaChart data={harvestTrend} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="harvestGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#16a34a" stopOpacity={0.2} />
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
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-700">Revenue vs Expenses — Monthly (₹)</h3>
            <span className="text-xs text-gray-400">P&L Comparison</span>
          </div>
          {revenueExpenses.length > 0 ? (
            <ResponsiveContainer width="100%" height={230}>
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

      {/* Row 2 Charts — Field-wise Harvest & Sales by Buyer */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Field-wise Harvest */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Leaf className="h-4 w-4 text-green-600" />
              <h3 className="text-sm font-semibold text-gray-700">Field-wise Harvest (Last 60 Days)</h3>
            </div>
            <span className="text-xs text-gray-400">{fieldWiseHarvest.length} fields active</span>
          </div>
          {fieldWiseHarvest.length > 0 ? (
            <ResponsiveContainer width="100%" height={230}>
              <BarChart data={fieldWiseHarvest} margin={{ top: 5, right: 15, left: -5, bottom: 25 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="field" tick={{ fontSize: 10 }} angle={-20} textAnchor="end" interval={0} />
                <YAxis tick={{ fontSize: 10 }} />
                <Tooltip formatter={(v: number) => [`${v.toFixed(2)} KG`, 'Total Harvest']} />
                <Bar dataKey="kg" name="Harvested (KG)" radius={[4, 4, 0, 0]}>
                  {fieldWiseHarvest.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={FIELD_COLORS[index % FIELD_COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No field harvest data in period</div>
          )}
        </div>

        {/* Sales by Buyer */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <ShoppingCart className="h-4 w-4 text-blue-600" />
              <h3 className="text-sm font-semibold text-gray-700">Sales & Exposure by Buyer (₹)</h3>
            </div>
            <span className="text-xs text-gray-400">Total volume</span>
          </div>
          {salesByBuyer.length > 0 ? (
            <ResponsiveContainer width="100%" height={230}>
              <BarChart data={salesByBuyer} layout="vertical" margin={{ top: 5, right: 20, left: 40, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis type="number" tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <YAxis dataKey="buyer" type="category" tick={{ fontSize: 10 }} width={80} />
                <Tooltip formatter={(v: number) => [formatCurrency(v), 'Volume / Outstanding']} />
                <Bar dataKey="amount" fill="#3b82f6" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No buyer transactions recorded</div>
          )}
        </div>
      </div>

      {/* Row 3 — Weekly Wage Trend & Pending Payments Overview */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Weekly Wage Trend */}
        <div className="card xl:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Wallet className="h-4 w-4 text-amber-600" />
              <h3 className="text-sm font-semibold text-gray-700">Weekly Wage Trends (₹)</h3>
            </div>
            <span className="text-xs text-gray-400">Labour expenditure</span>
          </div>
          {wageTrend.length > 0 ? (
            <ResponsiveContainer width="100%" height={210}>
              <AreaChart data={wageTrend} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="wageGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} />
                <YAxis tick={{ fontSize: 10 }} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v: number) => [formatCurrency(v), 'Wages Paid/Accrued']} />
                <Area type="monotone" dataKey="amount" stroke="#f59e0b" strokeWidth={2} fill="url(#wageGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-44 text-gray-400 text-sm">No wage data recorded</div>
          )}
        </div>

        {/* Pending Payments Breakdown */}
        <div className="card flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-semibold text-gray-700">Pending Balances</h3>
              <span className="badge badge-amber text-xs">Action Needed</span>
            </div>

            <div className="space-y-4">
              <div className="p-3 bg-red-50 border border-red-100 rounded-lg">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-medium text-red-700 flex items-center gap-1.5">
                    <Users className="h-3.5 w-3.5" /> Worker Outstanding Wages
                  </span>
                  <Link to="/wages" className="text-xs text-red-600 hover:underline flex items-center gap-0.5">
                    Pay <ArrowRight className="h-3 w-3" />
                  </Link>
                </div>
                <div className="text-xl font-bold text-red-800">
                  {formatCurrency(pendingPayments?.workerOutstanding ?? kpis.pendingWorkerWages)}
                </div>
                <p className="text-xs text-red-600 mt-1">Owed across all active harvesters</p>
              </div>

              <div className="p-3 bg-amber-50 border border-amber-100 rounded-lg">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-medium text-amber-700 flex items-center gap-1.5">
                    <ShoppingCart className="h-3.5 w-3.5" /> Buyer Outstanding Receivables
                  </span>
                  <Link to="/sales" className="text-xs text-amber-600 hover:underline flex items-center gap-0.5">
                    Collect <ArrowRight className="h-3 w-3" />
                  </Link>
                </div>
                <div className="text-xl font-bold text-amber-800">
                  {formatCurrency(pendingPayments?.buyerOutstanding ?? kpis.buyerOutstanding)}
                </div>
                <p className="text-xs text-amber-600 mt-1">Pending payments due from buyers</p>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-gray-100 text-xs text-gray-500 flex justify-between items-center">
            <span>Net Working Capital Delta:</span>
            <span className={`font-semibold ${
              (pendingPayments?.buyerOutstanding ?? 0) >= (pendingPayments?.workerOutstanding ?? 0)
                ? 'text-green-600' : 'text-red-600'
            }`}>
              {formatCurrency((pendingPayments?.buyerOutstanding ?? 0) - (pendingPayments?.workerOutstanding ?? 0))}
            </span>
          </div>
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
            { label: '📊 View Reports',   to: '/reports'    },
          ].map(q => (
            <Link key={q.to} to={q.to} className="btn-primary btn btn-sm">{q.label}</Link>
          ))}
        </div>
      </div>
    </div>
  )
}

