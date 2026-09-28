import { computed, Injectable, signal } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';

export const PREVIEW_ACCESS_PASSWORD = 'jewelvaulterp@kavith7';

export interface AuthUser {
  username: string;
  role: string;
  authenticated: boolean;
  mode: 'preview' | 'backend';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'jewelvault-auth-state';
  private readonly authState = signal<AuthUser>(this.readStoredState());

  readonly isAuthenticated = computed(() => this.authState().authenticated);
  readonly currentUser = computed(() => this.authState());

  login(username: string, password: string): Observable<AuthUser> {
    const trimmedUsername = username.trim();

    if (!trimmedUsername) {
      return throwError(() => new Error('Username and password are required.'));
    }

    if (password !== PREVIEW_ACCESS_PASSWORD) {
      return throwError(() => new Error('Invalid preview credentials.'));
    }

    const previewUser: AuthUser = {
      username: trimmedUsername,
      role: 'ERP Admin (preview)',
      authenticated: true,
      mode: 'preview',
    };

    this.persistState(previewUser);
    return of(previewUser);
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.authState.set({ username: '', role: '', authenticated: false, mode: 'preview' });
  }

  private persistState(nextState: AuthUser): void {
    localStorage.setItem(this.storageKey, JSON.stringify(nextState));
    this.authState.set(nextState);
  }

  private readStoredState(): AuthUser {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) {
        return { username: '', role: '', authenticated: false, mode: 'preview' };
      }

      const parsed = JSON.parse(raw) as Partial<AuthUser>;
      return {
        username: parsed.username ?? '',
        role: parsed.role ?? '',
        authenticated: Boolean(parsed.authenticated),
        mode: parsed.mode === 'backend' ? 'backend' : 'preview',
      };
    } catch {
      return { username: '', role: '', authenticated: false, mode: 'preview' };
    }
  }
}
