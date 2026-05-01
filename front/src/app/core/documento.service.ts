import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentoDTO, OcrResultadoDTO, OcrStatusDTO, ImagemBuscaDTO } from './models/documento.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class DocumentoService {
    private apiUrl = '/api/documentos';

    constructor(private http: HttpClient, private auth: AuthService) {}

    findAll(): Observable<DocumentoDTO[]> {
        return this.http.get<DocumentoDTO[]>(this.apiUrl);
    }

    searchEnriched(termo: string, modo: 'documentos' | 'imagens' = 'documentos'): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/search`, {
            params: { termo, modo }
        });
    }

    findByStatus(status: string): Observable<DocumentoDTO[]> {
        return this.http.get<DocumentoDTO[]>(`${this.apiUrl}/status/${status}`);
    }

    create(documento: DocumentoDTO, files: File[]): Observable<DocumentoDTO> {
        const formData = new FormData();
        formData.append('documento', new Blob([JSON.stringify(documento)], { type: 'application/json' }));
        files.forEach(file => formData.append('files', file));
        return this.http.post<DocumentoDTO>(this.apiUrl, formData);
    }

    uploadChunk(chunk: Blob, uploadId: string, chunkIndex: number, totalChunks: number, filename: string): Observable<any> {
        const formData = new FormData();
        formData.append('chunk', chunk);
        return this.http.post(`${this.apiUrl}/upload-chunk`, formData, {
            params: {
                uploadId,
                chunkIndex: chunkIndex.toString(),
                totalChunks: totalChunks.toString(),
                filename
            }
        });
    }

    findById(id: number): Observable<DocumentoDTO> {
        return this.http.get<DocumentoDTO>(`${this.apiUrl}/${id}`);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    approve(id: number): Observable<DocumentoDTO> {
        return this.http.put<DocumentoDTO>(`${this.apiUrl}/${id}/aprovar`, {});
    }

    ocrImagem(id: number, imagemUrl: string): Observable<OcrStatusDTO> {
        return this.http.post<OcrStatusDTO>(`${this.apiUrl}/${id}/ocr-imagem`, null, { 
            params: { imagemUrl } 
        });
    }
    testarOcr(file: File): Observable<OcrResultadoDTO> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<OcrResultadoDTO>(`${this.apiUrl}/testar-ocr`, formData);
    }
}
