import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateStockTransferRequest, StockTransferRecord, StockTransferStatus } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class StockTransferService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<StockTransferRecord[]> {
    return this.api.get<StockTransferRecord[]>('/api/stock-transfers');
  }

  get(id: string): Observable<StockTransferRecord> {
    return this.api.get<StockTransferRecord>(`/api/stock-transfers/${id}`);
  }

  create(request: CreateStockTransferRequest): Observable<StockTransferRecord> {
    return this.api.post<StockTransferRecord>('/api/stock-transfers', request);
  }

  setStatus(id: string, status: StockTransferStatus): Observable<StockTransferRecord> {
    return this.api.patch<StockTransferRecord>(`/api/stock-transfers/${id}/status?status=${status}`, {});
  }
}