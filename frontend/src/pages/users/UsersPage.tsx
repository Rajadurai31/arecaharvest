import { useState, useEffect, useCallback } from 'react'
import { Edit2, UserX, UserCheck } from 'lucide-react'
import api from '@/api/axios'
import type { PagedResponse } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import EmptyState from '@/components/common/EmptyState'
import Pagination from '@/components/common/Pagination'
import Modal from '@/components/common/Modal'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { Users } from 'lucide-react'
import { formatDateTime } from '@/utils'

interface UserSummary {
  id: number; username: string; fullName: string; email: string;
  phone: string; active: boolean; lastLoginAt: string; roles: string[]
}

export default function UsersPage() {
  const [paged, setPaged] = useState<PagedResponse<UserSummary> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [editUser, setEditUser] = useState<UserSummary | null>(null)
  const [deactivating, setDeactivating] = useState<UserSummary | null>(null)
  const [saving, setSaving] = useState(false)
  const [pwModal, setPwModal] = useState<UserSummary | null>(null)

  const { register, handleSubmit, reset } = useForm<any>()
  const { register: pwReg, handleSubmit: pwSubmit, reset: pwReset } = useForm<any>()

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const r = await api.get('/users', { params: { page, size: 20 } })
      setPaged(r.data.data)
    } finally { setLoading(false) }
  }, [page])

  useEffect(() => { load() }, [load])

  const onUpdate = async (data: any) => {
    if (!editUser) return
    setSaving(true)
    try {
      await api.put(`/users/${editUser.id}`, data)
      toast.success('User updated'); setEditUser(null); load()
    } finally { setSaving(false) }
  }

  const onDeactivate = async () => {
    if (!deactivating) return
    await api.delete(`/users/${deactivating.id}`)
    toast.success('User deactivated'); setDeactivating(null); load()
  }

  const onChangePw = async (data: any) => {
    if (!pwModal) return
    setSaving(true)
    try {
      await api.post('/auth/change-password', data)
      toast.success('Password changed'); setPwModal(null); pwReset()
    } finally { setSaving(false) }
  }

  const roleColor = (role: string) =>
    role.includes('ADMIN') ? 'badge-red' : role.includes('SUPERVISOR') ? 'badge-blue' : 'badge-gray'

  return (
    <div>
      <div className="page-header">
        <div><h1>User Management</h1><p className="text-sm text-gray-500 mt-0.5">{paged?.totalElements ?? 0} system users</p></div>
      </div>

      <div className="card">
        {loading ? <LoadingSpinner center /> : !paged?.content.length ? (
          <EmptyState icon={Users} title="No users found" />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead><tr className="border-b border-gray-100">
                  <th className="table-th">Username</th>
                  <th className="table-th">Full Name</th>
                  <th className="table-th hidden sm:table-cell">Email</th>
                  <th className="table-th">Roles</th>
                  <th className="table-th hidden md:table-cell">Last Login</th>
                  <th className="table-th">Status</th>
                  <th className="table-th text-right">Actions</th>
                </tr></thead>
                <tbody className="divide-y divide-gray-50">
                  {paged.content.map(u => (
                    <tr key={u.id} className="hover:bg-gray-50">
                      <td className="table-td font-mono font-medium">{u.username}</td>
                      <td className="table-td">{u.fullName}</td>
                      <td className="table-td hidden sm:table-cell text-gray-500">{u.email || '—'}</td>
                      <td className="table-td">
                        <div className="flex flex-wrap gap-1">
                          {u.roles.map(r => <span key={r} className={`badge ${roleColor(r)}`}>{r.replace('ROLE_','')}</span>)}
                        </div>
                      </td>
                      <td className="table-td hidden md:table-cell text-xs text-gray-400">{formatDateTime(u.lastLoginAt)}</td>
                      <td className="table-td"><span className={u.active ? 'badge-green' : 'badge-red'}>{u.active ? 'Active' : 'Inactive'}</span></td>
                      <td className="table-td text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button className="btn btn-secondary btn-sm px-2" title="Edit" onClick={() => { setEditUser(u); reset({ fullName: u.fullName, email: u.email, phone: u.phone, roles: u.roles.map(r=>r.replace('ROLE_','')) }) }}><Edit2 className="h-3.5 w-3.5" /></button>
                          <button className="btn btn-secondary btn-sm px-2" title="Change Password" onClick={() => setPwModal(u)}><UserCheck className="h-3.5 w-3.5" /></button>
                          {u.active && <button className="btn btn-danger btn-sm px-2" title="Deactivate" onClick={() => setDeactivating(u)}><UserX className="h-3.5 w-3.5" /></button>}
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

      {/* Edit User Modal */}
      <Modal open={!!editUser} onClose={() => setEditUser(null)} title="Edit User">
        <form onSubmit={handleSubmit(onUpdate)} className="space-y-4">
          <div><label className="label">Full Name</label><input {...register('fullName')} className="input" /></div>
          <div><label className="label">Email</label><input {...register('email')} type="email" className="input" /></div>
          <div><label className="label">Phone</label><input {...register('phone')} className="input" /></div>
          <div><label className="label">Roles (comma separated)</label>
            <input {...register('roles')} className="input" placeholder="ADMIN, SUPERVISOR, WORKER" /></div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setEditUser(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Update'}</button>
          </div>
        </form>
      </Modal>

      {/* Change Password Modal */}
      <Modal open={!!pwModal} onClose={() => setPwModal(null)} title={`Change Password — ${pwModal?.username}`}>
        <form onSubmit={pwSubmit(onChangePw)} className="space-y-4">
          <div><label className="label">Current Password</label><input {...pwReg('currentPassword')} type="password" className="input" /></div>
          <div><label className="label">New Password</label><input {...pwReg('newPassword')} type="password" className="input" placeholder="Min 8 characters" /></div>
          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button type="button" className="btn btn-secondary" onClick={() => setPwModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Change Password'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!deactivating} title="Deactivate User" message={`Deactivate ${deactivating?.username}?`} confirmLabel="Deactivate" onConfirm={onDeactivate} onCancel={() => setDeactivating(null)} danger />
    </div>
  )
}
