import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const configuredApiBaseUrl = (process.env.API_BASE_URL ?? '').trim().replace(/\/+$/, '');

if (configuredApiBaseUrl) {
  let apiUrl;
  try {
    apiUrl = new URL(configuredApiBaseUrl);
  } catch {
    console.error('API_BASE_URL must be an absolute HTTPS URL.');
    process.exit(1);
  }
  if (apiUrl.protocol !== 'https:') {
    console.error('API_BASE_URL must use HTTPS for production builds.');
    process.exit(1);
  }
} else {
  console.warn('API_BASE_URL is unset; production API requests will use the frontend origin.');
}

const cliPath = fileURLToPath(new URL('../node_modules/@angular/cli/bin/ng.js', import.meta.url));
const result = spawnSync(process.execPath, [
  cliPath,
  'build',
  '--define',
  `JEWELVAULT_API_BASE_URL=${JSON.stringify(configuredApiBaseUrl)}`,
], { stdio: 'inherit' });

if (result.error) {
  console.error(result.error.message);
  process.exit(1);
}
process.exit(result.status ?? 1);