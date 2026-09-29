import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AccountBalanceRecord,
  AccountRecord,
  AccountType,
  BalanceSheetReport,
  Company,
  JournalEntryRecord,
  ProfitLossReport,
  TrialBalanceReport,
} from '../../core/models/erp.models';
import { AccountingService } from '../../core/services/accounting.service';
import { AuthService } from '../../core/services/auth.service';
import { CompanyService } from '../../core/services/company.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-accounting-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PageHeaderComponent],
  template: `
    <app-page-header title="Accounting & Reports" subtitle="Maintain accounts, review journal entries, and inspect company financial reports." />
    @if (isPreview()) {
      <p class="notice" role="note">Accounting data is available in Real Mode only.</p>
    } @else {
      <section class="toolbar">
        <label for="finance-company">Company</label>
        <select id="finance-company" [value]="companyId()" (change)="changeCompany($any($event.target).value)" [disabled]="companies().length < 2">
          @for (company of companies(); track company.id) { <option [value]="company.id">{{ company.name }} ({{ company.currencyCode || 'currency' }})</option> }
        </select>
      </section>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (notice()) { <p class="success" role="status">{{ notice() }}</p> }
      <nav class="tabs" aria-label="Accounting views">
        <button type="button" [class.active]="tab() === 'accounts'" (click)="setTab('accounts')">Accounts</button>
        <button type="button" [class.active]="tab() === 'journals'" (click)="setTab('journals')">Journal entries</button>
        <button type="button" [class.active]="tab() === 'reports'" (click)="setTab('reports')">Reports</button>
      </nav>

      @if (tab() === 'accounts') {
        <section class="panel">
          <div class="panel-heading"><h2>Chart of accounts</h2><button class="primary" type="button" (click)="openAccountForm()">{{ accountEditor() ? 'Cancel' : 'Add account' }}</button></div>
          @if (accountEditor()) {
            <form class="form-grid" [formGroup]="accountForm" (ngSubmit)="saveAccount()">
              <label>Account code<input formControlName="accountCode" maxlength="50" required /></label>
              <label>Account name<input formControlName="name" maxlength="150" required /></label>
              <label>Type<select formControlName="accountType"><option value="ASSET">Asset</option><option value="LIABILITY">Liability</option><option value="EQUITY">Equity</option><option value="REVENUE">Revenue</option><option value="EXPENSE">Expense</option></select></label>
              <div class="form-actions"><button class="primary" type="submit" [disabled]="busy()">{{ busy() ? 'Saving...' : 'Save account' }}</button></div>
            </form>
          }
          @if (accountLoading()) { <p class="state" role="status">Loading accounts...</p> }
          @else if (!accounts().length) { <p class="state">No accounts are configured for this company.</p> }
          @else {
            <div class="table-scroll"><table><thead><tr><th>Code</th><th>Name</th><th>Type</th><th>Status</th><th>Actions</th></tr></thead><tbody>
              @for (account of accounts(); track account.id) {
                <tr><td>{{ account.accountCode }}</td><td>{{ account.name }}</td><td>{{ account.accountType }}</td><td>{{ account.active ? 'Active' : 'Inactive' }}</td>
                  <td class="actions"><button type="button" (click)="editAccount(account)">Edit</button><button type="button" (click)="setAccountActive(account, !account.active)">{{ account.active ? 'Deactivate' : 'Activate' }}</button></td></tr>
              }
            </tbody></table></div>
          }
        </section>
      }

      @if (tab() === 'journals') {
        <section class="panel">
          <div class="panel-heading"><h2>Journal entries</h2><button class="primary" type="button" (click)="journalEditor.update(open => !open)">{{ journalEditor() ? 'Cancel' : 'New journal entry' }}</button></div>
          @if (journalEditor()) {
            @if (!accounts().length) { <p class="state">Create accounts before entering a journal entry.</p> }
            @else {
              <form [formGroup]="journalForm" (ngSubmit)="saveJournalEntry()">
                <div class="form-grid">
                  <label>Entry number<input formControlName="entryNumber" maxlength="100" required /></label>
                  <label>Description<input formControlName="description" maxlength="500" /></label>
                  <label>Reference number<input formControlName="referenceNumber" maxlength="100" /></label>
                </div>
                <h3>Debit line</h3>
                <div class="form-grid" formGroupName="debitLine">
                  <label>Account<select formControlName="accountId" required><option value="">Choose account</option>@for (account of activeAccounts(); track account.id) { <option [value]="account.id">{{ account.accountCode }} · {{ account.name }}</option> }</select></label>
                  <label>Description<input formControlName="description" maxlength="500" /></label>
                  <label>Debit amount<input type="number" min="0" step="0.01" formControlName="debitAmount" /></label>
                  <label>Credit amount<input type="number" min="0" step="0.01" formControlName="creditAmount" /></label>
                </div>
                <h3>Credit line</h3>
                <div class="form-grid" formGroupName="creditLine">
                  <label>Account<select formControlName="accountId" required><option value="">Choose account</option>@for (account of activeAccounts(); track account.id) { <option [value]="account.id">{{ account.accountCode }} · {{ account.name }}</option> }</select></label>
                  <label>Description<input formControlName="description" maxlength="500" /></label>
                  <label>Debit amount<input type="number" min="0" step="0.01" formControlName="debitAmount" /></label>
                  <label>Credit amount<input type="number" min="0" step="0.01" formControlName="creditAmount" /></label>
                </div>
                <div class="form-actions"><button class="primary" type="submit" [disabled]="busy()">{{ busy() ? 'Saving...' : 'Save draft' }}</button></div>
              </form>
            }
          }
          @if (journalLoading()) { <p class="state" role="status">Loading journal entries...</p> }
          @else if (!journalEntries().length) { <p class="state">No journal entries are available.</p> }
          @else {
            <div class="table-scroll"><table><thead><tr><th>Entry</th><th>Date</th><th>Description</th><th>Debit</th><th>Credit</th><th>Status</th><th>Actions</th></tr></thead><tbody>
              @for (entry of journalEntries(); track entry.id) {
                <tr><td>{{ entry.entryNumber }}</td><td>{{ entry.entryDate | date:'mediumDate' }}</td><td>{{ entry.description || '—' }}</td><td>{{ entry.totalDebit | number:'1.2-2' }}</td><td>{{ entry.totalCredit | number:'1.2-2' }}</td><td>{{ entry.status }}</td>
                  <td class="actions">@if (entry.status === 'DRAFT') { <button type="button" (click)="changeJournalStatus(entry, 'post')">Post</button><button type="button" (click)="changeJournalStatus(entry, 'cancel')">Cancel</button> }</td></tr>
              }
            </tbody></table></div>
          }
        </section>
      }

      @if (tab() === 'reports') {
        <section class="panel">
          <div class="panel-heading"><h2>Accounting reports</h2>
            <label class="report-select">Report<select [value]="reportType()" (change)="selectReport($any($event.target).value)"><option value="balances">Account balances</option><option value="trial">Trial balance</option><option value="profit-loss">Profit & loss</option><option value="balance-sheet">Balance sheet</option></select></label>
          </div>
          @if (reportLoading()) { <p class="state" role="status">Loading report...</p> }
          @else if (reportError()) { <p class="error" role="alert">{{ reportError() }}</p> }
          @else if (reportType() === 'balances') {
            @if (!accountBalances().length) { <p class="state">No account balances are available.</p> }
            @else { <div class="table-scroll"><table><thead><tr><th>Code</th><th>Account</th><th>Type</th><th>Debits</th><th>Credits</th><th>Balance</th></tr></thead><tbody>@for (account of accountBalances(); track account.accountId) { <tr><td>{{ account.accountCode }}</td><td>{{ account.accountName }}</td><td>{{ account.accountType }}</td><td>{{ account.debitTotal | number:'1.2-2' }}</td><td>{{ account.creditTotal | number:'1.2-2' }}</td><td>{{ account.balance | number:'1.2-2' }}</td></tr> }</tbody></table></div> }
          } @else if (reportType() === 'trial' && trialBalance()) {
            <div class="metrics"><article><span>Total debits</span><strong>{{ trialBalance()?.totalDebits | number:'1.2-2' }}</strong></article><article><span>Total credits</span><strong>{{ trialBalance()?.totalCredits | number:'1.2-2' }}</strong></article></div>
            <div class="table-scroll"><table><thead><tr><th>Code</th><th>Account</th><th>Type</th><th>Debits</th><th>Credits</th><th>Balance</th></tr></thead><tbody>@for (account of trialBalance()?.accounts ?? []; track account.accountId) { <tr><td>{{ account.accountCode }}</td><td>{{ account.accountName }}</td><td>{{ account.accountType }}</td><td>{{ account.debitTotal | number:'1.2-2' }}</td><td>{{ account.creditTotal | number:'1.2-2' }}</td><td>{{ account.balance | number:'1.2-2' }}</td></tr> }</tbody></table></div>
          } @else if (reportType() === 'profit-loss' && profitLoss()) {
            <div class="metrics"><article><span>Revenue</span><strong>{{ profitLoss()?.revenue | number:'1.2-2' }}</strong></article><article><span>Expenses</span><strong>{{ profitLoss()?.expenses | number:'1.2-2' }}</strong></article><article><span>Net profit</span><strong>{{ profitLoss()?.netProfit | number:'1.2-2' }}</strong></article></div>
          } @else if (reportType() === 'balance-sheet' && balanceSheet()) {
            <div class="metrics"><article><span>Assets</span><strong>{{ balanceSheet()?.assets | number:'1.2-2' }}</strong></article><article><span>Liabilities</span><strong>{{ balanceSheet()?.liabilities | number:'1.2-2' }}</strong></article><article><span>Equity</span><strong>{{ balanceSheet()?.equity | number:'1.2-2' }}</strong></article><article><span>Net profit</span><strong>{{ balanceSheet()?.netProfit | number:'1.2-2' }}</strong></article><article><span>Liabilities + equity</span><strong>{{ balanceSheet()?.totalLiabilitiesAndEquity | number:'1.2-2' }}</strong></article></div>
          }
        </section>
      }
    }
  `,
  styles: [`
    :host { display: block; color: var(--primary-text); }
    .toolbar, .panel-heading, .form-actions, .actions { display: flex; align-items: center; gap: .65rem; }
    .toolbar { margin-bottom: 1rem; }
    .panel-heading { justify-content: space-between; margin-bottom: .9rem; }
    label { display: grid; gap: .35rem; color: var(--primary-text); font-size: .82rem; font-weight: 600; }
    input, select { width: 100%; min-width: 0; min-height: 2.5rem; padding: .55rem .65rem; border: 1px solid var(--border-color); border-radius: .4rem; background: var(--surface); color: inherit; font: inherit; font-weight: 400; }
    button { min-height: 2.35rem; padding: .45rem .75rem; border: 1px solid var(--border-color); border-radius: .4rem; background: var(--surface); color: inherit; font: inherit; cursor: pointer; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    button.primary { background: var(--primary); border-color: var(--primary); color: white; }
    .tabs { display: flex; gap: .35rem; margin-bottom: .9rem; border-bottom: 1px solid var(--border-color); }
    .tabs button { border: 0; border-radius: .35rem .35rem 0 0; background: transparent; }
    .tabs button.active { color: var(--primary); background: var(--surface); box-shadow: inset 0 -2px var(--primary); }
    .panel { padding: 1rem; border: 1px solid var(--border-color); border-radius: .45rem; background: var(--surface); }
    .panel h2 { margin: 0; font-size: 1.08rem; }
    .panel h3 { margin: .9rem 0 .55rem; font-size: .9rem; }
    .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr)); gap: .7rem; margin-bottom: .9rem; }
    .form-actions { justify-content: flex-end; margin: .6rem 0 1rem; }
    .report-select { min-width: 12rem; }
    .table-scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; font-size: .84rem; }
    th, td { padding: .65rem .5rem; border-bottom: 1px solid var(--border-color); white-space: nowrap; }
    th { color: var(--secondary-text); font-size: .72rem; text-transform: uppercase; }
    .actions { flex-wrap: wrap; }
    .metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(9rem, 1fr)); gap: .65rem; margin: .7rem 0 1rem; }
    .metrics article { display: grid; gap: .25rem; padding: .85rem; border: 1px solid var(--border-color); border-radius: .4rem; background: var(--surface-soft); }
    .metrics span, .state { color: var(--secondary-text); font-size: .86rem; }
    .metrics strong { font-size: 1.05rem; }
    .notice, .error, .success { padding: .7rem .85rem; border-radius: .4rem; }
    .notice { background: var(--surface-soft); color: var(--secondary-text); }
    .error { background: var(--ruby-light); color: var(--ruby); }
    .success { background: var(--emerald-light); color: var(--emerald); }
    @media (max-width: 620px) { .toolbar, .panel-heading { align-items: stretch; flex-direction: column; } .toolbar select { width: 100%; } .panel-heading > button { width: 100%; } }
  `],
})
export class AccountingPageComponent {
  private readonly auth = inject(AuthService);
  private readonly companyService = inject(CompanyService);
  private readonly accountingService = inject(AccountingService);

