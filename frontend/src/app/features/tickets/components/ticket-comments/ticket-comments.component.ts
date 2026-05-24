import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Output, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TicketComment } from '../../../../core/models/ticket.models';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-ticket-comments',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, EmptyStateComponent],
  templateUrl: './ticket-comments.component.html',
  styleUrl: './ticket-comments.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketCommentsComponent {
  readonly comments = input<TicketComment[]>([]);
  @Output() readonly addComment = new EventEmitter<{ comment: string; internal: boolean }>();

  readonly form = this.fb.nonNullable.group({
    comment: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(2000)]],
    internal: [false]
  });

  constructor(private readonly fb: FormBuilder) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.addComment.emit(this.form.getRawValue());
    this.form.reset({ comment: '', internal: false });
  }
}
