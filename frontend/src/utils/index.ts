import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { format, parseISO } from 'date-fns'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(date: string | null | undefined): string {
  if (!date) return '—'
  try { return format(parseISO(date), 'dd MMM yyyy') } catch { return date }
}

export function formatDateTime(date: string | null | undefined): string {
  if (!date) return '—'
  try { return format(parseISO(date), 'dd MMM yyyy, hh:mm a') } catch { return date }
}

export function formatCurrency(amount: number | null | undefined): string {
  if (amount == null) return '—'
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(amount)
}

export function formatNumber(n: number | null | undefined, decimals = 2): string {
  if (n == null) return '—'
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: decimals }).format(n)
}

export function formatKg(n: number | null | undefined): string {
  if (n == null) return '—'
  return `${formatNumber(n, 3)} KG`
}

export const fieldStatusColor: Record<string, string> = {
  ACTIVE: 'badge-green', HARVESTING: 'badge-blue', PENDING: 'badge-amber',
  COMPLETED: 'badge-gray', INACTIVE: 'badge-red',
}

export const contractStatusColor: Record<string, string> = {
  ACTIVE: 'badge-green', DRAFT: 'badge-gray', PAUSED: 'badge-amber',
  COMPLETED: 'badge-blue', CANCELLED: 'badge-red',
}

export const workerStatusColor: Record<string, string> = {
  ACTIVE: 'badge-green', INACTIVE: 'badge-gray', BLACKLISTED: 'badge-red',
}

export const attendanceStatusColor: Record<string, string> = {
  PRESENT: 'badge-green', HALF_DAY: 'badge-amber', ABSENT: 'badge-red', LEAVE: 'badge-gray',
}

export const saleStatusColor: Record<string, string> = {
  PAID: 'badge-green', PARTIAL: 'badge-amber', PENDING: 'badge-red',
}

export const sessionStatusColor: Record<string, string> = {
  COMPLETED: 'badge-green', IN_PROGRESS: 'badge-blue', PLANNED: 'badge-amber', CANCELLED: 'badge-gray',
}

export function getInitials(name: string): string {
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
}

export function todayISO(): string {
  return new Date().toISOString().split('T')[0]
}

export function monthStartISO(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}
