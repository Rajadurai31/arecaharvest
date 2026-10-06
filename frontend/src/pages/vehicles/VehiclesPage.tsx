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
import { Truck } from 'lucide-react'

export default function VehiclesPage() {
  const [paged, setPaged] = useState<PagedResponse<any> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const { register, handleSubmit, reset } = useForm()

  const load = async () => {
    setLoading(true)
    try { const r = await api.get('/vehicles', { params: { page, size: 20 } }); setPaged(r.data.data) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [page])

  const onSubmit = async (data: any) => {
    setSaving(true)
    try { await api.post('/vehicles', data); toast.success('Vehicle added'); setModalOpen(false); reset(); load() }
    finally { setSaving(false) }
  }

  const statusColor: Record<string, string> = { ACTIVE: 'badge-green', INACTIVE: 'badge-gray', MAINTENANCE: 'badge-amber' }

  return (
    <div>
      <div className="page-header">
        <div><h1>Vehicles</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} vehicles</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset() }}><Plus className="h-4 w-4" /> Add Vehicle</button>
      </div>
      <div className="card">
        {loading ? <LoadingSpinner center /> : !paged?.content?.length ? (
          <EmptyState icon={Truck} title="No vehicles" />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Registration</th>
                  <th className="table-th">Type</th>
                  <th className="table-th hidden sm:table-cell">Make / Model</th>
                  <th className="table-th hidden md:table-cell">Fuel</th>
                  <th className="table-th">Status</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map((v: any) => (
                    <tr key={v.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono font-medium">{v.registrationNumber}</td>
                      <td className="table-td"><span className="badge-blue badge">{v.vehicleType}</span></td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{[v.make, v.model, v.year].filter(Boolean).join(' ')}</td>
                      <td className="table-td hidden md:table-cell">{v.fuelType || '—'}</td>
                      <td className="table-td"><span className={statusColor[v.status] ?? 'badge-gray'}>{v.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Add Vehicle">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2"><label className="label">Registration Number *</label><input {...register('registrationNumber')} className="input" placeholder="KA-13-AB-1234" /></div>
            <div><label className="label">Type *</label><select {...register('vehicleType')} className="input"><option value="TRUCK">TRUCK</option><option value="TRACTOR">TRACTOR</option><option value="PICKUP">PICKUP</option><option value="AUTO">AUTO</option><option value="BIKE">BIKE</option><option value="OTHER">OTHER</option></select></div>
            <div><label className="label">Make</label><input {...register('make')} className="input" placeholder="TATA" /></div>
            <div><label className="label">Model</label><input {...register('model')} className="input" placeholder="Ace" /></div>
            <div><label className="label">Year</label><input {...register('year')} type="number" className="input" /></div>
            <div><label className="label">Fuel Type</label><select {...register('fuelType')} className="input"><option value="DIESEL">DIESEL</option><option value="PETROL">PETROL</option><option value="ELECTRIC">ELECTRIC</option></select></div>
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
