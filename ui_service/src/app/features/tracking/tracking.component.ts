import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Tracking } from '../../core/models';

@Component({
  selector: 'app-tracking',
  imports: [FormsModule, DatePipe],
  templateUrl: './tracking.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrackingComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  readonly result = signal<Tracking | undefined>(undefined);
  readonly loading = signal(false);
  readonly error = signal('');
  trackingNumber = '';

  ngOnInit(): void {
    this.trackingNumber = this.route.snapshot.queryParamMap.get('number') ?? '';
    if (this.trackingNumber) this.track();
  }

  track(): void {
    if (!this.trackingNumber.trim()) return;
    this.loading.set(true);
    this.error.set('');
    this.result.set(undefined);
    this.api.track(this.trackingNumber.trim())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: result => this.result.set(result),
        error: () => this.error.set('No shipment was found. Confirm the tracking number and ensure the Shipping service is running.')
      });
  }
}
