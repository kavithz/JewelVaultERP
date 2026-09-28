import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserRecord } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<UserRecord[]> {
    return this.api.get<UserRecord[]>('/api/users');
  }

  create(payload: Partial<UserRecord>): Observable<UserRecord> {
    return this.api.post<UserRecord>('/api/users', payload);
  }

  update(id: string, payload: Partial<UserRecord>): Observable<UserRecord> {
    return this.api.put<UserRecord>(`/api/users/${id}`, payload);
  }
}
