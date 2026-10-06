// ── API Envelope ─────────────────────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
}

// ── Auth ──────────────────────────────────────────────────────────────────────
export interface AuthResponse {
  id: number
  username: string
  fullName: string
  email: string
  roles: string[]
  token: string
  tokenType: string
  expiresIn: number
}

export interface UserProfile {
  id: number
  username: string
  fullName: string
  email: string
  phone: string
  lastLoginAt: string
  roles: string[]
}

// ── Enums ─────────────────────────────────────────────────────────────────────
export type PaymentMethod = 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'CHEQUE' | 'OTHER'
export type FieldStatus = 'PENDING' | 'ACTIVE' | 'HARVESTING' | 'COMPLETED' | 'INACTIVE'
export type ContractType = 'PER_KG' | 'PER_TREE' | 'FIXED_AMOUNT'
export type ContractStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
export type SkillType = 'CLIMBER' | 'CUTTER' | 'COLLECTOR' | 'LOADER' | 'HELPER' | 'DRIVER' | 'OTHER'
export type WorkerStatus = 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED'
export type AttendanceStatus = 'PRESENT' | 'HALF_DAY' | 'ABSENT' | 'LEAVE'
export type SessionStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type SalePaymentStatus = 'PENDING' | 'PARTIAL' | 'PAID'
export type PaymentType = 'PAYMENT' | 'ADVANCE'
export type InventoryStatus = 'AVAILABLE' | 'PARTIALLY_SOLD' | 'SOLD' | 'DAMAGED'

// ── Owner ─────────────────────────────────────────────────────────────────────
export interface Owner {
  id: number
  name: string
  phone: string
  alternatePhone?: string
  email?: string
  address?: string
  village?: string
  district?: string
  state?: string
  pincode?: string
  paymentPreference?: PaymentMethod
  bankName?: string
  bankAccountNumber?: string
  bankIfsc?: string
  upiId?: string
  notes?: string
  active: boolean
  createdAt?: string
  totalFields?: number
  activeContracts?: number
  completedContracts?: number
}

// ── Field ─────────────────────────────────────────────────────────────────────
export interface Field {
  id: number
  ownerId: number
  ownerName: string
  fieldName: string
  address?: string
  village?: string
  district?: string
  state?: string
  acreage?: number
  treeCount?: number
  gpsLatitude?: number
  gpsLongitude?: number
  expectedYieldKg?: number
  status: FieldStatus
  notes?: string
  createdAt?: string
  harvestedQuantityKg?: number
  remainingQuantityKg?: number
  progressPercent?: number
  activeContractId?: number
}

// ── Contract ──────────────────────────────────────────────────────────────────
export interface Contract {
  id: number
  ownerId: number
  ownerName: string
  fieldId: number
  fieldName: string
  contractNumber: string
  contractType: ContractType
  rate: number
  fixedAmount?: number
  expectedQuantityKg?: number
  startDate: string
  expectedEndDate?: string
  actualEndDate?: string
  status: ContractStatus
  supervisorId?: number
  supervisorName?: string
  notes?: string
  expectedRevenue?: number
  actualHarvestedKg?: number
  progressPercent?: number
  createdAt?: string
}

// ── Worker ────────────────────────────────────────────────────────────────────
export interface Worker {
  id: number
  name: string
  phone: string
  alternatePhone?: string
  address?: string
  village?: string
  district?: string
  state?: string
  skillType: SkillType
  experienceYears?: number
  dailyWage: number
  joiningDate?: string
  status: WorkerStatus
  emergencyContactName?: string
  emergencyContactPhone?: string
  notes?: string
  totalEarned?: number
  totalPaid?: number
  outstandingBalance?: number
  createdAt?: string
}

// ── Attendance ────────────────────────────────────────────────────────────────
export interface Attendance {
  id: number
  workerId: number
  workerName: string
  fieldId: number
  fieldName: string
  contractId?: number
  harvestSessionId?: number
  attendanceDate: string
  status: AttendanceStatus
  checkInTime?: string
  checkOutTime?: string
  workingHours?: number
  overtimeHours?: number
  dailyWageApplied?: number
  overtimeRate?: number
  wageForDay?: number
  remarks?: string
  markedByName?: string
  createdAt?: string
}

