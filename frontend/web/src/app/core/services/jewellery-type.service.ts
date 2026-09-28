import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateJewelleryTypeRequest, JewelleryType, UpdateJewelleryTypeRequest } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class JewelleryTypeService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<JewelleryType[]> {
    return this.api.get<JewelleryType[]>('/api/jewellery-types');
  }

  create(request: CreateJewelleryTypeRequest): Observable<JewelleryType> {
    return this.api.post<JewelleryType>('/api/jewellery-types', request);
  }

  update(id: string, request: UpdateJewelleryTypeRequest): Observable<JewelleryType> {
    return this.api.put<JewelleryType>(`/api/jewellery-types/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<JewelleryType> {
    return this.api.patch<JewelleryType>(`/api/jewellery-types/${id}/status?active=${active}`, {});
  }
}