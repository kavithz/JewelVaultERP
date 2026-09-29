import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { map, Observable } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CompanyService } from '../../core/services/company.service';
import { RoleService } from '../../core/services/role.service';
import { UserService } from '../../core/services/user.service';
import { Company, PermissionRecord, RoleRecord, UserRecord } from '../../core/models/erp.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-roles-permissions-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PageHeaderComponent],
  template: `
    <app-page-header title="Roles & Permissions" subtitle="Manage company roles, assigned permissions, and user membership." />
    @if (isPreview()) {
      <p class="notice" role="note">Role and permission administration is unavailable in Preview Mode.</p>
    } @else {
      <section class="toolbar" aria-label="Role company scope">
        <label for="role-company">Company</label>
        <select id="role-company" [value]="companyId()" (change)="changeCompany($any($event.target).value)" [disabled]="companies().length < 2">
          @for (company of companies(); track company.id) {
            <option [value]="company.id">{{ company.name }}</option>
          }
        </select>
        <button type="button" class="primary" (click)="startCreate()" [disabled]="!companyId()">Add role</button>
      </section>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (notice()) { <p class="success" role="status">{{ notice() }}</p> }
      @if (loading()) { <p class="state" role="status">Loading role data...</p> }
      @else {
        <div class="workspace">
          <section class="role-list" aria-labelledby="roles-heading">
            <h2 id="roles-heading">Company roles</h2>
            @if (!roles().length) {
              <p class="state">No roles are configured for this company.</p>
            } @else {
              <div class="table-scroll">
                <table>
                  <thead><tr><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
                  <tbody>
                    @for (role of roles(); track role.id) {
                      <tr [class.selected]="selectedRoleId() === role.id">
                        <td><button type="button" class="role-select" (click)="selectRole(role)">{{ role.name }}</button><small>{{ role.description || 'No description' }}</small></td>
                        <td>{{ role.active ? 'Active' : 'Inactive' }}</td>
                        <td class="actions">
                          <button type="button" (click)="editRole(role)">Edit</button>
                          <button type="button" (click)="toggleActive(role)">{{ role.active ? 'Deactivate' : 'Activate' }}</button>
                          <button type="button" class="danger" (click)="deleteRole(role)">Delete</button>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          </section>
          @if (editorOpen()) {
            <section class="editor" aria-labelledby="role-editor-title">
              <h2 id="role-editor-title">{{ editing() ? 'Edit role' : 'Create role' }}</h2>
              <form [formGroup]="form" (ngSubmit)="saveRole()">
                <label for="role-name">Name</label>
                <input id="role-name" formControlName="name" maxlength="100" required />
                <label for="role-description">Description</label>
                <textarea id="role-description" formControlName="description" maxlength="255" rows="3"></textarea>
                @if (form.controls.name.touched && form.controls.name.invalid) { <small class="error">Enter a role name (up to 100 characters).</small> }
                <div class="actions"><button class="primary" type="submit" [disabled]="saving()">{{ saving() ? 'Saving...' : 'Save role' }}</button><button type="button" (click)="cancelEdit()">Cancel</button></div>
              </form>
            </section>
          }
        </div>
        <div class="workspace lower">
          <section class="editor" aria-labelledby="permissions-heading">
            <h2 id="permissions-heading">Permissions</h2>
            @if (!selectedRole()) {
              <p class="state">Select a role to view its permissions.</p>
            } @else if (permissionLoading()) {
              <p class="state">Loading permissions...</p>
            } @else if (!permissions().length) {
              <p class="state">No permissions are defined in the permission catalog.</p>
            } @else {
              <p class="scope-note">Permissions available for {{ selectedRole()?.name }}.</p>
              <div class="permission-list">
                @for (permission of permissions(); track permission.id) {
                  <label><input type="checkbox" [checked]="hasPermission(permission.id)" (change)="togglePermission(permission, $any($event.target).checked)" [disabled]="permissionBusy()" /><span>{{ permission.name }}<small>{{ permission.description || 'No description' }}</small></span></label>
                }
              </div>
            }
          </section>
          <section class="editor" aria-labelledby="user-roles-heading">
            <h2 id="user-roles-heading">User role membership</h2>
            @if (!users().length) {
              <p class="state">No users are available in this company.</p>
            } @else {
              <label for="role-user">User</label>
              <select id="role-user" [value]="selectedUserId()" (change)="selectUser($any($event.target).value)">
                @for (user of users(); track user.id) { <option [value]="user.id">{{ user.username }} ({{ user.email }})</option> }
              </select>
              @if (userRoleLoading()) { <p class="state">Loading user roles...</p> }
              @else {
                <ul class="user-roles">
                  @for (role of userRoles(); track role.id) {
                    <li>{{ role.name }} <button type="button" (click)="removeUserRole(role)">Remove</button></li>
                  } @empty { <li class="state">No roles assigned.</li> }
                </ul>
                <div class="assign-row">
                  <select aria-label="Role to assign" [value]="roleToAssignId()" (change)="roleToAssignId.set($any($event.target).value)">
                    <option value="">Choose a role</option>
                    @for (role of roles(); track role.id) { <option [value]="role.id">{{ role.name }}</option> }
                  </select>
                  <button type="button" (click)="assignUserRole()" [disabled]="!roleToAssignId() || permissionBusy()">Assign</button>
                </div>
              }
            }
          </section>
        </div>
      }
    }
  `,
  styles: [`
    :host { display: block; color: var(--primary-text); }
    .toolbar { display: flex; align-items: center; gap: .65rem; margin: 0 0 1rem; }
    .toolbar label, label { font-size: .84rem; font-weight: 600; }
    select, input, textarea { min-height: 2.55rem; padding: .55rem .65rem; border: 1px solid var(--border-color); border-radius: .4rem; background: var(--surface); color: inherit; font: inherit; }
    textarea { resize: vertical; }
    button { min-height: 2.25rem; padding: .4rem .65rem; border: 1px solid var(--border-color); border-radius: .35rem; background: var(--surface); color: inherit; font: inherit; cursor: pointer; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    button.primary { border-color: var(--primary); background: var(--primary); color: white; }
    button.danger { color: var(--ruby); }
    .workspace { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(17rem, .8fr); gap: 1rem; align-items: start; }
    .workspace.lower { margin-top: 1rem; }
    .role-list, .editor { min-width: 0; padding: 1rem; border: 1px solid var(--border-color); border-radius: .45rem; background: var(--surface); }
    h2 { margin: 0 0 .8rem; font-size: 1.05rem; }
    .table-scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; font-size: .84rem; }
    th, td { padding: .65rem .5rem; border-bottom: 1px solid var(--border-color); vertical-align: top; }
    th { color: var(--secondary-text); font-size: .72rem; text-transform: uppercase; }
    tr.selected { background: var(--surface-soft); }
    .role-select { display: block; padding: 0; border: 0; background: none; color: var(--primary); font-weight: 600; text-align: left; }
    small { display: block; color: var(--secondary-text); margin-top: .2rem; }
    .actions { display: flex; flex-wrap: wrap; gap: .3rem; }
    form { display: grid; gap: .45rem; }
    form .actions { margin-top: .5rem; }
    .permission-list { display: grid; gap: .45rem; }
    .permission-list label { display: flex; gap: .55rem; align-items: flex-start; font-weight: 400; }
    .permission-list input { min-height: auto; margin-top: .18rem; }
    .user-roles { margin: .65rem 0; padding-left: 1.2rem; }
    .user-roles li { margin: .35rem 0; }
    .user-roles button { margin-left: .4rem; }
    .assign-row { display: flex; gap: .5rem; }
    .assign-row select { flex: 1; min-width: 0; }
    .state, .scope-note { color: var(--secondary-text); font-size: .88rem; }
    .notice, .error, .success { margin: .7rem 0; padding: .7rem .85rem; border-radius: .4rem; }
    .notice { background: var(--surface-soft); color: var(--secondary-text); }
    .error { background: var(--ruby-light); color: var(--ruby); }
    .success { background: var(--emerald-light); color: var(--emerald); }
    @media (max-width: 850px) { .workspace { grid-template-columns: 1fr; } }
    @media (max-width: 560px) { .toolbar { align-items: stretch; flex-direction: column; } .toolbar select, .toolbar button { width: 100%; } }
  `],
})
export class RolesPermissionsPageComponent {
  private readonly auth = inject(AuthService);
  private readonly companiesService = inject(CompanyService);
  private readonly roleService = inject(RoleService);
  private readonly userService = inject(UserService);

