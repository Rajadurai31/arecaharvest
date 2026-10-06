import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, TreePine, Ruler } from 'lucide-react'
import { fieldsApi, contractsApi, harvestApi } from '@/api'
import type { Field, Contract, HarvestSession } from '@/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import { formatDate, formatKg, fieldStatusColor, contractStatusColor } from '@/utils'

export default function FieldDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [field, setField] = useState<Field | null>(null)
  const [contracts, setContracts] = useState<Contract[]>([])
  const [sessions, setSessions] = useState<HarvestSession[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    Promise.all([
      fieldsApi.getById(Number(id)),
      contractsApi.list({ fieldId: id, size: 50 }),
      harvestApi.list({ fieldId: id, size: 20 }),
    ]).then(([f, c, s]) => {
      setField(f); setContracts(c.content); setSessions(s.content)
    }).finally(() => setLoading(false))
  }, [id])

  if (loading) return <LoadingSpinner center />
  if (!field) return <p className="text-center py-16 text-gray-500">Field not found</p>

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center gap-3">
        <Link to="/fields" className="btn btn-secondary btn-sm px-2"><ArrowLeft className="h-4 w-4" /></Link>
        <div>
          <h1>{field.fieldName}</h1>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-sm text-gray-500">{field.ownerName}</span>
            <span className={fieldStatusColor[field.status]}>{field.status}</span>
          </div>
        </div>
      </div>

      {/* Info cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="card text-center">
          <Ruler className="h-5 w-5 text-primary-600 mx-auto mb-1" />
          <p className="text-xl font-bold text-gray-900">{field.acreage ? `${field.acreage} ac` : '—'}</p>
          <p className="text-xs text-gray-500">Acreage</p>
        </div>
        <div className="card text-center">
          <TreePine className="h-5 w-5 text-green-600 mx-auto mb-1" />
          <p className="text-xl font-bold text-gray-900">{field.treeCount ?? '—'}</p>
          <p className="text-xs text-gray-500">Trees</p>
        </div>
        <div className="card text-center">
          <p className="text-xl font-bold text-gray-900">{field.expectedYieldKg ? formatKg(field.expectedYieldKg) : '—'}</p>
          <p className="text-xs text-gray-500">Expected Yield</p>
        </div>
        <div className="card text-center">
          <p className="text-xl font-bold text-primary-700">{field.harvestedQuantityKg ? formatKg(field.harvestedQuantityKg) : '—'}</p>
          <p className="text-xs text-gray-500">Harvested</p>
        </div>
      </div>

      {/* Progress */}
      {field.progressPercent != null && (
        <div className="card">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-gray-700">Harvest Progress</h3>
            <span className="text-sm font-bold text-primary-700">{field.progressPercent.toFixed(1)}%</span>
          </div>
          <div className="w-full bg-gray-100 rounded-full h-3">
            <div className="bg-primary-500 h-3 rounded-full transition-all duration-500"
                 style={{ width: `${Math.min(100, field.progressPercent)}%` }} />
          </div>
          <div className="flex justify-between text-xs text-gray-500 mt-1">
            <span>Harvested: {formatKg(field.harvestedQuantityKg)}</span>
            <span>Remaining: {formatKg(field.remainingQuantityKg)}</span>
          </div>
        </div>
      )}

      {/* Location */}
      {(field.village || field.gpsLatitude) && (
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-2">Location</h3>
          <div className="flex items-start gap-2 text-sm">
            <MapPin className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
            <span>{[field.village, field.district, field.state].filter(Boolean).join(', ')}</span>
          </div>
          {field.gpsLatitude && (
            <a href={`https://maps.google.com/?q=${field.gpsLatitude},${field.gpsLongitude}`}
               target="_blank" rel="noreferrer"
               className="text-xs text-primary-600 hover:underline mt-1 block">
              View on Google Maps ({field.gpsLatitude}, {field.gpsLongitude})
            </a>
          )}
        </div>
      )}

      {/* Contracts */}
      <div className="card">
        <h2 className="mb-4">Contracts ({contracts.length})</h2>
        {!contracts.length ? <p className="text-sm text-gray-500">No contracts yet</p> : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead><tr className="border-b border-gray-100">
                <th className="table-th">Contract #</th><th className="table-th">Type</th>
                <th className="table-th">Rate</th><th className="table-th">Start</th>
                <th className="table-th">Progress</th><th className="table-th">Status</th>
              </tr></thead>
              <tbody className="divide-y divide-gray-50">
                {contracts.map(c => (
                  <tr key={c.id} className="hover:bg-gray-50">
                    <td className="table-td font-mono text-xs">{c.contractNumber}</td>
                    <td className="table-td"><span className="badge-blue badge">{c.contractType}</span></td>
                    <td className="table-td">₹{c.rate}</td>
                    <td className="table-td text-xs">{formatDate(c.startDate)}</td>
                    <td className="table-td">
                      {c.progressPercent != null ? (
                        <div className="flex items-center gap-2">
                          <div className="w-16 bg-gray-100 rounded-full h-1.5">
                            <div className="bg-primary-500 h-1.5 rounded-full" style={{ width: `${Math.min(100, c.progressPercent)}%` }} />
                          </div>
                          <span className="text-xs">{c.progressPercent.toFixed(0)}%</span>
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

      {/* Recent Harvest Sessions */}
      <div className="card">
        <h2 className="mb-4">Recent Harvest Sessions</h2>
        {!sessions.length ? <p className="text-sm text-gray-500">No sessions yet</p> : (
          <div className="space-y-2">
            {sessions.map(s => (
              <div key={s.id} className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
                <div>
                  <p className="text-sm font-medium text-gray-900">{formatDate(s.sessionDate)}</p>
                  <p className="text-xs text-gray-500">{s.workerCount} workers · {formatKg(s.totalQuantity)}</p>
                </div>
                <span className={`badge ${s.status === 'COMPLETED' ? 'badge-green' : 'badge-amber'}`}>{s.status}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
