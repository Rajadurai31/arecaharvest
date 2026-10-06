import { useState, useEffect, useCallback } from 'react'
import { Plus, Edit2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { fieldsApi, ownersApi } from '@/api'
import type { Field, Owner, PagedResponse } from '@/types'
import SearchInput from '@/components/common/SearchInput'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { fieldStatusColor, formatKg } from '@/utils'
import { MapPin } from 'lucide-react'

const schema = z.object({
  ownerId: z.coerce.number().min(1, 'Owner required'),
  fieldName: z.string().min(1, 'Field name required'),
  village: z.string().optional(),
  district: z.string().optional(),
  acreage: z.coerce.number().optional(),
  treeCount: z.coerce.number().optional(),
  expectedYieldKg: z.coerce.number().optional(),
  status: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function FieldsPage() {
  const [paged, setPaged] = useState<PagedResponse<Field> | null>(null)
  const [owners, setOwners] = useState<Owner[]>([])
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Field | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, ownerData] = await Promise.all([
        fieldsApi.list({ search: search || undefined, status: statusFilter || undefined, page, size: 20 }),
        ownersApi.list({ active: true, size: 100 }),
      ])
      setPaged(data)
      setOwners(ownerData.content)
    } finally { setLoading(false) }
  }, [search, statusFilter, page])

  useEffect(() => { load() }, [load])
  useEffect(() => { setPage(0) }, [search, statusFilter])

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true) }
  const openEdit = (f: Field) => {
    setEditing(f)
    reset({ ownerId: f.ownerId, fieldName: f.fieldName, village: f.village ?? '', district: f.district ?? '',
            acreage: f.acreage ?? undefined, treeCount: f.treeCount ?? undefined,
            expectedYieldKg: f.expectedYieldKg ?? undefined, status: f.status, notes: f.notes ?? '' })
    setModalOpen(true)
  }

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      if (editing) { await fieldsApi.update(editing.id, data); toast.success('Field updated') }
      else { await fieldsApi.create(data as Partial<Field>); toast.success('Field created') }
      setModalOpen(false); load()
    } finally { setSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Fields</h1>
          <p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} fields registered</p>
        </div>
        <button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" /> Add Field</button>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-3 mb-4">
          <SearchInput value={search} onChange={setSearch} placeholder="Search fields…" className="flex-1 min-w-48" />
          <select className="input w-40" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All Status</option>
            {['ACTIVE','HARVESTING','PENDING','COMPLETED','INACTIVE'].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={MapPin} title="No fields found" description="Register your first areca field." action={<button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" />Add Field</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Field Name</th>
                  <th className="table-th">Owner</th>
                  <th className="table-th hidden sm:table-cell">Village</th>
                  <th className="table-th hidden md:table-cell">Area</th>
                  <th className="table-th hidden md:table-cell">Trees</th>
                  <th className="table-th">Expected Yield</th>
                  <th className="table-th">Progress</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(f => (
                    <tr key={f.id} className="hover:bg-gray-50">
                      <td className="table-td font-medium">{f.fieldName}</td>
                      <td className="table-td text-gray-600">{f.ownerName}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{f.village || '—'}</td>
                      <td className="table-td hidden md:table-cell">{f.acreage ? `${f.acreage} ac` : '—'}</td>
                      <td className="table-td hidden md:table-cell">{f.treeCount ?? '—'}</td>
                      <td className="table-td">{f.expectedYieldKg ? formatKg(f.expectedYieldKg) : '—'}</td>
                      <td className="table-td w-32">
                        {f.progressPercent != null ? (
                          <div className="flex items-center gap-2">
                            <div className="flex-1 bg-gray-100 rounded-full h-1.5">
                              <div className="bg-primary-500 h-1.5 rounded-full" style={{ width: `${Math.min(100, f.progressPercent)}%` }} />
                            </div>
                            <span className="text-xs text-gray-500 w-8 text-right">{f.progressPercent.toFixed(0)}%</span>
                          </div>
                        ) : '—'}
                      </td>
                      <td className="table-td"><span className={fieldStatusColor[f.status]}>{f.status}</span></td>
                      <td className="table-td text-right">
                        <button className="btn btn-secondary btn-sm px-2" onClick={() => openEdit(f)}><Edit2 className="h-3.5 w-3.5" /></button>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Field' : 'Add Field'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Owner *</label>
              <select {...register('ownerId', { valueAsNumber: true })} className="input">
                <option value="">Select owner…</option>
                {owners.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
              </select>
              {errors.ownerId && <p className="text-xs text-red-600 mt-1">{errors.ownerId.message}</p>}
            </div>
            <div>
              <label className="label">Field Name *</label>
              <input {...register('fieldName')} className="input" placeholder="North Plot" />
              {errors.fieldName && <p className="text-xs text-red-600 mt-1">{errors.fieldName.message}</p>}
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
              <label className="label">Acreage</label>
              <input {...register('acreage')} type="number" step="0.01" className="input" placeholder="2.50" />
            </div>
            <div>
              <label className="label">Tree Count</label>
              <input {...register('treeCount')} type="number" className="input" placeholder="120" />
            </div>
            <div>
              <label className="label">Expected Yield (KG)</label>
              <input {...register('expectedYieldKg')} type="number" step="0.001" className="input" placeholder="5000" />
            </div>
            <div>
              <label className="label">Status</label>
              <select {...register('status')} className="input">
                {['ACTIVE','HARVESTING','PENDING','COMPLETED','INACTIVE'].map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="col-span-2">
              <label className="label">Notes</label>
              <textarea {...register('notes')} className="input" rows={2} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : editing ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
