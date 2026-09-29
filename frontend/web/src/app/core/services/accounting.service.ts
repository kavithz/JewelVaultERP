import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AccountBalanceRecord,
  AccountRecord,
  BalanceSheetReport,
  JournalEntryRecord,
  ProfitLossReport,
  TrialBalanceReport,
} from '../models/erp.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class AccountingService {
  constructor(private readonly api: ApiService) {}

  accounts(): Observable<AccountRecord[]> {
    return this.api.get<AccountRecord[]>('/api/accounts');
  }

  createAccount(payload: Pick<AccountRecord, 'companyId' | 'accountCode' | 'name' | 'accountType'>): Observable<AccountRecord> {
    return this.api.post<AccountRecord>('/api/accounts', payload);
  }

  updateAccount(id: string, payload: Pick<AccountRecord, 'accountCode' | 'name' | 'accountType'>): Observable<AccountRecord> {
    return this.api.put<AccountRecord>(`/api/accounts/${id}`, payload);
  }

  setAccountActive(id: string, active: boolean): Observable<AccountRecord> {
    return this.api.patch<AccountRecord>(`/api/accounts/${id}/active?active=${active}`, {});
  }

  journalEntries(): Observable<JournalEntryRecord[]> {
    return this.api.get<JournalEntryRecord[]>('/api/journal-entries');
  }

  createJournalEntry(payload: unknown): Observable<JournalEntryRecord> {
    return this.api.post<JournalEntryRecord>('/api/journal-entries', payload);
  }

  setJournalEntryStatus(id: string, action: 'post' | 'cancel'): Observable<JournalEntryRecord> {
    return this.api.patch<JournalEntryRecord>(`/api/journal-entries/${id}/${action}`, {});
  }

  accountBalances(companyId: string): Observable<AccountBalanceRecord[]> {
    return this.api.get<AccountBalanceRecord[]>(`/api/accounting/reports/accounts/${companyId}`);
  }

  trialBalance(companyId: string): Observable<TrialBalanceReport> {
    return this.api.get<TrialBalanceReport>(`/api/accounting/reports/trial-balance/${companyId}`);
  }

  profitLoss(companyId: string): Observable<ProfitLossReport> {
    return this.api.get<ProfitLossReport>(`/api/accounting/reports/profit-loss/${companyId}`);
  }

  balanceSheet(companyId: string): Observable<BalanceSheetReport> {
    return this.api.get<BalanceSheetReport>(`/api/accounting/reports/balance-sheet/${companyId}`);
  }
}