import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateWarehouseRequest, UpdateWarehouseRequest, Warehouse } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class WarehouseService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Warehouse[]> {
    return this.api.get<Warehouse[]>('/api/warehouses');
  }

  create(request: CreateWarehouseRequest): Observable<Warehouse> {
    return this.api.post<Warehouse>('/api/warehouses', request);
  }

  update(id: string, request: UpdateWarehouseRequest): Observable<Warehouse> {
    return this.api.put<Warehouse>(`/api/warehouses/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Warehouse> {
    return this.api.patch<Warehouse>(`/api/warehouses/${id}/status?active=${active}`, {});
  }
}