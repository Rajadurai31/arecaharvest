import api from './axios'
import type {
  ApiResponse, PagedResponse, AuthResponse, Owner, Field, Contract, Worker,
  Attendance, WorkerLedgerSummary, WorkerPayment, HarvestSession, Inventory,
  Buyer, Sale, Expense, ExpenseCategory, DashboardKpis, Schedule,
} from '@/types'

// ── Helpers ───────────────────────────────────────────────────────────────────
const unwrap = <T>(res: { data: ApiResponse<T> }) => res.data.data

// ── Auth ──────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (username: string, password: string) =>
    api.post<ApiResponse<AuthResponse>>('/auth/login', { username, password }).then(unwrap),
  me: () => api.get<ApiResponse<unknown>>('/auth/me').then(unwrap),
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
export const dashboardApi = {
  getKpis: () => api.get<ApiResponse<DashboardKpis>>('/dashboard/kpis').then(unwrap),
  getHarvestTrend: (fromDate: string, toDate: string) =>
    api.get<ApiResponse<Record<string, number>>>('/dashboard/harvest-trend', { params: { fromDate, toDate } }).then(unwrap),
  getRevenueVsExpenses: (fromDate: string, toDate: string) =>
    api.get<ApiResponse<Record<string, Record<string, number>>>>('/dashboard/revenue-vs-expenses', { params: { fromDate, toDate } }).then(unwrap),
  getWageTrend: (fromDate: string, toDate: string) =>
    api.get<ApiResponse<Record<string, number>>>('/dashboard/wage-trend', { params: { fromDate, toDate } }).then(unwrap),
}

// ── Owners ────────────────────────────────────────────────────────────────────
export const ownersApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Owner>>>('/owners', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Owner>>(`/owners/${id}`).then(unwrap),
  create: (data: Partial<Owner>) =>
    api.post<ApiResponse<Owner>>('/owners', data).then(unwrap),
  update: (id: number, data: Partial<Owner>) =>
    api.put<ApiResponse<Owner>>(`/owners/${id}`, data).then(unwrap),
  deactivate: (id: number) =>
    api.delete<ApiResponse<void>>(`/owners/${id}`).then(unwrap),
}

// ── Fields ────────────────────────────────────────────────────────────────────
export const fieldsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Field>>>('/fields', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Field>>(`/fields/${id}`).then(unwrap),
  create: (data: Partial<Field>) =>
    api.post<ApiResponse<Field>>('/fields', data).then(unwrap),
  update: (id: number, data: Partial<Field>) =>
    api.put<ApiResponse<Field>>(`/fields/${id}`, data).then(unwrap),
  updateStatus: (id: number, status: string) =>
    api.patch<ApiResponse<Field>>(`/fields/${id}/status`, null, { params: { status } }).then(unwrap),
}

// ── Contracts ─────────────────────────────────────────────────────────────────
export const contractsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Contract>>>('/contracts', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Contract>>(`/contracts/${id}`).then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<Contract>>('/contracts', data).then(unwrap),
  update: (id: number, data: unknown) =>
    api.put<ApiResponse<Contract>>(`/contracts/${id}`, data).then(unwrap),
  updateStatus: (id: number, status: string) =>
    api.patch<ApiResponse<Contract>>(`/contracts/${id}/status`, null, { params: { status } }).then(unwrap),
}

// ── Workers ───────────────────────────────────────────────────────────────────
export const workersApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Worker>>>('/workers', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Worker>>(`/workers/${id}`).then(unwrap),
  create: (data: Partial<Worker>) =>
    api.post<ApiResponse<Worker>>('/workers', data).then(unwrap),
  update: (id: number, data: Partial<Worker>) =>
    api.put<ApiResponse<Worker>>(`/workers/${id}`, data).then(unwrap),
  updateStatus: (id: number, status: string) =>
    api.patch<ApiResponse<Worker>>(`/workers/${id}/status`, null, { params: { status } }).then(unwrap),
}

