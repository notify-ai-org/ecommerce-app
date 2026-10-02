interface Props {
  value: number;
  min?: number;
  max?: number;
  disabled?: boolean;
  onChange: (value: number) => void;
}

export default function QuantityStepper({ value, min = 1, max = 99, disabled, onChange }: Props) {
  return (
    <div className="stepper">
      <button type="button" aria-label="Decrease quantity" disabled={disabled || value <= min}
        onClick={() => onChange(value - 1)}>−</button>
      <span aria-live="polite">{value}</span>
      <button type="button" aria-label="Increase quantity" disabled={disabled || value >= max}
        onClick={() => onChange(value + 1)}>+</button>
    </div>
  );
}
