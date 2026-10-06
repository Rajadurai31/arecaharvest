import { useState, useEffect, useCallback } from 'react'
import { notificationsApi } from '@/api'
import type { PagedResponse } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import Pagination from '@/components/common/Pagination'
import { formatDateTime } from '@/utils'
import { Bell, CheckCheck } from 'lucide-react'
import toast from 'react-hot-toast'

const typeColor: Record<string, string> = {
  PENDING_WORKER_WAGES: 'badge-red',
  BUYER_OUTSTANDING: 'badge-amber',
  CONTRACT_NEARING_COMPLETION: 'badge-blue',
  SCHEDULED_WORK_REMINDER: 'badge-green',
  EQUIPMENT_MAINTENANCE_DUE: 'badge-amber',
  FIELD_HARVEST_COMPLETE: 'badge-green',
  PAYMENT_RECEIVED: 'badge-green',
  SYSTEM: 'badge-gray',
}

export default function NotificationsPage() {
  const [paged, setPaged] = useState<PagedResponse<any> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try { setPaged(await notificationsApi.list({ page, size: 20 })) }
    finally { setLoading(false) }
  }, [page])

  useEffect(() => { load() }, [load])

  const markAllRead = async () => {
    const count = await notificationsApi.markAllRead()
    toast.success(`${count} notifications marked as read`)
    load()
  }

  return (
    <div>
      <div className="page-header">
        <div><h1>Notifications</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} notifications</p></div>
        <button className="btn-secondary btn" onClick={markAllRead}><CheckCheck className="h-4 w-4" /> Mark All Read</button>
      </div>
      <div className="card">
        {loading ? <LoadingSpinner center /> : !paged?.content?.length ? (
          <EmptyState icon={Bell} title="No notifications" description="You're all caught up!" />
        ) : (
          <>
            <div className="divide-y divide-gray-50">
              {paged.content.map((n: any) => (
                <div key={n.id} className={`py-3 px-1 flex items-start gap-3 ${!n.read ? 'bg-primary-50/50' : ''}`}>
                  {!n.read && <div className="mt-2 h-2 w-2 rounded-full bg-primary-500 shrink-0" />}
                  {n.read && <div className="mt-2 h-2 w-2 rounded-full bg-transparent shrink-0" />}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className={`text-sm font-medium ${n.read ? 'text-gray-700' : 'text-gray-900'}`}>{n.title}</p>
                      <span className={typeColor[n.type] ?? 'badge-gray'}>{n.type?.replace(/_/g,' ')}</span>
                    </div>
                    <p className="text-sm text-gray-500 mt-0.5">{n.message}</p>
                    <p className="text-xs text-gray-400 mt-1">{formatDateTime(n.createdAt)}</p>
                  </div>
                </div>
              ))}
            </div>
            <Pagination paged={paged} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  )
}
