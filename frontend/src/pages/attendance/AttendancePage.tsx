import { useState, useEffect, useCallback } from 'react'
import { Plus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { attendanceApi, workersApi, fieldsApi, contractsApi } from '@/api'
import type { Attendance, Worker, Field, Contract, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { attendanceStatusColor, formatCurrency, formatDate, todayISO } from '@/utils'
import { ClipboardList } from 'lucide-react'

const schema = z.object({
  workerId: z.coerce.number().min(1),
  fieldId: z.coerce.number().min(1),
  contractId: z.coerce.number().optional(),
  attendanceDate: z.string().min(1),
  status: z.enum(['PRESENT','HALF_DAY','ABSENT','LEAVE']),
  checkInTime: z.string().optional(),
  checkOutTime: z.string().optional(),
  workingHours: z.coerce.number().optional(),
  overtimeHours: z.coerce.number().optional(),
  overtimeRate: z.coerce.number().optional(),
  remarks: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function AttendancePage() {
  const [paged, setPaged] = useState<PagedResponse<Attendance> | null>(null)
  const [workers, setWorkers] = useState<Worker[]>([])
  const [fields, setFields] = useState<Field[]>([])
  const [contracts, setContracts] = useState<Contract[]>([])
  const [page, setPage] = useState(0)
  const [dateFilter, setDateFilter] = useState(todayISO())
  const [workerFilter, setWorkerFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { attendanceDate: todayISO(), status: 'PRESENT' },
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, ws, fs, cs] = await Promise.all([
        attendanceApi.list({
          fromDate: dateFilter || undefined,
          toDate: dateFilter || undefined,
          workerId: workerFilter || undefined,
          page, size: 20,
        }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
        fieldsApi.list({ size: 100 }),
        contractsApi.list({ status: 'ACTIVE', size: 100 }),
      ])
      setPaged(data)
      setWorkers(ws.content)
      setFields(fs.content)
      setContracts(cs.content)
    } finally { setLoading(false) }
  }, [dateFilter, workerFilter, page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await attendanceApi.mark(data)
      toast.success('Attendance marked — wage ledger updated automatically')
      setModalOpen(false)
      reset({ attendanceDate: todayISO(), status: 'PRESENT' })
      load()
    } finally { setSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Attendance</h1>
          <p className="text-sm text-gray-500 mt-0.5">Daily attendance tracking</p>
        </div>
        <button className="btn-primary btn" onClick={() => setModalOpen(true)}>
          <Plus className="h-4 w-4" /> Mark Attendance
        </button>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-3 mb-4">
          <div>
            <label className="label text-xs">Date</label>
            <input type="date" className="input" value={dateFilter} onChange={e => setDateFilter(e.target.value)} />
          </div>
          <div className="flex-1 min-w-48">
            <label className="label text-xs">Worker</label>
            <select className="input" value={workerFilter} onChange={e => setWorkerFilter(e.target.value)}>
              <option value="">All Workers</option>
              {workers.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
            </select>
          </div>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={ClipboardList} title="No attendance records" description={`No attendance for ${formatDate(dateFilter)}`} action={<button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" />Mark Attendance</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Date</th>
                  <th className="table-th">Worker</th>
                  <th className="table-th">Field</th>
                  <th className="table-th">Status</th>
                  <th className="table-th hidden sm:table-cell">Working Hours</th>
                  <th className="table-th hidden sm:table-cell">Overtime</th>
                  <th className="table-th text-right">Wage</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(a => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">{a.attendanceDate}</td>
                      <td className="table-td font-medium">{a.workerName}</td>
                      <td className="table-td text-gray-500">{a.fieldName}</td>
                      <td className="table-td"><span className={attendanceStatusColor[a.status]}>{a.status}</span></td>
                      <td className="table-td hidden sm:table-cell">{a.workingHours != null ? `${a.workingHours}h` : '—'}</td>
                      <td className="table-td hidden sm:table-cell">{a.overtimeHours && a.overtimeHours > 0 ? `${a.overtimeHours}h` : '—'}</td>
                      <td className="table-td text-right font-semibold text-gray-900">{a.wageForDay != null ? formatCurrency(a.wageForDay) : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Mark Attendance">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
            <div>
              <label className="label">Worker *</label>
              <select {...register('workerId', { valueAsNumber: true })} className="input">
                <option value="">Select worker…</option>
                {workers.map(w => <option key={w.id} value={w.id}>{w.name} — ₹{w.dailyWage}/day</option>)}
              </select>
              {errors.workerId && <p className="text-xs text-red-600 mt-1">Required</p>}
            </div>
            <div>
              <label className="label">Field *</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">Select field…</option>
                {fields.map(f => <option key={f.id} value={f.id}>{f.fieldName}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Contract</label>
              <select {...register('contractId', { valueAsNumber: true })} className="input">
                <option value="">None</option>
                {contracts.map(c => <option key={c.id} value={c.id}>{c.contractNumber}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Date *</label>
              <input {...register('attendanceDate')} type="date" className="input" />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Status *</label>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {(['PRESENT','HALF_DAY','ABSENT','LEAVE'] as const).map(s => (
                  <label key={s} className="flex items-center gap-2 p-2 rounded-xl border border-gray-200 dark:border-gray-700 cursor-pointer hover:border-primary-400 has-[:checked]:border-primary-500 has-[:checked]:bg-primary-50 dark:has-[:checked]:bg-primary-950/40">
                    <input {...register('status')} type="radio" value={s} className="text-primary-600" />
                    <span className="text-xs font-medium">{s.replace('_',' ')}</span>
                  </label>
                ))}
              </div>
            </div>
            <div>
              <label className="label">Check In</label>
              <input {...register('checkInTime')} type="time" className="input" />
            </div>
            <div>
              <label className="label">Check Out</label>
              <input {...register('checkOutTime')} type="time" className="input" />
            </div>
            <div>
              <label className="label">Working Hours</label>
              <input {...register('workingHours')} type="number" step="0.5" className="input" placeholder="8" />
            </div>
            <div>
              <label className="label">Overtime Hours</label>
              <input {...register('overtimeHours')} type="number" step="0.5" className="input" placeholder="0" />
            </div>
            <div>
              <label className="label">Overtime Rate (₹/hr)</label>
              <input {...register('overtimeRate')} type="number" step="0.01" className="input" placeholder="0" />
            </div>
            <div>
              <label className="label">Remarks</label>
              <input {...register('remarks')} className="input" />
            </div>
          </div>
          <p className="text-xs text-gray-500 bg-blue-50 rounded-lg p-3">
            ℹ️ Wage ledger will be updated automatically when attendance is marked as PRESENT or HALF_DAY.
          </p>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Mark Attendance'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
