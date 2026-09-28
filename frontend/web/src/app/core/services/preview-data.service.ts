import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export interface PreviewUser {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
}

export interface PreviewProduct {
  id: number;
  title: string;
  brand?: string;
  sku: string;
  category: string;
  price: number;
  stock: number;
  availabilityStatus: string;
}

export interface PreviewUsersResponse {
  users: PreviewUser[];
  total: number;
  skip: number;
  limit: number;
}

export interface PreviewProductsResponse {
  products: PreviewProduct[];
  total: number;
  skip: number;
  limit: number;
}

@Injectable({ providedIn: 'root' })
export class PreviewDataService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = 'https://dummyjson.com';

  getUsers(): Observable<PreviewUsersResponse> {
    if (!this.authService.isPreviewAuthenticated()) return this.previewOnlyError();
    const params = new HttpParams().set('select', 'id,firstName,lastName,username,email');
    return this.http.get<PreviewUsersResponse>(`${this.baseUrl}/users`, { params });
  }

  getProducts(): Observable<PreviewProductsResponse> {
    if (!this.authService.isPreviewAuthenticated()) return this.previewOnlyError();
    const params = new HttpParams().set('select', 'id,title,brand,sku,category,price,stock,availabilityStatus');
    return this.http.get<PreviewProductsResponse>(`${this.baseUrl}/products`, { params });
  }

  private previewOnlyError<T>(): Observable<T> {
    return throwError(() => new Error('Sample data is available only in Preview Mode.'));
  }
}