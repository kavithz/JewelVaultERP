import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateSaleRequest, SaleRecord, SaleStatus } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SaleService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<SaleRecord[]> {
    return this.api.get<SaleRecord[]>('/api/sales');
  }

  get(id: string): Observable<SaleRecord> {
    return this.api.get<SaleRecord>(`/api/sales/${id}`);
  }

  create(request: CreateSaleRequest): Observable<SaleRecord> {
    return this.api.post<SaleRecord>('/api/sales', request);
  }

  setStatus(id: string, status: SaleStatus): Observable<SaleRecord> {
    return this.api.patch<SaleRecord>(`/api/sales/${id}/status?status=${status}`, {});
  }
}