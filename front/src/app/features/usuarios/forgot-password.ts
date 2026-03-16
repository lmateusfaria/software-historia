import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPassword {
  email = '';
  loading = false;
  success = false;
  error = '';

  constructor(public router: Router, private http: HttpClient) {}

  onSubmit(): void {
    if (!this.email) {
      this.error = 'Por favor, insira seu e-mail.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = false;

    this.http.post('/api/auth/forgot-password', { email: this.email }, { observe: 'response' }).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
      },
      error: (err) => {
        this.loading = false;
        if (err.error && err.error.message) {
          this.error = err.error.message;
        } else if (typeof err.error === 'string') {
          this.error = err.error;
        } else {
          this.error = 'Não foi possível solicitar a recuperação. Verifique o seu e-mail e tente novamente.';
        }
      }
    });
  }
}
