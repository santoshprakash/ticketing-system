import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject } from '@angular/core';
import { BreakpointObserver } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../core/auth/auth.service';
import { ModuleCode } from '../../core/models/admin.models';
import { AccessService } from '../../core/services/access.service';
import { LoadingService } from '../../core/services/loading.service';
import { ThemeService } from '../../core/services/theme.service';
import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { map } from 'rxjs';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  module: ModuleCode;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatToolbarModule
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShellComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);

  readonly isMobile = toSignal(
    this.breakpointObserver.observe('(max-width: 959px)').pipe(map((state) => state.matches)),
    { initialValue: false }
  );

  private readonly navItems: NavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: 'space_dashboard', module: 'DASHBOARD' },
    { label: 'Tickets', path: '/tickets', icon: 'confirmation_number', module: 'TICKETS' },
    { label: 'Service Manager', path: '/service-manager', icon: 'engineering', module: 'SERVICE_MANAGER' },
    { label: 'Admin', path: '/admin', icon: 'admin_panel_settings', module: 'ADMIN' }
  ];

  readonly visibleNavItems = computed(() => this.navItems.filter((item) => this.accessService.canAccess(item.module)));

  constructor(
    private readonly breakpointObserver: BreakpointObserver,
    readonly authService: AuthService,
    readonly accessService: AccessService,
    readonly loadingService: LoadingService,
    readonly themeService: ThemeService
  ) {
    this.accessService.load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe();
  }

  openChangePassword(): void {
    this.dialog.open(ChangePasswordDialogComponent, {
      width: '460px',
      maxWidth: 'calc(100vw - 24px)',
      autoFocus: 'dialog'
    });
  }
}
