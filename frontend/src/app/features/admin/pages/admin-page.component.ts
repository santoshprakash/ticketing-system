import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxChange, MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AdminUser, ModuleCode, TicketType } from '../../../core/models/admin.models';
import { RoleCode } from '../../../core/models/auth.models';
import { AccessService } from '../../../core/services/access.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MetricCardComponent } from '../../../shared/components/metric-card/metric-card.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MetricCardComponent,
    PageHeaderComponent
  ],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly notifications = inject(NotificationService);
  readonly accessService = inject(AccessService);

  readonly users = signal<AdminUser[]>([]);
  readonly ticketTypes = signal<TicketType[]>([]);
  readonly loading = signal(false);
  readonly savingUser = signal(false);
  readonly savingTicketType = signal(false);
  readonly resettingUserId = signal<string | null>(null);

  readonly roleOptions: RoleCode[] = ['CUSTOMER', 'SERVICE_MANAGER', 'ADMIN'];
  readonly moduleOptions: Array<{ code: ModuleCode; label: string; icon: string }> = [
    { code: 'DASHBOARD', label: 'Dashboard', icon: 'space_dashboard' },
    { code: 'TICKETS', label: 'Tickets', icon: 'confirmation_number' },
    { code: 'SERVICE_MANAGER', label: 'Service Manager', icon: 'engineering' },
    { code: 'ADMIN', label: 'Admin', icon: 'admin_panel_settings' }
  ];

  readonly userForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(320)]],
    password: ['', [Validators.required, Validators.minLength(12)]],
    phoneNumber: ['', [Validators.maxLength(30)]],
    role: this.fb.nonNullable.control<RoleCode>('CUSTOMER', Validators.required)
  });

  readonly ticketTypeForm = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(80)]],
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(500)]]
  });

  readonly resetPasswordForm = this.fb.nonNullable.group({
    newPassword: ['', [
      Validators.required,
      Validators.minLength(12),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)
    ]]
  });

  readonly metrics = computed(() => {
    const users = this.users();
    const ticketTypes = this.ticketTypes();
    return [
      { label: 'Active users', value: users.length, caption: 'Managed platform accounts', icon: 'group', tone: 'brand' as const },
      { label: 'Admin users', value: users.filter((user) => user.role === 'ADMIN').length, caption: 'Privileged accounts', icon: 'shield_person', tone: 'green' as const },
      { label: 'Ticket categories', value: ticketTypes.length, caption: 'Available request categories', icon: 'category', tone: 'amber' as const }
    ];
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.adminService.users().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.adminService.ticketTypes().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (ticketTypes) => this.ticketTypes.set(ticketTypes)
    });
  }

  createUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }
    this.savingUser.set(true);
    this.adminService.createUser(this.userForm.getRawValue()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (created) => {
        this.users.update((users) => [created, ...users]);
        this.userForm.reset({ fullName: '', email: '', password: '', phoneNumber: '', role: 'CUSTOMER' });
        this.notifications.success('User created with default module access.');
        this.savingUser.set(false);
      },
      error: () => this.savingUser.set(false)
    });
  }

  deleteUser(user: AdminUser): void {
    if (user.superAdmin) {
      return;
    }
    this.adminService.deleteUser(user.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.users.update((users) => users.filter((item) => item.id !== user.id));
        this.notifications.success('User deleted.');
      }
    });
  }

  startPasswordReset(user: AdminUser): void {
    if (!this.accessService.superAdmin()) {
      return;
    }
    this.resettingUserId.set(user.id);
    this.resetPasswordForm.reset({ newPassword: '' });
  }

  cancelPasswordReset(): void {
    this.resettingUserId.set(null);
    this.resetPasswordForm.reset({ newPassword: '' });
  }

  resetUserPassword(): void {
    const userId = this.resettingUserId();
    if (!userId) {
      return;
    }
    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }
    this.adminService.resetUserPassword(userId, this.resetPasswordForm.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notifications.success('Password reset successfully.');
          this.cancelPasswordReset();
        }
      });
  }

  toggleModule(user: AdminUser, moduleCode: ModuleCode, event: MatCheckboxChange): void {
    this.adminService.updateModuleAccess(user.id, { moduleCode, enabled: event.checked })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.users.update((users) => users.map((item) => item.id === updated.id ? updated : item));
          this.notifications.success(event.checked ? 'Module access granted.' : 'Module access removed.');
        },
        error: () => {
          event.source.checked = user.moduleAccess.includes(moduleCode);
        }
      });
  }

  createTicketType(): void {
    if (this.ticketTypeForm.invalid) {
      this.ticketTypeForm.markAllAsTouched();
      return;
    }
    this.savingTicketType.set(true);
    this.adminService.createTicketType(this.ticketTypeForm.getRawValue()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (created) => {
        this.ticketTypes.update((types) => [...types, created].sort((a, b) => a.name.localeCompare(b.name)));
        this.ticketTypeForm.reset({ code: '', name: '', description: '' });
        this.notifications.success('Ticket type added.');
        this.savingTicketType.set(false);
      },
      error: () => this.savingTicketType.set(false)
    });
  }

  deleteTicketType(ticketType: TicketType): void {
    this.adminService.deleteTicketType(ticketType.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.ticketTypes.update((types) => types.filter((item) => item.id !== ticketType.id));
        this.notifications.success('Ticket type deleted.');
      }
    });
  }

  canToggle(user: AdminUser, moduleCode: ModuleCode): boolean {
    if (user.superAdmin || !this.accessService.superAdmin()) {
      return false;
    }
    if (moduleCode === 'ADMIN') {
      return user.role === 'ADMIN';
    }
    if (moduleCode === 'SERVICE_MANAGER') {
      return user.role === 'ADMIN' || user.role === 'SERVICE_MANAGER';
    }
    return true;
  }
}
