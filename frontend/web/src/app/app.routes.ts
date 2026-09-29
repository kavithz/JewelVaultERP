import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/login/login';
import { AppShellComponent } from './shared/components/layout/app-shell/app-shell';
import { DashboardPageComponent } from './features/dashboard/dashboard-page';
import { CompaniesPageComponent } from './features/companies/companies-page';
import { UsersPageComponent } from './features/users/users-page';
import { BranchesPageComponent } from './features/branches/branches-page';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardPageComponent },
      { path: 'companies', component: CompaniesPageComponent },
      { path: 'users', component: UsersPageComponent },
      { path: 'branches', component: BranchesPageComponent },
      {
        path: 'products',
        loadComponent: () => import('./features/products/products-page').then((module) => module.ProductsPageComponent),
      },
      {
        path: 'purities',
        loadComponent: () => import('./features/purities/purities-page').then((module) => module.PuritiesPageComponent),
      },
      {
        path: 'jewellery-types',
        loadComponent: () => import('./features/jewellery-types/jewellery-types-page').then((module) => module.JewelleryTypesPageComponent),
      },
      {
        path: 'gemstones',
        loadComponent: () => import('./features/gemstones/gemstones-page').then((module) => module.GemstonesPageComponent),
      },
      {
        path: 'warehouses',
        loadComponent: () => import('./features/warehouses/warehouses-page').then((module) => module.WarehousesPageComponent),
      },
      {
        path: 'suppliers',
        loadComponent: () => import('./features/suppliers/suppliers-page').then((module) => module.SuppliersPageComponent),
      },
      {
        path: 'customers',
        loadComponent: () => import('./features/customers/customers-page').then((module) => module.CustomersPageComponent),
      },
      {
        path: 'inventory',
        loadComponent: () => import('./features/inventory/inventory-page').then((module) => module.InventoryPageComponent),
      },
      {
        path: 'stock-movements',
        loadComponent: () => import('./features/stock-movements/stock-movements-page').then((module) => module.StockMovementsPageComponent),
      },
      {
        path: 'stock-adjustments',
        loadComponent: () => import('./features/stock-adjustments/stock-adjustments-page').then((module) => module.StockAdjustmentsPageComponent),
      },
      {
        path: 'sales',
        loadComponent: () => import('./features/sales/sales-page').then((module) => module.SalesPageComponent),
      },
      {
        path: 'sales/:id',
        loadComponent: () => import('./features/sales/sale-details-page').then((module) => module.SaleDetailsPageComponent),
      },
      {
        path: 'employees',
        loadComponent: () => import('./features/employees/employees-page').then((module) => module.EmployeesPageComponent),
      },
      {
        path: 'employees/:id',
        loadComponent: () => import('./features/employees/employee-details-page').then((module) => module.EmployeeDetailsPageComponent),
      },
      {
        path: 'stock-transfers',
        loadComponent: () => import('./features/stock-transfers/stock-transfers-page').then((module) => module.StockTransfersPageComponent),
      },
      {
        path: 'stock-transfers/:id',
        loadComponent: () => import('./features/stock-transfers/stock-transfer-details-page').then((module) => module.StockTransferDetailsPageComponent),
      },
      {
        path: 'purchases/new',
        loadComponent: () => import('./features/purchases/purchase-create-page').then((module) => module.PurchaseCreatePageComponent),
      },
      {
        path: 'purchases',
        loadComponent: () => import('./features/purchases/purchases-page').then((module) => module.PurchasesPageComponent),
      },
      {
        path: 'purchases/:id',
        loadComponent: () => import('./features/purchases/purchase-details-page').then((module) => module.PurchaseDetailsPageComponent),
      },
      {
        path: 'roles-permissions',
        loadComponent: () => import('./features/roles-permissions/roles-permissions-page').then((module) => module.RolesPermissionsPageComponent),
      },
      {
        path: 'accounting',
        loadComponent: () => import('./features/accounting/accounting-page').then((module) => module.AccountingPageComponent),
      },
      {
        path: '**',
        loadComponent: () => import('./shared/components/not-found/not-found-page').then((module) => module.NotFoundPageComponent),
      },
    ],
  },
];
