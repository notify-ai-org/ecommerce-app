import { money } from '../api';

export default function Price({ price, listPrice }: { price: number; listPrice: number }) {
  const off = listPrice > price ? Math.round(((listPrice - price) / listPrice) * 100) : 0;
  return (
    <span className="price">
      <span className="price-now">{money(price)}</span>
      {off > 0 && (
        <>
          <s className="price-was">{money(listPrice)}</s>
          <span className="price-off">−{off}%</span>
        </>
      )}
    </span>
  );
}
