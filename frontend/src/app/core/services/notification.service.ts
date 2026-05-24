import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  constructor(private readonly snackBar: MatSnackBar) {}

  success(message: string): void {
    this.open(message, 'success');
  }

  error(message: string): void {
    this.open(message, 'error');
  }

  info(message: string): void {
    this.open(message, 'info');
  }

  private open(message: string, panelClass: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4500,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: [`toast-${panelClass}`]
    });
  }
}
