import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateInventoryRequest, InventoryRecord, UpdateInventoryRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<InventoryRecord[]> {
    return this.api.get<InventoryRecord[]>('/api/inventory');
  }

  get(id: string): Observable<InventoryRecord> {
    return this.api.get<InventoryRecord>(`/api/inventory/${id}`);
  }

  create(request: CreateInventoryRequest): Observable<InventoryRecord> {
    return this.api.post<InventoryRecord>('/api/inventory', request);
  }

  update(id: string, request: UpdateInventoryRequest): Observable<InventoryRecord> {
    return this.api.put<InventoryRecord>(`/api/inventory/${id}`, request);
  }
}