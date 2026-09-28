import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiBaseUrl;
  private readonly router = inject(Router);

  constructor(private readonly http: HttpClient, private readonly authService: AuthService) {}

  get<T>(path: string, params?: Record<string, string | number | boolean | null | undefined>): Observable<T> {
    const denied = this.accessDenied<T>();
    if (denied) return denied;
    return this.http
      .get<T>(this.buildUrl(path), { params: this.toParams(params) })
      .pipe(catchError((error) => this.handleError(error)));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    const denied = this.accessDenied<T>();
    if (denied) return denied;
    return this.http
      .post<T>(this.buildUrl(path), body)
      .pipe(catchError((error) => this.handleError(error)));
  }

  put<T>(path: string, body: unknown): Observable<T> {
    const denied = this.accessDenied<T>();
    if (denied) return denied;
    return this.http
      .put<T>(this.buildUrl(path), body)
      .pipe(catchError((error) => this.handleError(error)));
  }

  patch<T>(path: string, body: unknown): Observable<T> {
    const denied = this.accessDenied<T>();
    if (denied) return denied;
    return this.http
      .patch<T>(this.buildUrl(path), body)
      .pipe(catchError((error) => this.handleError(error)));
  }

  delete<T>(path: string): Observable<T> {
    const denied = this.accessDenied<T>();
    if (denied) return denied;
    return this.http
      .delete<T>(this.buildUrl(path))
      .pipe(catchError((error) => this.handleError(error)));
  }

  private buildUrl(path: string): string {
    const normalized = path.startsWith('/') ? path : `/${path}`;
    return `${this.baseUrl}${normalized}`;
  }

  private accessDenied<T>(): Observable<T> | null {
    if (this.authService.isRealAuthenticated()) return null;
    if (this.authService.mode() === 'real' && this.authService.accessToken()) {
      this.authService.logout();
      void this.router.navigateByUrl('/login');
    }
    const message = this.authService.mode() === 'preview'
      ? 'No data available in preview mode.'
      : 'Sign in with Real Mode to access ERP data.';
    return throwError(() => new Error(message));
  }

  private toParams(params?: Record<string, string | number | boolean | null | undefined>): HttpParams | undefined {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        httpParams = httpParams.set(key, String(value));
      }
    });

    return httpParams;
  }

  private handleError(error: HttpErrorResponse) {
    const message = error.error?.message || error.message || 'Unable to load this data. Please try again.';
    return throwError(() => new Error(message));
  }
}
