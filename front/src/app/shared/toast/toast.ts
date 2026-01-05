import { Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ToastService, Toast } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [],
  templateUrl: './toast.html',
  styleUrls: ['./toast.css']
})
export class ToastContainer implements OnDestroy {
  toasts: Toast[] = [];
  private sub: Subscription;

  constructor(private toast: ToastService) {
    // Usando cast temporário para contornar problema de tipos
    this.sub = (this.toast.toasts$ as any).subscribe({
      next: (list: Toast[]) => (this.toasts = list)
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  close(id: number) {
    this.toast.dismiss(id);
  }
}
