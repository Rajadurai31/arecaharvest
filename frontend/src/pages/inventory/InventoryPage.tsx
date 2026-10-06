import { useState, useEffect, useCallback } from 'react'
import { inventoryApi } from '@/api'
import type { Inventory, PagedResponse } from '@/types'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { formatKg, formatDate } from '@/utils'
import { Package } from 'lucide-react'
import StatCard from '@/components/common/StatCard'

const statusColor: Record<string, string> = {
  AVAILABLE: 'badge-green', PARTIALLY_SOLD: 'badge-amber', SOLD: 'badge-gray', DAMAGED: 'badge-red',
}

export default function InventoryPage() {
  const [paged, setPaged] = useState<PagedResponse<Inventory> | null>(null)
  const [totalAvailable, setTotalAvailable] = useState<number>(0)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [data, total] = await Promise.all([
        inventoryApi.list({ status: statusFilter || undefined, page, size: 20 }),
        inventoryApi.getAvailableTotal(),
      ])
      setPaged(data); setTotalAvailable(total)
    } finally { setLoading(false) }
  }, [statusFilter, page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-6">
      <div className="page-header"><h1>Inventory</h1></div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Total Available" value={formatKg(totalAvailable)} icon={Package} color="green" />
        <StatCard label="Inventory Lots" value={paged?.totalElements ?? 0} icon={Package} color="blue" />
      </div>

      <div className="card">
        <div className="flex gap-3 mb-4">
          <select className="input w-44" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All Status</option>
            {['AVAILABLE','PARTIALLY_SOLD','SOLD','DAMAGED'].map(s => <option key={s}>{s}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Package} title="No inventory" description="Inventory is created automatically when harvest sessions are completed." />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Lot #</th>
                  <th className="table-th">Received</th>
                  <th className="table-th">Received Qty</th>
                  <th className="table-th">Sold Qty</th>
                  <th className="table-th">Available</th>
                  <th className="table-th hidden sm:table-cell">Storage</th>
                  <th className="table-th">Status</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(i => (
                    <tr key={i.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono text-xs">#{i.id}</td>
                      <td className="table-td text-xs">{formatDate(i.receivedDate)}</td>
                      <td className="table-td">{formatKg(i.quantityReceived)}</td>
                      <td className="table-td text-red-600">{i.quantitySold > 0 ? formatKg(i.quantitySold) : '—'}</td>
                      <td className="table-td font-semibold text-primary-700">{formatKg(i.availableQuantity)}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{i.storageLocation || '—'}</td>
                      <td className="table-td"><span className={statusColor[i.status]}>{i.status.replace('_',' ')}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  )
}
