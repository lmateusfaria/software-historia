import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../shared/toast/toast.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';

@Component({
    selector: 'app-galeria',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './galeria.html',
    styleUrls: ['./galeria.css']
})
export class GaleriaComponent implements OnInit {
    documentos: DocumentoDTO[] = [];
    filtro = '';
    loading = false;

    constructor(
        private documentoService: DocumentoService,
        private toast: ToastService
    ) { }

    ngOnInit() {
        this.carregarAcervo();
    }

    carregarAcervo() {
        this.loading = true;
        this.documentoService.findAll().subscribe({
            next: (docs) => {
                this.documentos = docs;
                this.loading = false;
            },
            error: () => {
                this.toast.error('Erro ao carregar acervo.');
                this.loading = false;
            }
        });
    }

    get documentosFiltrados() {
        return this.documentos.filter(doc =>
            doc.descricao.toLowerCase().includes(this.filtro.toLowerCase()) ||
            doc.tipo?.toLowerCase().includes(this.filtro.toLowerCase())
        );
    }
}
