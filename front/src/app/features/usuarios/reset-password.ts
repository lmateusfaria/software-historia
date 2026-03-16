import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./forgot-password.css'] // reaproveitando a animação fade-in
})
export class ResetPassword implements OnInit {
  token = '';
  newPassword = '';
  confirmPassword = '';
  loading = false;
  success = false;
  error = '';

  constructor(public router: Router, private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.error = 'Token de recuperação inválido ou ausente.';
      }
    });
  }

  isSenhaForte(senha: string): boolean {
    const minLength = senha.length >= 8;
    const hasUpper = /[A-Z]/.test(senha);
    const hasLower = /[a-z]/.test(senha);
    const hasNumber = /[0-9]/.test(senha);
    return minLength && hasUpper && hasLower && hasNumber;
  }

  onSubmit(): void {
    if (!this.token) {
      this.error = 'Não é possível redefinir sem um token válido.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.error = 'As senhas não coincidem.';
      return;
    }

    if (!this.isSenhaForte(this.newPassword)) {
      this.error = 'A senha deve ter no mínimo 8 caracteres, incluindo maiúsculas, minúsculas e números.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = false;

    this.http.post('/api/auth/reset-password', { token: this.token, newPassword: this.newPassword }, { observe: 'response' }).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (err) => {
        this.loading = false;
        if (err.error && err.error.message) {
          this.error = err.error.message;
        } else if (typeof err.error === 'string') {
          this.error = err.error;
        } else {
          this.error = 'Ocorreu um erro ao redefinir a senha. O token pode estar expirado.';
        }
      }
    });
  }
}
