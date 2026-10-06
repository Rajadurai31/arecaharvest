import { type LucideIcon, Inbox } from 'lucide-react'

interface Props {
  icon?: LucideIcon
  title: string
  description?: string
  action?: React.ReactNode
}

export default function EmptyState({ icon: Icon = Inbox, title, description, action }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gray-100 mb-4">
        <Icon className="h-8 w-8 text-gray-400" />
      </div>
      <h3 className="text-base font-semibold text-gray-900 mb-1">{title}</h3>
      {description && <p className="text-sm text-gray-500 max-w-sm mb-4">{description}</p>}
      {action}
    </div>
  )
}
