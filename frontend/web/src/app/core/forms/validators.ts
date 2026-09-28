import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const notBlankValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  return typeof control.value === 'string' && control.value.trim().length > 0 ? null : { required: true };
};

export const positiveNumberValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (control.value === null || control.value === undefined || control.value === '') {
    return null;
  }
  return Number(control.value) > 0 ? null : { positive: true };
};