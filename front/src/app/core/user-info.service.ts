import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface UsuarioInfo {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  perfil: string;
  dataCriacao: string;
  podeCadastrar: boolean;
  senha?: string; // campo opcional para edição
}

@Injectable({ providedIn: 'root' })
export class UserInfoService {
  // Ajustado para usar o endpoint /usuarios/me
  private apiUrl = '/api/usuarios/me';

  constructor(private http: HttpClient, private auth: AuthService) { }

  getMe(): Observable<UsuarioInfo> {
    const token = this.auth.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.get<UsuarioInfo>(this.apiUrl, { headers });
  }
}