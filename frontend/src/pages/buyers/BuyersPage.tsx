import { useState, useEffect, useCallback } from 'react'
import { Plus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { buyersApi } from '@/api'
import type { Buyer, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import SearchInput from '@/components/common/SearchInput'
import { formatCurrency } from '@/utils'
import { Users } from 'lucide-react'

const schema = z.object({
  name: z.string().min(1),
  phone: z.string().regex(/^[6-9]\d{9}$/, 'Invalid mobile'),
  companyName: z.string().optional(),
  village: z.string().optional(),
  district: z.string().optional(),
  paymentTerms: z.string().optional(),
  defaultRatePerKg: z.coerce.number().optional(),
  upiId: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function BuyersPage() {
  const [paged, setPaged] = useState<PagedResponse<Buyer> | null>(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) })

  const load = useCallback(async () => {
    setLoading(true)
    try { setPaged(await buyersApi.list({ search: search || undefined, page, size: 20 })) }
    finally { setLoading(false) }
  }, [search, page])

  useEffect(() => { load() }, [load])
  useEffect(() => { setPage(0) }, [search])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try { await buyersApi.create(data); toast.success('Buyer added'); setModalOpen(false); load() }
    finally { setSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div><h1>Buyers</h1><p className="text-sm text-gray-500 mt-0.5">Nut buyers / traders</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset() }}><Plus className="h-4 w-4" /> Add Buyer</button>
      </div>
      <div className="card">
        <div className="mb-4"><SearchInput value={search} onChange={setSearch} placeholder="Search buyers…" className="max-w-xs" /></div>
        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Users} title="No buyers" action={<button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" />Add Buyer</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Name / Company</th>
                  <th className="table-th">Phone</th>
                  <th className="table-th hidden sm:table-cell">Village</th>
                  <th className="table-th hidden md:table-cell">Default Rate</th>
                  <th className="table-th hidden md:table-cell">Payment Terms</th>
                  <th className="table-th">Outstanding</th>
                  <th className="table-th">Status</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map((b: any) => (
                    <tr key={b.id} className="hover:bg-gray-50">
                      <td className="table-td"><div className="font-medium">{b.name}</div>{b.companyName && <div className="text-xs text-gray-500">{b.companyName}</div>}</td>
                      <td className="table-td">{b.phone}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{b.village || '—'}</td>
                      <td className="table-td hidden md:table-cell">{b.defaultRate ? `₹${b.defaultRate}/KG` : '—'}</td>
                      <td className="table-td hidden md:table-cell text-gray-500">{b.paymentTerms || '—'}</td>
                      <td className="table-td font-semibold text-red-600">{b.outstandingBalance > 0 ? formatCurrency(b.outstandingBalance) : '—'}</td>
                      <td className="table-td"><span className={b.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}>{b.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Add Buyer">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2"><label className="label">Name *</label><input {...register('name')} className="input" />{errors.name && <p className="text-xs text-red-600 mt-1">{errors.name.message}</p>}</div>
            <div><label className="label">Phone *</label><input {...register('phone')} className="input" />{errors.phone && <p className="text-xs text-red-600 mt-1">{errors.phone.message}</p>}</div>
            <div><label className="label">Company</label><input {...register('companyName')} className="input" /></div>
            <div><label className="label">Village</label><input {...register('village')} className="input" /></div>
            <div><label className="label">District</label><input {...register('district')} className="input" /></div>
            <div><label className="label">Default Rate (₹/KG)</label><input {...register('defaultRatePerKg')} type="number" step="0.01" className="input" /></div>
            <div><label className="label">Payment Terms</label><input {...register('paymentTerms')} className="input" placeholder="Net 7 days" /></div>
            <div><label className="label">UPI ID</label><input {...register('upiId')} className="input" /></div>
            <div><label className="label">Notes</label><textarea {...register('notes')} className="input" rows={2} /></div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Add Buyer'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
