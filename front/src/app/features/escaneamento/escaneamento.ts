import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../shared/toast/toast.service';
import { UserService } from '../../core/user.service';
import { DocumentoService } from '../../core/documento.service';
import { DocumentoDTO } from '../../core/models/documento.model';
import { lastValueFrom } from 'rxjs';

import { ModalComponent } from '../../shared/components/modal/modal';

@Component({
    selector: 'app-escaneamento',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
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
    isUploading = false;
    uploadProgress = 0;
    uploadError: string | null = null;
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
        this.isUploading = true;
        this.uploadProgress = 0;
        this.uploadError = null;

        try {
            // 1. Comprimir arquivos em paralelo
            console.log('Iniciando compressão de imagens...');
            const compressedFiles = await Promise.all(
                this.selectedFiles.map(async (file) => {
                    if (file.type === 'application/pdf') return file;
                    const blob = await this.compressImage(file);
                    return new File([blob], file.name, { type: 'image/jpeg' });
                })
            );

            // 2. Calcular total de chunks para o progresso global
            const chunkSize = 10 * 1024 * 1024; // 10MB para upload mais fluido
            const fileChunksInfo = compressedFiles.map(file => Math.ceil(file.size / chunkSize));
            const totalChunksGlobal = fileChunksInfo.reduce((a, b) => a + b, 0);
            let uploadedChunksCount = 0;

            const preUploadedFiles: string[] = [];

            // 3. Upload paralelo de arquivos (limitado para não saturar a banda)
            // Vamos processar os arquivos um a um mas com chunks rápidos, ou em grupos. 
            // Para maior estabilidade, faremos os arquivos em série mas chunks ultra otimizados.
            for (let fileIndex = 0; fileIndex < compressedFiles.length; fileIndex++) {
                const file = compressedFiles[fileIndex];
                const totalChunks = fileChunksInfo[fileIndex];
                const uploadId = Math.random().toString(36).substring(7);

                console.log(`Enviando: ${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)`);

                for (let i = 0; i < totalChunks; i++) {
                    const start = i * chunkSize;
                    const end = Math.min(start + chunkSize, file.size);
                    const chunk = file.slice(start, end);
                    
                    const response: any = await lastValueFrom(this.documentoService.uploadChunk(chunk, uploadId, i, totalChunks, file.name));
                    
                    uploadedChunksCount++;
                    this.uploadProgress = Math.round((uploadedChunksCount / totalChunksGlobal) * 100);

                    if (i === totalChunks - 1 && response && response.filePath) {
                        preUploadedFiles.push(response.filePath);
                    }
                }
            }

            this.documento.preUploadedFiles = preUploadedFiles;
            await lastValueFrom(this.documentoService.create(this.documento, []));

            this.toast.success('Documento enviado com sucesso!');
            this.resetForm();
        } catch (error: any) {
            console.error('Erro no upload:', error);
            this.uploadError = 'Erro ao enviar documentos. ' + (error.message || 'Verifique sua conexão.');
            this.loading = false;
        } finally {
            if (!this.uploadError) {
                this.loading = false;
                this.isUploading = false;
            }
        }
    }

    private compressImage(file: File): Promise<Blob> {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (event: any) => {
                const img = new Image();
                img.src = event.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let width = img.width;
                    let height = img.height;

                    const maxDim = 2500;
                    if (width > maxDim || height > maxDim) {
                        if (width > height) {
                            height *= maxDim / width;
                            width = maxDim;
                        } else {
                            width *= maxDim / height;
                            height = maxDim;
                        }
                    }

                    canvas.width = width;
                    canvas.height = height;
                    const ctx = canvas.getContext('2d');
                    ctx?.drawImage(img, 0, 0, width, height);

                    canvas.toBlob(
                        (blob) => blob ? resolve(blob) : reject(new Error('Erro no canvas')),
                        'image/jpeg',
                        0.75
                    );
                };
                img.onerror = reject;
            };
            reader.onerror = reject;
        });
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
