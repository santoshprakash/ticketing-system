import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusChipComponent } from './status-chip.component';

describe('StatusChipComponent', () => {
  let fixture: ComponentFixture<StatusChipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusChipComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StatusChipComponent);
  });

  it('renders the supplied label', () => {
    fixture.componentRef.setInput('label', 'IN_PROGRESS');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('IN_PROGRESS');
  });
});
