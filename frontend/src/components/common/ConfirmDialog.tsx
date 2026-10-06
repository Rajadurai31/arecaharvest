import { AlertTriangle } from 'lucide-react'

interface Props {
  open: boolean
  title: string
  message: string
  confirmLabel?: string
  onConfirm: () => void
  onCancel: () => void
  danger?: boolean
}

export default function ConfirmDialog({
  open, title, message, confirmLabel = 'Confirm',
  onConfirm, onCancel, danger = false,
}: Props) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onCancel} />
      <div className="relative bg-white rounded-2xl shadow-xl p-6 max-w-sm w-full">
        <div className="flex items-start gap-4">
          <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${danger ? 'bg-red-100' : 'bg-amber-100'}`}>
            <AlertTriangle className={`h-5 w-5 ${danger ? 'text-red-600' : 'text-amber-600'}`} />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900 mb-1">{title}</h3>
            <p className="text-sm text-gray-600">{message}</p>
          </div>
        </div>
        <div className="flex gap-3 mt-6 justify-end">
          <button className="btn-secondary btn" onClick={onCancel}>Cancel</button>
          <button
            className={danger ? 'btn-danger btn' : 'btn-primary btn'}
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
