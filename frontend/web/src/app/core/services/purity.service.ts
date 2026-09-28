import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreatePurityRequest, Purity, UpdatePurityRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class PurityService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Purity[]> {
    return this.api.get<Purity[]>('/api/purities');
  }

  create(request: CreatePurityRequest): Observable<Purity> {
    return this.api.post<Purity>('/api/purities', request);
  }

  update(id: string, request: UpdatePurityRequest): Observable<Purity> {
    return this.api.put<Purity>(`/api/purities/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<Purity> {
    return this.api.patch<Purity>(`/api/purities/${id}/status?active=${active}`, {});
  }
}