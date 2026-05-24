import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Change password</h2>
    <form [formGroup]="form" (ngSubmit)="save()">
      <mat-dialog-content class="dialog-content">
        <mat-form-field appearance="outline">
          <mat-label>Current password</mat-label>
          <mat-icon matPrefix>lock</mat-icon>
          <input matInput type="password" formControlName="currentPassword" autocomplete="current-password" />
          @if (form.controls.currentPassword.hasError('required')) {
            <mat-error>Current password is required.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>New password</mat-label>
          <mat-icon matPrefix>enhanced_encryption</mat-icon>
          <input matInput type="password" formControlName="newPassword" autocomplete="new-password" />
          <mat-hint>Use at least 12 characters with upper, lower, number, and special character.</mat-hint>
          @if (form.controls.newPassword.invalid && form.controls.newPassword.touched) {
            <mat-error>Enter a stronger password.</mat-error>
          }
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="saving()">
          <mat-icon>{{ saving() ? 'hourglass_top' : 'save' }}</mat-icon>
          {{ saving() ? 'Saving...' : 'Save password' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .dialog-content {
      display: grid;
      gap: 0.85rem;
      min-width: min(420px, 78vw);
      padding-top: 0.25rem;
    }

    mat-form-field {
      width: 100%;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChangePasswordDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ChangePasswordDialogComponent>);
  private readonly authService = inject(AuthService);
  private readonly notifications = inject(NotificationService);

  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [
      Validators.required,
      Validators.minLength(12),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)
    ]]
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue();
    this.authService.changePassword(value.currentPassword, value.newPassword).subscribe({
      next: () => {
        this.notifications.success('Password changed successfully.');
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false)
    });
  }
}
