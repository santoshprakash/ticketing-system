import { RoleCode } from './auth.models';

export type ModuleCode = 'DASHBOARD' | 'TICKETS' | 'ADMIN' | 'SERVICE_MANAGER';

export interface ModuleAccessResponse {
  superAdmin: boolean;
  modules: ModuleCode[];
}

export interface AdminUser {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: RoleCode;
  status: 'ACTIVE' | 'INVITED' | 'LOCKED' | 'DISABLED';
  superAdmin: boolean;
  moduleAccess: ModuleCode[];
  createdAt: string;
}

export interface CreateAdminUserRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: RoleCode;
}

export interface UpdateModuleAccessRequest {
  moduleCode: ModuleCode;
  enabled: boolean;
}

export interface AdminResetPasswordRequest {
  newPassword: string;
}

export interface TicketType {
  id: string;
  code: string;
  name: string;
  description?: string;
  active: boolean;
}

export interface CreateTicketTypeRequest {
  code: string;
  name: string;
  description?: string;
}
