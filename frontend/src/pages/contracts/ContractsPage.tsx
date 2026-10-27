import { useState, useEffect, useCallback } from 'react'
import { Plus, Edit2, CheckCircle } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { contractsApi, ownersApi, fieldsApi } from '@/api'
import type { Contract, Owner, Field, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { contractStatusColor, formatCurrency, formatDate, formatKg } from '@/utils'
import { FileText } from 'lucide-react'

const schema = z.object({
  ownerId: z.coerce.number().min(1),
  fieldId: z.coerce.number().min(1),
  contractNumber: z.string().min(1),
  contractType: z.enum(['PER_KG','PER_TREE','FIXED_AMOUNT']),
  rate: z.coerce.number().min(0),
  fixedAmount: z.coerce.number().optional(),
  expectedQuantityKg: z.coerce.number().optional(),
  startDate: z.string().min(1),
  expectedEndDate: z.string().optional(),
  status: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function ContractsPage() {
  const [paged, setPaged] = useState<PagedResponse<Contract> | null>(null)
  const [owners, setOwners] = useState<Owner[]>([])
  const [fields, setFields] = useState<Field[]>([])
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Contract | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { contractType: 'PER_KG', status: 'DRAFT' },
  })
  const contractType = watch('contractType')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, os, fs] = await Promise.all([
        contractsApi.list({ status: statusFilter || undefined, page, size: 20 }),
        ownersApi.list({ active: true, size: 100 }),
        fieldsApi.list({ size: 200 }),
      ])
      setPaged(data); setOwners(os.content); setFields(fs.content)
    } finally { setLoading(false) }
  }, [statusFilter, page])

  useEffect(() => { load() }, [load])

  const openCreate = () => {
    setEditing(null)
    reset({ contractType: 'PER_KG', status: 'DRAFT', startDate: new Date().toISOString().split('T')[0], rate: 0 })
    setModalOpen(true)
  }

  const openEdit = (c: Contract) => {
    setEditing(c)
    reset({
      ownerId: c.ownerId,
      fieldId: c.fieldId,
      contractNumber: c.contractNumber,
      contractType: c.contractType,
      rate: c.rate ?? 0,
      fixedAmount: c.fixedAmount ?? undefined,
      expectedQuantityKg: c.expectedQuantityKg ?? undefined,
      startDate: c.startDate,
      expectedEndDate: c.expectedEndDate ?? '',
      status: c.status,
      notes: c.notes ?? '',
    })
    setModalOpen(true)
  }

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      if (editing) { await contractsApi.update(editing.id, data); toast.success('Contract updated') }
      else { await contractsApi.create(data); toast.success('Contract created') }
      setModalOpen(false); load()
    } finally { setSaving(false) }
  }

  const activate = async (c: Contract) => {
    await contractsApi.updateStatus(c.id, 'ACTIVE')
    toast.success('Contract activated')
    load()
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Contracts</h1>
          <p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} harvesting contracts</p>
        </div>
        <button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" /> New Contract</button>
      </div>

      <div className="card">
        <div className="flex gap-3 mb-4">
          <select className="input w-40" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All Status</option>
            {['DRAFT','ACTIVE','PAUSED','COMPLETED','CANCELLED'].map(s => <option key={s}>{s}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={FileText} title="No contracts" action={<button className="btn-primary btn" onClick={openCreate}><Plus className="h-4 w-4" />New Contract</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Contract #</th>
                  <th className="table-th">Owner / Field</th>
                  <th className="table-th hidden md:table-cell">Type</th>
                  <th className="table-th hidden md:table-cell">Rate</th>
                  <th className="table-th hidden lg:table-cell">Expected Rev.</th>
                  <th className="table-th">Progress</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(c => (
                    <tr key={c.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs font-medium">{c.contractNumber}</td>
                      <td className="table-td">
                        <div className="font-medium text-gray-900">{c.ownerName}</div>
                        <div className="text-xs text-gray-500">{c.fieldName}</div>
                      </td>
                      <td className="table-td hidden md:table-cell"><span className="badge-blue badge">{c.contractType}</span></td>
                      <td className="table-td hidden md:table-cell">₹{c.rate}</td>
                      <td className="table-td hidden lg:table-cell">{c.expectedRevenue != null ? formatCurrency(c.expectedRevenue) : '—'}</td>
                      <td className="table-td w-32">
                        {c.progressPercent != null ? (
                          <div className="flex items-center gap-2">
                            <div className="flex-1 bg-gray-100 rounded-full h-1.5">
                              <div className="bg-primary-500 h-1.5 rounded-full" style={{ width: `${Math.min(100, c.progressPercent)}%` }} />
                            </div>
                            <span className="text-xs">{c.progressPercent.toFixed(0)}%</span>
                          </div>
                        ) : '—'}
                      </td>
                      <td className="table-td"><span className={contractStatusColor[c.status]}>{c.status}</span></td>
                      <td className="table-td text-right">
                        <div className="flex items-center justify-end gap-1">
                          {c.status === 'DRAFT' && <button className="btn btn-secondary btn-sm px-2" onClick={() => activate(c)} title="Activate"><CheckCircle className="h-3.5 w-3.5 text-green-600" /></button>}
                          <button className="btn btn-secondary btn-sm px-2" onClick={() => openEdit(c)} title="Edit Contract"><Edit2 className="h-3.5 w-3.5" /></button>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Contract' : 'New Contract'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Owner *</label>
              <select {...register('ownerId', { valueAsNumber: true })} className="input">
                <option value="">Select owner…</option>
                {owners.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Field *</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">Select field…</option>
                {fields.map(f => <option key={f.id} value={f.id}>{f.fieldName} ({f.ownerName})</option>)}
              </select>
            </div>
            <div>
              <label className="label">Contract Number *</label>
              <input {...register('contractNumber')} className="input" placeholder="CTR-2026-001" />
            </div>
            <div>
              <label className="label">Contract Type *</label>
              <select {...register('contractType')} className="input">
                <option value="PER_KG">Per KG</option>
                <option value="PER_TREE">Per Tree</option>
                <option value="FIXED_AMOUNT">Fixed Amount</option>
              </select>
            </div>
            {contractType !== 'FIXED_AMOUNT' && (
              <div>
                <label className="label">Rate (₹/{contractType === 'PER_KG' ? 'KG' : 'Tree'}) *</label>
                <input {...register('rate')} type="number" step="0.01" className="input" placeholder="4.50" />
              </div>
            )}
            {contractType === 'FIXED_AMOUNT' && (
              <div>
                <label className="label">Fixed Amount (₹) *</label>
                <input {...register('fixedAmount')} type="number" step="0.01" className="input" placeholder="28000" />
              </div>
            )}
            <div>
              <label className="label">Expected Qty (KG)</label>
              <input {...register('expectedQuantityKg')} type="number" step="0.001" className="input" />
            </div>
            <div>
              <label className="label">Start Date *</label>
              <input {...register('startDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Expected End Date</label>
              <input {...register('expectedEndDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Status</label>
              <select {...register('status')} className="input">
                <option value="DRAFT">DRAFT</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="PAUSED">PAUSED</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
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
