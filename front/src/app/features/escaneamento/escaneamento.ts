import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../shared/toast/toast.service';
import { UserService } from '../../core/user.service';

@Component({
    selector: 'app-escaneamento',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './escaneamento.html',
    styleUrls: ['./escaneamento.css']
})
export class EscaneamentoComponent implements OnInit {
    documento = {
        titulo: '',
        descricao: '',
        urlImagem: '',
        usuarioId: 0
    };

    loading = false;
    selectedFile: File | null = null;

    constructor(
        private http: HttpClient,
        private toast: ToastService,
        private userService: UserService
    ) { }

    ngOnInit() {
        this.userService.getMe().subscribe(user => {
            this.documento.usuarioId = user.id;
        });
    }

    onFileSelected(event: any) {
        this.selectedFile = event.target.files[0];
        if (this.selectedFile) {
            // Por enquanto, simulamos o upload gerando um placeholder
            // No futuro, aqui teremos a integração real com S3/Firebase/Local
            this.documento.urlImagem = 'https://via.placeholder.com/800x600?text=Documento+Escaneado';
        }
    }

    onSubmit() {
        if (!this.documento.titulo) {
            this.toast.error('O título é obrigatório.');
            return;
        }

        this.loading = true;
        this.http.post('/api/documentos', this.documento).subscribe({
            next: () => {
                this.toast.success('Documento enviado para revisão com sucesso!');
                this.documento.titulo = '';
                this.documento.descricao = '';
                this.documento.urlImagem = '';
                this.selectedFile = null;
                this.loading = false;
            },
            error: () => {
                this.toast.error('Erro ao enviar documento.');
                this.loading = false;
            }
        });
    }
}
