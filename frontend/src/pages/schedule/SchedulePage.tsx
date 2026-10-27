import { useState, useEffect } from 'react'
import { schedulesApi, fieldsApi, contractsApi, workersApi } from '@/api'
import type { Schedule, Field, Contract, Worker } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { formatDate, formatKg } from '@/utils'
import { Calendar, Plus, CheckCircle, XCircle } from 'lucide-react'
import Modal from '@/components/common/Modal'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import toast from 'react-hot-toast'

const schema = z.object({
  scheduleDate: z.string().min(1),
  fieldId: z.coerce.number().min(1),
  contractId: z.coerce.number().optional(),
  plannedStartTime: z.string().optional(),
  expectedQuantityKg: z.coerce.number().optional(),
  expectedWorkerCount: z.coerce.number().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function SchedulePage() {
  const [today, setToday] = useState<Schedule[]>([])
  const [upcoming, setUpcoming] = useState<Schedule[]>([])
  const [fields, setFields] = useState<Field[]>([])
  const [contracts, setContracts] = useState<Contract[]>([])
  const [workers, setWorkers] = useState<Worker[]>([])
  const [selectedWorkers, setSelectedWorkers] = useState<number[]>([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { scheduleDate: new Date().toISOString().split('T')[0] },
  })

  const load = async () => {
    setLoading(true)
    try {
      const [t, u, fs, cs, ws] = await Promise.all([
        schedulesApi.getToday(),
        schedulesApi.list({ status: 'PLANNED', fromDate: new Date().toISOString().split('T')[0], size: 20 }).then(r => r.content),
        fieldsApi.list({ size: 100 }),
        contractsApi.list({ status: 'ACTIVE', size: 100 }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
      ])
      setToday(t); setUpcoming(u); setFields(fs.content); setContracts(cs.content); setWorkers(ws.content)
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await schedulesApi.create({ ...data, workerIds: selectedWorkers })
      toast.success('Schedule created')
      setModalOpen(false); reset(); setSelectedWorkers([]); load()
    } catch { /* handled */ } finally { setSaving(false) }
  }

  const markStatus = async (id: number, status: string) => {
    await schedulesApi.updateStatus(id, status)
    toast.success(`Schedule ${status.toLowerCase()}`)
    load()
  }

  if (loading) return <LoadingSpinner center />

  return (
    <div className="space-y-6">
      <div className="page-header">
        <h1>Work Schedule</h1>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset({ scheduleDate: new Date().toISOString().split('T')[0] }) }}>
          <Plus className="h-4 w-4" /> Schedule Work
        </button>
      </div>

      {/* Today */}
      <div className="card">
        <h2 className="mb-4 flex items-center gap-2">
          <Calendar className="h-4 w-4 text-primary-600" />
          Today's Work ({today.length})
        </h2>
        {today.length === 0 ? (
          <EmptyState title="Nothing scheduled today" description="Add today's work to get started." />
        ) : (
          <div className="space-y-3">
            {today.map(s => (
              <div key={s.id} className="flex items-center justify-between p-3 rounded-xl border border-gray-100 hover:border-primary-200 hover:bg-primary-50 transition-colors">
                <div>
                  <p className="font-medium text-gray-900">{s.field?.fieldName ?? `Field #${s.fieldId}`}</p>
                  <p className="text-sm text-gray-500">
                    {s.plannedStartTime && `${s.plannedStartTime} • `}
                    {s.expectedWorkerCount && `${s.expectedWorkerCount} workers`}
                    {s.expectedQuantityKg && ` • ${formatKg(s.expectedQuantityKg)} expected`}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`badge ${s.status === 'COMPLETED' ? 'badge-green' : s.status === 'CANCELLED' ? 'badge-red' : 'badge-amber'}`}>{s.status}</span>
                  {s.status === 'PLANNED' && (
                    <>
                      <button className="btn btn-secondary btn-sm px-2" onClick={() => markStatus(s.id, 'COMPLETED')} title="Mark Completed"><CheckCircle className="h-3.5 w-3.5 text-green-600" /></button>
                      <button className="btn btn-secondary btn-sm px-2" onClick={() => markStatus(s.id, 'CANCELLED')} title="Cancel"><XCircle className="h-3.5 w-3.5 text-red-500" /></button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Upcoming */}
      <div className="card">
        <h2 className="mb-4">Upcoming Work ({upcoming.length})</h2>
        {upcoming.length === 0 ? <p className="text-sm text-gray-500">No upcoming work scheduled.</p> : (
          <div className="space-y-2">
            {upcoming.map(s => (
              <div key={s.id} className="flex items-center justify-between p-3 rounded-xl border border-gray-100">
                <div>
                  <p className="font-medium text-gray-900">{formatDate(s.scheduleDate)} — {s.field?.fieldName ?? `Field #${s.fieldId}`}</p>
                  {s.expectedWorkerCount && <p className="text-xs text-gray-500">{s.expectedWorkerCount} workers planned</p>}
                </div>
                <span className="badge-amber badge">PLANNED</span>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Schedule Work" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
            <div>
              <label className="label">Date *</label>
              <input {...register('scheduleDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Field *</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">Select…</option>
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
              <label className="label">Planned Start Time</label>
              <input {...register('plannedStartTime')} type="time" className="input" />
            </div>
            <div>
              <label className="label">Expected KG</label>
              <input {...register('expectedQuantityKg')} type="number" step="0.001" className="input" />
            </div>
            <div>
              <label className="label">Expected Workers</label>
              <input {...register('expectedWorkerCount')} type="number" className="input" />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Assign Workers</label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-36 overflow-y-auto border border-gray-200 dark:border-gray-700 rounded-xl p-3">
                {workers.map(w => (
                  <label key={w.id} className="flex items-center gap-2 text-sm cursor-pointer">
                    <input type="checkbox" checked={selectedWorkers.includes(w.id)} onChange={() => setSelectedWorkers(p => p.includes(w.id) ? p.filter(x => x !== w.id) : [...p, w.id])} className="rounded text-primary-600" />
                    <span>{w.name}</span>
                  </label>
                ))}
              </div>
            </div>
            <div className="sm:col-span-2">
              <label className="label">Notes</label>
              <textarea {...register('notes')} className="input" rows={2} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Schedule'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
