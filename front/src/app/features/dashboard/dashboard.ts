import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserInfoService, UsuarioInfo } from '../../core/user-info.service';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { AuthService } from '../../core/auth.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO, OcrResultadoDTO } from '../../core/models/documento.model';
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
  minhaContribuicao: number = 0;
  statusCrescimento: 'positivo' | 'negativo' | 'neutro' = 'neutro';
  crescimentoVariavel: number = 0;
  statusRevisao: 'positivo' | 'negativo' | 'neutro' = 'neutro';
  crescimentoRevisao: number = 0;
  tempoUltimaRevisao: string = 'Nenhuma revisão recente';
  atividades: any[] = [];
  distribuicaoTipos: any[] = [];
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

  // OCR Test
  showOcrTest: boolean = false;
  ocrFile: File | null = null;
  ocrResult: OcrResultadoDTO | null = null;
  ocrLoading: boolean = false;
  ocrError: string = '';

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
        
        // Logica para contribuicao do usuario logado
        if (this.usuario) {
          this.minhaContribuicao = docs.filter(d => d.usuarioId === this.usuario?.id).length;
        }

        // Dados simulados para manter a estetica do dashboard (podem ser integrados futuramente)
        this.statusCrescimento = 'positivo';
        this.crescimentoVariavel = 12;
        this.statusRevisao = 'negativo';
        this.crescimentoRevisao = 5;
        this.tempoUltimaRevisao = 'Ha 2 horas';

        this.atividades = [
          { inicial: 'AD', nome: 'Admin', acao: 'Aprovou 5 documentos', tempo: '15 min atras', cor: 'bg-primary/20 text-primary' },
          { inicial: 'US', nome: 'Usuario', acao: 'Fez upload de "Ata_1950.pdf"', tempo: '1 hora atras', cor: 'bg-blue-100 text-blue-600' },
          { inicial: 'LG', nome: 'Log', acao: 'Processamento OCR concluido', tempo: '3 horas atras', cor: 'bg-green-100 text-green-600' }
        ];

        this.distribuicaoTipos = [
          { tipo: 'Documentos Oficiais', percentual: 45, quantidade: Math.round(this.totalAcervos * 0.45), cor: 'bg-primary' },
          { tipo: 'Fotografias', percentual: 30, quantidade: Math.round(this.totalAcervos * 0.30), cor: 'bg-blue-500' },
          { tipo: 'Raridades', percentual: 25, quantidade: Math.round(this.totalAcervos * 0.25), cor: 'bg-amber-500' }
        ];
      },
      error: (err) => {
        console.error('Erro ao carregar documentos', err);
      }
    });
  }

  // OCR Test Methods
  onTestarOcr() {
    this.showOcrTest = true;
    this.ocrFile = null;
    this.ocrResult = null;
    this.ocrError = '';
  }

  onCloseOcr() {
    this.showOcrTest = false;
    this.ocrFile = null;
    this.ocrResult = null;
    this.ocrError = '';
    this.ocrLoading = false;
  }

  onOcrFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.ocrFile = input.files[0];
      this.ocrResult = null;
      this.ocrError = '';
    }
  }

  onSubmitOcr() {
    if (!this.ocrFile) return;
    this.ocrLoading = true;
    this.ocrError = '';
    this.ocrResult = null;

    this.documentoService.testarOcr(this.ocrFile).subscribe({
      next: (result) => {
        this.ocrResult = result;
        this.ocrLoading = false;
        this.toast.success('Extracao OCR concluida com sucesso!');
      },
      error: (err) => {
        this.ocrLoading = false;
        this.ocrError = err.error?.textoCompleto || 'Erro ao processar OCR. Verifique se a API Key esta configurada.';
        this.toast.error('Erro ao processar OCR');
      }
    });
  }
}