  readonly isPreview = computed(() => this.auth.isPreviewAuthenticated());
  readonly companies = signal<Company[]>([]);
  readonly companyId = signal('');
  readonly tab = signal<'accounts' | 'journals' | 'reports'>('accounts');
  readonly accounts = signal<AccountRecord[]>([]);
  readonly activeAccounts = computed(() => this.accounts().filter((account) => account.active));
  readonly journalEntries = signal<JournalEntryRecord[]>([]);
  readonly accountBalances = signal<AccountBalanceRecord[]>([]);
  readonly trialBalance = signal<TrialBalanceReport | null>(null);
  readonly profitLoss = signal<ProfitLossReport | null>(null);
  readonly balanceSheet = signal<BalanceSheetReport | null>(null);
  readonly reportType = signal<'balances' | 'trial' | 'profit-loss' | 'balance-sheet'>('balances');
  readonly accountLoading = signal(false);
  readonly journalLoading = signal(false);
  readonly reportLoading = signal(false);
  readonly busy = signal(false);
  readonly accountEditor = signal(false);
  readonly journalEditor = signal(false);
  readonly error = signal<string | null>(null);
  readonly reportError = signal<string | null>(null);
  readonly notice = signal<string | null>(null);
  private editingAccountId: string | null = null;

  readonly accountForm = new FormGroup({
    accountCode: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
    accountType: new FormControl<AccountType>('ASSET', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly journalForm = new FormGroup({
    entryNumber: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    referenceNumber: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    debitLine: new FormGroup({
      accountId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
      debitAmount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
      creditAmount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    }),
    creditLine: new FormGroup({
      accountId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
      debitAmount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
      creditAmount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    }),
  });

  constructor() {
    if (this.isPreview()) return;
    this.companyService.list().subscribe({
      next: (companies) => {
        this.companies.set(companies);
        const userCompany = this.auth.currentUser().companyId;
        const selected = companies.find((company) => company.id === userCompany) ?? companies[0];
        if (selected) {
          this.companyId.set(selected.id);
          this.loadAccounts();
          this.loadJournals();
          this.loadReport();
        }
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  changeCompany(companyId: string): void {
    this.companyId.set(companyId);
    this.loadReport();
  }

  setTab(tab: 'accounts' | 'journals' | 'reports'): void {
    this.tab.set(tab);
    this.error.set(null);
    if (tab === 'accounts') this.loadAccounts();
    if (tab === 'journals') this.loadJournals();
    if (tab === 'reports') this.loadReport();
  }

  openAccountForm(): void {
    this.error.set(null);
    if (this.accountEditor()) {
      this.accountEditor.set(false);
      this.editingAccountId = null;
      this.accountForm.reset({ accountCode: '', name: '', accountType: 'ASSET' });
      return;
    }
    this.editingAccountId = null;
    this.accountForm.reset({ accountCode: '', name: '', accountType: 'ASSET' });
    this.accountEditor.set(true);
  }

  editAccount(account: AccountRecord): void {
    this.editingAccountId = account.id;
    this.accountForm.reset({ accountCode: account.accountCode, name: account.name, accountType: account.accountType });
    this.accountEditor.set(true);
    this.tab.set('accounts');
  }

  saveAccount(): void {
    if (this.accountForm.invalid || this.busy() || !this.companyId()) {
      this.accountForm.markAllAsTouched();
      return;
    }
    const value = this.accountForm.getRawValue();
    this.busy.set(true);
    const request = this.editingAccountId
      ? this.accountingService.updateAccount(this.editingAccountId, value)
      : this.accountingService.createAccount({ ...value, companyId: this.companyId() });
    request.subscribe({
      next: () => {
        this.busy.set(false);
        this.accountEditor.set(false);
        this.notice.set(this.editingAccountId ? 'Account updated.' : 'Account created.');
        this.editingAccountId = null;
        this.loadAccounts();
      },
      error: (error: Error) => {
        this.busy.set(false);
        this.error.set(error.message);
      },
    });
  }

  setAccountActive(account: AccountRecord, active: boolean): void {
    this.accountingService.setAccountActive(account.id, active).subscribe({
      next: () => {
        this.notice.set(`Account ${active ? 'activated' : 'deactivated'}.`);
        this.loadAccounts();
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  saveJournalEntry(): void {
    if (this.journalForm.invalid || this.busy() || !this.companyId()) {
      this.journalForm.markAllAsTouched();
      return;
    }
    const value = this.journalForm.getRawValue();
    const lines = [value.debitLine, value.creditLine].map((line) => ({
      accountId: line.accountId,
      description: line.description.trim() || null,
      debitAmount: Number(line.debitAmount),
      creditAmount: Number(line.creditAmount),
    }));
    const totalDebit = lines.reduce((sum, line) => sum + line.debitAmount, 0);
    const totalCredit = lines.reduce((sum, line) => sum + line.creditAmount, 0);
    if (!lines.every((line) => (line.debitAmount > 0) !== (line.creditAmount > 0)) || totalDebit !== totalCredit) {
      this.error.set('Each journal line must contain either debit or credit, and totals must balance.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.accountingService.createJournalEntry({
      companyId: this.companyId(),
      entryNumber: value.entryNumber.trim(),
      description: value.description.trim() || null,
      referenceNumber: value.referenceNumber.trim() || null,
      lines,
    }).subscribe({
      next: () => {
        this.busy.set(false);
        this.journalEditor.set(false);
        this.notice.set('Journal entry saved as a draft.');
        this.resetJournalForm();
        this.loadJournals();
      },
      error: (error: Error) => {
        this.busy.set(false);
        this.error.set(error.message);
      },
    });
  }

  changeJournalStatus(entry: JournalEntryRecord, action: 'post' | 'cancel'): void {
    this.accountingService.setJournalEntryStatus(entry.id, action).subscribe({
      next: () => {
        this.notice.set(action === 'post' ? 'Journal entry posted.' : 'Journal entry cancelled.');
        this.loadJournals();
      },
      error: (error: Error) => this.error.set(error.message),
    });
  }

  selectReport(report: 'balances' | 'trial' | 'profit-loss' | 'balance-sheet'): void {
    this.reportType.set(report);
    this.loadReport();
  }

  private loadAccounts(): void {
    this.accountLoading.set(true);
    this.accountingService.accounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.accountLoading.set(false);
      },
      error: (error: Error) => {
        this.accountLoading.set(false);
        this.error.set(error.message);
      },
    });
  }

  private loadJournals(): void {
    this.journalLoading.set(true);
    this.accountingService.journalEntries().subscribe({
      next: (entries) => {
        this.journalEntries.set(entries);
        this.journalLoading.set(false);
      },
      error: (error: Error) => {
        this.journalLoading.set(false);
        this.error.set(error.message);
      },
    });
  }

  private loadReport(): void {
    if (!this.companyId()) return;
    this.reportLoading.set(true);
    this.reportError.set(null);
    const companyId = this.companyId();
    const done = () => this.reportLoading.set(false);
    if (this.reportType() === 'balances') {
      this.accountingService.accountBalances(companyId).subscribe({ next: (data) => { this.accountBalances.set(data); done(); }, error: (error: Error) => { this.reportError.set(error.message); done(); } });
    } else if (this.reportType() === 'trial') {
      this.accountingService.trialBalance(companyId).subscribe({ next: (data) => { this.trialBalance.set(data); done(); }, error: (error: Error) => { this.reportError.set(error.message); done(); } });
    } else if (this.reportType() === 'profit-loss') {
      this.accountingService.profitLoss(companyId).subscribe({ next: (data) => { this.profitLoss.set(data); done(); }, error: (error: Error) => { this.reportError.set(error.message); done(); } });
    } else {
      this.accountingService.balanceSheet(companyId).subscribe({ next: (data) => { this.balanceSheet.set(data); done(); }, error: (error: Error) => { this.reportError.set(error.message); done(); } });
    }
  }

  private resetJournalForm(): void {
    this.journalForm.reset({
      entryNumber: '', description: '', referenceNumber: '',
      debitLine: { accountId: '', description: '', debitAmount: 0, creditAmount: 0 },
      creditLine: { accountId: '', description: '', debitAmount: 0, creditAmount: 0 },
    });
  }
}
