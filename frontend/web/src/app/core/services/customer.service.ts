import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateCustomerRequest, Customer, UpdateCustomerRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Customer[]> {
    return this.api.get<Customer[]>('/api/customers');
  }

  create(request: CreateCustomerRequest): Observable<Customer> {
    return this.api.post<Customer>('/api/customers', request);
  }

  update(id: string, request: UpdateCustomerRequest): Observable<Customer> {
    return this.api.put<Customer>(`/api/customers/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Customer> {
    return this.api.patch<Customer>(`/api/customers/${id}/status?active=${active}`, {});
  }
}