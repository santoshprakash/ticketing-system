import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, ViewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BehaviorSubject, Observable, debounceTime, startWith, switchMap } from 'rxjs';
import { Page, Ticket, TicketFilter, TicketPriority, TicketStatus } from '../../../core/models/ticket.models';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { TicketsService } from '../services/tickets.service';

@Component({
  selector: 'app-ticket-list-page',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    EmptyStateComponent,
    PageHeaderComponent,
    StatusChipComponent
  ],
  templateUrl: './ticket-list-page.component.html',
  styleUrl: './ticket-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketListPageComponent {
  @ViewChild(MatPaginator) private readonly paginator?: MatPaginator;
  @ViewChild(MatSort) private readonly sort?: MatSort;

  readonly columns = ['ticketNumber', 'title', 'status', 'priority', 'category', 'assignee', 'sla', 'updatedAt'];
  readonly statuses: TicketStatus[] = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED'];
  readonly priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly pageState$ = new BehaviorSubject({ page: 0, size: 10 });
  readonly form = this.fb.nonNullable.group({
    search: [''],
    status: [''],
    priority: [''],
    category: ['']
  });

  readonly tickets$: Observable<Page<Ticket>> = this.form.valueChanges.pipe(
    debounceTime(250),
    startWith(this.form.getRawValue()),
    switchMap(() => this.pageState$),
    switchMap((pageState) => this.ticketsService.search(this.filter(), pageState.page, pageState.size))
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly ticketsService: TicketsService
  ) {}

  handlePage(event: PageEvent): void {
    this.pageState$.next({ page: event.pageIndex, size: event.pageSize });
  }

  sortChanged(_: Sort): void {
    this.resetToFirstPage();
  }

  resetFilters(): void {
    this.form.reset({ search: '', status: '', priority: '', category: '' });
    this.resetToFirstPage();
  }

  private filter(): TicketFilter {
    const raw = this.form.getRawValue();
    return {
      search: raw.search,
      status: raw.status ? (raw.status as TicketStatus) : undefined,
      priority: raw.priority ? (raw.priority as TicketPriority) : undefined,
      category: raw.category
    };
  }

  private resetToFirstPage(): void {
    this.paginator?.firstPage();
    this.pageState$.next({ page: 0, size: this.pageState$.value.size });
  }
}
