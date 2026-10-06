import { useState, useCallback } from 'react'

interface UseApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

export function useApi<T>() {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
  })

  const execute = useCallback(async (apiCall: () => Promise<T>) => {
    setState({ data: null, loading: true, error: null })
    try {
      const data = await apiCall()
      setState({ data, loading: false, error: null })
      return data
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } }; message?: string })
        ?.response?.data?.message ?? (err as Error)?.message ?? 'Unknown error'
      setState({ data: null, loading: false, error: message })
      throw err
    }
  }, [])

  return { ...state, execute }
}

export function usePagination(initialSize = 20) {
  const [page, setPage] = useState(0)
  const [size] = useState(initialSize)

  const reset = () => setPage(0)

  return { page, size, setPage, reset }
}
