import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PermissionRecord, RoleRecord } from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class RoleService {
  constructor(private readonly api: ApiService) {}

  list(companyId: string): Observable<RoleRecord[]> {
    return this.api.get<RoleRecord[]>(`/api/roles/company/${companyId}`);
  }

  create(payload: { companyId: string; name: string; description: string | null }): Observable<RoleRecord> {
    return this.api.post<RoleRecord>('/api/roles', payload);
  }

  update(id: string, companyId: string, payload: { name: string; description: string | null; active: boolean }): Observable<RoleRecord> {
    return this.api.patch<RoleRecord>(`/api/roles/${id}?companyId=${encodeURIComponent(companyId)}`, payload);
  }

  setActive(id: string, companyId: string, active: boolean): Observable<RoleRecord> {
    const action = active ? 'activate' : 'deactivate';
    return this.api.patch<RoleRecord>(`/api/roles/${id}/${action}?companyId=${encodeURIComponent(companyId)}`, {});
  }

  delete(id: string, companyId: string): Observable<void> {
    return this.api.delete<void>(`/api/roles/${id}?companyId=${encodeURIComponent(companyId)}`);
  }

  listPermissions(): Observable<PermissionRecord[]> {
    return this.api.get<PermissionRecord[]>('/api/permissions');
  }

  listRolePermissions(roleId: string, companyId: string): Observable<PermissionRecord[]> {
    return this.api.get<PermissionRecord[]>(`/api/permissions/role/${roleId}`, { companyId });
  }

  assignPermission(roleId: string, companyId: string, permissionId: string): Observable<PermissionRecord> {
    return this.api.post<PermissionRecord>(`/api/permissions/role/${roleId}?companyId=${encodeURIComponent(companyId)}`, { permissionId });
  }

  removePermission(roleId: string, companyId: string, permissionId: string): Observable<void> {
    return this.api.delete<void>(`/api/permissions/role/${roleId}/${permissionId}?companyId=${encodeURIComponent(companyId)}`);
  }

  userRoles(userId: string): Observable<RoleRecord[]> {
    return this.api.get<RoleRecord[]>(`/api/user-roles/user/${userId}`);
  }

  assignRole(userId: string, roleId: string): Observable<RoleRecord> {
    return this.api.post<RoleRecord>(`/api/user-roles/user/${userId}`, { roleId });
  }

  removeRole(userId: string, roleId: string): Observable<void> {
    return this.api.delete<void>(`/api/user-roles/user/${userId}/${roleId}`);
  }
}