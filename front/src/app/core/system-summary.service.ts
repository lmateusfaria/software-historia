import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OperationalSummaryDTO } from './models/operational-summary.model';

@Injectable({ providedIn: 'root' })
export class SystemSummaryService {
  private apiUrl = '/api/system/summary';

  constructor(private http: HttpClient) {}

  getSummary(): Observable<OperationalSummaryDTO> {
    return this.http.get<OperationalSummaryDTO>(this.apiUrl);
  }
}
