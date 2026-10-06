import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import ProtectedRoute from '@/routes/ProtectedRoute'
import AppLayout from '@/components/layout/AppLayout'

// Auth
import LoginPage from '@/pages/auth/LoginPage'

// Pages
import DashboardPage from '@/pages/dashboard/DashboardPage'
import OwnersPage from '@/pages/owners/OwnersPage'
import OwnerDetailPage from '@/pages/owners/OwnerDetailPage'
import FieldsPage from '@/pages/fields/FieldsPage'
import FieldDetailPage from '@/pages/fields/FieldDetailPage'
import ContractsPage from '@/pages/contracts/ContractsPage'
import WorkersPage from '@/pages/workers/WorkersPage'
import WorkerDetailPage from '@/pages/workers/WorkerDetailPage'
import WorkerAssignmentPage from '@/pages/workers/WorkerAssignmentPage'
import AttendancePage from '@/pages/attendance/AttendancePage'
import WagesPage from '@/pages/wages/WagesPage'
import HarvestPage from '@/pages/harvest/HarvestPage'
import InventoryPage from '@/pages/inventory/InventoryPage'
import BuyersPage from '@/pages/buyers/BuyersPage'
import BuyerDetailPage from '@/pages/buyers/BuyerDetailPage'
import SalesPage from '@/pages/sales/SalesPage'
import ExpensesPage from '@/pages/expenses/ExpensesPage'
import VehiclesPage from '@/pages/vehicles/VehiclesPage'
import EquipmentPage from '@/pages/equipment/EquipmentPage'
import SchedulePage from '@/pages/schedule/SchedulePage'
import ReportsPage from '@/pages/reports/ReportsPage'
import NotificationsPage from '@/pages/notifications/NotificationsPage'
import UsersPage from '@/pages/users/UsersPage'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/owners" element={<OwnersPage />} />
            <Route path="/owners/:id" element={<OwnerDetailPage />} />
            <Route path="/fields" element={<FieldsPage />} />
            <Route path="/fields/:id" element={<FieldDetailPage />} />
            <Route path="/contracts" element={<ContractsPage />} />
            <Route path="/workers" element={<WorkersPage />} />
            <Route path="/workers/:id" element={<WorkerDetailPage />} />
            <Route path="/worker-assignments" element={<WorkerAssignmentPage />} />
            <Route path="/attendance" element={<AttendancePage />} />
            <Route path="/wages" element={<WagesPage />} />
            <Route path="/harvest" element={<HarvestPage />} />
            <Route path="/inventory" element={<InventoryPage />} />
            <Route path="/buyers" element={<BuyersPage />} />
            <Route path="/buyers/:id" element={<BuyerDetailPage />} />
            <Route path="/sales" element={<SalesPage />} />
            <Route path="/expenses" element={<ExpensesPage />} />
            <Route path="/vehicles" element={<VehiclesPage />} />
            <Route path="/equipment" element={<EquipmentPage />} />
            <Route path="/schedule" element={<SchedulePage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
