import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentoService } from '../../core/documento.service';
import { UserInfoService, UsuarioInfo } from '../../core/user-info.service';
import { DocumentoDTO, OcrResultadoDTO, OcrResultadoUpdateDTO } from '../../core/models/documento.model';
import { ToastService } from '../../shared/toast/toast.service';

type CategoriaEntidade = 'pessoas' | 'locais' | 'eventos' | 'organizacoes' | 'assuntos';

@Component({
    selector: 'app-documento-detalhe',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './documento-detalhe.html',
    styleUrls: ['./documento-detalhe.css']
})
export class DocumentoDetalheComponent implements OnInit {
    documento?: DocumentoDTO;
    usuario?: UsuarioInfo;
    loading = true;
    imagemSelecionada?: string;
    loadingOcr: { [url: string]: boolean } = {};
    ocrResultados: { [url: string]: OcrResultadoDTO } = {};
    imageLoaded = false;
    mostrarOriginal = false;

    editandoOcr = false;
    ocrEmEdicao?: OcrResultadoDTO;
    salvandoOcr = false;
    novoChip: { [k in CategoriaEntidade]: string } = { pessoas: '', locais: '', eventos: '', organizacoes: '', assuntos: '' };

    // Zoom e Pan State
    zoom = 1;
    isPanning = false;
    startX = 0;
    startY = 0;
    posX = 0;
    posY = 0;
    isFullscreen = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private documentoService: DocumentoService,
        private userInfoService: UserInfoService,
        private toast: ToastService
    ) { }

    ngOnInit() {
        this.carregarUsuario();
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.carregarDocumento(+id);
        } else {
            this.router.navigate(['/acervo']);
        }
    }

    carregarUsuario() {
        this.userInfoService.getMe().subscribe({
            next: (user) => this.usuario = user,
            error: () => console.error('Erro ao carregar info do usuário')
        });
    }

    get isProfessor(): boolean {
        return this.usuario?.perfil === 'ROLE_PROFESSOR' || this.usuario?.perfil === 'PROFESSOR';
    }

    get isAluno(): boolean {
        return this.usuario?.perfil === 'ROLE_ALUNO' || this.usuario?.perfil === 'ALUNO';
    }

    get podeEditarDadosIA(): boolean {
        return this.isProfessor || this.isAluno;
    }

    get aguardandoAprovacao(): boolean {
        return this.documento?.status === 'AGUARDANDO_APROVACAO';
    }

    aprovar() {
        if (this.documento?.id) {
            this.documentoService.approve(this.documento.id).subscribe({
                next: () => {
                    this.toast.success('Documento aprovado com sucesso!');
                    this.carregarDocumento(this.documento!.id!);
                },
                error: () => this.toast.error('Erro ao aprovar documento.')
            });
        }
    }

    excluir() {
        if (this.documento?.id && confirm('Tem certeza que deseja excluir este documento? Esta ação é irreversível.')) {
            this.documentoService.delete(this.documento.id).subscribe({
                next: () => {
                    this.toast.success('Documento excluído com sucesso!');
                    this.router.navigate(['/acervo']);
                },
                error: () => this.toast.error('Erro ao excluir documento.')
            });
        }
    }

    carregarDocumento(id: number) {
        this.loading = true;
        this.documentoService.findById(id).subscribe({
            next: (doc) => {
                this.documento = doc;
                this.loading = false;
                if (doc.imagensUrls && doc.imagensUrls.length > 0) {
                    this.imagemSelecionada = doc.imagensUrls[0];
                }
                if (doc.ocrResultadosImagem) {
                    this.ocrResultados = { ...doc.ocrResultadosImagem };
                }
            },
            error: () => {
                this.toast.error('Erro ao carregar detalhes do documento.');
                this.loading = false;
                this.router.navigate(['/acervo']);
            }
        });
    }

    selecionarImagem(url: string) {
        if (this.imagemSelecionada === url) return;
        this.imagemSelecionada = url;
        this.imageLoaded = false;
        this.mostrarOriginal = false; // Volta para preview ao trocar de imagem
        this.editandoOcr = false;
        this.ocrEmEdicao = undefined;
        this.resetZoom();
    }

    onImageLoad() {
        this.imageLoaded = true;
    }

    get thumbSelecionada(): string | undefined {
        if (!this.documento || !this.imagemSelecionada || !this.documento.imagensUrls || !this.documento.thumbnailsUrls) return undefined;
        const index = this.documento.imagensUrls.indexOf(this.imagemSelecionada);
        return index !== -1 ? this.documento.thumbnailsUrls[index] : undefined;
    }

    get urlExibicao(): string | undefined {
        if (!this.documento || !this.imagemSelecionada) return undefined;
        if (this.mostrarOriginal) return this.imagemSelecionada;

        const index = this.documento.imagensUrls.indexOf(this.imagemSelecionada);
        if (index !== -1 && this.documento.previewsUrls && this.documento.previewsUrls[index]) {
            return this.documento.previewsUrls[index];
        }
        return this.imagemSelecionada;
    }

    toggleOriginal() {
        this.mostrarOriginal = !this.mostrarOriginal;
        this.imageLoaded = false;
    }

    trackByUrl(index: number, url: string): string {
        return url;
    }

    // Métodos de Zoom e Pan
    zoomIn() {
        this.zoom = Math.min(this.zoom + 0.2, 3);
    }

    zoomOut() {
        this.zoom = Math.max(this.zoom - 0.2, 1);
        if (this.zoom === 1) this.resetPan();
    }

    resetZoom() {
        this.zoom = 1;
        this.resetPan();
    }

    resetPan() {
        this.posX = 0;
        this.posY = 0;
    }

    toggleFullscreen() {
        this.isFullscreen = !this.isFullscreen;
        if (!this.isFullscreen) this.resetZoom();
    }

    onMouseDown(e: MouseEvent) {
        if (this.zoom > 1) {
            this.isPanning = true;
            this.startX = e.clientX - this.posX;
            this.startY = e.clientY - this.posY;
            e.preventDefault();
        }
    }

    onMouseMove(e: MouseEvent) {
        if (this.isPanning && this.zoom > 1) {
            this.posX = e.clientX - this.startX;
            this.posY = e.clientY - this.startY;
        }
    }

    onMouseUp() {
        this.isPanning = false;
    }

    // Touch events for mobile
    onTouchStart(e: TouchEvent) {
        if (this.zoom > 1 && e.touches.length === 1) {
            this.isPanning = true;
            this.startX = e.touches[0].clientX - this.posX;
            this.startY = e.touches[0].clientY - this.posY;
        }
    }

    onTouchMove(e: TouchEvent) {
        if (this.isPanning && this.zoom > 1 && e.touches.length === 1) {
            this.posX = e.touches[0].clientX - this.startX;
            this.posY = e.touches[0].clientY - this.startY;
            e.preventDefault();
        }
    }

    onTouchEnd() {
        this.isPanning = false;
    }

    get transformStyle() {
        return `scale(${this.zoom}) translate(${this.posX / this.zoom}px, ${this.posY / this.zoom}px)`;
    }

    extrairOcrDaImagem() {
        if (!this.imagemSelecionada || !this.documento?.id) return;
        
        let urlClean = this.imagemSelecionada;
        if (urlClean.includes('/download/')) {
            urlClean = urlClean.split('/download/')[1];
        }

        this.loadingOcr[this.imagemSelecionada] = true;
        this.documentoService.ocrImagem(this.documento.id, urlClean).subscribe({
            next: (res) => {
                this.toast.info(res.mensagem || 'Processamento iniciado em segundo plano!');
                this.loadingOcr[this.imagemSelecionada!] = false;
                
                // Agendamos uma recarga do documento após 15 segundos para tentar pegar o resultado
                setTimeout(() => {
                    if (this.documento?.id) {
                        this.carregarDocumento(this.documento.id);
                    }
                }, 15000);
            },
            error: (err) => {
                if (err.status === 504) {
                    this.toast.info('O processamento está demorando, mas continua em segundo plano. Verifique em instantes.');
                } else {
                    this.toast.error('Erro ao solicitar OCR da imagem.');
                }
                this.loadingOcr[this.imagemSelecionada!] = false;
            }
        });
    }

    get ocrAtual(): OcrResultadoDTO | undefined {
        return this.imagemSelecionada ? this.ocrResultados[this.imagemSelecionada] : undefined;
    }

    iniciarEdicaoOcr() {
        if (!this.ocrAtual) return;
        this.ocrEmEdicao = {
            ...this.ocrAtual,
            pessoas: [...(this.ocrAtual.pessoas ?? [])],
            locais: [...(this.ocrAtual.locais ?? [])],
            eventos: [...(this.ocrAtual.eventos ?? [])],
            organizacoes: [...(this.ocrAtual.organizacoes ?? [])],
            assuntos: [...(this.ocrAtual.assuntos ?? [])],
        };
        this.editandoOcr = true;
    }

    cancelarEdicaoOcr() {
        this.editandoOcr = false;
        this.ocrEmEdicao = undefined;
    }

    adicionarChip(categoria: CategoriaEntidade) {
        const valor = this.novoChip[categoria]?.trim();
        if (!valor || !this.ocrEmEdicao) return;
        this.ocrEmEdicao[categoria].push(valor);
        this.novoChip[categoria] = '';
    }

    removerChip(categoria: CategoriaEntidade, index: number) {
        if (!this.ocrEmEdicao) return;
        this.ocrEmEdicao[categoria].splice(index, 1);
    }

    salvarOcr() {
        if (!this.documento?.id || !this.ocrEmEdicao?.id || !this.imagemSelecionada) return;
        this.salvandoOcr = true;
        const payload: OcrResultadoUpdateDTO = {
            textoCompleto: this.ocrEmEdicao.textoCompleto,
            pessoas: this.ocrEmEdicao.pessoas,
            locais: this.ocrEmEdicao.locais,
            eventos: this.ocrEmEdicao.eventos,
            organizacoes: this.ocrEmEdicao.organizacoes,
            assuntos: this.ocrEmEdicao.assuntos,
        };
        this.documentoService.atualizarOcrResultado(this.documento.id, this.ocrEmEdicao.id, payload).subscribe({
            next: (res) => {
                this.ocrResultados[this.imagemSelecionada!] = res;
                this.editandoOcr = false;
                this.ocrEmEdicao = undefined;
                this.salvandoOcr = false;
                this.toast.success('Dados extraídos atualizados com sucesso!');
            },
            error: () => {
                this.salvandoOcr = false;
                this.toast.error('Erro ao salvar as alterações.');
            }
        });
    }

    excluirOcrAtual() {
        if (!this.documento?.id || !this.ocrAtual?.id || !this.imagemSelecionada) return;
        if (!confirm('Tem certeza que deseja excluir os dados extraídos por IA desta imagem? O texto e as entidades desta página serão apagados.')) return;
        this.documentoService.excluirOcrResultado(this.documento.id, this.ocrAtual.id).subscribe({
            next: () => {
                delete this.ocrResultados[this.imagemSelecionada!];
                this.editandoOcr = false;
                this.ocrEmEdicao = undefined;
                this.toast.success('Dados extraídos removidos.');
            },
            error: () => this.toast.error('Erro ao excluir dados extraídos.')
        });
    }

    voltar() {
        this.router.navigate(['/acervo']);
    }
}
