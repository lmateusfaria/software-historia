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

  isLoggedIn(): boolean {
    return !!this.auth.getToken();
  }

  onSubmit(): void {
    this.error = '';
    this.success = false;
    this.loading = true;
    const usuario: UsuarioDTO = {
      nome: this.nome,
      email: this.email,
      senha: this.senha,
      cpf: this.cpf,
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