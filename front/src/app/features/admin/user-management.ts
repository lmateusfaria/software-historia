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

  // Filtros e Ordenação
  searchTerm: string = '';
  filtroPerfil: string = 'TODOS';
  filtroPermissao: string = 'TODAS';
  ordenacao: string = 'NOME_AZ';

  // Paginação
  itemsPerPage: number = 10;
  currentPage: number = 1;
  opcoesItensPorPagina = [10, 20, 50, 100];

  opcoesFiltro = [
    { label: 'Todos', value: 'TODOS' },
    { label: 'Professores', value: 'PROFESSOR' },
    { label: 'Alunos', value: 'ALUNO' },
    { label: 'Pesq.', value: 'PESQUISADOR' }
  ];

  opcoesPermissao = [
    { label: 'Todas as Permissões', value: 'TODAS' },
    { label: 'Somente Leitura', value: 'LEITURA' },
    { label: 'Pode Digitalizar', value: 'DIGITALIZAR' }
  ];

  opcoesOrdenacao = [
    { label: 'Nome (A-Z)', value: 'NOME_AZ' },
    { label: 'Nome (Z-A)', value: 'NOME_ZA' },
    { label: 'Data de Criação (Mais Recentes)', value: 'RECENTE' }
  ];

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  // Reseta a paginação sempre que um filtro mudar
  onFilterChange(): void {
    this.currentPage = 1;
  }

  get usuariosFiltrados(): UsuarioDTO[] {
    let filtrados = this.usuarios;

    // Filtro por texto
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtrados = filtrados.filter(u => 
        u.nome.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.cpf.includes(term)
      );
    }

    // Filtro por perfil
    if (this.filtroPerfil !== 'TODOS') {
      filtrados = filtrados.filter(u => u.perfil === this.filtroPerfil);
    }

    // Filtro por permissão
    if (this.filtroPermissao === 'LEITURA') {
      filtrados = filtrados.filter(u => !u.podeCadastrar);
    } else if (this.filtroPermissao === 'DIGITALIZAR') {
      filtrados = filtrados.filter(u => u.podeCadastrar);
    }

    // Ordenação
    filtrados = [...filtrados].sort((a, b) => {
      if (this.ordenacao === 'NOME_AZ') {
        return a.nome.localeCompare(b.nome);
      } else if (this.ordenacao === 'NOME_ZA') {
        return b.nome.localeCompare(a.nome);
      } else if (this.ordenacao === 'RECENTE') {
        return (b.id || 0) - (a.id || 0); // Prioriza IDs maiores que tendem a ser mais recentes
      }
      return 0;
    });

    return filtrados;
  }

  // Dados com paginação
  get usuariosPaginados(): UsuarioDTO[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.usuariosFiltrados.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.usuariosFiltrados.length / this.itemsPerPage));
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
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
