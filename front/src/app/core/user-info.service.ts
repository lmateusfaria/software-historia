import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UsuarioInfo {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  dataCriacao: string;
  senha?: string; // campo opcional para edição
}

@Injectable({ providedIn: 'root' })
export class UserInfoService {
  // Ajustado para usar o endpoint /usuarios/me
  private apiUrl = 'http://localhost:8080/usuarios/me';

  constructor(private http: HttpClient) {}

  getMe(): Observable<UsuarioInfo> {
    return this.http.get<UsuarioInfo>(this.apiUrl);
  }
}