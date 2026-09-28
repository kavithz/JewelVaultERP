import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateSupplierRequest, Supplier, UpdateSupplierRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Supplier[]> {
    return this.api.get<Supplier[]>('/api/suppliers');
  }

  create(request: CreateSupplierRequest): Observable<Supplier> {
    return this.api.post<Supplier>('/api/suppliers', request);
  }

  update(id: string, request: UpdateSupplierRequest): Observable<Supplier> {
    return this.api.put<Supplier>(`/api/suppliers/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Supplier> {
    return this.api.patch<Supplier>(`/api/suppliers/${id}/status?active=${active}`, {});
  }
}