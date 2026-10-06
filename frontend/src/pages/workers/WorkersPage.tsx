import { useState, useEffect, useCallback } from 'react'
import { Plus, Edit2, Eye } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { workersApi } from '@/api'
import type { Worker, PagedResponse } from '@/types'
import SearchInput from '@/components/common/SearchInput'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { workerStatusColor, formatCurrency } from '@/utils'
import { UserCheck } from 'lucide-react'

const schema = z.object({
  name: z.string().min(1, 'Name required'),
  phone: z.string().regex(/^[6-9]\d{9}$/, 'Invalid mobile'),
  skillType: z.string().min(1, 'Skill required'),
  dailyWage: z.coerce.number().min(1, 'Wage required'),
  experienceYears: z.coerce.number().optional(),
  village: z.string().optional(),
  district: z.string().optional(),
  joiningDate: z.string().optional(),
  status: z.string().optional(),
  emergencyContactName: z.string().optional(),
  emergencyContactPhone: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const SKILLS = ['CLIMBER','CUTTER','COLLECTOR','LOADER','HELPER','DRIVER','OTHER']

export default function WorkersPage() {
  const [paged, setPaged] = useState<PagedResponse<Worker> | null>(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [skillFilter, setSkillFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Worker | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const data = await workersApi.list({
        search: search || undefined,
        status: statusFilter || undefined,
        skillType: skillFilter || undefined,
        page, size: 20,
      })
      setPaged(data)
    } finally { setLoading(false) }
  }, [search, statusFilter, skillFilter, page])

  useEffect(() => { load() }, [load])
  useEffect(() => { setPage(0) }, [search, statusFilter, skillFilter])

  const openCreate = () => { setEditing(null); reset({ status: 'ACTIVE', skillType: 'HELPER' }); setModalOpen(true) }
  const openEdit = (w: Worker) => {
    setEditing(w)
    reset({ name: w.name, phone: w.phone, skillType: w.skillType, dailyWage: w.dailyWage,
            experienceYears: w.experienceYears ?? 0, village: w.village ?? '', district: w.district ?? '',
            joiningDate: w.joiningDate ?? '', status: w.status,
            emergencyContactName: w.emergencyContactName ?? '', notes: w.notes ?? '' })
    setModalOpen(true)
  }

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      if (editing) { await workersApi.update(editing.id, data as Partial<Worker>); toast.success('Worker updated') }
      else { await workersApi.create(data as Partial<Worker>); toast.success('Worker added') }
      setModalOpen(false); load()
    } finally { setSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Workers</h1>
          <p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} workers registered</p>
        </div>
        <button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" /> Add Worker</button>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-3 mb-4">
          <SearchInput value={search} onChange={setSearch} placeholder="Search by name or phone…" className="flex-1 min-w-48" />
          <select className="input w-36" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All Status</option>
            {['ACTIVE','INACTIVE','BLACKLISTED'].map(s => <option key={s}>{s}</option>)}
          </select>
          <select className="input w-36" value={skillFilter} onChange={e => setSkillFilter(e.target.value)}>
            <option value="">All Skills</option>
            {SKILLS.map(s => <option key={s}>{s}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={UserCheck} title="No workers found" action={<button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" />Add Worker</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Name</th>
                  <th className="table-th">Phone</th>
                  <th className="table-th hidden sm:table-cell">Skill</th>
                  <th className="table-th hidden md:table-cell">Experience</th>
                  <th className="table-th">Daily Wage</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(w => (
                    <tr key={w.id} className="hover:bg-gray-50">
                      <td className="table-td font-medium">{w.name}</td>
                      <td className="table-td">{w.phone}</td>
                      <td className="table-td hidden sm:table-cell"><span className="badge-blue badge">{w.skillType}</span></td>
                      <td className="table-td hidden md:table-cell">{w.experienceYears != null ? `${w.experienceYears} yrs` : '—'}</td>
                      <td className="table-td font-semibold text-gray-900">{formatCurrency(w.dailyWage)}</td>
                      <td className="table-td"><span className={workerStatusColor[w.status]}>{w.status}</span></td>
                      <td className="table-td text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Link to={`/workers/${w.id}`} className="btn btn-secondary btn-sm px-2"><Eye className="h-3.5 w-3.5" /></Link>
                          <button className="btn btn-secondary btn-sm px-2" onClick={() => openEdit(w)}><Edit2 className="h-3.5 w-3.5" /></button>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Worker' : 'Add Worker'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Full Name *</label>
              <input {...register('name')} className="input" placeholder="Raju Gowda" />
              {errors.name && <p className="text-xs text-red-600 mt-1">{errors.name.message}</p>}
            </div>
            <div>
              <label className="label">Phone *</label>
              <input {...register('phone')} className="input" placeholder="9876543210" />
              {errors.phone && <p className="text-xs text-red-600 mt-1">{errors.phone.message}</p>}
            </div>
            <div>
              <label className="label">Skill Type *</label>
              <select {...register('skillType')} className="input">
                {SKILLS.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
              {errors.skillType && <p className="text-xs text-red-600 mt-1">{errors.skillType.message}</p>}
            </div>
            <div>
              <label className="label">Daily Wage (₹) *</label>
              <input {...register('dailyWage')} type="number" step="0.01" className="input" placeholder="900" />
              {errors.dailyWage && <p className="text-xs text-red-600 mt-1">{errors.dailyWage.message}</p>}
            </div>
            <div>
              <label className="label">Experience (years)</label>
              <input {...register('experienceYears')} type="number" step="0.5" className="input" placeholder="5" />
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
              <label className="label">Joining Date</label>
              <input {...register('joiningDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Status</label>
              <select {...register('status')} className="input">
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
            <div>
              <label className="label">Emergency Contact Name</label>
              <input {...register('emergencyContactName')} className="input" />
            </div>
            <div className="col-span-2">
              <label className="label">Notes</label>
              <textarea {...register('notes')} className="input" rows={2} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : editing ? 'Update' : 'Add Worker'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
