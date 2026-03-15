import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserInfoService, UsuarioInfo } from '../../core/user-info.service';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { AuthService } from '../../core/auth.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';
import { ToastService } from '../../shared/toast/toast.service';
import { DecimalPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit, OnDestroy {
  documentos: DocumentoDTO[] = [];
  totalAcervos: number = 0;
  totalAguardandoRevisao: number = 0;
  /*
# Projeto Biblioteca Digital: Refatoração e Integração

## Fase 1: Refinamento e Planejamento
- [x] Explorar estrutura atual do Backend e Frontend
- [x] Validar modelos de dados atuais e necessários
- [x] Definir perguntas de refinamento para o usuário
- [x] Criar Plano de Implementação detalhado

## Fase 2: Reformulação do Backend
- [x] Ajustar perfis de usuário (Professor, Aluno, Pesquisador)
- [x] Criar/Ajustar entidades para Documentos e Escaneamento
- [x] Implementar lógica de permissões e segurança

## Fase 3: Reformulação do Frontend
- [x] Reformular tela de Login
- [x] Implementar novo design premium/histórico no Frontend (Login/Register)
- [x] Adaptar Dashboard para Biblioteca Digital
- [x] Implementar Componente de Escaneamento (Para Alunos)
- [x] Implementar interface de busca para Pesquisadores

## Fase 4: Integração e Verificação
- [x] Testar integração Backend-Frontend
- [x] Validar fluxo de escaneamento
- [x] Walkthrough final
*/
  lastUpdated?: Date;
  usuario: UsuarioInfo | undefined;
  usuarioLoading = false;
  usuarioError = '';
  showUser = false;
  editMode = false;
  editUsuario: UsuarioInfo | undefined;
  editError = '';
  userServiceUpdateLoading = false;
  private routerSub?: Subscription;
  showDeleteAccountModal: boolean = false;
  deleteAccountPassword: string = '';

  constructor(
    private userInfo: UserInfoService,
    private userService: UserService,
    private auth: AuthService,
    private documentoService: DocumentoService,
    private toast: ToastService,
    private router: Router
  ) { }
  onShowUser() {
    console.log('[DASHBOARD] onShowUser chamado');
    this.showUser = true;
    this.editMode = false;
    this.editError = '';
    // Se já temos usuário carregado, prepara editUsuario sem nova requisição
    if (this.usuario) {
      this.editUsuario = { ...this.usuario, senha: '' };
    } else {
      this.loadUser();
    }
  }

  onShowNewTransaction() {
    this.router.navigate(['/escaneamento']);
  }

  onExplorarAcervo() {
    this.router.navigate(['/acervo']);
  }

  onManageUsers() {
    this.router.navigate(['/admin/usuarios']);
  }

  get isProfessor(): boolean {
    return this.usuario?.perfil === 'PROFESSOR';
  }

  onEditUser() {
    console.log('[DASHBOARD] onEditUser chamado');
    if (this.usuario) {
      // Cria um objeto de edição para bind no formulário
      this.editUsuario = { ...this.usuario, senha: '' };
      this.editMode = true;
      this.editError = '';
      console.log('[DASHBOARD] editUsuario:', this.editUsuario);
    }
  }

  onCancelEdit() {
    this.editMode = false;
    this.editError = '';
    // Recarrega o usuário para descartar alterações não salvas
    this.editUsuario = this.usuario ? { ...this.usuario, senha: '' } : undefined;
  }

  onSaveEdit() {
    console.log('[DASHBOARD] onSaveEdit chamado');
    if (!this.editUsuario) {
      console.warn('[DASHBOARD] editUsuario está indefinido');
      return;
    }
    if (!this.editUsuario.senha) {
      alert('A senha é obrigatória para atualizar o usuário.');
      return;
    }
    const dto: UsuarioDTO = {
      id: this.editUsuario.id,
      nome: this.editUsuario.nome,
      email: this.editUsuario.email,
      cpf: this.editUsuario.cpf,
      senha: this.editUsuario.senha,
      perfil: this.editUsuario.perfil
    };
    console.log('[DASHBOARD] Enviando update para userService.update:', dto);
    this.userServiceUpdate(dto);
  }

  private userServiceUpdate(dto: UsuarioDTO) {
    if (!dto.id) {
      console.warn('[DASHBOARD] userServiceUpdate chamado sem id');
      return;
    }
    this.userServiceUpdateLoading = true;
    this.userService.update(dto.id, dto).subscribe({
      next: (_res: any) => {
        this.editMode = false;
        this.toast.success('Dados do usuário atualizados com sucesso.');
        this.loadUser();
        this.userServiceUpdateLoading = false;
      },
      error: (err: any) => {
        this.editError = 'Erro ao atualizar usuário.';
        this.toast.error('Erro ao atualizar usuário.');
        this.userServiceUpdateLoading = false;
      }
    });
  }

  logout() {
    this.auth.logout();
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }

  ngOnInit() {
    if (typeof window !== 'undefined' && typeof document !== 'undefined') {
      this.loadUser();
      this.loadDocumentos();
    }
    this.routerSub = this.router.events.subscribe((event: any) => {
      if (event instanceof NavigationEnd && this.router.url.startsWith('/dashboard')) {
        if (typeof window !== 'undefined' && typeof document !== 'undefined') {
          this.loadUser();
          this.loadDocumentos();
        }
      }
    });
  }

  ngOnDestroy() {
    this.routerSub?.unsubscribe();
  }

  loadUser(): void {
    this.usuarioLoading = true;
    this.userService.getMe().subscribe({
      next: (user: any) => {
        this.usuario = user;
        this.editUsuario = { ...user, senha: '' };
        this.usuarioLoading = false;
      },
      error: (err: any) => {
        if (err.status === 401 || err.status === 403) {
          this.logout();
        } else {
          this.usuarioLoading = false;
          this.usuarioError = 'Erro ao carregar dados do usuário.';
        }
      }
    });
  }

  onCloseUser() {
    this.showUser = false;
  }

  loadDocumentos(): void {
    this.documentoService.findAll().subscribe({
      next: (docs) => {
        this.documentos = docs;
        this.totalAcervos = docs.length;
        this.totalAguardandoRevisao = docs.filter(d => d.status === 'PENDENTE' || d.status === 'AGUARDANDO_REVISAO' || !d.status).length;
      },
      error: (err) => {
        console.error('Erro ao carregar documentos', err);
      }
    });
  }
}