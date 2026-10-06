import { useState, useEffect, useCallback } from 'react'
import { Plus, XCircle, CheckCircle } from 'lucide-react'
import api from '@/api/axios'
import { workersApi, fieldsApi, contractsApi } from '@/api'
import type { Worker, Field, Contract, PagedResponse } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import Pagination from '@/components/common/Pagination'
import Modal from '@/components/common/Modal'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { UserCheck } from 'lucide-react'
import { formatDate } from '@/utils'
import { todayISO } from '@/utils'

export default function WorkerAssignmentPage() {
  const [paged, setPaged] = useState<PagedResponse<any> | null>(null)
  const [workers, setWorkers] = useState<Worker[]>([])
  const [fields, setFields] = useState<Field[]>([])
  const [contracts, setContracts] = useState<Contract[]>([])
  const [page, setPage] = useState(0)
  const [workerFilter, setWorkerFilter] = useState('')
  const [fieldFilter, setFieldFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset } = useForm<any>({
    defaultValues: { assignedDate: todayISO() }
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, ws, fs, cs] = await Promise.all([
        api.get('/worker-assignments', { params: { workerId: workerFilter || undefined, fieldId: fieldFilter || undefined, page, size: 20 } }),
        workersApi.list({ status: 'ACTIVE', size: 200 }),
        fieldsApi.list({ size: 100 }),
        contractsApi.list({ status: 'ACTIVE', size: 100 }),
      ])
      setPaged(data.data.data); setWorkers(ws.content); setFields(fs.content); setContracts(cs.content)
    } finally { setLoading(false) }
  }, [workerFilter, fieldFilter, page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: any) => {
    setSaving(true)
    try {
      await api.post('/worker-assignments', data)
      toast.success('Assignment created'); setModalOpen(false); reset({ assignedDate: todayISO() }); load()
    } finally { setSaving(false) }
  }

  const cancel = async (id: number) => {
    await api.patch(`/worker-assignments/${id}/cancel`)
    toast.success('Assignment cancelled'); load()
  }

  const complete = async (id: number) => {
    await api.patch(`/worker-assignments/${id}/complete`)
    toast.success('Assignment completed'); load()
  }

  const statusColor: Record<string, string> = {
    ACTIVE: 'badge-green', COMPLETED: 'badge-blue', CANCELLED: 'badge-red'
  }

  return (
    <div>
      <div className="page-header">
        <div><h1>Worker Assignments</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} assignments</p></div>
        <button className="btn-primary btn" onClick={() => { setModalOpen(true); reset({ assignedDate: todayISO() }) }}>
          <Plus className="h-4 w-4" /> Assign Worker
        </button>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-3 mb-4">
          <select className="input w-48" value={workerFilter} onChange={e => setWorkerFilter(e.target.value)}>
            <option value="">All Workers</option>
            {workers.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
          <select className="input w-48" value={fieldFilter} onChange={e => setFieldFilter(e.target.value)}>
            <option value="">All Fields</option>
            {fields.map(f => <option key={f.id} value={f.id}>{f.fieldName}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content?.length ? (
          <EmptyState icon={UserCheck} title="No assignments" action={<button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" />Assign Worker</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Worker</th>
                  <th className="table-th">Field</th>
                  <th className="table-th hidden sm:table-cell">Contract</th>
                  <th className="table-th">Date</th>
                  <th className="table-th hidden sm:table-cell">Role</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map((a: any) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="table-td font-medium">{a.worker?.name}</td>
                      <td className="table-td">{a.field?.fieldName}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{a.contract?.contractNumber || '—'}</td>
                      <td className="table-td font-mono text-xs">{formatDate(a.assignedDate)}</td>
                      <td className="table-td hidden sm:table-cell"><span className="badge-blue badge">{a.assignedRole || '—'}</span></td>
                      <td className="table-td"><span className={statusColor[a.status] ?? 'badge-gray'}>{a.status}</span></td>
                      <td className="table-td text-right">
                        {a.status === 'ACTIVE' && (
                          <div className="flex items-center justify-end gap-1">
                            <button className="btn btn-secondary btn-sm px-2" title="Complete" onClick={() => complete(a.id)}><CheckCircle className="h-3.5 w-3.5 text-green-600" /></button>
                            <button className="btn btn-danger btn-sm px-2" title="Cancel" onClick={() => cancel(a.id)}><XCircle className="h-3.5 w-3.5" /></button>
                          </div>
                        )}
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Assign Worker">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Worker *</label>
              <select {...register('workerId', { valueAsNumber: true })} className="input">
                <option value="">Select…</option>
                {workers.map(w => <option key={w.id} value={w.id}>{w.name} ({w.skillType})</option>)}
              </select>
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
              <label className="label">Date *</label>
              <input {...register('assignedDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Assigned Role</label>
              <select {...register('assignedRole')} className="input">
                <option value="">Same as skill</option>
                {['CLIMBER','CUTTER','COLLECTOR','LOADER','HELPER','DRIVER','OTHER'].map(s => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Notes</label>
              <input {...register('notes')} className="input" />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Assign'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