// ── Wage Ledger ───────────────────────────────────────────────────────────────
export interface WageLedgerEntry {
  id: number
  workerId: number
  workerName: string
  transactionType: string
  amount: number
  adjustmentSign?: number
  transactionDate: string
  attendanceId?: number
  paymentId?: number
  fieldName?: string
  description?: string
  createdAt?: string
}

export interface WorkerLedgerSummary {
  workerId: number
  workerName: string
  totalEarned: number
  totalPaid: number
  outstandingBalance: number
  transactions: WageLedgerEntry[]
}

export interface WorkerPayment {
  id: number
  workerId: number
  workerName: string
  paymentDate: string
  amount: number
  paymentType: PaymentType
  paymentMethod: PaymentMethod
  referenceNumber?: string
  remarks?: string
  createdAt?: string
}

// ── Harvest Session ───────────────────────────────────────────────────────────
export interface HarvestSession {
  id: number
  sessionDate: string
  fieldId: number
  fieldName: string
  contractId: number
  contractNumber: string
  supervisorId?: number
  supervisorName?: string
  startTime?: string
  endTime?: string
  totalBags?: number
  totalQuantity: number
  quantityUnit: string
  workerCount: number
  totalWorkingHours?: number
  status: SessionStatus
  remarks?: string
  workerNames?: string[]
  createdAt?: string
}

// ── Inventory ─────────────────────────────────────────────────────────────────
export interface Inventory {
  id: number
  fieldId: number
  quantityReceived: number
  quantityUnit: string
  quantitySold: number
  availableQuantity: number
  storageLocation?: string
  receivedDate: string
  status: InventoryStatus
  notes?: string
}

// ── Buyer ─────────────────────────────────────────────────────────────────────
export interface Buyer {
  id: number
  name: string
  phone: string
  companyName?: string
  village?: string
  district?: string
  defaultRate?: number
  paymentTerms?: string
  status: string
  outstandingBalance?: number
}

// ── Sale ──────────────────────────────────────────────────────────────────────
export interface Sale {
  id: number
  saleNumber: string
  buyerId: number
  buyerName: string
  inventoryId: number
  saleDate: string
  quantityKg: number
  quantityUnit: string
  ratePerUnit: number
  grossAmount: number
  transportCost?: number
  loadingCost?: number
  otherCharges?: number
  netAmount: number
  paymentStatus: SalePaymentStatus
  amountReceived: number
  outstanding: number
  notes?: string
  createdAt?: string
}

// ── Expense ───────────────────────────────────────────────────────────────────
export interface Expense {
  id: number
  expenseDate: string
  categoryId: number
  categoryName: string
  amount: number
  paymentMethod: PaymentMethod
  description: string
  fieldId?: number
  fieldName?: string
  contractId?: number
  vehicleId?: number
  receiptUrl?: string
  createdAt?: string
}

export interface ExpenseCategory {
  id: number
  name: string
  description?: string
  active: boolean
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
export interface DashboardKpis {
  activeFields: number
  totalActiveWorkers: number
  workersWorkingToday: number
  activeContracts: number
  todayHarvestKg: number
  todayWorkerWage: number
  todayExpenses: number
  todaySalesRevenue: number
  pendingWorkerWages: number
  buyerOutstanding: number
  currentInventoryKg: number
  monthRevenue: number
  monthExpenses: number
  monthHarvestKg: number
}

// ── Schedule ──────────────────────────────────────────────────────────────────
export interface Schedule {
  id: number
  scheduleDate: string
  fieldId: number
  field?: { id: number; fieldName: string }
  contractId?: number
  supervisorId?: number
  plannedStartTime?: string
  plannedEndTime?: string
  expectedQuantityKg?: number
  expectedWorkerCount?: number
  notes?: string
  status: string
}
