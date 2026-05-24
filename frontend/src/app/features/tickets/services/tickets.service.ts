import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  AddCommentRequest,
  AssignTicketRequest,
  ChangeTicketStatusRequest,
  CreateTicketRequest,
  Page,
  Ticket,
  TicketAttachment,
  TicketComment,
  TicketFilter,
  TicketHistory,
  UpdateTicketRequest
} from '../../../core/models/ticket.models';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  constructor(private readonly api: ApiService) {}

  search(filter: TicketFilter = {}, page = 0, size = 20): Observable<Page<Ticket>> {
    return this.api.post<Page<Ticket>>('/tickets/search', {}, { ...filter, page, size, sort: 'createdAt,desc' });
  }

  getById(ticketId: string): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}`);
  }

  create(request: CreateTicketRequest): Observable<Ticket> {
    return this.api.post<Ticket>('/tickets', request);
  }

  update(ticketId: string, request: UpdateTicketRequest): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}/update`, request);
  }

  assign(ticketId: string, request: AssignTicketRequest): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}/assign`, request);
  }

  changeStatus(ticketId: string, request: ChangeTicketStatusRequest): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}/status`, request);
  }

  resolve(ticketId: string, note: string): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}/resolve`, { note });
  }

  reopen(ticketId: string, note: string): Observable<Ticket> {
    return this.api.post<Ticket>(`/tickets/${ticketId}/reopen`, { note });
  }

  comments(ticketId: string): Observable<TicketComment[]> {
    return this.api.post<TicketComment[]>(`/tickets/${ticketId}/comments/search`);
  }

  addComment(ticketId: string, request: AddCommentRequest): Observable<TicketComment> {
    return this.api.post<TicketComment>(`/tickets/${ticketId}/comments`, request);
  }

  history(ticketId: string): Observable<TicketHistory[]> {
    return this.api.post<TicketHistory[]>(`/tickets/${ticketId}/history/search`);
  }

  attachments(ticketId: string): Observable<TicketAttachment[]> {
    return this.api.post<TicketAttachment[]>(`/tickets/${ticketId}/attachments/search`);
  }

  uploadAttachment(ticketId: string, file: File): Observable<TicketAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.post<TicketAttachment>(`/tickets/${ticketId}/attachments`, formData);
  }
}
