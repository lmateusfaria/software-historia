import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastService } from '../../shared/toast/toast.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO, ImagemBuscaDTO } from '../../core/models/documento.model';
import { DocumentCardComponent } from '../../shared/components/document-card/document-card';

@Component({
    selector: 'app-galeria',
    standalone: true,
    imports: [CommonModule, FormsModule, DocumentCardComponent],
    templateUrl: './galeria.html',
    styleUrls: ['./galeria.css']
})
export class GaleriaComponent implements OnInit {
    documentos: DocumentoDTO[] = [];
    imagens: ImagemBuscaDTO[] = [];
    filtro = '';
    filtroTipo = '';
    filtroAno = '';
    filtroLocal = '';
    loading = false;
    viewMode: 'documentos' | 'imagens' = 'documentos';
    termoBuscaEnriquecida = '';

    constructor(
        private documentoService: DocumentoService,
        private toast: ToastService,
        private router: Router
    ) { }

    ngOnInit() {
        this.carregarAcervo();
    }

    carregarAcervo() {
        this.loading = true;
        if (this.termoBuscaEnriquecida) {
            this.documentoService.searchEnriched(this.termoBuscaEnriquecida, this.viewMode).subscribe({
                next: (res) => {
                    if (this.viewMode === 'documentos') {
                        this.documentos = res as DocumentoDTO[];
                        this.imagens = [];
                    } else {
                        this.imagens = res as ImagemBuscaDTO[];
                        this.documentos = [];
                    }
                    this.loading = false;
                },
                error: () => {
                    this.toast.error('Erro na pesquisa enriquecida.');
                    this.loading = false;
                }
            });
        } else {
            this.documentoService.findAll().subscribe({
                next: (docs) => {
                    this.documentos = docs;
                    this.imagens = [];
                    this.loading = false;
                },
                error: () => {
                    this.toast.error('Erro ao carregar acervo.');
                    this.loading = false;
                }
            });
        }
    }

    mudarModo(modo: 'documentos' | 'imagens') {
        this.viewMode = modo;
        this.carregarAcervo();
    }

    limparBusca() {
        this.termoBuscaEnriquecida = '';
        this.carregarAcervo();
    }

    verDetalhes(id: number) {
        this.router.navigate(['/acervo', id]);
    }

    get documentosFiltrados() {
        return this.documentos.filter(doc => {
            const matchesTexto = !this.filtro || 
                doc.descricao?.toLowerCase().includes(this.filtro.toLowerCase()) ||
                doc.tipo?.toLowerCase().includes(this.filtro.toLowerCase()) ||
                doc.localOrigem?.toLowerCase().includes(this.filtro.toLowerCase());
            
            const matchesTipo = !this.filtroTipo || doc.tipo === this.filtroTipo;
            const matchesAno = !this.filtroAno || doc.anoDocumento?.toString() === this.filtroAno;
            const matchesLocal = !this.filtroLocal || doc.localOrigem?.toLowerCase().includes(this.filtroLocal.toLowerCase());

            return matchesTexto && matchesTipo && matchesAno && matchesLocal;
        });
    }

    get tiposDisponiveis() {
        return [...new Set(this.documentos.map(d => d.tipo))].filter(Boolean);
    }

    get anosDisponiveis() {
        return [...new Set(this.documentos.map(d => d.anoDocumento))].filter(Boolean).sort((a, b) => b! - a!);
    }
}
