import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MetricCardComponent } from '../../../shared/components/metric-card/metric-card.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-service-manager-page',
  standalone: true,
  imports: [MatButtonModule, MatCardModule, MatIconModule, MetricCardComponent, PageHeaderComponent],
  templateUrl: './service-manager-page.component.html',
  styleUrl: './service-manager-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServiceManagerPageComponent {
  readonly queues = [
    { name: 'Unassigned', count: 18, caption: 'Needs triage', icon: 'assignment_late', tone: 'amber' as const },
    { name: 'At risk SLA', count: 6, caption: 'Due today', icon: 'timer', tone: 'red' as const },
    { name: 'Resolved today', count: 31, caption: 'Across all teams', icon: 'task_alt', tone: 'green' as const }
  ];
}
