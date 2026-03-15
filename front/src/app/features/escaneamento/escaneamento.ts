import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../shared/toast/toast.service';
import { UserService } from '../../core/user.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';
import { lastValueFrom } from 'rxjs';

@Component({
    selector: 'app-escaneamento',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './escaneamento.html',
    styleUrls: ['./escaneamento.css']
})
export class EscaneamentoComponent implements OnInit {
    documento: DocumentoDTO = {
        descricao: '',
        usuarioId: 0,
        tipo: 'Jornal',
        diaDocumento: undefined,
        mesDocumento: undefined,
        anoDocumento: new Date().getFullYear(),
        localOrigem: '',
        edicao: '',
        marcadores: ''
    };

    currentYear = new Date().getFullYear();

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
            
            if (file.type === 'application/pdf') {
                // Se for PDF, usar um ícone genérico ou cover genérico 
                this.previews.push('assets/pdf-icon.png'); 
            } else {
                const reader = new FileReader();
                reader.onload = (e: any) => {
                    this.previews.push(e.target.result);
                };
                reader.readAsDataURL(file);
            }
        }
    }

    removeFile(index: number) {
        this.selectedFiles.splice(index, 1);
        this.previews.splice(index, 1);
    }

    async onSubmit() {
        if (this.selectedFiles.length === 0) {
            this.toast.error('Selecione pelo menos uma imagem do documento.');
            return;
        }

        if (this.documento.diaDocumento && (this.documento.diaDocumento < 1 || this.documento.diaDocumento > 31)) {
            this.toast.error('Dia inválido.');
            return;
        }

        if (this.documento.mesDocumento && (this.documento.mesDocumento < 1 || this.documento.mesDocumento > 12)) {
            this.toast.error('Mês inválido.');
            return;
        }

        if (this.documento.anoDocumento && (this.documento.anoDocumento < 1500 || this.documento.anoDocumento > this.currentYear)) {
            this.toast.error('Ano inválido.');
            return;
        }

        this.loading = true;
        const uploadId = Math.random().toString(36).substring(7);
        const preUploadedFiles: string[] = [];

        try {
            for (const file of this.selectedFiles) {
                const chunkSize = 5 * 1024 * 1024; // 5MB
                const totalChunks = Math.ceil(file.size / chunkSize);
                let lastResponse: any;

                console.log(`Iniciando upload chunked para: ${file.name} (${totalChunks} partes)`);

                for (let i = 0; i < totalChunks; i++) {
                    const start = i * chunkSize;
                    const end = Math.min(start + chunkSize, file.size);
                    const chunk = file.slice(start, end);
                    
                    lastResponse = await lastValueFrom(this.documentoService.uploadChunk(chunk, uploadId, i, totalChunks, file.name));
                }

                if (lastResponse && lastResponse.filePath) {
                    preUploadedFiles.push(lastResponse.filePath);
                }
            }

            this.documento.preUploadedFiles = preUploadedFiles;
            
            // Envia o DTO final sem enviar os arquivos novamente (já estão no servidor)
            await lastValueFrom(this.documentoService.create(this.documento, []));

            this.toast.success('Documento enviado para revisão com sucesso!');
            this.resetForm();
        } catch (error) {
            console.error('Erro no upload chunked:', error);
            this.toast.error('Erro ao enviar documento em partes. Verifique sua conexão.');
        } finally {
            this.loading = false;
        }
    }

    resetForm() {
        this.documento = {
            descricao: '',
            usuarioId: this.documento.usuarioId,
            tipo: 'Jornal',
            diaDocumento: undefined,
            mesDocumento: undefined,
            anoDocumento: new Date().getFullYear(),
            localOrigem: '',
            edicao: '',
            marcadores: ''
        };
        this.selectedFiles = [];
        this.previews = [];
    }
}
