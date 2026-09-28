import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateProductRequest, Product, UpdateProductRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Product[]> {
    return this.api.get<Product[]>('/api/products');
  }

  create(request: CreateProductRequest): Observable<Product> {
    return this.api.post<Product>('/api/products', request);
  }

  update(id: string, request: UpdateProductRequest): Observable<Product> {
    return this.api.put<Product>(`/api/products/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Product> {
    return this.api.patch<Product>(`/api/products/${id}/status?active=${active}`, {});
  }
}