const GLYPHS: Record<string, string> = {
  'P-1001': '⌨️', 'P-1002': '🖱️', 'P-1003': '🎧', 'P-1004': '🖥️',
  'P-2001': '🧶', 'P-2002': '👟', 'P-2003': '🧥',
  'P-3001': '☕', 'P-3002': '🍳', 'P-3003': '🛏️',
  'P-4001': '📘', 'P-4002': '📗',
};
const CATEGORY_GLYPHS: Record<string, string> = {
  Electronics: '🔌', Apparel: '👕', Home: '🏠', Books: '📚',
};

/** Placeholder product imagery: a category-tinted tile with a glyph. */
export default function ProductArt({ id, category, size = 'md' }: { id: string; category: string; size?: 'sm' | 'md' | 'lg' }) {
  const glyph = GLYPHS[id] ?? CATEGORY_GLYPHS[category] ?? '🛍️';
  return (
    <div className={`art art-${size} art-${category.toLowerCase()}`} aria-hidden>
      <span>{glyph}</span>
    </div>
  );
}
