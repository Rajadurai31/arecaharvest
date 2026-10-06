import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Eye, Edit2, UserX } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { ownersApi } from '@/api'
import type { Owner, PagedResponse } from '@/types'
import SearchInput from '@/components/common/SearchInput'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import ConfirmDialog from '@/components/common/ConfirmDialog'

const schema = z.object({
  name: z.string().min(1, 'Name required'),
  phone: z.string().regex(/^[6-9]\d{9}$/, 'Invalid mobile number'),
  alternatePhone: z.string().optional().or(z.literal('')),
  email: z.string().email().optional().or(z.literal('')),
  village: z.string().optional(),
  district: z.string().optional(),
  state: z.string().optional(),
  paymentPreference: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function OwnersPage() {
  const [paged, setPaged] = useState<PagedResponse<Owner> | null>(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Owner | null>(null)
  const [deactivating, setDeactivating] = useState<Owner | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const data = await ownersApi.list({ search: search || undefined, page, size: 20 })
      setPaged(data)
    } finally { setLoading(false) }
  }, [search, page])

  useEffect(() => { load() }, [load])
  useEffect(() => { setPage(0) }, [search])

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true) }
  const openEdit = (o: Owner) => {
    setEditing(o)
    reset({ name: o.name, phone: o.phone, alternatePhone: o.alternatePhone ?? '', email: o.email ?? '',
            village: o.village ?? '', district: o.district ?? '', state: o.state ?? '',
            paymentPreference: o.paymentPreference ?? '', notes: o.notes ?? '' })
    setModalOpen(true)
  }

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      if (editing) {
        await ownersApi.update(editing.id, data)
        toast.success('Owner updated')
      } else {
        await ownersApi.create(data)
        toast.success('Owner created')
      }
      setModalOpen(false)
      load()
    } finally { setSaving(false) }
  }

  const onDeactivate = async () => {
    if (!deactivating) return
    await ownersApi.deactivate(deactivating.id)
    toast.success('Owner deactivated')
    setDeactivating(null)
    load()
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Owners</h1>
          <p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} areca nut owners</p>
        </div>
        <button className="btn-primary btn" onClick={openCreate}>
          <Plus className="h-4 w-4" /> Add Owner
        </button>
      </div>

      <div className="card">
        <div className="flex gap-3 mb-4">
          <SearchInput value={search} onChange={setSearch} placeholder="Search by name, phone, village…" className="flex-1 max-w-xs" />
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState title="No owners found" description="Add your first areca nut field owner to get started." action={<button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" />Add Owner</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Name</th>
                  <th className="table-th">Phone</th>
                  <th className="table-th hidden sm:table-cell">Village / District</th>
                  <th className="table-th hidden md:table-cell">Payment</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(o => (
                    <tr key={o.id} className="hover:bg-gray-50 transition-colors">
                      <td className="table-td font-medium text-gray-900">{o.name}</td>
                      <td className="table-td">{o.phone}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">
                        {[o.village, o.district].filter(Boolean).join(', ') || '—'}
                      </td>
                      <td className="table-td hidden md:table-cell">
                        <span className="badge-gray badge">{o.paymentPreference ?? 'CASH'}</span>
                      </td>
                      <td className="table-td">
                        <span className={o.active ? 'badge-green' : 'badge-red'}>{o.active ? 'Active' : 'Inactive'}</span>
                      </td>
                      <td className="table-td text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Link to={`/owners/${o.id}`} className="btn btn-secondary btn-sm px-2" title="View"><Eye className="h-3.5 w-3.5" /></Link>
                          <button className="btn btn-secondary btn-sm px-2" onClick={() => openEdit(o)} title="Edit"><Edit2 className="h-3.5 w-3.5" /></button>
                          {o.active && <button className="btn btn-danger btn-sm px-2" onClick={() => setDeactivating(o)} title="Deactivate"><UserX className="h-3.5 w-3.5" /></button>}
                        </div>
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

      {/* Create / Edit Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Owner' : 'Add Owner'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Full Name *</label>
              <input {...register('name')} className="input" placeholder="Suresh Gowda" />
              {errors.name && <p className="text-xs text-red-600 mt-1">{errors.name.message}</p>}
            </div>
            <div>
              <label className="label">Phone *</label>
              <input {...register('phone')} className="input" placeholder="9845001001" />
              {errors.phone && <p className="text-xs text-red-600 mt-1">{errors.phone.message}</p>}
            </div>
            <div>
              <label className="label">Alternate Phone</label>
              <input {...register('alternatePhone')} className="input" placeholder="Optional" />
            </div>
            <div>
              <label className="label">Email</label>
              <input {...register('email')} type="email" className="input" placeholder="owner@example.com" />
            </div>
            <div>
              <label className="label">Village</label>
              <input {...register('village')} className="input" placeholder="Sakleshpur" />
            </div>
            <div>
              <label className="label">District</label>
              <input {...register('district')} className="input" placeholder="Hassan" />
            </div>
            <div>
              <label className="label">State</label>
              <input {...register('state')} className="input" placeholder="Karnataka" />
            </div>
            <div>
              <label className="label">Payment Preference</label>
              <select {...register('paymentPreference')} className="input">
                <option value="CASH">Cash</option>
                <option value="UPI">UPI</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
                <option value="CHEQUE">Cheque</option>
              </select>
            </div>
            <div className="col-span-2">
              <label className="label">Notes</label>
              <textarea {...register('notes')} className="input" rows={2} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving…' : editing ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deactivating}
        title="Deactivate Owner"
        message={`Are you sure you want to deactivate ${deactivating?.name}?`}
        confirmLabel="Deactivate"
        onConfirm={onDeactivate}
        onCancel={() => setDeactivating(null)}
        danger
      />
    </div>
  )
}
