import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { PreviewDataService } from './preview-data.service';

describe('PreviewDataService', () => {
  let auth: AuthService;
  let previewData: PreviewDataService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        PreviewDataService,
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    previewData = TestBed.inject(PreviewDataService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('gets only selected user display fields in Preview Mode', () => {
    auth.loginPreview('preview-user', 'jewelvaulterp@kavith7');
    previewData.getUsers().subscribe();

    const request = httpTesting.expectOne('https://dummyjson.com/users?select=id,firstName,lastName,username,email');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ users: [], total: 0, skip: 0, limit: 0 });
  });

  it('gets selected product demo fields with a read-only request', () => {
    auth.loginPreview('preview-user', 'jewelvaulterp@kavith7');
    previewData.getProducts().subscribe();

    const request = httpTesting.expectOne('https://dummyjson.com/products?select=id,title,brand,sku,category,price,stock,availabilityStatus');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ products: [], total: 0, skip: 0, limit: 0 });
  });

  it('never calls DummyJSON in Real Mode', () => {
    const login = auth.loginReal('alice', 'real-password').subscribe();
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'real-mode-token',
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
    login.unsubscribe();

    let error = '';
    previewData.getUsers().subscribe({ error: (value: Error) => error = value.message });

    expect(error).toBe('Sample data is available only in Preview Mode.');
    httpTesting.expectNone((request) => request.url.startsWith('https://dummyjson.com/'));
  });
});