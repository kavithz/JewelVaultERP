import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateEmployeeRequest, EmployeeRecord, EmploymentStatus } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<EmployeeRecord[]> {
    return this.api.get<EmployeeRecord[]>('/api/employees');
  }

  get(id: string): Observable<EmployeeRecord> {
    return this.api.get<EmployeeRecord>(`/api/employees/${id}`);
  }

  byCompany(companyId: string): Observable<EmployeeRecord[]> {
    return this.api.get<EmployeeRecord[]>(`/api/employees/company/${companyId}`);
  }

  byBranch(branchId: string): Observable<EmployeeRecord[]> {
    return this.api.get<EmployeeRecord[]>(`/api/employees/branch/${branchId}`);
  }

  byStatus(status: EmploymentStatus): Observable<EmployeeRecord[]> {
    return this.api.get<EmployeeRecord[]>(`/api/employees/status/${status}`);
  }

  create(request: CreateEmployeeRequest): Observable<EmployeeRecord> {
    return this.api.post<EmployeeRecord>('/api/employees', request);
  }

  updateStatus(id: string, status: EmploymentStatus): Observable<EmployeeRecord> {
    return this.api.patch<EmployeeRecord>(`/api/employees/${id}/status/${status}`, {});
  }

  setExitDate(id: string, exitDate: string): Observable<EmployeeRecord> {
    return this.api.patch<EmployeeRecord>(`/api/employees/${id}/exit-date?exitDate=${encodeURIComponent(exitDate)}`, {});
  }
}