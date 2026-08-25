export interface ServiceHealthDTO {
  name: string;
  status: 'UP' | 'DEGRADED' | 'DOWN';
  details: string;
}

export interface SystemHealthDTO {
  status: 'UP' | 'DEGRADED' | 'DOWN';
  checkedAt: string;
  services: ServiceHealthDTO[];
}
