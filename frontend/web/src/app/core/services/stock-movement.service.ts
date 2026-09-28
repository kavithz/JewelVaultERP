import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateStockMovementRequest, StockMovementRecord } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class StockMovementService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<StockMovementRecord[]> {
    return this.api.get<StockMovementRecord[]>('/api/stock-movements');
  }

  get(id: string): Observable<StockMovementRecord> {
    return this.api.get<StockMovementRecord>(`/api/stock-movements/${id}`);
  }

  create(request: CreateStockMovementRequest): Observable<StockMovementRecord> {
    return this.api.post<StockMovementRecord>('/api/stock-movements', request);
  }
}