import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.html'
})
export class UserManagementComponent implements OnInit {
  usuarios: UsuarioDTO[] = [];
  loading = true;
  usuarioSelecionado: UsuarioDTO | null = null;
  editando = false;
  
  perfis = [
    { label: 'Professor / Gestor', value: 'PROFESSOR' },
    { label: 'Aluno / Digitalizador', value: 'ALUNO' },
    { label: 'Pesquisador', value: 'PESQUISADOR' }
  ];

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    this.loading = true;
    this.userService.findAll().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar usuários', err);
        this.loading = false;
        alert('Erro ao carregar lista de usuários.');
      }
    });
  }

  abrirEdicao(usuario: UsuarioDTO): void {
    this.usuarioSelecionado = { ...usuario };
    this.editando = true;
  }

  fecharEdicao(): void {
    this.usuarioSelecionado = null;
    this.editando = false;
  }

  salvar(): void {
    if (!this.usuarioSelecionado || !this.usuarioSelecionado.id) return;

    this.userService.update(this.usuarioSelecionado.id, this.usuarioSelecionado).subscribe({
      next: () => {
        alert('Usuário atualizado com sucesso!');
        this.fecharEdicao();
        this.carregarUsuarios();
      },
      error: (err) => {
        console.error('Erro ao atualizar usuário', err);
        alert('Erro ao atualizar usuário. Verifique os dados.');
      }
    });
  }

  excluir(id: number): void {
    if (confirm('Tem certeza que deseja excluir este usuário? Esta ação não pode ser desfeita.')) {
      this.userService.delete(id).subscribe({
        next: () => {
          alert('Usuário excluído com sucesso!');
          this.carregarUsuarios();
        },
        error: (err) => {
          console.error('Erro ao excluir usuário', err);
          alert('Erro ao excluir usuário. Verifique se você tem permissão ou se o usuário não é você mesmo.');
        }
      });
    }
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}
