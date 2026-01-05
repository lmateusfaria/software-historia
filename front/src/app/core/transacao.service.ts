import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export type TipoTransacao = 'ENTRADA' | 'SAIDA';

export interface TransacaoDTO {
  id?: number;
  tipo: TipoTransacao;
  valor: number;
  data?: string; // formato: dd/MM/yyyy HH:mm:ss
  descricao?: string;
}

@Injectable({ providedIn: 'root' })
export class TransacaoService {
  private apiUrl = 'http://localhost:8080/transacoes';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private authHeaders(): { headers?: HttpHeaders } {
    const token = this.auth.getToken();
    if (token) {
      return { headers: new HttpHeaders().set('Authorization', `Bearer ${token}`) };
    }
    return {};
  }

  criar(dto: TransacaoDTO): Observable<TransacaoDTO> {
    return this.http.post<TransacaoDTO>(this.apiUrl, dto, this.authHeaders());
  }

  minhas(tipo?: TipoTransacao): Observable<TransacaoDTO[]> {
    const url = tipo ? `${this.apiUrl}/minhas?tipo=${tipo}` : `${this.apiUrl}/minhas`;
    return this.http.get<TransacaoDTO[]>(url, this.authHeaders());
  }

  saldo(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/saldo`, this.authHeaders());
  }

  ultimas(limit: number = 5): Observable<TransacaoDTO[]> {
    return this.http.get<TransacaoDTO[]>(`${this.apiUrl}/minhas/ultimas?limit=${limit}`, this.authHeaders());
  }
}
