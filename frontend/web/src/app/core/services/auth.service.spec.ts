import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { AuthService, PREVIEW_ACCESS_PASSWORD } from './auth.service';

describe('AuthService preview login', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [AuthService] });
  });

  afterEach(() => localStorage.clear());

  it('accepts kavith with the exact preview password', async () => {
    const auth = TestBed.inject(AuthService);
    const user = await firstValueFrom(auth.login('kavith', PREVIEW_ACCESS_PASSWORD));

    expect(user.authenticated).toBe(true);
    expect(user.username).toBe('kavith');
    expect(user.mode).toBe('preview');
  });

  it('accepts any other non-empty username with the exact preview password', async () => {
    const auth = TestBed.inject(AuthService);
    const user = await firstValueFrom(auth.login('anything', PREVIEW_ACCESS_PASSWORD));

    expect(user.authenticated).toBe(true);
    expect(user.username).toBe('anything');
  });

  it('rejects a blank username', async () => {
    const auth = TestBed.inject(AuthService);

    await expect(firstValueFrom(auth.login('   ', PREVIEW_ACCESS_PASSWORD)))
      .rejects.toThrow('Username and password are required.');
  });

  it('rejects the wrong password for kavith', async () => {
    const auth = TestBed.inject(AuthService);

    await expect(firstValueFrom(auth.login('kavith', 'wrong-password')))
      .rejects.toThrow('Invalid preview credentials.');
  });

  it('rejects the wrong password for any other username', async () => {
    const auth = TestBed.inject(AuthService);

    await expect(firstValueFrom(auth.login('anything', 'wrong-password')))
      .rejects.toThrow('Invalid preview credentials.');
  });

  it('keeps preview sessions protected and logout clears the stored session', async () => {
    const auth = TestBed.inject(AuthService);
    await firstValueFrom(auth.login('anything', PREVIEW_ACCESS_PASSWORD));

    expect(auth.isAuthenticated()).toBe(true);
    expect(localStorage.getItem('jewelvault-auth-state')).not.toBeNull();

    auth.logout();

    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('jewelvault-auth-state')).toBeNull();
  });
});