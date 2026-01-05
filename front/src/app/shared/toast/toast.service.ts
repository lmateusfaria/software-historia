import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
  duration?: number; // ms
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  private readonly _toasts$ = new BehaviorSubject<Toast[]>([]);
  readonly toasts$ = this._toasts$;

  private push(toast: Omit<Toast, 'id'>) {
    const id = ++this.counter;
    const item: Toast = { id, duration: 3500, ...toast };
    const list = [...this._toasts$.value, item];
    this._toasts$.next(list);
    // auto-remove
    const duration = item.duration ?? 3500;
    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  success(message: string, duration?: number) {
    this.push({ type: 'success', message, duration });
  }

  error(message: string, duration?: number) {
    this.push({ type: 'error', message, duration });
  }

  info(message: string, duration?: number) {
    this.push({ type: 'info', message, duration });
  }

  dismiss(id: number) {
    this._toasts$.next(this._toasts$.value.filter(t => t.id !== id));
  }

  clear() {
    this._toasts$.next([]);
  }
}
