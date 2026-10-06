import { useState } from 'react'
import { dashboardApi, expensesApi, wagesApi, workersApi } from '@/api'
import { formatCurrency, formatKg, monthStartISO, todayISO } from '@/utils'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts'
import { FileText } from 'lucide-react'

const COLORS = ['#16a34a', '#f59e0b', '#3b82f6', '#ef4444', '#8b5cf6', '#ec4899']

export default function ReportsPage() {
  const [fromDate, setFromDate] = useState(monthStartISO())
  const [toDate, setToDate] = useState(todayISO())
  const [report, setReport] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  const generate = async () => {
    setLoading(true)
    try {
      const [rev, harvest, wages] = await Promise.all([
        dashboardApi.getRevenueVsExpenses(fromDate, toDate),
        dashboardApi.getHarvestTrend(fromDate, toDate),
        dashboardApi.getWageTrend(fromDate, toDate),
      ])
      setReport({ rev, harvest, wages })
    } finally { setLoading(false) }
  }

  const revData = report ? Object.entries(report.rev).map(([month, vals]: any) => ({
    month, revenue: +vals.revenue, expenses: +vals.expenses, profit: +vals.profit,
  })) : []

  const harvestData = report ? Object.entries(report.harvest).slice(-14).map(([date, kg]: any) => ({
    date: date.slice(5), kg: +kg,
  })) : []

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div><h1>Reports</h1><p className="text-sm text-gray-500 mt-0.5">Business analytics and financial overview</p></div>
      </div>

      {/* Date range */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">Report Period</h3>
        <div className="flex flex-wrap items-end gap-4">
          <div>
            <label className="label">From</label>
            <input type="date" className="input" value={fromDate} onChange={e => setFromDate(e.target.value)} />
          </div>
          <div>
            <label className="label">To</label>
            <input type="date" className="input" value={toDate} onChange={e => setToDate(e.target.value)} />
          </div>
          <button className="btn-primary btn" onClick={generate} disabled={loading}>
            <FileText className="h-4 w-4" />
            {loading ? 'Generating…' : 'Generate Report'}
          </button>
        </div>
      </div>

      {loading && <LoadingSpinner center />}

      {report && !loading && (
        <div className="space-y-6">
          {/* Revenue vs Expenses */}
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">Revenue vs Expenses vs Profit</h3>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={revData} margin={{ top: 5, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
                <Legend wrapperStyle={{ fontSize: 11 }} />
                <Bar dataKey="revenue"  name="Revenue"  fill="#16a34a" radius={[4,4,0,0]} />
                <Bar dataKey="expenses" name="Expenses" fill="#f59e0b" radius={[4,4,0,0]} />
                <Bar dataKey="profit"   name="Profit"   fill="#3b82f6" radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Harvest trend */}
          {harvestData.length > 0 && (
            <div className="card">
              <h3 className="text-sm font-semibold text-gray-700 mb-4">Daily Harvest Quantity (KG)</h3>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={harvestData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="date" tick={{ fontSize: 10 }} interval={2} />
                  <YAxis tick={{ fontSize: 10 }} />
                  <Tooltip formatter={(v: number) => [`${v.toFixed(3)} KG`, 'Harvest']} />
                  <Bar dataKey="kg" fill="#16a34a" radius={[3,3,0,0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Summary table */}
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">Financial Summary</h3>
            <table className="w-full">
              <thead><tr className="border-b"><th className="table-th">Period</th><th className="table-th text-right">Revenue</th><th className="table-th text-right">Expenses</th><th className="table-th text-right">Profit</th></tr></thead>
              <tbody className="divide-y divide-gray-50">
                {revData.map(r => (
                  <tr key={r.month} className="hover:bg-gray-50">
                    <td className="table-td font-medium">{r.month}</td>
                    <td className="table-td text-right text-green-700">{formatCurrency(r.revenue)}</td>
                    <td className="table-td text-right text-amber-700">{formatCurrency(r.expenses)}</td>
                    <td className={`table-td text-right font-semibold ${r.profit >= 0 ? 'text-primary-700' : 'text-red-600'}`}>{formatCurrency(r.profit)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!report && !loading && (
        <div className="card flex flex-col items-center justify-center py-16 text-center">
          <FileText className="h-12 w-12 text-gray-300 mb-3" />
          <p className="text-gray-500">Select a date range and click Generate Report</p>
        </div>
      )}
    </div>
  )
}
