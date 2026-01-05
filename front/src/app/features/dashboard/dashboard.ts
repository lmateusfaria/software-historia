import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserInfoService, UsuarioInfo } from '../../core/user-info.service';
import { UserService, UsuarioDTO } from '../../core/user.service';
import { TransacaoService, TransacaoDTO, TipoTransacao } from '../../core/transacao.service';
import { AuthService } from '../../core/auth.service';
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
  saldo = 0;
  contas = [];
  lancamentos: Array<{ descricao?: string; valor: number; tipo: TipoTransacao; data?: string; dataObj?: Date }> = [];
  private allLancamentos: Array<{ descricao?: string; valor: number; tipo: TipoTransacao; data?: string; dataObj?: Date }> = [];
  lastUpdated?: Date;
  private refreshTimer?: any;
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
  // nova transação
  showNewTransacaoModal = false;
  newTransacao: TransacaoDTO = { tipo: 'ENTRADA', valor: 0 };
  newTransacaoDataLocal: string = '';
  savingNewTransacao = false;
  // filtros lançamentos
  filterTipo: 'TODOS' | TipoTransacao = 'TODOS';
  filterDia: string = '';

  constructor(
    private userInfo: UserInfoService,
    private userService: UserService,
    private auth: AuthService,
    private transacaoService: TransacaoService,
    private toast: ToastService,
    private router: Router
  ) {}
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

  // Nova Transação ----------------------
  onShowNewTransaction() {
    this.showNewTransacaoModal = true;
    this.newTransacao = { tipo: 'ENTRADA', valor: 0, descricao: '' };
    this.newTransacaoDataLocal = '';
  }

  onCancelNewTransaction() {
    this.showNewTransacaoModal = false;
  }

  private toBackendDateString(dtLocal?: string): string | undefined {
    if (!dtLocal) return undefined;
    // dtLocal vem como 'YYYY-MM-DDTHH:mm'
    // Backend espera 'dd/MM/yyyy HH:mm:ss'
    try {
      const [date, time] = dtLocal.split('T');
      const [y, m, d] = date.split('-');
      const [hh, mm] = time.split(':');
      return `${d}/${m}/${y} ${hh}:${mm}:00`;
    } catch {
      return undefined;
    }
  }

  onSubmitNewTransaction() {
    if (!this.newTransacao || !this.newTransacao.tipo || !this.newTransacao.valor || this.newTransacao.valor <= 0) {
      this.toast.info('Informe tipo e um valor maior que zero.');
      return;
    }
    const payload: TransacaoDTO = {
      ...this.newTransacao,
      data: this.toBackendDateString(this.newTransacaoDataLocal)
    };
    // Fecha o modal imediatamente ao clicar em Salvar e evita cliques duplos
    this.savingNewTransacao = true;
    this.showNewTransacaoModal = false;
    this.transacaoService.criar(payload).subscribe({
      next: (transacao: TransacaoDTO) => {
        this.toast.success('Transação criada com sucesso.');
        // Atualiza saldo e últimos lançamentos após criar
        this.loadSaldo();
        this.loadLancamentos();
        this.savingNewTransacao = false;
      },
      error: (err: any) => {
        console.error('Erro ao criar transação', err);
        this.toast.error('Erro ao criar transação.');
        this.savingNewTransacao = false;
      }
    });
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
      contasIds: [],
      centrosCustoIds: []
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
    console.log('[DASHBOARD] Chamando userService.update:', dto.id, dto);
    this.userService.update(dto.id, dto).subscribe({
      next: (_res: any) => {
        console.log('[DASHBOARD] Usuário atualizado com sucesso');
        this.editMode = false;
        this.editError = '';
        this.toast.success('Dados do usuário atualizados com sucesso.');
        this.loadUser();
        this.userServiceUpdateLoading = false;
      },
      error: (err: any) => {
        console.error('[DASHBOARD] Erro ao atualizar usuário:', err);
        this.editError = 'Erro ao atualizar usuário.';
        this.toast.error('Erro ao atualizar usuário.');
        this.userServiceUpdateLoading = false;
      }
    });
  }

  onDeleteUser() {
    console.log('[DASHBOARD] onDeleteUser chamado');
    if (!this.usuario?.id) {
      console.warn('[DASHBOARD] Não há usuario.id para excluir');
      return;
    }
    if (!confirm('Tem certeza que deseja excluir sua conta?')) {
      console.log('[DASHBOARD] Exclusão cancelada pelo usuário');
      return;
    }
    console.log('[DASHBOARD] Chamando userService.delete para id:', this.usuario.id);
    this.userService.delete(this.usuario.id).subscribe({
      next: () => {
        console.log('[DASHBOARD] Usuário excluído com sucesso, realizando logout');
        this.logout();
      },
      error: (err: any) => {
        console.error('[DASHBOARD] Erro ao excluir usuário:', err);
        this.usuarioError = 'Erro ao excluir usuário.';
      }
    });
  }

  onConfirmDeleteAccount() {
    this.showDeleteAccountModal = true;
    this.deleteAccountPassword = '';
  }

  onCancelDeleteAccount() {
    this.showDeleteAccountModal = false;
    this.deleteAccountPassword = '';
  }

  onDeleteAccount() {
    if (!this.deleteAccountPassword) {
      alert('Por favor, insira sua senha para confirmar a exclusão.');
      return;
    }

    this.userService.validatePassword(this.deleteAccountPassword).subscribe({
      next: (isValid: boolean) => {
        if (isValid) {
          if (confirm('Tem certeza que deseja excluir sua conta?')) {
            this.userService.deleteAccount().subscribe({
              next: () => {
                alert('Conta excluída com sucesso.');
                this.logout();
              },
              error: (err: any) => {
                console.error('Erro ao excluir conta:', err);
                alert('Erro ao excluir conta. Tente novamente mais tarde.');
              }
            });
          }
        } else {
          alert('Senha incorreta. Tente novamente.');
        }
      },
      error: (err: any) => {
        console.error('Erro ao validar senha:', err);
        alert('Erro ao validar senha. Tente novamente mais tarde.');
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
    console.log('[DASHBOARD] ngOnInit chamado');
    if (typeof window !== 'undefined' && typeof document !== 'undefined') {
      this.loadUser();
      this.loadSaldo();
      this.loadLancamentos();
      // auto-refresh leve a cada 30s
      this.refreshTimer = setInterval(() => {
        this.loadSaldo(false);
        this.loadLancamentos(false);
      }, 30000);
    }
    this.routerSub = this.router.events.subscribe((event: any) => {
      if (event instanceof NavigationEnd && this.router.url.startsWith('/dashboard')) {
        console.log('[DASHBOARD] NavigationEnd para dashboard, recarregando usuário');
        if (typeof window !== 'undefined' && typeof document !== 'undefined') {
          this.loadUser();
          this.loadSaldo();
          this.loadLancamentos();
        }
      }
    });
  }

  ngOnDestroy() {
    this.routerSub?.unsubscribe();
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  }

  loadUser(): void {
    console.log('[DASHBOARD] loadUser chamado');
    this.usuarioLoading = true;
    const svc: any = this.userInfo as any;
    const obs = (!svc || typeof svc.getMe !== 'function')
      ? (console.warn('[DASHBOARD] userInfo.getMe não é uma função. Usando UserService.getMe().'), this.userService.getMe())
      : svc.getMe();
    obs.subscribe({
      next: (user: UsuarioInfo) => {
        console.log('[DASHBOARD] Usuário carregado:', user);
        this.usuario = user;
        // Mantém o objeto de edição sincronizado com dados atuais
        this.editUsuario = { ...user, senha: '' };
        this.usuarioLoading = false;
      },
      error: (err: any) => {
        if (err.status === 401 || err.status === 403) {
          this.logout();
        } else {
          this.usuario = undefined;
          this.usuarioLoading = false;
          this.usuarioError = 'Erro ao carregar dados do usuário.';
          console.error('[DASHBOARD] Erro ao buscar usuário:', err);
        }
      }
    });
  }

  private loadSaldo(markTime: boolean = true) {
    this.transacaoService.saldo().subscribe({
      next: (value: number) => {
        this.saldo = Number(value || 0);
        if (markTime) this.lastUpdated = new Date();
      },
      error: (err: any) => {
        console.error('[DASHBOARD] Erro ao buscar saldo:', err);
      }
    });
  }

  private loadLancamentos(markTime: boolean = true) {
    const hasFilters = this.hasActiveFilters();
    const tipoParam = this.filterTipo !== 'TODOS' ? this.filterTipo : undefined;
    const obs = hasFilters ? this.transacaoService.minhas(tipoParam) : this.transacaoService.ultimas(5);
    obs.subscribe({
      next: (list: TransacaoDTO[]) => {
        const mapped = list.map((t: TransacaoDTO) => ({
          descricao: t.descricao,
          valor: t.valor,
          tipo: t.tipo,
          data: t.data,
          dataObj: this.parseBrDateTime(t.data)
        }));
        this.allLancamentos = mapped;
        this.applyLancamentoFilters();
        if (markTime) this.lastUpdated = new Date();
      },
      error: (err: any) => {
        console.error('[DASHBOARD] Erro ao listar transações:', err);
      }
    });
  }

  private parseBrDateTime(dt?: string): Date | undefined {
    if (!dt) return undefined;
    // esperado: dd/MM/yyyy HH:mm:ss
    try {
      const [date, time] = dt.split(' ');
      const [d, m, y] = date.split('/').map(Number);
      const [hh, mm, ss] = (time || '00:00:00').split(':').map(Number);
      return new Date(y, (m - 1), d, hh || 0, mm || 0, ss || 0);
    } catch {
      return undefined;
    }
  }

  private hasActiveFilters(): boolean {
    return (this.filterTipo !== 'TODOS') || !!this.filterDia;
  }

  onFiltroChange() {
    // Recarrega a fonte se filtros mudam para pegar mais dados quando necessário
    this.loadLancamentos(false);
  }

  clearFiltros() {
    this.filterTipo = 'TODOS';
    this.filterDia = '';
    this.loadLancamentos();
  }

  private applyLancamentoFilters() {
    let list = [...this.allLancamentos];
    // Filtro por dia específico
    if (this.filterDia) {
      // filterDia vem como YYYY-MM-DD
      const [y, m, d] = this.filterDia.split('-').map(Number);
      list = list.filter(l => {
        if (!l.dataObj) return false;
        return l.dataObj.getFullYear() === y && (l.dataObj.getMonth() + 1) === m && l.dataObj.getDate() === d;
      });
    }
    // Tipo já foi aplicado no backend quando hasFilters true com tipo; mas se estiver usando ultimas(), aplica aqui também
    if (this.filterTipo !== 'TODOS') {
      list = list.filter(l => l.tipo === this.filterTipo);
    }
    this.lancamentos = list;
  }

  onCloseUser() {
    console.log('[DASHBOARD] onCloseUser chamado');
    this.showUser = false;
  }
}