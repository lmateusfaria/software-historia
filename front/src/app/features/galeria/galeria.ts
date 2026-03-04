import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
    selector: 'app-galeria',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './galeria.html',
    styleUrls: ['./galeria.css']
})
export class GaleriaComponent implements OnInit {
    documentos: any[] = [];
    filtro = '';
    loading = false;

    constructor(
        private http: HttpClient,
        private toast: ToastService
    ) { }

    ngOnInit() {
        this.carregarAcervo();
    }

    carregarAcervo() {
        this.loading = true;
        this.http.get<any[]>('/api/documentos').subscribe({
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
            doc.titulo.toLowerCase().includes(this.filtro.toLowerCase()) ||
            doc.descricao.toLowerCase().includes(this.filtro.toLowerCase())
        );
    }
}
