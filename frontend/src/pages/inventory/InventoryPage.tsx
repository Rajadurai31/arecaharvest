import { useState, useEffect, useCallback } from 'react'
import { inventoryApi } from '@/api'
import type { Inventory, PagedResponse } from '@/types'
import Pagination from '@/components/common/Pagination'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import { formatKg, formatDate } from '@/utils'
import { Package, Warehouse, Layers, ArrowUpRight, CheckCircle2 } from 'lucide-react'
import StatCard from '@/components/common/StatCard'

const statusColor: Record<string, string> = {
  AVAILABLE: 'badge-green',
  PARTIALLY_SOLD: 'badge-amber',
  SOLD: 'badge-gray',
  DAMAGED: 'badge-red',
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
      setPaged(data)
      setTotalAvailable(total)
    } finally {
      setLoading(false)
    }
  }, [statusFilter, page])

  useEffect(() => {
    load()
  }, [load])

  const soldCount = paged?.content.filter(i => i.status === 'SOLD' || i.status === 'PARTIALLY_SOLD').length ?? 0

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h1>Stock Inventory & Godowns</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Real-time harvested arecanut stock across regional processing godowns
          </p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total Available Stock"
          value={formatKg(totalAvailable)}
          icon={Package}
          color="green"
          subtitle="Ready for auction & sale"
        />
        <StatCard
          label="Active Storage Lots"
          value={paged?.totalElements ?? 0}
          icon={Layers}
          color="blue"
          subtitle="Across all godown bays"
        />
        <StatCard
          label="Avg Lot Size"
          value={formatKg(paged?.totalElements ? totalAvailable / paged.totalElements : 0)}
          icon={Warehouse}
          color="amber"
          subtitle="Per harvest consignment"
        />
        <StatCard
          label="Active Commercial Dispatches"
          value={soldCount}
          icon={ArrowUpRight}
          color="purple"
          subtitle="Partially or fully fulfilled"
        />
      </div>

      <div className="card space-y-4">
        {/* Status Filter Buttons */}
        <div className="flex flex-wrap items-center justify-between gap-3 pb-2 border-b border-slate-100 dark:border-slate-800">
          <div className="flex flex-wrap items-center gap-2">
            {[
              { id: '', label: 'All Lots' },
              { id: 'AVAILABLE', label: 'Available Stock' },
              { id: 'PARTIALLY_SOLD', label: 'Partially Sold' },
              { id: 'SOLD', label: 'Fulfilled / Sold' },
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => {
                  setStatusFilter(tab.id)
                  setPage(0)
                }}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                  statusFilter === tab.id
                    ? 'bg-emerald-600 text-white shadow-sm'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <span className="text-xs font-medium text-slate-400">
            Showing {paged?.content.length ?? 0} of {paged?.totalElements ?? 0} lots
          </span>
        </div>

        {loading ? (
          <LoadingSpinner center size="lg" />
        ) : !paged?.content.length ? (
          <EmptyState
            icon={Package}
            title="No inventory lots found"
            description="Inventory lots are created automatically when harvest sessions are completed."
          />
        ) : (
          <>
            <div className="table-container">
              <table className="w-full">
                <thead>
                  <tr>
                    <th className="table-th">Lot Code</th>
                    <th className="table-th">Received Date</th>
                    <th className="table-th">Harvest Origin</th>
                    <th className="table-th text-right">Received Qty</th>
                    <th className="table-th text-right">Dispatched Qty</th>
                    <th className="table-th text-right">Current Available</th>
                    <th className="table-th">Storage Godown</th>
                    <th className="table-th text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                  {paged.content.map(i => (
                    <tr key={i.id} className="table-tr">
                      <td className="table-td font-mono font-bold text-xs text-slate-900 dark:text-slate-100">
                        LOT-{(1000 + i.id).toString()}
                      </td>
                      <td className="table-td text-xs text-slate-600 dark:text-slate-300 font-medium">
                        {formatDate(i.receivedDate)}
                      </td>
                      <td className="table-td">
                        <span className="font-semibold text-slate-800 dark:text-slate-200">
                          {i.notes || `Field #${i.fieldId || 'Plantation'}`}
                        </span>
                      </td>
                      <td className="table-td text-right font-medium tabular-nums">
                        {formatKg(i.quantityReceived)}
                      </td>
                      <td className="table-td text-right font-medium tabular-nums text-rose-600 dark:text-rose-400">
                        {i.quantitySold > 0 ? formatKg(i.quantitySold) : '—'}
                      </td>
                      <td className="table-td text-right font-bold text-emerald-700 dark:text-emerald-400 tabular-nums">
                        {formatKg(i.availableQuantity)}
                      </td>
                      <td className="table-td text-slate-600 dark:text-slate-300">
                        <span className="flex items-center gap-1.5 text-xs">
                          <Warehouse className="h-3.5 w-3.5 text-slate-400 shrink-0" />
                          {i.storageLocation || 'Central Warehouse'}
                        </span>
                      </td>
                      <td className="table-td text-center">
                        <span className={statusColor[i.status] ?? 'badge-gray'}>
                          {i.status.replace('_', ' ')}
                        </span>
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
    </div>
  )
}
