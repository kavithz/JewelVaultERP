declare const JEWELVAULT_API_BASE_URL: string;

export const environment = {
  production: true,
  appName: 'JewelVaultERP',
  apiBaseUrl: JEWELVAULT_API_BASE_URL,
  authMode: 'real' as 'preview' | 'real',
  defaultCompanyId: '00000000-0000-0000-0000-000000000000',
};
