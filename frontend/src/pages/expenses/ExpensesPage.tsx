import { useState, useEffect, useCallback } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { expensesApi, fieldsApi } from '@/api'
import type { Expense, ExpenseCategory, Field, PagedResponse } from '@/types'
import Modal from '@/components/common/Modal'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { formatCurrency, formatDate, todayISO } from '@/utils'
import { Receipt } from 'lucide-react'

const schema = z.object({
  expenseDate: z.string().min(1),
  categoryId: z.coerce.number().min(1),
  amount: z.coerce.number().min(0.01),
  paymentMethod: z.enum(['CASH','UPI','BANK_TRANSFER','OTHER']),
  description: z.string().min(1),
  fieldId: z.coerce.number().optional(),
  contractId: z.coerce.number().optional(),
})
type FormData = z.infer<typeof schema>

export default function ExpensesPage() {
  const [paged, setPaged] = useState<PagedResponse<Expense> | null>(null)
  const [categories, setCategories] = useState<ExpenseCategory[]>([])
  const [fields, setFields] = useState<Field[]>([])
  const [page, setPage] = useState(0)
  const [catFilter, setCatFilter] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [deleting, setDeleting] = useState<Expense | null>(null)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { expenseDate: todayISO(), paymentMethod: 'CASH' },
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, cats, fs] = await Promise.all([
        expensesApi.list({ categoryId: catFilter || undefined, fromDate: fromDate || undefined, toDate: toDate || undefined, page, size: 20 }),
        expensesApi.getCategories(),
        fieldsApi.list({ size: 100 }),
      ])
      setPaged(data); setCategories(cats); setFields(fs.content)
    } finally { setLoading(false) }
  }, [catFilter, fromDate, toDate, page])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data: FormData) => {
    setSaving(true)
    try {
      await expensesApi.create(data)
      toast.success('Expense recorded')
      setModalOpen(false); reset({ expenseDate: todayISO(), paymentMethod: 'CASH' }); load()
    } finally { setSaving(false) }
  }

  const onDelete = async () => {
    if (!deleting) return
    await expensesApi.delete(deleting.id)
    toast.success('Expense deleted')
    setDeleting(null); load()
  }

  return (
    <div>
      <div className="page-header">
        <div><h1>Expenses</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} records</p></div>
        <button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" /> Add Expense</button>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-3 mb-4">
          <select className="input w-40" value={catFilter} onChange={e => setCatFilter(e.target.value)}>
            <option value="">All Categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <input type="date" className="input w-40" value={fromDate} onChange={e => setFromDate(e.target.value)} placeholder="From" />
          <input type="date" className="input w-40" value={toDate} onChange={e => setToDate(e.target.value)} placeholder="To" />
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Receipt} title="No expenses" action={<button className="btn-primary btn" onClick={() => setModalOpen(true)}><Plus className="h-4 w-4" />Add Expense</button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Date</th>
                  <th className="table-th">Category</th>
                  <th className="table-th">Description</th>
                  <th className="table-th hidden sm:table-cell">Field</th>
                  <th className="table-th hidden sm:table-cell">Method</th>
                  <th className="table-th text-right">Amount</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(e => (
                    <tr key={e.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">{formatDate(e.expenseDate)}</td>
                      <td className="table-td"><span className="badge-amber badge">{e.categoryName}</span></td>
                      <td className="table-td text-gray-600 max-w-xs truncate">{e.description}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{e.fieldName || '—'}</td>
                      <td className="table-td hidden sm:table-cell"><span className="badge-gray badge">{e.paymentMethod}</span></td>
                      <td className="table-td text-right font-semibold text-red-600">{formatCurrency(e.amount)}</td>
                      <td className="table-td text-right">
                        <button className="btn btn-danger btn-sm px-2" onClick={() => setDeleting(e)}><Trash2 className="h-3.5 w-3.5" /></button>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Add Expense">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Date *</label>
              <input {...register('expenseDate')} type="date" className="input" />
            </div>
            <div>
              <label className="label">Category *</label>
              <select {...register('categoryId', { valueAsNumber: true })} className="input">
                <option value="">Select…</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Amount (₹) *</label>
              <input {...register('amount')} type="number" step="0.01" className="input" />
            </div>
            <div>
              <label className="label">Payment Method *</label>
              <select {...register('paymentMethod')} className="input">
                <option value="CASH">CASH</option>
                <option value="UPI">UPI</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="col-span-2">
              <label className="label">Description *</label>
              <input {...register('description')} className="input" placeholder="Fuel for vehicle, worker food…" />
            </div>
            <div>
              <label className="label">Related Field</label>
              <select {...register('fieldId', { valueAsNumber: true })} className="input">
                <option value="">None</option>
                {fields.map(f => <option key={f.id} value={f.id}>{f.fieldName}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Add Expense'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!deleting} title="Delete Expense" message={`Delete ₹${deleting?.amount} expense?`} confirmLabel="Delete" onConfirm={onDelete} onCancel={() => setDeleting(null)} danger />
    </div>
  )
}
