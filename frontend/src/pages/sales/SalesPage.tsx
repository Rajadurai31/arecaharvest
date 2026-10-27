import { useState, useEffect, useCallback } from 'react'
import { Plus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { salesApi, buyersApi, inventoryApi } from '@/api'
import type { Sale, Buyer, Inventory, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { saleStatusColor, formatCurrency, formatDate } from '@/utils'
import { ShoppingCart } from 'lucide-react'

const schema = z.object({
  saleNumber: z.string().min(1),
  buyerId: z.coerce.number().min(1),
  inventoryId: z.coerce.number().min(1),
  saleDate: z.string().min(1),
  quantityKg: z.coerce.number().min(0.001),
  ratePerUnit: z.coerce.number().min(0.01),
  transportCost: z.coerce.number().optional(),
  loadingCost: z.coerce.number().optional(),
  otherCharges: z.coerce.number().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function SalesPage() {
  const [paged, setPaged] = useState<PagedResponse<Sale> | null>(null)
  const [buyers, setBuyers] = useState<Buyer[]>([])
  const [inventory, setInventory] = useState<Inventory[]>([])
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [payModal, setPayModal] = useState<Sale | null>(null)
  const [paying, setPaying] = useState(false)
  const [payData, setPayData] = useState({
    amount: '',
    paymentDate: new Date().toISOString().split('T')[0],
    paymentMethod: 'UPI',
    referenceNumber: '',
    remarks: '',
  })

  const { register, handleSubmit, reset, watch } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { saleDate: new Date().toISOString().split('T')[0], saleNumber: `SAL-${Date.now()}` },
  })
  const qty = watch('quantityKg') || 0
  const rate = watch('ratePerUnit') || 0
  const transport = watch('transportCost') || 0
  const loading2 = watch('loadingCost') || 0
  const other = watch('otherCharges') || 0
  const gross = qty * rate
  const net = gross - transport - loading2 - other

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, bs, inv] = await Promise.all([
        salesApi.list({ status: statusFilter || undefined, page, size: 20 }),
        buyersApi.list({ status: 'ACTIVE', size: 100 }),
        inventoryApi.list({ status: 'AVAILABLE', size: 100 }),
      ])
      setPaged(data); setBuyers(bs.content); setInventory(inv.content)
    } finally { setLoading(false) }
  }, [statusFilter, page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await salesApi.create(data)
      toast.success('Sale recorded — inventory updated')
      setModalOpen(false); load()
    } finally { setSaving(false) }
  }

  const openPayModal = (s: Sale) => {
    setPayModal(s)
    setPayData({
      amount: String(s.outstanding > 0 ? s.outstanding : s.netAmount),
      paymentDate: new Date().toISOString().split('T')[0],
      paymentMethod: 'UPI',
      referenceNumber: '',
      remarks: `Payment against ${s.saleNumber}`,
    })
  }

  const onRecordPayment = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!payModal) return
    setPaying(true)
    try {
      await salesApi.recordPayment({
        buyerId: payModal.buyerId,
        saleId: payModal.id,
        paymentDate: payData.paymentDate,
        transactionType: 'PAYMENT',
        amount: Number(payData.amount),
        paymentMethod: payData.paymentMethod,
        referenceNumber: payData.referenceNumber || undefined,
        remarks: payData.remarks || undefined,
      })
      toast.success('Buyer payment recorded successfully')
      setPayModal(null)
      load()
    } finally { setPaying(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div><h1>Sales</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} sales</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset({ saleDate: new Date().toISOString().split('T')[0], saleNumber: `SAL-${Date.now()}` }) }}>
          <Plus className="h-4 w-4" /> Record Sale
        </button>
      </div>

      <div className="card">
        <div className="flex gap-3 mb-4">
          <select className="input w-40" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All Status</option>
            {['PENDING','PARTIAL','PAID'].map(s => <option key={s}>{s}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={ShoppingCart} title="No sales yet" />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Sale #</th>
                  <th className="table-th">Date</th>
                  <th className="table-th">Buyer</th>
                  <th className="table-th">Qty (KG)</th>
                  <th className="table-th hidden md:table-cell">Rate</th>
                  <th className="table-th">Net Amount</th>
                  <th className="table-th hidden sm:table-cell">Received</th>
                  <th className="table-th">Outstanding</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(s => (
                    <tr key={s.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">{s.saleNumber}</td>
                      <td className="table-td text-xs">{formatDate(s.saleDate)}</td>
                      <td className="table-td font-medium">{s.buyerName}</td>
                      <td className="table-td">{s.quantityKg.toFixed(3)}</td>
                      <td className="table-td hidden md:table-cell">₹{s.ratePerUnit}</td>
                      <td className="table-td font-semibold">{formatCurrency(s.netAmount)}</td>
                      <td className="table-td hidden sm:table-cell text-green-700">{formatCurrency(s.amountReceived)}</td>
                      <td className="table-td text-red-600 font-medium">{s.outstanding > 0 ? formatCurrency(s.outstanding) : '—'}</td>
                      <td className="table-td"><span className={saleStatusColor[s.paymentStatus]}>{s.paymentStatus}</span></td>
                      <td className="table-td text-right">
                        {s.paymentStatus !== 'PAID' ? (
                          <button
                            type="button"
                            className="btn btn-secondary btn-sm px-2 text-xs font-medium text-primary-700 hover:bg-primary-50"
                            onClick={() => openPayModal(s)}
                            title="Record payment from buyer"
                          >
                            Pay
                          </button>
                        ) : (
                          <span className="text-xs text-green-600 font-medium">Settled</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Record Sale" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Sale Number *</label>
              <input {...register('saleNumber')} className="input" placeholder="SAL-2026-001" />
            </div>
            <div>
              <label className="label">Buyer *</label>
              <select {...register('buyerId', { valueAsNumber: true })} className="input">
                <option value="">Select buyer…</option>
                {buyers.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Inventory Lot *</label>
              <select {...register('inventoryId', { valueAsNumber: true })} className="input">
                <option value="">Select inventory…</option>
                {inventory.map(i => <option key={i.id} value={i.id}>Lot #{i.id} — {i.availableQuantity.toFixed(3)} KG available</option>)}
              </select>
            </div>
            <div>
              <label className="label">Sale Date *</label>
              <input {...register('saleDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Quantity (KG) *</label>
              <input {...register('quantityKg')} type="number" step="0.001" className="input" placeholder="0.000" />
            </div>
            <div>
              <label className="label">Rate per KG (₹) *</label>
              <input {...register('ratePerUnit')} type="number" step="0.01" className="input" placeholder="58.50" />
            </div>
            <div>
              <label className="label">Transport Cost (₹)</label>
              <input {...register('transportCost')} type="number" step="0.01" className="input" defaultValue={0} />
            </div>
            <div>
              <label className="label">Loading Cost (₹)</label>
              <input {...register('loadingCost')} type="number" step="0.01" className="input" defaultValue={0} />
            </div>
            <div>
              <label className="label">Other Charges (₹)</label>
              <input {...register('otherCharges')} type="number" step="0.01" className="input" defaultValue={0} />
            </div>
          </div>

          {/* Live calculation */}
          {qty > 0 && rate > 0 && (
            <div className="bg-gray-50 rounded-lg p-4 space-y-1 text-sm">
              <div className="flex justify-between"><span className="text-gray-600">Gross Amount</span><span>{formatCurrency(gross)}</span></div>
              <div className="flex justify-between text-red-600"><span>Deductions</span><span>−{formatCurrency(transport + loading2 + other)}</span></div>
              <div className="flex justify-between font-bold text-primary-700 text-base pt-1 border-t border-gray-200">
                <span>Net Amount</span><span>{formatCurrency(net)}</span>
              </div>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Record Sale'}</button>
          </div>
        </form>
      </Modal>

      {/* Record Buyer Payment Modal */}
      <Modal open={!!payModal} onClose={() => setPayModal(null)} title={`Record Payment — ${payModal?.saleNumber}`}>
        {payModal && (
          <form onSubmit={onRecordPayment} className="space-y-4">
            <div className="bg-gray-50 rounded-lg p-3 text-xs space-y-1">
              <div className="flex justify-between text-gray-600"><span>Buyer:</span><span className="font-semibold text-gray-800">{payModal.buyerName}</span></div>
              <div className="flex justify-between text-gray-600"><span>Net Sale Amount:</span><span>{formatCurrency(payModal.netAmount)}</span></div>
              <div className="flex justify-between text-gray-600"><span>Already Received:</span><span className="text-green-700">{formatCurrency(payModal.amountReceived)}</span></div>
              <div className="flex justify-between text-red-700 font-semibold pt-1 border-t border-gray-200">
                <span>Remaining Outstanding:</span><span>{formatCurrency(payModal.outstanding)}</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Payment Date *</label>
                <input
                  type="date"
                  className="input"
                  value={payData.paymentDate}
                  onChange={e => setPayData({ ...payData, paymentDate: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="label">Amount (₹) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="input"
                  value={payData.amount}
                  onChange={e => setPayData({ ...payData, amount: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="label">Payment Method *</label>
                <select
                  className="input"
                  value={payData.paymentMethod}
                  onChange={e => setPayData({ ...payData, paymentMethod: e.target.value })}
                >
                  <option value="UPI">UPI</option>
                  <option value="BANK_TRANSFER">Bank Transfer</option>
                  <option value="CASH">Cash</option>
                  <option value="CHEQUE">Cheque</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div>
                <label className="label">Reference Number</label>
                <input
                  type="text"
                  className="input"
                  placeholder="Txn ID / Cheque #"
                  value={payData.referenceNumber}
                  onChange={e => setPayData({ ...payData, referenceNumber: e.target.value })}
                />
              </div>
              <div className="col-span-2">
                <label className="label">Remarks</label>
                <input
                  type="text"
                  className="input"
                  value={payData.remarks}
                  onChange={e => setPayData({ ...payData, remarks: e.target.value })}
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
              <button type="button" className="btn btn-secondary" onClick={() => setPayModal(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={paying}>{paying ? 'Saving…' : 'Record Payment'}</button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  )
}
