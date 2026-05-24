export type RoleCode = 'CUSTOMER' | 'ADMIN' | 'SERVICE_MANAGER';

export interface AuthUser {
  id: string;
  email: string;
  fullName: string;
  role: RoleCode;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: AuthUser;
}
