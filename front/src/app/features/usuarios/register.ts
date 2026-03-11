import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  nome = '';
  email = '';
  senha = '';
  cpf = '';
  perfil = 'PESQUISADOR';
  error = '';
  loading = false;
  success = false;
  podeCadastrarOutros = false;

  constructor(public router: Router, private userService: UserService, public auth: AuthService) { }

  ngOnInit(): void {
    if (this.isLoggedIn()) {
      this.userService.getMe().subscribe({
        next: (user) => {
          this.podeCadastrarOutros = user.podeCadastrar;
        },
        error: () => {
          this.podeCadastrarOutros = false;
        }
      });
    }
  }

  onCpfInput(event: any): void {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length > 11) value = value.substring(0, 11);

    if (value.length > 9) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})/, '$1.$2.$3');
    } else if (value.length > 3) {
      value = value.replace(/(\d{3})(\d{3})/, '$1.$2');
    }

    this.cpf = value;
    event.target.value = value;
  }

  isSenhaForte(senha: string): boolean {
    const minLength = senha.length >= 8;
    const hasUpper = /[A-Z]/.test(senha);
    const hasLower = /[a-z]/.test(senha);
    const hasNumber = /[0-9]/.test(senha);
    return minLength && hasUpper && hasLower && hasNumber;
  }

  isLoggedIn(): boolean {
    return !!this.auth.getToken();
  }

  onSubmit(): void {
    if (this.cpf.length < 14) {
      this.error = 'CPF incompleto ou inválido.';
      return;
    }

    if (!this.isSenhaForte(this.senha)) {
      this.error = 'A senha deve ter no mínimo 8 caracteres, incluindo letras maiúsculas, minúsculas e números.';
      return;
    }

    this.error = '';
    this.success = false;
    this.loading = true;
    const usuario: UsuarioDTO = {
      nome: this.nome,
      email: this.email,
      senha: this.senha,
      cpf: this.cpf.replace(/\D/g, ''), // Envia apenas números para o backend
      perfil: this.perfil
    };
    this.userService.register(usuario).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.success = true;
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err: any) => {
        this.loading = false;
        if (err?.error && typeof err.error === 'string') {
          this.error = err.error;
        } else if (err?.status === 0) {
          this.error = 'Não foi possível conectar ao servidor.';
        } else {
          this.error = 'Erro ao registrar usuário.';
        }
      }
    });
  }
}