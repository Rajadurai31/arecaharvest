import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Phone, MapPin } from 'lucide-react'
import { workersApi, wagesApi } from '@/api'
import type { Worker, WorkerLedgerSummary } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import { formatCurrency, formatDate, workerStatusColor } from '@/utils'

export default function WorkerDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [worker, setWorker] = useState<Worker | null>(null)
  const [ledger, setLedger] = useState<WorkerLedgerSummary | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    Promise.all([
      workersApi.getById(Number(id)),
      wagesApi.getLedger(Number(id)),
    ]).then(([w, l]) => { setWorker(w); setLedger(l) })
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <LoadingSpinner center />
  if (!worker) return <p className="text-center py-16 text-gray-500">Worker not found</p>

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-3">
        <Link to="/workers" className="btn btn-secondary btn-sm px-2"><ArrowLeft className="h-4 w-4" /></Link>
        <div>
          <h1>{worker.name}</h1>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="badge-blue badge">{worker.skillType}</span>
            <span className={workerStatusColor[worker.status]}>{worker.status}</span>
          </div>
        </div>
      </div>

      {/* Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="card space-y-3">
          <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Contact & Info</h3>
          <div className="space-y-2 text-sm">
            <div className="flex items-center gap-2"><Phone className="h-4 w-4 text-gray-400" />{worker.phone}</div>
            {worker.village && <div className="flex items-center gap-2"><MapPin className="h-4 w-4 text-gray-400" />{[worker.village, worker.district].filter(Boolean).join(', ')}</div>}
            <div><span className="text-gray-500">Experience:</span> {worker.experienceYears ?? 0} years</div>
            <div><span className="text-gray-500">Joined:</span> {formatDate(worker.joiningDate)}</div>
            <div><span className="text-gray-500">Daily Wage:</span> <strong>{formatCurrency(worker.dailyWage)}</strong></div>
          </div>
        </div>

        {/* Ledger Summary */}
        {ledger && (
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wide mb-3">Wage Summary</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-sm text-gray-600">Total Earned</span>
                <span className="font-semibold text-gray-900">{formatCurrency(ledger.totalEarned)}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-sm text-gray-600">Total Paid</span>
                <span className="font-semibold text-green-600">{formatCurrency(ledger.totalPaid)}</span>
              </div>
              <div className="flex justify-between items-center py-2 bg-amber-50 rounded-lg px-3">
                <span className="text-sm font-semibold text-amber-800">Outstanding Balance</span>
                <span className="font-bold text-amber-700 text-lg">{formatCurrency(ledger.outstandingBalance)}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Ledger transactions */}
      {ledger && (
        <div className="card">
          <h2 className="mb-4">Wage Ledger ({ledger.transactions.length} recent transactions)</h2>
          {ledger.transactions.length === 0 ? (
            <p className="text-sm text-gray-500">No ledger entries yet</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Date</th>
                  <th className="table-th">Type</th>
                  <th className="table-th">Description</th>
                  <th className="table-th text-right">Amount</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {ledger.transactions.map(t => {
                    const isDebit = ['DAILY_WAGE','OVERTIME','BONUS'].includes(t.transactionType)
                    return (
                      <tr key={t.id} className="hover:bg-gray-50">
                        <td className="table-td font-mono text-xs">{t.transactionDate}</td>
                        <td className="table-td">
                          <span className={isDebit ? 'badge-green' : 'badge-amber'}>{t.transactionType}</span>
                        </td>
                        <td className="table-td text-gray-500 max-w-xs truncate">{t.description || '—'}</td>
                        <td className={`table-td text-right font-semibold ${isDebit ? 'text-green-700' : 'text-red-600'}`}>
                          {isDebit ? '+' : '−'} {formatCurrency(t.amount)}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
