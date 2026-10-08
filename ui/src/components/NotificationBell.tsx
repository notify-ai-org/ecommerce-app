import { useCallback, useEffect, useRef, useState } from 'react';
import { api, type NotificationList } from '../api';

const POLL_MS = 15_000;

const relative = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });

function timeAgo(iso: string) {
  const seconds = Math.round((new Date(iso).getTime() - Date.now()) / 1000);
  if (Math.abs(seconds) < 60) return 'just now';
  const minutes = Math.round(seconds / 60);
  if (Math.abs(minutes) < 60) return relative.format(minutes, 'minute');
  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 24) return relative.format(hours, 'hour');
  return relative.format(Math.round(hours / 24), 'day');
}

/** Bell in the top bar: polls the in-app inbox and opens it as a panel. */
export default function NotificationBell() {
  const [list, setList] = useState<NotificationList | null>(null);
  const [open, setOpen] = useState(false);
  // Ids that were unread when the panel was opened, so they stay highlighted after being marked read.
  const [fresh, setFresh] = useState<Set<string>>(new Set());
  const root = useRef<HTMLDivElement>(null);

  const refresh = useCallback(() => {
    api.notifications().then(setList).catch(() => {});
  }, []);

  useEffect(() => {
    refresh();
    const timer = setInterval(refresh, POLL_MS);
    return () => clearInterval(timer);
  }, [refresh]);

  useEffect(() => {
    if (!open) return;
    function onPointerDown(e: PointerEvent) {
      if (!root.current?.contains(e.target as Node)) setOpen(false);
    }
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false);
    }
    document.addEventListener('pointerdown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('pointerdown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [open]);

  async function toggle() {
    if (open) {
      setOpen(false);
      return;
    }
    setOpen(true);
    try {
      const latest = await api.notifications();
      setFresh(new Set(latest.items.filter((n) => !n.read).map((n) => n.id)));
      setList(latest.unreadCount > 0 ? await api.markNotificationsRead() : latest);
    } catch {
      // Keep showing the last list; the next poll retries.
    }
  }

  const unread = list?.unreadCount ?? 0;
  const items = list?.items ?? [];

  return (
    <div className="notify" ref={root}>
      <button
        type="button"
        className="notify-button"
        aria-label={unread > 0 ? `Notifications, ${unread} unread` : 'Notifications'}
        aria-haspopup="dialog"
        aria-expanded={open}
        onClick={toggle}
      >
        <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden>
          <path
            d="M6 9a6 6 0 0 1 12 0c0 5 2 6.5 2 6.5H4S6 14 6 9z"
            fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"
          />
          <path d="M10 19a2 2 0 0 0 4 0" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
        {unread > 0 && <span className="badge notify-count">{unread > 99 ? '99+' : unread}</span>}
      </button>
      {open && (
        <div className="notify-panel card" role="dialog" aria-label="Notifications">
          <div className="notify-head">Notifications</div>
          {list === null ? (
            <p className="notify-empty muted">Loading…</p>
          ) : items.length === 0 ? (
            <p className="notify-empty muted">No notifications yet.</p>
          ) : (
            <ul className="notify-list">
              {items.map((n) => (
                <li key={n.id} className={fresh.has(n.id) ? 'notify-item notify-new' : 'notify-item'}>
                  <p className="notify-message">{n.message}</p>
                  <time className="muted small" dateTime={n.createdAt}>{timeAgo(n.createdAt)}</time>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
