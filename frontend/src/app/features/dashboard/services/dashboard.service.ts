import { Injectable } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { Observable, of } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface DashboardMetric {
  label: string;
  value: number | string;
  caption: string;
  icon: string;
  tone: 'brand' | 'green' | 'amber' | 'red';
}

export interface DashboardActivity {
  id: string;
  actor: string;
  action: string;
  time: string;
}

export interface DashboardOverview {
  metrics: DashboardMetric[];
  volumeChart: ChartConfiguration;
  slaChart: ChartConfiguration;
  activity: DashboardActivity[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly api: ApiService) {}

  overview(): Observable<DashboardOverview> {
    // API-ready fallback data keeps the dashboard usable while backend analytics mature.
    return of({
      metrics: [
        { label: 'Total tickets', value: 184, caption: '+12% from last week', icon: 'confirmation_number', tone: 'brand' },
        { label: 'Open tickets', value: 42, caption: '18 awaiting assignment', icon: 'pending_actions', tone: 'amber' },
        { label: 'SLA compliance', value: '94%', caption: '6 at risk today', icon: 'timer', tone: 'green' },
        { label: 'Escalations', value: 7, caption: '3 critical priority', icon: 'priority_high', tone: 'red' }
      ],
      volumeChart: {
        type: 'line',
        data: {
          labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
          datasets: [
            { label: 'Created', data: [22, 31, 28, 37, 45, 19, 24], borderColor: '#4f46e5', backgroundColor: 'rgba(79,70,229,.12)', tension: 0.35, fill: true },
            { label: 'Resolved', data: [18, 24, 30, 32, 39, 17, 28], borderColor: '#0f9f6e', backgroundColor: 'rgba(15,159,110,.10)', tension: 0.35, fill: true }
          ]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
      },
      slaChart: {
        type: 'doughnut',
        data: {
          labels: ['On track', 'At risk', 'Breached'],
          datasets: [{ data: [78, 16, 6], backgroundColor: ['#16a34a', '#f59e0b', '#dc2626'], borderWidth: 0 }]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } }, cutout: '68%' }
      },
      activity: [
        { id: '1', actor: 'Ava Service', action: 'resolved TK-1024 after customer confirmation', time: '10 minutes ago' },
        { id: '2', actor: 'Noah Admin', action: 'reassigned TK-1019 to Network Operations', time: '28 minutes ago' },
        { id: '3', actor: 'Mia Customer', action: 'added screenshots to TK-1012', time: '47 minutes ago' }
      ]
    });
  }
}
