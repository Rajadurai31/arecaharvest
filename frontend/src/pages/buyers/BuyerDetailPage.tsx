import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Plus } from 'lucide-react'
import { buyersApi, salesApi } from '@/api'
import api from '@/api/axios'
import type { Sale, PagedResponse } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import Pagination from '@/components/common/Pagination'
import Modal from '@/components/common/Modal'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { formatCurrency, formatDate, saleStatusColor } from '@/utils'

export default function BuyerDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [buyer, setBuyer] = useState<any>(null)
  const [sales, setSales] = useState<PagedResponse<Sale> | null>(null)
  const [ledger, setLedger] = useState<PagedResponse<any> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [payModal, setPayModal] = useState<Sale | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset } = useForm<any>({
    defaultValues: { paymentDate: new Date().toISOString().split('T')[0], transactionType: 'PAYMENT' }
  })

  const load = async () => {
    if (!id) return
    setLoading(true)
    try {
      const [b, s, l] = await Promise.all([
        buyersApi.getById(Number(id)),
        salesApi.list({ buyerId: id, page, size: 20 }),
        api.get(`/buyers/${id}/ledger`, { params: { page: 0, size: 50 } }),
      ])
      setBuyer(b); setSales(s); setLedger(l.data.data)
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [id, page])

  const onPayment = async (data: any) => {
    setSaving(true)
    try {
      await salesApi.recordPayment({ ...data, buyerId: Number(id), saleId: payModal?.id })
      toast.success('Payment recorded'); setPayModal(null); reset(); load()
    } finally { setSaving(false) }
  }

  if (loading) return <LoadingSpinner center />
  if (!buyer) return <p className="text-center py-16 text-gray-500">Buyer not found</p>

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center gap-3">
        <Link to="/buyers" className="btn btn-secondary btn-sm px-2"><ArrowLeft className="h-4 w-4" /></Link>
        <div>
          <h1>{buyer.name}</h1>
          {buyer.companyName && <p className="text-sm text-gray-500">{buyer.companyName}</p>}
        </div>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-3 gap-4">
        <div className="card text-center">
          <p className="text-2xl font-bold text-gray-900">{sales?.totalElements ?? 0}</p>
          <p className="text-xs text-gray-500">Total Sales</p>
        </div>
        <div className="card text-center">
          <p className="text-2xl font-bold text-green-700">{buyer.phone}</p>
          <p className="text-xs text-gray-500">Phone</p>
        </div>
        <div className="card text-center">
          <p className="text-2xl font-bold text-red-600">{formatCurrency(buyer.outstandingBalance)}</p>
          <p className="text-xs text-gray-500">Outstanding</p>
        </div>
      </div>

      {/* Sales */}
      <div className="card">
        <h2 className="mb-4">Purchase History</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead><tr className="border-b border-gray-100">
              <th className="table-th">Sale #</th><th className="table-th">Date</th>
              <th className="table-th">Qty KG</th><th className="table-th">Net Amount</th>
              <th className="table-th">Received</th><th className="table-th">Outstanding</th>
              <th className="table-th">Status</th><th className="table-th text-right">Action</th>
            </tr></thead>
            <tbody className="divide-y divide-gray-50">
              {sales?.content.map(s => (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="table-td font-mono text-xs">{s.saleNumber}</td>
                  <td className="table-td text-xs">{formatDate(s.saleDate)}</td>
                  <td className="table-td">{s.quantityKg.toFixed(3)}</td>
                  <td className="table-td font-semibold">{formatCurrency(s.netAmount)}</td>
                  <td className="table-td text-green-700">{formatCurrency(s.amountReceived)}</td>
                  <td className="table-td text-red-600">{s.outstanding > 0 ? formatCurrency(s.outstanding) : '—'}</td>
                  <td className="table-td"><span className={saleStatusColor[s.paymentStatus]}>{s.paymentStatus}</span></td>
                  <td className="table-td text-right">
                    {s.paymentStatus !== 'PAID' && (
                      <button className="btn btn-secondary btn-sm" onClick={() => { setPayModal(s); reset({ paymentDate: new Date().toISOString().split('T')[0], transactionType: 'PAYMENT', buyerId: buyer.id, saleId: s.id, amount: s.outstanding }) }}>
                        <Plus className="h-3.5 w-3.5" /> Pay
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {sales && <Pagination paged={sales} onPageChange={setPage} />}
      </div>

      {/* Ledger */}
      {ledger && ledger.content.length > 0 && (
        <div className="card">
          <h2 className="mb-4">Payment Ledger</h2>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead><tr className="border-b border-gray-100">
                <th className="table-th">Date</th><th className="table-th">Type</th>
                <th className="table-th hidden sm:table-cell">Method</th><th className="table-th text-right">Amount</th>
              </tr></thead>
              <tbody className="divide-y divide-gray-50">
                {ledger.content.map((l: any) => (
                  <tr key={l.id} className="hover:bg-gray-50">
                    <td className="table-td text-xs">{formatDate(l.paymentDate)}</td>
                    <td className="table-td">
                      <span className={l.transactionType === 'PAYMENT' ? 'badge-green' : l.transactionType === 'SALE' ? 'badge-red' : 'badge-gray'}>{l.transactionType}</span>
                    </td>
                    <td className="table-td hidden sm:table-cell text-gray-500">{l.paymentMethod || '—'}</td>
                    <td className={`table-td text-right font-semibold ${l.transactionType === 'PAYMENT' ? 'text-green-700' : 'text-red-600'}`}>
                      {l.transactionType === 'PAYMENT' ? '+' : '-'}{formatCurrency(l.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Payment Modal */}
      <Modal open={!!payModal} onClose={() => setPayModal(null)} title={`Record Payment — ${payModal?.saleNumber}`}>
        <form onSubmit={handleSubmit(onPayment)} className="space-y-4">
          <div className="bg-gray-50 rounded-lg p-3 text-sm">
            <div className="flex justify-between"><span className="text-gray-600">Sale Amount</span><span className="font-semibold">{formatCurrency(payModal?.netAmount)}</span></div>
            <div className="flex justify-between"><span className="text-gray-600">Already Received</span><span className="text-green-700">{formatCurrency(payModal?.amountReceived)}</span></div>
            <div className="flex justify-between font-bold border-t border-gray-200 pt-1 mt-1"><span>Outstanding</span><span className="text-red-600">{formatCurrency(payModal?.outstanding)}</span></div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div><label className="label">Date *</label><input {...register('paymentDate')} type="date" className="input" /></div>
            <div><label className="label">Amount (₹) *</label><input {...register('amount')} type="number" step="0.01" className="input" /></div>
            <div><label className="label">Method</label>
              <select {...register('paymentMethod')} className="input">
                <option value="CASH">CASH</option><option value="UPI">UPI</option>
                <option value="BANK_TRANSFER">Bank Transfer</option><option value="CHEQUE">Cheque</option>
              </select>
            </div>
            <div><label className="label">Reference #</label><input {...register('referenceNumber')} className="input" /></div>
            <div className="col-span-2"><label className="label">Remarks</label><input {...register('remarks')} className="input" /></div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setPayModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Record Payment'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
