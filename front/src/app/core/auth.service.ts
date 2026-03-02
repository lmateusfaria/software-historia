import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

interface LoginPayload {
  login: string;
  password: string;
}

interface TokenDTO {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth/login'; // ajustado para proxy Nginx

  constructor(private http: HttpClient) {}

  login(login: string, password: string): Observable<TokenDTO> {
    return this.http.post<TokenDTO>(this.apiUrl, { login, password }).pipe(
      tap((res: TokenDTO) => {
        localStorage.setItem('token', res.token);
      })
    );
  }

  logout(): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('token');
    }
  }

  // Ajuste: só acessa localStorage no browser (SSR safe)
  getToken(): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('token');
    }
    return null;
  }
}