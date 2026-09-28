import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Company } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class CompanyService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<Company[]> {
    return this.api.get<Company[]>('/api/companies');
  }
}
