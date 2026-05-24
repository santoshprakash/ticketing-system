import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { filter, map, shareReplay, switchMap, tap } from 'rxjs';
import { TicketPriority } from '../../../core/models/ticket.models';
import { NotificationService } from '../../../core/services/notification.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { TicketsService } from '../services/tickets.service';

@Component({
  selector: 'app-ticket-edit-page',
  standalone: true,
  imports: [AsyncPipe, ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, PageHeaderComponent],
  templateUrl: './ticket-edit-page.component.html',
  styleUrl: './ticket-form-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketEditPageComponent {
  readonly priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly categories = ['ACCESS', 'BILLING', 'HARDWARE', 'SOFTWARE', 'NETWORK', 'SECURITY'];
  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(5000)]],
    priority: ['HIGH' as TicketPriority, Validators.required],
    category: ['ACCESS', [Validators.required, Validators.maxLength(80)]]
  });

  readonly ticket$ = this.route.paramMap.pipe(
    map((params) => params.get('ticketId')),
    filter((ticketId): ticketId is string => Boolean(ticketId)),
    switchMap((ticketId) => this.ticketsService.getById(ticketId)),
    tap((ticket) => this.form.patchValue(ticket)),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly ticketsService: TicketsService,
    private readonly notifications: NotificationService
  ) {}

  submit(ticketId: string): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.ticketsService.update(ticketId, this.form.getRawValue()).subscribe((ticket) => {
      this.notifications.success('Ticket updated successfully.');
      void this.router.navigate(['/tickets', ticket.id]);
    });
  }
}
