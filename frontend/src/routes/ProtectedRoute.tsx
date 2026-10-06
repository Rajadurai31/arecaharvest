import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import LoadingSpinner from '@/components/common/LoadingSpinner'

interface Props {
  requiredRole?: string
}

export default function ProtectedRoute({ requiredRole }: Props) {
  const { isAuthenticated, isLoading, hasRole } = useAuth()

  if (isLoading) return <LoadingSpinner center size="lg" />
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (requiredRole && !hasRole(requiredRole)) return <Navigate to="/" replace />

  return <Outlet />
}
