export default function LoadingSpinner({ size = 'md', center = false }: {
  size?: 'sm' | 'md' | 'lg'
  center?: boolean
}) {
  const s = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' }[size]
  return (
    <div className={center ? 'flex items-center justify-center py-16' : 'inline-flex'}>
      <div className={`${s} animate-spin rounded-full border-2 border-gray-200 border-t-primary-600`} />
    </div>
  )
}
