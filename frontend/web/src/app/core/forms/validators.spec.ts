import { FormControl } from '@angular/forms';
import { notBlankValidator, positiveNumberValidator } from './validators';

describe('master-data form validators', () => {
  it('rejects blank or whitespace-only required text', () => {
    expect(new FormControl('   ', notBlankValidator).errors).toEqual({ required: true });
  });

  it('accepts non-blank text without trimming the submitted value', () => {
    const control = new FormControl(' SKU-1 ', notBlankValidator);
    expect(control.valid).toBe(true);
    expect(control.value).toBe(' SKU-1 ');
  });

  it('rejects zero and negative values for strictly positive fields', () => {
    expect(new FormControl(0, positiveNumberValidator).errors).toEqual({ positive: true });
    expect(new FormControl(-1, positiveNumberValidator).errors).toEqual({ positive: true });
  });

  it('accepts positive values and leaves requiredness to the composed required validator', () => {
    expect(new FormControl(0.001, positiveNumberValidator).valid).toBe(true);
    expect(new FormControl(null, positiveNumberValidator).valid).toBe(true);
  });
});