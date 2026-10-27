import { useState, useEffect } from 'react'
import { Plus } from 'lucide-react'
import api from '@/api/axios'
import type { PagedResponse } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import Pagination from '@/components/common/Pagination'
import Modal from '@/components/common/Modal'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { Wrench } from 'lucide-react'

const CATEGORIES = ['LADDER','ROPE','CUTTER','HARVESTING_MACHINE','BAGS','SAFETY_EQUIPMENT','TARPAULIN','OTHER']

interface EquipmentForm {
  name: string
  category: string
  quantity: number
  purchaseDate?: string
  purchaseCost?: number
}

export default function EquipmentPage() {
  const [paged, setPaged] = useState<PagedResponse<any> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const { register, handleSubmit, reset } = useForm<EquipmentForm>({ defaultValues: { quantity: 1, category: 'LADDER' } })

  const load = async () => {
    setLoading(true)
    try { const r = await api.get('/equipment', { params: { page, size: 20 } }); setPaged(r.data.data) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [page])

  const onSubmit = async (data: any) => {
    setSaving(true)
    try { await api.post('/equipment', data); toast.success('Equipment added'); setModalOpen(false); reset({ quantity: 1, category: 'LADDER' }); load() }
    finally { setSaving(false) }
  }

  const conditionColor: Record<string, string> = { GOOD: 'badge-green', FAIR: 'badge-amber', POOR: 'badge-red', DAMAGED: 'badge-red' }

  return (
    <div>
      <div className="page-header">
        <div><h1>Equipment</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} items</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset({ quantity: 1, category: 'LADDER' }) }}><Plus className="h-4 w-4" /> Add Equipment</button>
      </div>
      <div className="card">
        {loading ? <LoadingSpinner center /> : !paged?.content?.length ? (
          <EmptyState icon={Wrench} title="No equipment tracked" />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Name</th>
                  <th className="table-th">Category</th>
                  <th className="table-th">Qty</th>
                  <th className="table-th">Condition</th>
                  <th className="table-th">Status</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map((e: any) => (
                    <tr key={e.id} className="hover:bg-gray-50">
                      <td className="table-td font-medium">{e.name}</td>
                      <td className="table-td"><span className="badge-blue badge">{e.category}</span></td>
                      <td className="table-td">{e.quantity}</td>
                      <td className="table-td"><span className={conditionColor[e.conditionStatus] ?? 'badge-gray'}>{e.conditionStatus}</span></td>
                      <td className="table-td"><span className={e.status === 'AVAILABLE' ? 'badge-green' : e.status === 'IN_USE' ? 'badge-blue' : 'badge-amber'}>{e.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Add Equipment">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2"><label className="label">Name *</label><input {...register('name')} className="input" placeholder="Bamboo Ladders 15ft" /></div>
            <div><label className="label">Category *</label><select {...register('category')} className="input">{CATEGORIES.map(c => <option key={c} value={c}>{c.replace('_',' ')}</option>)}</select></div>
            <div><label className="label">Quantity *</label><input {...register('quantity')} type="number" className="input" /></div>
            <div><label className="label">Purchase Date</label><input {...register('purchaseDate')} type="date" className="input" /></div>
            <div><label className="label">Purchase Cost (₹)</label><input {...register('purchaseCost')} type="number" step="0.01" className="input" /></div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Add'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
