import { useState, useEffect, useCallback } from 'react'
import { Plus } from 'lucide-react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { harvestApi, fieldsApi, contractsApi, workersApi } from '@/api'
import type { HarvestSession, Field, Contract, Worker, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { sessionStatusColor, formatKg, formatDate, todayISO } from '@/utils'
import { Leaf } from 'lucide-react'

const schema = z.object({
  sessionDate: z.string().min(1),
  fieldId: z.coerce.number().min(1),
  contractId: z.coerce.number().min(1),
  totalBags: z.coerce.number().min(0).optional(),
  totalQuantity: z.coerce.number().min(0),
  startTime: z.string().optional(),
  endTime: z.string().optional(),
  status: z.string(),
  remarks: z.string().optional(),
  workerIds: z.array(z.number()).optional(),
})
type FormData = z.infer<typeof schema>

export default function HarvestPage() {
  const [paged, setPaged] = useState<PagedResponse<HarvestSession> | null>(null)
  const [fields, setFields] = useState<Field[]>([])
  const [contracts, setContracts] = useState<Contract[]>([])
  const [workers, setWorkers] = useState<Worker[]>([])
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [selectedWorkers, setSelectedWorkers] = useState<number[]>([])

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { sessionDate: todayISO(), status: 'COMPLETED', totalQuantity: 0 },
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, fs, cs, ws] = await Promise.all([
        harvestApi.list({ page, size: 20 }),
        fieldsApi.list({ size: 100 }),
        contractsApi.list({ status: 'ACTIVE', size: 100 }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
      ])
      setPaged(data); setFields(fs.content); setContracts(cs.content); setWorkers(ws.content)
    } finally { setLoading(false) }
  }, [page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await harvestApi.create({ ...data, workerIds: selectedWorkers })
      toast.success('Harvest session recorded' + (data.status === 'COMPLETED' ? ' — inventory updated' : ''))
      setModalOpen(false); reset(); setSelectedWorkers([]); load()
    } finally { setSaving(false) }
  }

  const toggleWorker = (id: number) =>
    setSelectedWorkers(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id])

  return (
    <div>
      <div className="page-header">
        <div><h1>Harvest Sessions</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} sessions</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset({ sessionDate: todayISO(), status: 'COMPLETED', totalQuantity: 0 }); setSelectedWorkers([]) }}>
          <Plus className="h-4 w-4" /> Record Session
        </button>
      </div>

      <div className="card">
        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Leaf} title="No harvest sessions" />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Date</th>
                  <th className="table-th">Field</th>
                  <th className="table-th hidden md:table-cell">Contract</th>
                  <th className="table-th">Workers</th>
                  <th className="table-th">Bags</th>
                  <th className="table-th">Quantity</th>
                  <th className="table-th">Status</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(s => (
                    <tr key={s.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">{formatDate(s.sessionDate)}</td>
                      <td className="table-td font-medium">{s.fieldName}</td>
                      <td className="table-td hidden md:table-cell text-gray-500">{s.contractNumber}</td>
                      <td className="table-td">{s.workerCount}</td>
                      <td className="table-td">{s.totalBags ?? 0}</td>
                      <td className="table-td font-semibold text-primary-700">{formatKg(s.totalQuantity)}</td>
                      <td className="table-td"><span className={sessionStatusColor[s.status]}>{s.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Record Harvest Session" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Date *</label>
              <input {...register('sessionDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Field *</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">Select…</option>
                {fields.map(f => <option key={f.id} value={f.id}>{f.fieldName}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Contract *</label>
              <select {...register('contractId', { valueAsNumber: true })} className="input">
                <option value="">Select…</option>
                {contracts.map(c => <option key={c.id} value={c.id}>{c.contractNumber} — {c.fieldName}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Status *</label>
              <select {...register('status')} className="input">
                <option value="PLANNED">PLANNED</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div>
              <label className="label">Quantity (KG) *</label>
              <input {...register('totalQuantity')} type="number" step="0.001" className="input" placeholder="0.000" />
            </div>
            <div>
              <label className="label">Number of Bags</label>
              <input {...register('totalBags')} type="number" className="input" placeholder="0" />
            </div>
            <div>
              <label className="label">Start Time</label>
              <input {...register('startTime')} type="time" className="input" />
            </div>
            <div>
              <label className="label">End Time</label>
              <input {...register('endTime')} type="time" className="input" />
            </div>
            <div className="col-span-2">
              <label className="label">Workers Present</label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-40 overflow-y-auto border border-gray-200 rounded-lg p-3">
                {workers.map(w => (
                  <label key={w.id} className="flex items-center gap-2 text-sm cursor-pointer">
                    <input type="checkbox" checked={selectedWorkers.includes(w.id)} onChange={() => toggleWorker(w.id)} className="rounded text-primary-600" />
                    <span className={selectedWorkers.includes(w.id) ? 'font-medium text-gray-900' : 'text-gray-600'}>{w.name}</span>
                  </label>
                ))}
              </div>
              <p className="text-xs text-gray-500 mt-1">{selectedWorkers.length} workers selected</p>
            </div>
            <div className="col-span-2">
              <label className="label">Remarks</label>
              <textarea {...register('remarks')} className="input" rows={2} />
            </div>
          </div>
          {/* Info about auto-inventory */}
          <p className="text-xs text-blue-700 bg-blue-50 rounded-lg p-3">
            ℹ️ Setting status to COMPLETED will automatically create an inventory entry for this harvest quantity.
          </p>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Record Session'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
