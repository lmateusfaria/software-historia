import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentoDTO } from './models/documento.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class DocumentoService {
    private apiUrl = '/api/documentos';

    constructor(private http: HttpClient, private auth: AuthService) {}

    findAll(): Observable<DocumentoDTO[]> {
        return this.http.get<DocumentoDTO[]>(this.apiUrl);
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

    findById(id: number): Observable<DocumentoDTO> {
        return this.http.get<DocumentoDTO>(`${this.apiUrl}/${id}`);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    approve(id: number): Observable<DocumentoDTO> {
        return this.http.put<DocumentoDTO>(`${this.apiUrl}/${id}/aprovar`, {});
    }
}
