import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../shared/toast/toast.service';
import { UserService } from '../../core/user.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';

@Component({
    selector: 'app-escaneamento',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './escaneamento.html',
    styleUrls: ['./escaneamento.css']
})
export class EscaneamentoComponent implements OnInit {
    documento: DocumentoDTO = {
        titulo: '',
        descricao: '',
        usuarioId: 0
    };

    loading = false;
    selectedFiles: File[] = [];
    previews: string[] = [];

    constructor(
        private documentoService: DocumentoService,
        private toast: ToastService,
        private userService: UserService
    ) { }

    ngOnInit() {
        this.userService.getMe().subscribe(user => {
            this.documento.usuarioId = user.id;
        });
    }

    onFileSelected(event: any) {
        const files: FileList = event.target.files;
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            this.selectedFiles.push(file);
            
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.previews.push(e.target.result);
            };
            reader.readAsDataURL(file);
        }
    }

    removeFile(index: number) {
        this.selectedFiles.splice(index, 1);
        this.previews.splice(index, 1);
    }

    onSubmit() {
        if (!this.documento.titulo) {
            this.toast.error('O título é obrigatório.');
            return;
        }

        if (this.selectedFiles.length === 0) {
            this.toast.error('Selecione pelo menos uma imagem do documento.');
            return;
        }

        this.loading = true;
        this.documentoService.create(this.documento, this.selectedFiles).subscribe({
            next: () => {
                this.toast.success('Documento enviado para revisão com sucesso!');
                this.resetForm();
                this.loading = false;
            },
            error: () => {
                this.toast.error('Erro ao enviar documento.');
                this.loading = false;
            }
        });
    }

    resetForm() {
        this.documento = {
            titulo: '',
            descricao: '',
            usuarioId: this.documento.usuarioId
        };
        this.selectedFiles = [];
        this.previews = [];
    }
}
