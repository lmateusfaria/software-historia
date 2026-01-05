import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import type { UsuarioInfo } from './user-info.service';

export interface UsuarioDTO {
  id?: number;
  cpf: string;
  nome: string;
  email: string;
  senha: string;
  dataCriacao?: string;
  contasIds?: number[];
  centrosCustoIds?: number[];
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8080/usuarios'; // ajuste se necessário

  constructor(private http: HttpClient, private auth: AuthService) {}

  register(usuario: UsuarioDTO): Observable<any> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.post(this.apiUrl, usuario, { headers, observe: 'response' });
  }

  update(id: number, usuario: UsuarioDTO): Observable<any> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.put(`${this.apiUrl}/${id}`, usuario, { headers });
  }

  delete(id: number): Observable<any> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.delete(`${this.apiUrl}/${id}`, { headers });
  }

  getMe(): Observable<UsuarioInfo> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.get<UsuarioInfo>(`${this.apiUrl}/me`, { headers });
  }

  deleteAccount(): Observable<void> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.delete<void>(`${this.apiUrl}/delete-account`, { headers });
  }

  validatePassword(password: string): Observable<boolean> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.post<boolean>('http://localhost:8080/auth/validate-password', { password }, { headers });
  }
}