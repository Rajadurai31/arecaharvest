import { useState, useEffect } from 'react'
import { schedulesApi, fieldsApi, contractsApi, workersApi } from '@/api'
import type { Schedule, Field, Contract, Worker } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import StatCard from '@/components/common/StatCard'
import { formatDate, formatKg } from '@/utils'
import { Calendar, Plus, CheckCircle, XCircle, Clock, MapPin, Users, Truck, FileText, ArrowRight } from 'lucide-react'
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
        schedulesApi.list({ fromDate: new Date().toISOString().split('T')[0], size: 50 }).then(r =>
          r.content.filter(s => s.scheduleDate !== new Date().toISOString().split('T')[0])
        ),
        fieldsApi.list({ size: 100 }),
        contractsApi.list({ status: 'ACTIVE', size: 100 }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
      ])
      setToday(t)
      setUpcoming(u)
      setFields(fs.content)
      setContracts(cs.content)
      setWorkers(ws.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await schedulesApi.create({ ...data, workerIds: selectedWorkers })
      toast.success('Schedule created successfully')
      setModalOpen(false)
      reset()
      setSelectedWorkers([])
      load()
    } catch {
      /* handled */
    } finally {
      setSaving(false)
    }
  }

  const markStatus = async (id: number, status: string) => {
    await schedulesApi.updateStatus(id, status)
    toast.success(`Schedule updated to ${status.toLowerCase()}`)
    load()
  }

  if (loading) return <LoadingSpinner center size="lg" />

  const totalTodayKg = today.reduce((sum, s) => sum + (s.expectedQuantityKg ?? 0), 0)
  const totalTodayWorkers = today.reduce((sum, s) => sum + (s.expectedWorkerCount ?? (s.workers?.length || 0)), 0)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1>Harvest Schedule</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Operational field schedules, labor assignments, and harvest targets
          </p>
        </div>
        <button
          className="btn-primary"
          onClick={() => {
            setModalOpen(true)
            reset({ scheduleDate: new Date().toISOString().split('T')[0] })
          }}
        >
          <Plus className="h-4 w-4" /> Schedule Operation
        </button>
      </div>

      {/* Stat Cards Overview */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Today's Active Tasks"
          value={today.length}
          icon={Calendar}
          color="green"
          subtitle="Field sessions scheduled today"
        />
        <StatCard
          label="Today's Target Yield"
          value={formatKg(totalTodayKg)}
          icon={Calendar}
          color="blue"
          subtitle="Estimated bunch weight"
        />
        <StatCard
          label="Harvesters Deployed"
          value={totalTodayWorkers}
          icon={Users}
          color="amber"
          subtitle="Climbers, cutters & loaders"
        />
        <StatCard
          label="Upcoming Operations"
          value={upcoming.length}
          icon={Calendar}
          color="purple"
          subtitle="Next 7 days roadmap"
        />
      </div>

      {/* Main Grid: Today's Work & Upcoming Work side-by-side on Desktop */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 items-start">
        {/* Today's Schedule Panel */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
                <Calendar className="h-4 w-4" />
              </div>
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                Today's Operations ({today.length})
              </h2>
            </div>
            <span className="badge-green">LIVE TODAY</span>
          </div>

          {today.length === 0 ? (
            <EmptyState
              title="Nothing scheduled today"
              description="Add today's plantation harvest schedule to assign field crews."
            />
          ) : (
            <div className="space-y-3">
              {today.map(s => (
                <div
                  key={s.id}
                  className="p-4 rounded-xl border border-slate-200/80 dark:border-slate-800 hover:border-emerald-300 dark:hover:border-emerald-700 bg-white dark:bg-slate-900/60 transition-all hover:shadow-md"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-extrabold text-base text-slate-900 dark:text-white">
                          {s.field?.fieldName ?? `Field #${s.fieldId}`}
                        </span>
                        {s.field?.village && (
                          <span className="text-xs text-slate-400 flex items-center gap-0.5">
                            <MapPin className="h-3 w-3" /> {s.field.village}
                          </span>
                        )}
                      </div>

                      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-500 dark:text-slate-400">
                        {s.plannedStartTime && (
                          <span className="flex items-center gap-1 font-mono">
                            <Clock className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400" />
                            {s.plannedStartTime.slice(0, 5)} - {s.plannedEndTime ? s.plannedEndTime.slice(0, 5) : '17:00'}
                          </span>
                        )}
                        {s.expectedWorkerCount && (
                          <span className="flex items-center gap-1">
                            <Users className="h-3.5 w-3.5" />
                            {s.expectedWorkerCount} crew members
                          </span>
                        )}
                        {s.expectedQuantityKg && (
                          <span className="font-semibold text-emerald-700 dark:text-emerald-400">
                            • Target: {formatKg(s.expectedQuantityKg)}
                          </span>
                        )}
                      </div>

                      {s.notes && (
                        <p className="text-xs text-slate-600 dark:text-slate-400 italic pt-1">
                          "{s.notes}"
                        </p>
                      )}
                    </div>

                    <div className="flex flex-col items-end gap-2 shrink-0">
                      <span
                        className={`badge ${
                          s.status === 'COMPLETED'
                            ? 'badge-green'
                            : s.status === 'IN_PROGRESS'
                            ? 'badge-blue'
                            : s.status === 'CANCELLED'
                            ? 'badge-red'
                            : 'badge-amber'
                        }`}
                      >
                        {s.status.replace('_', ' ')}
                      </span>

                      {s.status !== 'COMPLETED' && s.status !== 'CANCELLED' && (
                        <div className="flex items-center gap-1 pt-1">
                          <button
                            className="btn btn-secondary btn-sm p-1.5 hover:bg-emerald-50 dark:hover:bg-emerald-950/60"
                            onClick={() => markStatus(s.id, 'COMPLETED')}
                            title="Mark Completed"
                          >
                            <CheckCircle className="h-4 w-4 text-emerald-600" />
                          </button>
                          <button
                            className="btn btn-secondary btn-sm p-1.5 hover:bg-rose-50 dark:hover:bg-rose-950/60"
                            onClick={() => markStatus(s.id, 'CANCELLED')}
                            title="Cancel Operation"
                          >
                            <XCircle className="h-4 w-4 text-rose-500" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Upcoming Work Panel */}
        <div className="card space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-sky-100 dark:bg-sky-950/60 text-sky-600 dark:text-sky-400">
                <Calendar className="h-4 w-4" />
              </div>
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                Upcoming Pipeline ({upcoming.length})
              </h2>
            </div>
            <span className="badge-blue">NEXT 7 DAYS</span>
          </div>

          {upcoming.length === 0 ? (
            <EmptyState
              title="No upcoming work"
              description="Plan future operations to maintain steady areca harvesting throughput."
            />
          ) : (
            <div className="space-y-3">
              {upcoming.map(s => (
                <div
                  key={s.id}
                  className="p-3.5 rounded-xl border border-slate-200/80 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 bg-white dark:bg-slate-900/60 transition-all flex items-center justify-between gap-3"
                >
                  <div className="space-y-0.5 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="px-2 py-0.5 rounded text-[11px] font-mono font-bold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                        {formatDate(s.scheduleDate)}
                      </span>
                      <span className="font-bold text-sm text-slate-900 dark:text-white truncate">
                        {s.field?.fieldName ?? `Field #${s.fieldId}`}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slate-500 dark:text-slate-400">
                      {s.expectedWorkerCount && <span>{s.expectedWorkerCount} workers</span>}
                      {s.expectedQuantityKg && <span>• {formatKg(s.expectedQuantityKg)} target</span>}
                    </div>
                  </div>

                  <span className="badge-amber shrink-0">{s.status}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Schedule Work Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Schedule Harvest Operation" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Operation Date *</label>
              <input {...register('scheduleDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Plantation Field *</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">Select Field…</option>
                {fields.map(f => (
                  <option key={f.id} value={f.id}>
                    {f.fieldName} ({f.village})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Harvest Contract</label>
              <select {...register('contractId', { valueAsNumber: true })} className="input">
                <option value="">None (Ad-hoc Harvest)</option>
                {contracts.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.contractNumber} ({c.ownerName})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Planned Start Time</label>
              <input {...register('plannedStartTime')} type="time" className="input" />
            </div>
            <div>
              <label className="label">Target Yield (KG)</label>
              <input {...register('expectedQuantityKg')} type="number" step="0.001" className="input" placeholder="e.g. 2500" />
            </div>
            <div>
              <label className="label">Estimated Crew Size</label>
              <input {...register('expectedWorkerCount')} type="number" className="input" placeholder="e.g. 5" />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Assign Labor & Climbers</label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-40 overflow-y-auto border border-slate-200 dark:border-slate-700 rounded-xl p-3 bg-slate-50/50 dark:bg-slate-800/40 custom-scrollbar">
                {workers.map(w => (
                  <label key={w.id} className="flex items-center gap-2 text-xs font-medium cursor-pointer hover:text-emerald-600 transition-colors">
                    <input
                      type="checkbox"
                      checked={selectedWorkers.includes(w.id)}
                      onChange={() =>
                        setSelectedWorkers(p =>
                          p.includes(w.id) ? p.filter(x => x !== w.id) : [...p, w.id]
                        )
                      }
                      className="rounded text-emerald-600 focus:ring-emerald-500"
                    />
                    <span className="truncate">{w.name} ({w.skillType})</span>
                  </label>
                ))}
              </div>
            </div>
            <div className="sm:col-span-2">
              <label className="label">Operation Notes</label>
              <textarea
                {...register('notes')}
                className="input"
                rows={2}
                placeholder="Special notes on block numbers, climbing rope requirement, transport truck..."
              />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-3 border-t border-slate-200 dark:border-slate-800">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Scheduling…' : 'Confirm Schedule'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
