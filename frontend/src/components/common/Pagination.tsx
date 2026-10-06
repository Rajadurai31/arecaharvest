import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { PagedResponse } from '@/types'

interface Props {
  paged: Pick<PagedResponse<unknown>, 'page' | 'totalPages' | 'totalElements' | 'first' | 'last'>
  onPageChange: (page: number) => void
}

export default function Pagination({ paged, onPageChange }: Props) {
  if (paged.totalPages <= 1) return null

  const pages = Array.from({ length: Math.min(paged.totalPages, 7) }, (_, i) => {
    if (paged.totalPages <= 7) return i
    const start = Math.max(0, paged.page - 3)
    return Math.min(start + i, paged.totalPages - 1)
  })

  return (
    <div className="flex items-center justify-between px-1 py-3">
      <p className="text-sm text-gray-500">
        Page {paged.page + 1} of {paged.totalPages} ({paged.totalElements} total)
      </p>
      <div className="flex items-center gap-1">
        <button
          className="btn btn-secondary btn-sm px-2"
          onClick={() => onPageChange(paged.page - 1)}
          disabled={paged.first}
        >
          <ChevronLeft className="h-4 w-4" />
        </button>
        {pages.map(p => (
          <button
            key={p}
            className={`btn btn-sm px-3 ${p === paged.page ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => onPageChange(p)}
          >
            {p + 1}
          </button>
        ))}
        <button
          className="btn btn-secondary btn-sm px-2"
          onClick={() => onPageChange(paged.page + 1)}
          disabled={paged.last}
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}
