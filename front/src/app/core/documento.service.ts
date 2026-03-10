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
        const token = this.auth.getToken();
        let headers = new HttpHeaders();
        if (token) {
            headers = headers.set('Authorization', `Bearer ${token}`);
        }
        return this.http.get<DocumentoDTO[]>(this.apiUrl, { headers });
    }

    findByStatus(status: string): Observable<DocumentoDTO[]> {
        const token = this.auth.getToken();
        let headers = new HttpHeaders();
        if (token) {
            headers = headers.set('Authorization', `Bearer ${token}`);
        }
        return this.http.get<DocumentoDTO[]>(`${this.apiUrl}/status/${status}`, { headers });
    }

    create(documento: DocumentoDTO, files: File[]): Observable<DocumentoDTO> {
        const formData = new FormData();
        
        // Adiciona o DTO como um Blob JSON para o @RequestPart("documento")
        formData.append('documento', new Blob([JSON.stringify(documento)], {
            type: 'application/json'
        }));

        // Adiciona os arquivos para o @RequestPart("files")
        files.forEach(file => {
            formData.append('files', file);
        });

        const token = this.auth.getToken();
        let headers = new HttpHeaders();
        if (token) {
            headers = headers.set('Authorization', `Bearer ${token}`);
        }

        return this.http.post<DocumentoDTO>(this.apiUrl, formData, { headers });
    }
}
