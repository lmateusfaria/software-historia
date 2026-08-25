import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SystemHealthDTO } from './models/system-health.model';

@Injectable({ providedIn: 'root' })
export class SystemHealthService {
  private apiUrl = '/api/system/health';

  constructor(private http: HttpClient) {}

  getHealth(): Observable<SystemHealthDTO> {
    return this.http.get<SystemHealthDTO>(this.apiUrl);
  }
}
