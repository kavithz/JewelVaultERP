import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateStockAdjustmentRequest, StockAdjustmentRecord, StockAdjustmentStatus } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class StockAdjustmentService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<StockAdjustmentRecord[]> {
    return this.api.get<StockAdjustmentRecord[]>('/api/stock-adjustments');
  }

  get(id: string): Observable<StockAdjustmentRecord> {
    return this.api.get<StockAdjustmentRecord>(`/api/stock-adjustments/${id}`);
  }

  create(request: CreateStockAdjustmentRequest): Observable<StockAdjustmentRecord> {
    return this.api.post<StockAdjustmentRecord>('/api/stock-adjustments', request);
  }

  setStatus(id: string, status: StockAdjustmentStatus): Observable<StockAdjustmentRecord> {
    return this.api.patch<StockAdjustmentRecord>(`/api/stock-adjustments/${id}/status?status=${status}`, {});
  }
}