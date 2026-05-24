import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TicketHistory } from '../../../../core/models/ticket.models';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-ticket-timeline',
  standalone: true,
  imports: [DatePipe, MatIconModule, EmptyStateComponent],
  templateUrl: './ticket-timeline.component.html',
  styleUrl: './ticket-timeline.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketTimelineComponent {
  readonly history = input<TicketHistory[]>([]);
}
