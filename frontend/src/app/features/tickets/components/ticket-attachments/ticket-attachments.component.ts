import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Output, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TicketAttachment } from '../../../../core/models/ticket.models';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-ticket-attachments',
  standalone: true,
  imports: [DatePipe, DecimalPipe, MatButtonModule, MatIconModule, EmptyStateComponent],
  templateUrl: './ticket-attachments.component.html',
  styleUrl: './ticket-attachments.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketAttachmentsComponent {
  readonly attachments = input<TicketAttachment[]>([]);
  @Output() readonly upload = new EventEmitter<File>();

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.upload.emit(file);
      input.value = '';
    }
  }
}
