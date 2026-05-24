import { Routes } from '@angular/router';
import { TicketCreatePageComponent } from './pages/ticket-create-page.component';
import { TicketDetailPageComponent } from './pages/ticket-detail-page.component';
import { TicketEditPageComponent } from './pages/ticket-edit-page.component';
import { TicketListPageComponent } from './pages/ticket-list-page.component';

export const TICKETS_ROUTES: Routes = [
  { path: '', component: TicketListPageComponent },
  { path: 'new', component: TicketCreatePageComponent },
  { path: ':ticketId/edit', component: TicketEditPageComponent },
  { path: ':ticketId', component: TicketDetailPageComponent }
];
