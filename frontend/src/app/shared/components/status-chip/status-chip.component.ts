import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TicketPriority, TicketStatus } from '../../../core/models/ticket.models';

@Component({
  selector: 'app-status-chip',
  standalone: true,
  template: `<span class="inline-flex items-center rounded px-2 py-1 text-xs font-medium" [class]="classes()">{{ label() }}</span>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatusChipComponent {
  readonly label = input.required<TicketStatus | TicketPriority | string>();
  readonly classes = computed(() => {
    const value = this.label();
    if (value === 'CRITICAL' || value === 'HIGH' || value === 'REOPENED') {
      return 'bg-red-50 text-red-700';
    }
    if (value === 'RESOLVED' || value === 'CLOSED' || value === 'LOW') {
      return 'bg-green-50 text-green-700';
    }
    if (value === 'IN_PROGRESS' || value === 'ASSIGNED' || value === 'MEDIUM') {
      return 'bg-amber-50 text-amber-700';
    }
    return 'bg-slate-100 text-slate-700';
  });
}
