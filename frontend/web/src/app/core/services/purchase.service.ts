import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreatePurchaseRequest, PurchaseRecord, PurchaseStatus } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class PurchaseService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<PurchaseRecord[]> {
    return this.api.get<PurchaseRecord[]>('/api/purchases');
  }

  get(id: string): Observable<PurchaseRecord> {
    return this.api.get<PurchaseRecord>(`/api/purchases/${id}`);
  }

  create(request: CreatePurchaseRequest): Observable<PurchaseRecord> {
    return this.api.post<PurchaseRecord>('/api/purchases', request);
  }

  setStatus(id: string, status: PurchaseStatus): Observable<PurchaseRecord> {
    return this.api.patch<PurchaseRecord>(`/api/purchases/${id}/status?status=${status}`, {});
  }
}