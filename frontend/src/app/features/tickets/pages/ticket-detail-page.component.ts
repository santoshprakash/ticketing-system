import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { combineLatest, filter, map, shareReplay, startWith, Subject, switchMap } from 'rxjs';
import { Ticket } from '../../../core/models/ticket.models';
import { NotificationService } from '../../../core/services/notification.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { TicketAttachmentsComponent } from '../components/ticket-attachments/ticket-attachments.component';
import { TicketCommentsComponent } from '../components/ticket-comments/ticket-comments.component';
import { TicketTimelineComponent } from '../components/ticket-timeline/ticket-timeline.component';
import { TicketsService } from '../services/tickets.service';

@Component({
  selector: 'app-ticket-detail-page',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTabsModule,
    PageHeaderComponent,
    StatusChipComponent,
    TicketAttachmentsComponent,
    TicketCommentsComponent,
    TicketTimelineComponent
  ],
  templateUrl: './ticket-detail-page.component.html',
  styleUrl: './ticket-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketDetailPageComponent {
  private readonly refresh$ = new Subject<void>();
  private readonly ticketId$ = this.route.paramMap.pipe(
    map((params) => params.get('ticketId')),
    filter((ticketId): ticketId is string => Boolean(ticketId)),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  readonly ticket$ = combineLatest([this.ticketId$, this.refresh$.pipe(startWith(undefined))]).pipe(
    switchMap(([ticketId]) => this.ticketsService.getById(ticketId)),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  readonly history$ = combineLatest([this.ticketId$, this.refresh$.pipe(startWith(undefined))]).pipe(
    switchMap(([ticketId]) => this.ticketsService.history(ticketId))
  );

  readonly comments$ = combineLatest([this.ticketId$, this.refresh$.pipe(startWith(undefined))]).pipe(
    switchMap(([ticketId]) => this.ticketsService.comments(ticketId))
  );

  readonly attachments$ = combineLatest([this.ticketId$, this.refresh$.pipe(startWith(undefined))]).pipe(
    switchMap(([ticketId]) => this.ticketsService.attachments(ticketId))
  );

  constructor(
    private readonly route: ActivatedRoute,
    private readonly ticketsService: TicketsService,
    private readonly notifications: NotificationService
  ) {}

  resolve(ticket: Ticket): void {
    this.ticketsService.resolve(ticket.id, 'Resolved from service desk workspace.').subscribe(() => {
      this.notifications.success('Ticket resolved.');
      this.refresh$.next();
    });
  }

  reopen(ticket: Ticket): void {
    this.ticketsService.reopen(ticket.id, 'Reopened for additional service follow-up.').subscribe(() => {
      this.notifications.success('Ticket reopened.');
      this.refresh$.next();
    });
  }

  addComment(ticketId: string, request: { comment: string; internal: boolean }): void {
    this.ticketsService.addComment(ticketId, request).subscribe(() => {
      this.notifications.success('Comment added.');
      this.refresh$.next();
    });
  }

  uploadAttachment(ticketId: string, file: File): void {
    this.ticketsService.uploadAttachment(ticketId, file).subscribe(() => {
      this.notifications.success('Attachment uploaded.');
      this.refresh$.next();
    });
  }
}
