import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { Router } from '@angular/router';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent],
  templateUrl: './user-management.html'
})
export class UserManagementComponent implements OnInit {
  usuarios: UsuarioDTO[] = [];
  loading = true;
  usuarioSelecionado: UsuarioDTO | null = null;
  editando = false;
  usuarioIdParaExcluir: number | null = null;

  perfis = [
    { label: 'Professor / Gestor', value: 'PROFESSOR' },
    { label: 'Aluno / Digitalizador', value: 'ALUNO' },
    { label: 'Pesquisador', value: 'PESQUISADOR' }
  ];

  constructor(
    private userService: UserService,
    private router: Router,
    private toast: ToastService
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
        this.toast.error('Erro ao carregar lista de usuários.');
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
        this.toast.success('Usuário atualizado com sucesso!');
        this.fecharEdicao();
        this.carregarUsuarios();
      },
      error: (err) => {
        console.error('Erro ao atualizar usuário', err);
        this.toast.error('Erro ao atualizar usuário. Verifique os dados.');
      }
    });
  }

  pedirExclusao(id: number): void {
    this.usuarioIdParaExcluir = id;
  }

  cancelarExclusao(): void {
    this.usuarioIdParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.usuarioIdParaExcluir) return;
    const id = this.usuarioIdParaExcluir;
    this.usuarioIdParaExcluir = null;

    this.userService.delete(id).subscribe({
      next: () => {
        this.toast.success('Usuário excluído com sucesso!');
        this.carregarUsuarios();
      },
      error: (err) => {
        console.error('Erro ao excluir usuário', err);
        this.toast.error('Erro ao excluir usuário. Verifique se você tem permissão ou se o usuário não é você mesmo.');
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}
