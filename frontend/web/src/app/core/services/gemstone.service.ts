import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateGemstoneRequest, Gemstone, UpdateGemstoneRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class GemstoneService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Gemstone[]> {
    return this.api.get<Gemstone[]>('/api/gemstones');
  }

  create(request: CreateGemstoneRequest): Observable<Gemstone> {
    return this.api.post<Gemstone>('/api/gemstones', request);
  }

  update(id: string, request: UpdateGemstoneRequest): Observable<Gemstone> {
    return this.api.put<Gemstone>(`/api/gemstones/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Gemstone> {
    return this.api.patch<Gemstone>(`/api/gemstones/${id}/status?active=${active}`, {});
  }
}