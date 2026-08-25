import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { SystemHealthService } from '../../core/system-health.service';
import { SystemHealthDTO } from '../../core/models/system-health.model';
import { ToastService } from '../toast/toast.service';

@Component({
  selector: 'app-system-health-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './system-health-panel.html',
  styleUrls: ['./system-health-panel.css']
})
export class SystemHealthPanelComponent implements OnInit {
  loading = true;
  error = '';
  health?: SystemHealthDTO;

  constructor(
    private systemHealthService: SystemHealthService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loadHealth();
  }

  loadHealth(): void {
    this.loading = true;
    this.error = '';

    this.systemHealthService.getHealth().subscribe({
      next: (health) => {
        this.health = health;
        this.loading = false;

        if (health.status === 'DOWN') {
          const downServices = health.services
            .filter((service) => service.status === 'DOWN')
            .map((service) => service.name);
          this.toast.error(
            downServices.length > 0
              ? `Infraestrutura indisponivel: ${downServices.join(', ')}.`
              : 'Infraestrutura indisponivel.'
          );
        } else if (health.status === 'DEGRADED') {
          const degradedServices = health.services
            .filter((service) => service.status !== 'UP')
            .map((service) => `${service.name}: ${service.details}`);
          this.toast.warning(
            degradedServices.length > 0
              ? `Sistema com alerta: ${degradedServices.join(' | ')}`
              : 'Sistema operando com alerta.'
          );
        }
      },
      error: () => {
        this.loading = false;
        this.error = 'Nao foi possivel consultar a saude da infraestrutura.';
        this.toast.error('Falha ao verificar Neo4j, PostgreSQL e MinIO.');
      }
    });
  }

  get isHealthy(): boolean {
    return this.health?.status === 'UP';
  }
}
