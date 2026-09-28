import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BranchRecord } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class BranchService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<BranchRecord[]> {
    return this.api.get<BranchRecord[]>('/api/branches');
  }

  create(payload: Partial<BranchRecord>): Observable<BranchRecord> {
    return this.api.post<BranchRecord>('/api/branches', payload);
  }

  update(id: string, payload: Partial<BranchRecord>): Observable<BranchRecord> {
    return this.api.put<BranchRecord>(`/api/branches/${id}`, payload);
  }

  toggleStatus(id: string, active: boolean): Observable<BranchRecord> {
    return this.api.patch<BranchRecord>(`/api/branches/${id}/status`, { active });
  }
}
