import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { NavigationEnd, provideRouter, Router } from '@angular/router';
import { filter, firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { ApiService } from './api.service';
import { authInterceptor } from './auth.interceptor';

@Component({ standalone: true, template: '' })
class LoginRouteTestComponent {}

describe('ApiService authentication boundary', () => {
  let api: ApiService;
  let auth: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        ApiService,
        AuthService,
        provideRouter([{ path: 'login', component: LoginRouteTestComponent }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    api = TestBed.inject(ApiService);
    auth = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('blocks preview reads and mutations without sending requests', () => {
    auth.loginPreview('preview-user', 'jewelvaulterp@kavith7');
    let readError = '';
    let mutationError = '';

    api.get('/api/companies').subscribe({ error: (error: Error) => readError = error.message });
    api.post('/api/purchases', {}).subscribe({ error: (error: Error) => mutationError = error.message });

    expect(readError).toBe('No data available in preview mode.');
    expect(mutationError).toBe('No data available in preview mode.');
    httpTesting.expectNone('http://localhost:8080/api/companies');
    httpTesting.expectNone('http://localhost:8080/api/purchases');
  });

  it('sends the bearer token for real-mode API calls', async () => {
    const login = firstValueFrom(auth.loginReal('alice', 'real-password'));
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'signed-test-token',
      tokenType: 'Bearer',
      expiresIn: 900,
      user: {
        id: 'user-id',
        username: 'alice',
        email: 'alice@example.test',
        companyId: 'company-id',
        roles: ['ADMIN'],
        permissions: ['VIEW_REPORTS'],
      },
    });
    await login;
    expect(localStorage.getItem('jewelvault-auth-state')).toBeNull();
    expect(sessionStorage.getItem('jewelvault-auth-state')).not.toBeNull();

    const request = firstValueFrom(api.get<unknown[]>('/api/companies'));
    const apiRequest = httpTesting.expectOne('http://localhost:8080/api/companies');
    expect(apiRequest.request.headers.get('Authorization')).toBe('Bearer signed-test-token');
    apiRequest.flush([]);
    await expect(request).resolves.toEqual([]);
  });

  it('clears a real session after an API 401 response', async () => {
    const login = firstValueFrom(auth.loginReal('alice', 'real-password'));
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'expired-token',
      tokenType: 'Bearer',
      expiresIn: 900,
      user: {
        id: 'user-id',
        username: 'alice',
        email: 'alice@example.test',
        companyId: 'company-id',
        roles: [],
        permissions: [],
      },
    });
    await login;

    const request = firstValueFrom(api.get('/api/companies'));
    const router = TestBed.inject(Router);
    const loginNavigation = firstValueFrom(router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
    ));
    httpTesting.expectOne('http://localhost:8080/api/companies').flush({}, { status: 401, statusText: 'Unauthorized' });

    await expect(request).rejects.toThrow();
    await loginNavigation;
    expect(auth.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('jewelvault-auth-state')).toBeNull();
    expect(router.url).toBe('/login');
  });
});