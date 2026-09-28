import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface DashboardSummary {
  companyId: string;
  totalSales: number;
  totalPurchases: number;
  totalExpenses: number;
  outstandingReceivables: number;
  outstandingPayables: number;
  inventoryValue: number;
  activeUsers: number;
  totalCustomers: number;
  totalSuppliers: number;
  totalProducts: number;
  activeEmployees: number;
  salesToday: number;
  salesThisMonth: number;
  expensesToday: number;
  expensesThisMonth: number;
  payrollThisMonth: number;
  unreadNotifications: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly api: ApiService) {}

  getSummary(companyId: string): Observable<DashboardSummary> {
    return this.api.get<DashboardSummary>(`/api/dashboard/${companyId}`);
  }
}
