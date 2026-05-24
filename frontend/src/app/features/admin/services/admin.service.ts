import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AdminResetPasswordRequest,
  AdminUser,
  CreateAdminUserRequest,
  CreateTicketTypeRequest,
  TicketType,
  UpdateModuleAccessRequest
} from '../../../core/models/admin.models';
import { ApiService } from '../../../core/services/api.service';

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private readonly api: ApiService) {}

  users(): Observable<AdminUser[]> {
    return this.api.post<AdminUser[]>('/admin/users/search');
  }

  createUser(request: CreateAdminUserRequest): Observable<AdminUser> {
    return this.api.post<AdminUser>('/admin/users', request);
  }

  deleteUser(userId: string): Observable<void> {
    return this.api.post<void>(`/admin/users/${userId}/delete`);
  }

  resetUserPassword(userId: string, request: AdminResetPasswordRequest): Observable<void> {
    return this.api.post<void>(`/admin/users/${userId}/reset-password`, request);
  }

  updateModuleAccess(userId: string, request: UpdateModuleAccessRequest): Observable<AdminUser> {
    return this.api.post<AdminUser>(`/admin/users/${userId}/module-access`, request);
  }

  ticketTypes(): Observable<TicketType[]> {
    return this.api.post<TicketType[]>('/admin/ticket-types/search');
  }

  createTicketType(request: CreateTicketTypeRequest): Observable<TicketType> {
    return this.api.post<TicketType>('/admin/ticket-types', request);
  }

  deleteTicketType(ticketTypeId: string): Observable<void> {
    return this.api.post<void>(`/admin/ticket-types/${ticketTypeId}/delete`);
  }
}
