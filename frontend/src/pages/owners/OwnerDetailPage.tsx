import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, Phone, Mail, CreditCard } from 'lucide-react'
import { ownersApi, fieldsApi, contractsApi } from '@/api'
import type { Owner, Field, Contract } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import { formatDate, fieldStatusColor, contractStatusColor, formatKg } from '@/utils'

export default function OwnerDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [owner, setOwner] = useState<Owner | null>(null)
  const [fields, setFields] = useState<Field[]>([])
  const [contracts, setContracts] = useState<Contract[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    Promise.all([
      ownersApi.getById(Number(id)),
      fieldsApi.list({ ownerId: id, size: 50 }),
      contractsApi.list({ ownerId: id, size: 50 }),
    ]).then(([o, f, c]) => {
      setOwner(o)
      setFields(f.content)
      setContracts(c.content)
    }).finally(() => setLoading(false))
  }, [id])

  if (loading) return <LoadingSpinner center />
  if (!owner) return <p className="text-center text-gray-500 py-16">Owner not found</p>

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center gap-3">
        <Link to="/owners" className="btn btn-secondary btn-sm px-2"><ArrowLeft className="h-4 w-4" /></Link>
        <h1>{owner.name}</h1>
        <span className={owner.active ? 'badge-green' : 'badge-red'}>{owner.active ? 'Active' : 'Inactive'}</span>
      </div>

      {/* Info Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="card space-y-3">
          <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Contact</h3>
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-sm"><Phone className="h-4 w-4 text-gray-400" />{owner.phone}</div>
            {owner.alternatePhone && <div className="flex items-center gap-2 text-sm"><Phone className="h-4 w-4 text-gray-400" />{owner.alternatePhone}</div>}
            {owner.email && <div className="flex items-center gap-2 text-sm"><Mail className="h-4 w-4 text-gray-400" />{owner.email}</div>}
          </div>
        </div>
        <div className="card space-y-3">
          <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Address</h3>
          <div className="flex items-start gap-2 text-sm text-gray-700">
            <MapPin className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
            <span>{[owner.village, owner.district, owner.state].filter(Boolean).join(', ') || '—'}</span>
          </div>
        </div>
        <div className="card space-y-3">
          <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Payment</h3>
          <div className="flex items-center gap-2 text-sm">
            <CreditCard className="h-4 w-4 text-gray-400" />
            <span className="badge-gray badge">{owner.paymentPreference ?? 'CASH'}</span>
          </div>
          {owner.upiId && <p className="text-sm text-gray-600">UPI: {owner.upiId}</p>}
          {owner.bankName && <p className="text-sm text-gray-600">Bank: {owner.bankName}</p>}
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Total Fields',       value: fields.length },
          { label: 'Active Contracts',   value: contracts.filter(c => c.status === 'ACTIVE').length },
          { label: 'Completed Contracts',value: contracts.filter(c => c.status === 'COMPLETED').length },
        ].map(s => (
          <div key={s.label} className="card text-center">
            <p className="text-2xl font-bold text-gray-900">{s.value}</p>
            <p className="text-xs text-gray-500 mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Fields */}
      <div className="card">
        <h2 className="mb-4">Fields ({fields.length})</h2>
        {fields.length === 0 ? <p className="text-sm text-gray-500">No fields yet</p> : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead><tr className="border-b border-gray-100">
                <th className="table-th">Field Name</th>
                <th className="table-th">Village</th>
                <th className="table-th">Area</th>
                <th className="table-th">Trees</th>
                <th className="table-th">Expected Yield</th>
                <th className="table-th">Status</th>
              </tr></thead>
              <tbody className="divide-y divide-gray-50">
                {fields.map(f => (
                  <tr key={f.id} className="hover:bg-gray-50">
                    <td className="table-td font-medium">{f.fieldName}</td>
                    <td className="table-td text-gray-500">{f.village || '—'}</td>
                    <td className="table-td">{f.acreage ? `${f.acreage} ac` : '—'}</td>
                    <td className="table-td">{f.treeCount ?? '—'}</td>
                    <td className="table-td">{f.expectedYieldKg ? formatKg(f.expectedYieldKg) : '—'}</td>
                    <td className="table-td"><span className={fieldStatusColor[f.status]}>{f.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Contract history */}
      <div className="card">
        <h2 className="mb-4">Contract History ({contracts.length})</h2>
        {contracts.length === 0 ? <p className="text-sm text-gray-500">No contracts yet</p> : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead><tr className="border-b border-gray-100">
                <th className="table-th">Contract #</th>
                <th className="table-th">Field</th>
                <th className="table-th">Type</th>
                <th className="table-th">Rate</th>
                <th className="table-th">Start Date</th>
                <th className="table-th">Progress</th>
                <th className="table-th">Status</th>
              </tr></thead>
              <tbody className="divide-y divide-gray-50">
                {contracts.map(c => (
                  <tr key={c.id} className="hover:bg-gray-50">
                    <td className="table-td font-mono text-xs">{c.contractNumber}</td>
                    <td className="table-td">{c.fieldName}</td>
                    <td className="table-td"><span className="badge-blue badge">{c.contractType}</span></td>
                    <td className="table-td">₹{c.rate}</td>
                    <td className="table-td">{formatDate(c.startDate)}</td>
                    <td className="table-td">
                      {c.progressPercent != null ? (
                        <div className="flex items-center gap-2">
                          <div className="flex-1 bg-gray-100 rounded-full h-1.5 w-20">
                            <div className="bg-primary-500 h-1.5 rounded-full" style={{ width: `${Math.min(100, c.progressPercent)}%` }} />
                          </div>
                          <span className="text-xs text-gray-500">{c.progressPercent.toFixed(0)}%</span>
                        </div>
                      ) : '—'}
                    </td>
                    <td className="table-td"><span className={contractStatusColor[c.status]}>{c.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
