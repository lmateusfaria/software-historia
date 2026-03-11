import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
    selector: 'app-documento-detalhe',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './documento-detalhe.html',
    styleUrls: ['./documento-detalhe.css']
})
export class DocumentoDetalheComponent implements OnInit {
    documento?: DocumentoDTO;
    loading = true;
    imagemSelecionada?: string;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private documentoService: DocumentoService,
        private toast: ToastService
    ) { }

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.carregarDocumento(+id);
        } else {
            this.router.navigate(['/acervo']);
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
            },
            error: () => {
                this.toast.error('Erro ao carregar detalhes do documento.');
                this.loading = false;
                this.router.navigate(['/acervo']);
            }
        });
    }

    selecionarImagem(url: string) {
        this.imagemSelecionada = url;
    }

    voltar() {
        this.router.navigate(['/acervo']);
    }
}
