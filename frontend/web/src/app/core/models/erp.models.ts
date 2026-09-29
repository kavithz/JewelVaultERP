export interface Company {
  id: string;
  name: string;
  legalName?: string;
  countryCode?: string;
  currencyCode?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface User {
  id: string;
  companyId?: string;
  username: string;
  email?: string | null;
  roleName?: string;
  enabled?: boolean;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Branch {
  id: string;
  companyId?: string;
  name: string;
  code?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  countryCode?: string;
  phone?: string;
  email?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id: string;
  companyId: string;
  sku: string;
  name: string;
  jewelleryType: string | null;
  metalType: string | null;
  purity: string | null;
  grossWeight: number | null;
  netWeight: number | null;
  makingCharge: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  companyId: string;
  sku: string;
  name: string;
  jewelleryType: string;
  metalType: string;
  purity: string;
  grossWeight: number;
  netWeight: number;
  makingCharge: number;
}

export type UpdateProductRequest = Omit<CreateProductRequest, 'companyId'>;

export interface Purity {
  id: string;
  companyId: string;
  name: string;
  code: string;
  fineness: number | null;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePurityRequest {
  companyId: string;
  name: string;
  code: string;
  fineness: number;
  description: string | null;
}

export type UpdatePurityRequest = Omit<CreatePurityRequest, 'companyId'>;

export interface JewelleryType {
  id: string;
  companyId: string;
  name: string;
  code: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateJewelleryTypeRequest {
  companyId: string;
  name: string;
  code: string;
  description: string | null;
}

export type UpdateJewelleryTypeRequest = Omit<CreateJewelleryTypeRequest, 'companyId'>;

export interface Gemstone {
  id: string;
  companyId: string;
  name: string;
  code: string;
  category: string | null;
  color: string | null;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateGemstoneRequest {
  companyId: string;
  name: string;
  code: string;
  category: string | null;
  color: string | null;
  description: string | null;
}

export type UpdateGemstoneRequest = Omit<CreateGemstoneRequest, 'companyId'>;

export interface Warehouse {
  id: string;
  branchId: string;
  companyId: string;
  name: string;
  code: string;
  address: string | null;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWarehouseRequest {
  branchId: string;
  name: string;
  code: string;
  address: string | null;
  description: string | null;
}

export type UpdateWarehouseRequest = Omit<CreateWarehouseRequest, 'branchId'>;

export interface Supplier {
  id: string;
  companyId: string;
  name: string;
  code: string;
  contactPerson: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  taxNumber: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupplierRequest {
  companyId: string;
  name: string;
  code: string;
  contactPerson: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  taxNumber: string | null;
}

export type UpdateSupplierRequest = Omit<CreateSupplierRequest, 'companyId'>;

export interface Customer {
  id: string;
  companyId: string;
  name: string;
  code: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  taxNumber: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  companyId: string;
  name: string;
  code: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  taxNumber: string | null;
}

export type UpdateCustomerRequest = Omit<CreateCustomerRequest, 'companyId'>;

export type UserRecord = User;
export type BranchRecord = Branch;

export interface CrudState<T> {
  loading: boolean;
  error: string | null;
  data: T[];
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface InventoryRecord {
  id: string;
  warehouseId: string;
  productId: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInventoryRequest {
  warehouseId: string;
  productId: string;
  quantity: number;
  reservedQuantity: number;
}

export interface UpdateInventoryRequest {
  quantity: number;
  reservedQuantity: number;
}

export type MovementType =
  | 'PURCHASE'
  | 'SALE'
  | 'TRANSFER_IN'
  | 'TRANSFER_OUT'
  | 'ADJUSTMENT_IN'
  | 'ADJUSTMENT_OUT'
  | 'MANUFACTURING_IN'
  | 'MANUFACTURING_OUT';

export interface StockMovementRecord {
  id: string;
  inventoryId: string;
  warehouseId: string;
  productId: string;
  movementType: MovementType;
  quantity: number;
  referenceNumber: string | null;
  notes: string | null;
  movementDate: string;
  createdAt: string;
}

export interface CreateStockMovementRequest {
  inventoryId: string;
  movementType: MovementType;
  quantity: number;
  referenceNumber: string | null;
  notes: string | null;
  movementDate?: string;
}

export type AdjustmentType = 'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT';
export type StockAdjustmentStatus = 'DRAFT' | 'COMPLETED' | 'CANCELLED';

export interface StockAdjustmentItem {
  id: string;
  productId: string;
  quantity: number;
  adjustmentType: AdjustmentType;
}

export interface StockAdjustmentRecord {
  id: string;
  companyId: string;
  warehouseId: string;
  adjustmentNumber: string;
  adjustmentDate: string;
  status: StockAdjustmentStatus;
  notes: string | null;
  items: StockAdjustmentItem[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateStockAdjustmentItemRequest {
  productId: string;
  quantity: number;
  adjustmentType: AdjustmentType;
}

export interface CreateStockAdjustmentRequest {
  companyId: string;
  warehouseId: string;
  adjustmentNumber: string;
  notes: string | null;
  items: CreateStockAdjustmentItemRequest[];
}

export type SaleStatus = 'DRAFT' | 'COMPLETED' | 'CANCELLED';

export interface SaleItemRecord {
  id: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface SaleRecord {
  id: string;
  companyId: string;
  customerId: string;
  warehouseId: string;
  saleNumber: string;
  saleDate: string;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  status: SaleStatus;
  items: SaleItemRecord[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateSaleItemRequest {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateSaleRequest {
  companyId: string;
  customerId: string;
  warehouseId: string;
  saleNumber: string;
  saleDate?: string;
  taxAmount: number;
  discountAmount: number;
  items: CreateSaleItemRequest[];
}

export type EmploymentStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'TERMINATED' | 'RESIGNED';

export interface EmployeeRecord {
  id: string;
  companyId: string;
  branchId: string | null;
  employeeNumber: string;
  firstName: string;
  lastName: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  department: string | null;
  designation: string | null;
  employmentStatus: EmploymentStatus;
  joiningDate: string;
  exitDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  companyId: string;
  branchId: string | null;
  employeeNumber: string;
  firstName: string;
  lastName: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  department: string | null;
  designation: string | null;
  joiningDate: string;
}

export type StockTransferStatus = 'DRAFT' | 'COMPLETED' | 'CANCELLED';

export interface StockTransferItemRecord {
  id: string;
  productId: string;
  quantity: number;
}

export interface StockTransferRecord {
  id: string;
  companyId: string;
  sourceWarehouseId: string;
  destinationWarehouseId: string;
  transferNumber: string;
  transferDate: string;
  status: StockTransferStatus;
  notes: string | null;
  items: StockTransferItemRecord[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateStockTransferItemRequest {
  productId: string;
  quantity: number;
}

export interface CreateStockTransferRequest {
  companyId: string;
  sourceWarehouseId: string;
  destinationWarehouseId: string;
  transferNumber: string;
  transferDate?: string;
  notes: string | null;
  items: CreateStockTransferItemRequest[];
}

export type PurchaseStatus = 'DRAFT' | 'RECEIVED' | 'CANCELLED';

export interface PurchaseItemRecord {
  id: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface PurchaseRecord {
  id: string;
  companyId: string;
  supplierId: string;
  warehouseId: string;
  purchaseNumber: string;
  purchaseDate: string;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  status: PurchaseStatus;
  items: PurchaseItemRecord[];
  createdAt: string;
  updatedAt: string;
}

export interface CreatePurchaseItemRequest {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface CreatePurchaseRequest {
  companyId: string;
  supplierId: string;
  warehouseId: string;
  purchaseNumber: string;
  purchaseDate?: string;
  taxAmount: number;
  discountAmount: number;
  items: CreatePurchaseItemRequest[];
}

export interface RoleRecord {
  id: string;
  companyId: string;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionRecord {
  id: string;
  name: string;
  description: string | null;
}

export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';

export interface AccountRecord {
  id: string;
  companyId: string;
  accountCode: string;
  name: string;
  accountType: AccountType;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export type JournalEntryStatus = 'DRAFT' | 'POSTED' | 'CANCELLED';

export interface JournalEntryLineRecord {
  id: string;
  accountId: string;
  description: string | null;
  debitAmount: number;
  creditAmount: number;
}

export interface JournalEntryRecord {
  id: string;
  companyId: string;
  entryNumber: string;
  entryDate: string;
  description: string | null;
  referenceNumber: string | null;
  status: JournalEntryStatus;
  totalDebit: number;
  totalCredit: number;
  lines: JournalEntryLineRecord[];
  createdAt: string;
  updatedAt: string;
}

export interface AccountBalanceRecord {
  accountId: string;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  debitTotal: number;
  creditTotal: number;
  balance: number;
}

export interface TrialBalanceReport {
  accounts: AccountBalanceRecord[];
  totalDebits: number;
  totalCredits: number;
}

export interface ProfitLossReport {
  revenue: number;
  expenses: number;
  netProfit: number;
}

export interface BalanceSheetReport {
  assets: number;
  liabilities: number;
  equity: number;
  netProfit: number;
  totalLiabilitiesAndEquity: number;
}