  readonly isPreview = computed(() => this.auth.isPreviewAuthenticated());
  readonly companies = signal<Company[]>([]);
  readonly companyId = signal('');
  readonly roles = signal<RoleRecord[]>([]);
  readonly permissions = signal<PermissionRecord[]>([]);
  readonly assignedPermissions = signal<PermissionRecord[]>([]);
  readonly users = signal<UserRecord[]>([]);
  readonly userRoles = signal<RoleRecord[]>([]);
  readonly selectedRoleId = signal<string | null>(null);
  readonly selectedUserId = signal('');
  readonly roleToAssignId = signal('');
  readonly selectedRole = computed(() => this.roles().find((role) => role.id === this.selectedRoleId()) ?? null);
  readonly loading = signal(false);
  readonly permissionLoading = signal(false);
  readonly userRoleLoading = signal(false);
  readonly saving = signal(false);
  readonly permissionBusy = signal(false);
  readonly editorOpen = signal(false);
  readonly editing = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255)] }),
  });

  constructor() {
    if (this.isPreview()) return;
    this.companiesService.list().subscribe({
      next: (companies) => {
        this.companies.set(companies);
        const authCompanyId = this.auth.currentUser().companyId;
        const selected = companies.find((company) => company.id === authCompanyId) ?? companies[0];
        if (selected) {
          this.companyId.set(selected.id);
          this.loadCompanyData();
        }
      },
      error: (error: Error) => this.error.set(error.message),
    });
    this.roleService.listPermissions().subscribe({
      next: (permissions) => this.permissions.set(permissions),
      error: (error: Error) => this.error.set(error.message),
    });
    this.userService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        if (users[0]) this.selectUser(users[0].id);
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  changeCompany(companyId: string): void {
    this.companyId.set(companyId);
    this.selectedRoleId.set(null);
    this.loadCompanyData();
  }

  selectRole(role: RoleRecord): void {
    this.selectedRoleId.set(role.id);
    this.permissionLoading.set(true);
    this.roleService.listRolePermissions(role.id, this.companyId()).subscribe({
      next: (permissions) => {
        this.assignedPermissions.set(permissions);
        this.permissionLoading.set(false);
      },
      error: (error: Error) => {
        this.error.set(error.message);
        this.permissionLoading.set(false);
      },
    });
  }

  startCreate(): void {
    this.editing.set(false);
    this.editorOpen.set(true);
    this.form.reset({ name: '', description: '' });
    this.error.set(null);
    this.notice.set(null);
  }

  editRole(role: RoleRecord): void {
    this.selectedRoleId.set(role.id);
    this.editing.set(true);
    this.editorOpen.set(true);
    this.form.reset({ name: role.name, description: role.description ?? '' });
    this.error.set(null);
    this.notice.set(null);
  }

  saveRole(): void {
    if (this.form.invalid || this.saving() || !this.companyId()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    const request = this.editing() && this.selectedRoleId()
      ? this.roleService.update(this.selectedRoleId()!, this.companyId(), { ...value, description: value.description.trim() || null, active: this.selectedRole()?.active ?? true })
      : this.roleService.create({ companyId: this.companyId(), name: value.name.trim(), description: value.description.trim() || null });
    request.subscribe({
      next: (role) => {
        this.saving.set(false);
        this.editorOpen.set(false);
        this.notice.set(this.editing() ? 'Role updated.' : 'Role created.');
        this.loadCompanyData(role.id);
      },
      error: (error: Error) => {
        this.saving.set(false);
        this.error.set(error.message);
      },
    });
  }

  cancelEdit(): void {
    this.editorOpen.set(false);
    this.form.reset();
  }

  toggleActive(role: RoleRecord): void {
    this.roleService.setActive(role.id, this.companyId(), !role.active).subscribe({
      next: () => {
        this.notice.set(`Role ${role.active ? 'deactivated' : 'activated'}.`);
        this.loadCompanyData(role.id);
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  deleteRole(role: RoleRecord): void {
    if (!globalThis.confirm(`Delete the ${role.name} role?`)) return;
    this.roleService.delete(role.id, this.companyId()).subscribe({
      next: () => {
        this.notice.set('Role deleted.');
        this.selectedRoleId.set(null);
        this.loadCompanyData();
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  hasPermission(permissionId: string): boolean {
    return this.assignedPermissions().some((permission) => permission.id === permissionId);
  }

  togglePermission(permission: PermissionRecord, assign: boolean): void {
    const roleId = this.selectedRoleId();
    if (!roleId) return;
    this.permissionBusy.set(true);
    const request: Observable<void> = assign
      ? this.roleService.assignPermission(roleId, this.companyId(), permission.id).pipe(map(() => undefined))
      : this.roleService.removePermission(roleId, this.companyId(), permission.id);
    request.subscribe({
      next: () => {
        this.permissionBusy.set(false);
        this.notice.set(assign ? 'Permission assigned.' : 'Permission removed.');
        this.selectRole(this.selectedRole()!);
      },
      error: (error: Error) => {
        this.permissionBusy.set(false);
        this.error.set(error.message);
      },
    });
  }

  selectUser(userId: string): void {
    this.selectedUserId.set(userId);
    this.userRoleLoading.set(true);
    this.roleService.userRoles(userId).subscribe({
      next: (roles) => {
        this.userRoles.set(roles);
        this.userRoleLoading.set(false);
      },
      error: (error: Error) => {
        this.error.set(error.message);
        this.userRoleLoading.set(false);
      },
    });
  }

  assignUserRole(): void {
    if (!this.selectedUserId() || !this.roleToAssignId()) return;
    this.permissionBusy.set(true);
    this.roleService.assignRole(this.selectedUserId(), this.roleToAssignId()).subscribe({
      next: () => {
        this.permissionBusy.set(false);
        this.notice.set('Role assigned to user.');
        this.roleToAssignId.set('');
        this.selectUser(this.selectedUserId());
      },
      error: (error: Error) => {
        this.permissionBusy.set(false);
        this.error.set(error.message);
      },
    });
  }

  removeUserRole(role: RoleRecord): void {
    this.permissionBusy.set(true);
    this.roleService.removeRole(this.selectedUserId(), role.id).subscribe({
      next: () => {
        this.permissionBusy.set(false);
        this.notice.set('Role removed from user.');
        this.selectUser(this.selectedUserId());
      },
      error: (error: Error) => {
        this.permissionBusy.set(false);
        this.error.set(error.message);
      },
    });
  }

  private loadCompanyData(selectRoleId?: string): void {
    if (!this.companyId()) return;
    this.loading.set(true);
    this.roleService.list(this.companyId()).subscribe({
      next: (roles) => {
        this.roles.set(roles);
        this.loading.set(false);
        const role = roles.find((item) => item.id === selectRoleId) ?? roles[0];
        if (role) this.selectRole(role);
        else this.selectedRoleId.set(null);
      },
      error: (error: Error) => {
        this.loading.set(false);
        this.error.set(error.message);
      },
    });
  }
}
