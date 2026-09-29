import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export const PREVIEW_ACCESS_PASSWORD = 'jewelvaulterp@kavith7';
export type AuthMode = 'preview' | 'real';

export interface AuthUser {
  username: string;
  authenticated: boolean;
  mode: AuthMode;
  id?: string;
  email?: string | null;
  companyId?: string;
  roles?: string[];
  permissions?: string[];
  accessToken?: string;
  expiresAt?: number;
}

interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: {
    id: string;
    username: string;
    email: string | null;
    companyId: string;
    roles: string[];
    permissions: string[];
  };
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'jewelvault-auth-state';
  private readonly authState = signal<AuthUser>(this.readStoredState());

  readonly isAuthenticated = computed(() => {
    const user = this.authState();
    return user.authenticated && (user.mode === 'preview' || Boolean(
      user.accessToken && user.expiresAt && user.expiresAt > Date.now(),
    ));
  });
  readonly mode = computed(() => this.authState().mode);
  readonly accessToken = computed(() => this.authState().accessToken ?? null);
  readonly isRealAuthenticated = computed(() => this.mode() === 'real' && this.isAuthenticated());
  readonly isPreviewAuthenticated = computed(() => this.mode() === 'preview' && this.isAuthenticated());
  readonly currentUser = computed(() => this.authState());

  login(username: string, password: string): Observable<AuthUser> {
    return this.loginPreview(username, password);
  }

  loginPreview(username: string, password: string): Observable<AuthUser> {
    const trimmedUsername = username.trim();

    if (!trimmedUsername) {
      return throwError(() => new Error('Username and password are required.'));
    }

    if (password !== PREVIEW_ACCESS_PASSWORD) {
      return throwError(() => new Error('Invalid preview credentials.'));
    }

    const previewUser: AuthUser = {
      username: trimmedUsername,
      authenticated: true,
      mode: 'preview',
    };

    this.persistState(previewUser);
    return of(previewUser);
  }

  loginReal(username: string, password: string): Observable<AuthUser> {
    const trimmedUsername = username.trim();
    if (!trimmedUsername || !password) {
      return throwError(() => new Error('Username and password are required.'));
    }

    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/api/auth/login`, {
      username: trimmedUsername,
      password,
    }).pipe(
      map((response) => {
        const user: AuthUser = {
          id: response.user.id,
          username: response.user.username,
          email: response.user.email,
          companyId: response.user.companyId,
          roles: response.user.roles,
          permissions: response.user.permissions,
          accessToken: response.accessToken,
          expiresAt: Date.now() + response.expiresIn * 1000,
          authenticated: true,
          mode: 'real',
        };
        this.persistState(user);
        return user;
      }),
      catchError((error: unknown) => {
        this.logout();
        return throwError(() => new Error(this.loginErrorMessage(error)));
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    sessionStorage.removeItem(this.storageKey);
    this.authState.set(this.emptyState(environment.authMode));
  }

  private persistState(nextState: AuthUser): void {
    if (nextState.mode === 'real') {
      localStorage.removeItem(this.storageKey);
      sessionStorage.setItem(this.storageKey, JSON.stringify(nextState));
    } else {
      sessionStorage.removeItem(this.storageKey);
      localStorage.setItem(this.storageKey, JSON.stringify(nextState));
    }
    this.authState.set(nextState);
  }

  private readStoredState(): AuthUser {
    try {
      const sessionState = sessionStorage.getItem(this.storageKey);
      const storage = sessionState ? sessionStorage : localStorage;
      const raw = sessionState ?? localStorage.getItem(this.storageKey);
      if (!raw) {
        return this.emptyState(environment.authMode);
      }

      const parsed = JSON.parse(raw) as Partial<AuthUser>;
      if (parsed.mode !== 'real' && parsed.mode !== 'preview') {
        storage.removeItem(this.storageKey);
        return this.emptyState(environment.authMode);
      }
      const mode = parsed.mode;
      if (mode === 'real' && (!parsed.accessToken || !parsed.expiresAt || parsed.expiresAt <= Date.now())) {
        storage.removeItem(this.storageKey);
        return this.emptyState(environment.authMode);
      }
      return {
        username: parsed.username ?? '',
        authenticated: Boolean(parsed.authenticated),
        mode,
        id: parsed.id,
        email: parsed.email,
        companyId: parsed.companyId,
        roles: parsed.roles ?? [],
        permissions: parsed.permissions ?? [],
        accessToken: parsed.accessToken,
        expiresAt: parsed.expiresAt,
      };
    } catch {
      localStorage.removeItem(this.storageKey);
      sessionStorage.removeItem(this.storageKey);
      return this.emptyState(environment.authMode);
    }
  }

  private emptyState(mode: AuthMode): AuthUser {
    return { username: '', authenticated: false, mode };
  }

  private loginErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { detail?: unknown; message?: unknown } | null;
      if (typeof body?.detail === 'string') return body.detail;
      if (typeof body?.message === 'string') return body.message;
    }
    return error instanceof Error ? error.message : 'Unable to sign in right now. Please try again.';
  }

  private readonly http = inject(HttpClient);
}
