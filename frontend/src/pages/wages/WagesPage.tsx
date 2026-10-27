import { useState, useEffect, useCallback } from 'react'
import { Plus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { wagesApi, workersApi } from '@/api'
import type { Worker, PagedResponse, WorkerPayment } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { formatCurrency, formatDate, todayISO } from '@/utils'
import { Wallet } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Eye } from 'lucide-react'

const schema = z.object({
  workerId: z.coerce.number().min(1),
  paymentDate: z.string().min(1),
  amount: z.coerce.number().min(1),
  paymentType: z.enum(['PAYMENT','ADVANCE']),
  paymentMethod: z.enum(['CASH','UPI','BANK_TRANSFER','OTHER']),
  referenceNumber: z.string().optional(),
  remarks: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function WagesPage() {
  const [paged, setPaged] = useState<PagedResponse<WorkerPayment> | null>(null)
  const [workers, setWorkers] = useState<Worker[]>([])
  const [page, setPage] = useState(0)
  const [workerFilter, setWorkerFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [adjModalOpen, setAdjModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [adjSaving, setAdjSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { paymentDate: todayISO(), paymentType: 'PAYMENT', paymentMethod: 'CASH' },
  })

  const [adjData, setAdjData] = useState({
    workerId: '',
    transactionType: 'BONUS',
    amount: '',
    adjustmentSign: 1,
    transactionDate: todayISO(),
    description: '',
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, ws] = await Promise.all([
        wagesApi.listPayments({ workerId: workerFilter || undefined, page, size: 20 }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
      ])
      setPaged(data)
      setWorkers(ws.content)
    } finally { setLoading(false) }
  }, [workerFilter, page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await wagesApi.recordPayment(data)
      toast.success('Payment recorded and ledger updated')
      setModalOpen(false)
      reset({ paymentDate: todayISO(), paymentType: 'PAYMENT', paymentMethod: 'CASH' })
      load()
    } finally { setSaving(false) }
  }

  const onAdjustmentSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!adjData.workerId || !adjData.amount) {
      toast.error('Worker and amount are required')
      return
    }
    setAdjSaving(true)
    try {
      await wagesApi.addAdjustment({
        workerId: Number(adjData.workerId),
        transactionType: adjData.transactionType,
        amount: Number(adjData.amount),
        adjustmentSign: Number(adjData.adjustmentSign),
        transactionDate: adjData.transactionDate,
        description: adjData.description || undefined,
      })
      toast.success('Wage bonus / adjustment recorded')
      setAdjModalOpen(false)
      setAdjData({
        workerId: '',
        transactionType: 'BONUS',
        amount: '',
        adjustmentSign: 1,
        transactionDate: todayISO(),
        description: '',
      })
      load()
    } finally { setAdjSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Wages & Payments</h1>
          <p className="text-sm text-gray-500 mt-0.5">Worker payment history and outstanding balances</p>
        </div>
        <div className="flex gap-2">
          <button className="btn-secondary btn" onClick={() => setAdjModalOpen(true)}>
            <Plus className="h-4 w-4" /> Add Bonus / Adjustment
          </button>
          <button className="btn-primary btn" onClick={() => setModalOpen(true)}>
            <Plus className="h-4 w-4" /> Record Payment
          </button>
        </div>
      </div>

      {/* Quick outstanding — click worker to see ledger */}
      <div className="card mb-6">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">Workers with Outstanding Balance</h3>
        <div className="flex flex-wrap gap-2">
          {workers.slice(0, 10).map(w => (
            <Link key={w.id} to={`/workers/${w.id}`}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-200 hover:border-primary-400 hover:bg-primary-50 transition-colors text-sm">
              <span className="font-medium text-gray-900">{w.name}</span>
              <Eye className="h-3 w-3 text-gray-400" />
            </Link>
          ))}
        </div>
      </div>

      <div className="card">
        <div className="flex gap-3 mb-4">
          <select className="input w-48" value={workerFilter} onChange={e => setWorkerFilter(e.target.value)}>
            <option value="">All Workers</option>
            {workers.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Wallet} title="No payment records" action={<button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" />Record Payment</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Date</th>
                  <th className="table-th">Worker</th>
                  <th className="table-th">Type</th>
                  <th className="table-th">Method</th>
                  <th className="table-th hidden sm:table-cell">Reference</th>
                  <th className="table-th text-right">Amount</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(p => (
                    <tr key={p.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">{formatDate(p.paymentDate)}</td>
                      <td className="table-td font-medium">{p.workerName}</td>
                      <td className="table-td">
                        <span className={p.paymentType === 'PAYMENT' ? 'badge-green' : 'badge-amber'}>{p.paymentType}</span>
                      </td>
                      <td className="table-td"><span className="badge-gray badge">{p.paymentMethod}</span></td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{p.referenceNumber || '—'}</td>
                      <td className="table-td text-right font-semibold text-green-700">{formatCurrency(p.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Record Payment / Advance">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Worker *</label>
              <select {...register('workerId', { valueAsNumber: true })} className="input">
                <option value="">Select worker…</option>
                {workers.map(w => <option key={w.id} value={w.id}>{w.name} ({w.skillType})</option>)}
              </select>
              {errors.workerId && <p className="text-xs text-red-600 mt-1">Required</p>}
            </div>
            <div>
              <label className="label">Date *</label>
              <input {...register('paymentDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Amount (₹) *</label>
              <input {...register('amount')} type="number" step="0.01" className="input" placeholder="500" />
              {errors.amount && <p className="text-xs text-red-600 mt-1">{errors.amount.message}</p>}
            </div>
            <div>
              <label className="label">Payment Type *</label>
              <select {...register('paymentType')} className="input">
                <option value="PAYMENT">PAYMENT (against wages)</option>
                <option value="ADVANCE">ADVANCE</option>
              </select>
            </div>
            <div>
              <label className="label">Payment Method *</label>
              <select {...register('paymentMethod')} className="input">
                <option value="CASH">CASH</option>
                <option value="UPI">UPI</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div>
              <label className="label">Reference Number</label>
              <input {...register('referenceNumber')} className="input" placeholder="UPI transaction ID" />
            </div>
            <div>
              <label className="label">Remarks</label>
              <input {...register('remarks')} className="input" />
            </div>
          </div>
          <p className="text-xs text-amber-700 bg-amber-50 rounded-lg p-3">
            ⚠️ PAYMENT type will be rejected if amount exceeds outstanding balance. Use ADVANCE for advance payments.
          </p>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Record Payment'}</button>
          </div>
        </form>
      </Modal>

      {/* Bonus / Adjustment Modal */}
      <Modal open={adjModalOpen} onClose={() => setAdjModalOpen(false)} title="Record Bonus / Adjustment">
        <form onSubmit={onAdjustmentSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Worker *</label>
              <select
                className="input"
                value={adjData.workerId}
                onChange={e => setAdjData({ ...adjData, workerId: e.target.value })}
                required
              >
                <option value="">Select worker…</option>
                {workers.map(w => <option key={w.id} value={w.id}>{w.name} ({w.skillType})</option>)}
              </select>
            </div>
            <div>
              <label className="label">Transaction Type *</label>
              <select
                className="input"
                value={adjData.transactionType}
                onChange={e => setAdjData({ ...adjData, transactionType: e.target.value })}
              >
                <option value="BONUS">BONUS (Increases wage payable)</option>
                <option value="ADJUSTMENT">ADJUSTMENT (Manual correction)</option>
              </select>
            </div>
            <div>
              <label className="label">Adjustment Effect *</label>
              <select
                className="input"
                value={adjData.adjustmentSign}
                onChange={e => setAdjData({ ...adjData, adjustmentSign: Number(e.target.value) })}
              >
                <option value={1}>+ Add to balance (Owed to worker)</option>
                <option value={-1}>− Deduct from balance (Recovery/Penalty)</option>
              </select>
            </div>
            <div>
              <label className="label">Amount (₹) *</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                className="input"
                placeholder="500.00"
                value={adjData.amount}
                onChange={e => setAdjData({ ...adjData, amount: e.target.value })}
                required
              />
            </div>
            <div>
              <label className="label">Date *</label>
              <input
                type="date"
                className="input"
                value={adjData.transactionDate}
                onChange={e => setAdjData({ ...adjData, transactionDate: e.target.value })}
                required
              />
            </div>
            <div className="col-span-2">
              <label className="label">Reason / Description</label>
              <input
                type="text"
                className="input"
                placeholder="e.g. Festival bonus / Equipment reimbursement / Fuel allowance"
                value={adjData.description}
                onChange={e => setAdjData({ ...adjData, description: e.target.value })}
              />
            </div>
          </div>
          <p className="text-xs text-blue-700 bg-blue-50 rounded-lg p-3">
            ℹ️ This posts directly to the worker's Wage Ledger without changing attendance history.
          </p>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setAdjModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={adjSaving}>{adjSaving ? 'Saving…' : 'Record Entry'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
