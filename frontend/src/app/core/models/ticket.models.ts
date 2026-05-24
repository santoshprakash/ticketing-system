export type TicketStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REOPENED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type TicketHistoryAction = 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'REASSIGNED' | 'STATUS_CHANGED' | 'COMMENTED' | 'ATTACHED' | 'RESOLVED' | 'REOPENED';

export interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  category: string;
  createdBy: string;
  assignedTo?: string | null;
  firstResponseDueAt?: string | null;
  resolutionDueAt?: string | null;
  resolvedAt?: string | null;
  closedAt?: string | null;
  slaBreached: boolean;
  escalationLevel: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  category: string;
}

export interface UpdateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  category: string;
}

export interface AssignTicketRequest {
  assigneeId: string;
  reason?: string;
}

export interface ChangeTicketStatusRequest {
  status: TicketStatus;
  note?: string;
}

export interface AddCommentRequest {
  comment: string;
  internal: boolean;
}

export interface TicketFilter {
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: string;
  createdBy?: string;
  assignedTo?: string;
  search?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface TicketComment {
  id: string;
  ticketId: string;
  authorName: string;
  comment: string;
  internal: boolean;
  createdAt: string;
}

export interface TicketAttachment {
  id: string;
  ticketId: string;
  fileName: string;
  contentType: string;
  size: number;
  uploadedBy: string;
  createdAt: string;
  downloadUrl?: string;
}

export interface TicketHistory {
  id: string;
  ticketId: string;
  action: TicketHistoryAction;
  actorName: string;
  previousValue?: string | null;
  newValue?: string | null;
  note?: string | null;
  createdAt: string;
}
