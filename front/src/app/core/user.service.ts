import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import type { UsuarioInfo } from './user-info.service';

export interface UsuarioDTO {
  id?: number;
  nome: string;
  email: string;
  cpf: string;
  senha?: string;
  perfil?: string;
  podeCadastrar?: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserService {
    private apiUrl = '/api/usuarios';

    constructor(private http: HttpClient) { }

    findAll(): Observable<UsuarioDTO[]> {
        return this.http.get<UsuarioDTO[]>(this.apiUrl);
    }

    findById(id: number): Observable<UsuarioDTO> {
        return this.http.get<UsuarioDTO>(`${this.apiUrl}/${id}`);
    }

    register(usuario: UsuarioDTO): Observable<any> {
        return this.http.post(this.apiUrl, usuario, { observe: 'response' });
    }

    update(id: number, usuario: UsuarioDTO): Observable<any> {
        return this.http.put(`${this.apiUrl}/${id}`, usuario);
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${id}`);
    }

    getMe(): Observable<UsuarioInfo> {
        return this.http.get<UsuarioInfo>(`${this.apiUrl}/me`);
    }

    deleteAccount(): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/delete-account`);
    }

    validatePassword(password: string): Observable<boolean> {
        return this.http.post<boolean>('/api/auth/validate-password', { password });
    }
}