// ── Attendance ────────────────────────────────────────────────────────────────
export const attendanceApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Attendance>>>('/attendance', { params }).then(unwrap),
  mark: (data: unknown) =>
    api.post<ApiResponse<Attendance>>('/attendance', data).then(unwrap),
  update: (id: number, data: unknown) =>
    api.put<ApiResponse<Attendance>>(`/attendance/${id}`, data).then(unwrap),
}

// ── Wages ─────────────────────────────────────────────────────────────────────
export const wagesApi = {
  getLedger: (workerId: number, params?: Record<string, unknown>) =>
    api.get<ApiResponse<WorkerLedgerSummary>>(`/wages/workers/${workerId}/ledger`, { params }).then(unwrap),
  recordPayment: (data: unknown) =>
    api.post<ApiResponse<WorkerPayment>>('/wages/payments', data).then(unwrap),
  listPayments: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<WorkerPayment>>>('/wages/payments', { params }).then(unwrap),
}

// ── Harvest Sessions ──────────────────────────────────────────────────────────
export const harvestApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<HarvestSession>>>('/harvest-sessions', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<HarvestSession>>(`/harvest-sessions/${id}`).then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<HarvestSession>>('/harvest-sessions', data).then(unwrap),
  update: (id: number, data: unknown) =>
    api.put<ApiResponse<HarvestSession>>(`/harvest-sessions/${id}`, data).then(unwrap),
}

// ── Inventory ─────────────────────────────────────────────────────────────────
export const inventoryApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Inventory>>>('/inventory', { params }).then(unwrap),
  getAvailableTotal: () =>
    api.get<ApiResponse<number>>('/inventory/available-total').then(unwrap),
}

// ── Buyers ────────────────────────────────────────────────────────────────────
export const buyersApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Buyer>>>('/buyers', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Buyer>>(`/buyers/${id}`).then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<Buyer>>('/buyers', data).then(unwrap),
  update: (id: number, data: unknown) =>
    api.put<ApiResponse<Buyer>>(`/buyers/${id}`, data).then(unwrap),
}

// ── Sales ─────────────────────────────────────────────────────────────────────
export const salesApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Sale>>>('/sales', { params }).then(unwrap),
  getById: (id: number) =>
    api.get<ApiResponse<Sale>>(`/sales/${id}`).then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<Sale>>('/sales', data).then(unwrap),
  recordPayment: (data: unknown) =>
    api.post<ApiResponse<Sale>>('/sales/payments', data).then(unwrap),
}

// ── Expenses ──────────────────────────────────────────────────────────────────
export const expensesApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Expense>>>('/expenses', { params }).then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<Expense>>('/expenses', data).then(unwrap),
  update: (id: number, data: unknown) =>
    api.put<ApiResponse<Expense>>(`/expenses/${id}`, data).then(unwrap),
  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/expenses/${id}`).then(unwrap),
  getCategories: () =>
    api.get<ApiResponse<ExpenseCategory[]>>('/expenses/categories').then(unwrap),
}

// ── Schedules ─────────────────────────────────────────────────────────────────
export const schedulesApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<Schedule>>>('/schedules', { params }).then(unwrap),
  getToday: () =>
    api.get<ApiResponse<Schedule[]>>('/schedules/today').then(unwrap),
  create: (data: unknown) =>
    api.post<ApiResponse<Schedule>>('/schedules', data).then(unwrap),
  updateStatus: (id: number, status: string) =>
    api.patch<ApiResponse<Schedule>>(`/schedules/${id}/status`, null, { params: { status } }).then(unwrap),
}

// ── Notifications ─────────────────────────────────────────────────────────────
export const notificationsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<ApiResponse<PagedResponse<unknown>>>('/notifications', { params }).then(unwrap),
  getUnreadCount: () =>
    api.get<ApiResponse<number>>('/notifications/unread-count').then(unwrap),
  markAllRead: () =>
    api.post<ApiResponse<number>>('/notifications/mark-all-read').then(unwrap),
}
