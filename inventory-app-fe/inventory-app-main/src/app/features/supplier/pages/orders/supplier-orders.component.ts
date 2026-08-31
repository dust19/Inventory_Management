import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupplierService } from '../../../admin/services/supplier.service';
import { SupplierOrderService } from '../../services/supplier-order.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { PurchaseResponse } from '../../../common/models/purchase.model';
import { statusBadge } from '../../../../core/utils/role.util';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { ToastService } from 'src/app/shared/components/toast/toast.service';
import { PurchaseService } from 'src/app/features/admin/services/purchase.service';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

@Component({
  selector: 'app-supplier-orders',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LoaderComponent,
    StatStripComponent,
    AppTableComponent,
    PaginatorComponent
  ],
  templateUrl: './supplier-orders.component.html',
  styleUrls: ['./supplier-orders.component.css'],
  animations: [fadeIn]
})
export class SupplierOrdersComponent implements OnInit {
  private auth = inject(AuthService);
  private supplierSvc = inject(SupplierService);
  private orderSvc = inject(SupplierOrderService);
  private svc = inject(PurchaseService);
  private toast = inject(ToastService);

  orders = signal<PurchaseResponse[]>([]);
  filtered = signal<PurchaseResponse[]>([]);
  loading = signal(true);
  detailItem = signal<PurchaseResponse | null>(null);
  statusBadge = statusBadge;

  search = '';
  selectedStatus = '';
  selectedSort = '';

  page = signal(1);
  pageSize = signal(10);

  statusFilterOptions = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'DELIVERED', label: 'Delivered' },
    { value: 'CONFIRMED', label: 'Confirmed' },
    { value: 'CANCELED', label: 'Canceled' },
  ];

  sortOptions = [
    { value: 'date_desc', label: 'Date (Newest)' },
    { value: 'date_asc', label: 'Date (Oldest)' },
    { value: 'amount_desc', label: 'Amount (High to Low)' },
    { value: 'amount_asc', label: 'Amount (Low to High)' },
  ];

  dashboardStats = computed<StatStripItem[]>(() => [
    { icon: 'bi-cart-fill', value: this.orders().length, label: 'Total Orders', iconClass: 'icon-primary', format: 'number' },
    { icon: 'bi-hourglass-split', value: this.pendingCount, label: 'Pending', iconClass: 'icon-warning', format: 'number' },
    { icon: 'bi-truck', value: this.deliveredCount, label: 'Delivered', iconClass: 'icon-accent', format: 'number' },
    { icon: 'bi-check-circle-fill', value: this.confirmedCount, label: 'Confirmed', iconClass: 'icon-success', format: 'number' },
    { icon: 'bi-x-circle-fill', value: this.cancelledCount, label: 'Cancelled', iconClass: 'icon-danger', format: 'number' },
  ]);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    const user = this.auth.currentUser();
    if (!user) return;
    this.supplierSvc.getByUser(user.id).subscribe({
      next: s => {
        this.orderSvc.getBySupplier(s.id).subscribe({
          next: o => { this.orders.set(o); this.applyFilter(); this.loading.set(false); },
          error: () => this.loading.set(false)
        });
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilter(): void {
    const s = this.search.toLowerCase();
    let list = this.orders().filter(o =>
      o.id.toString().includes(s) ||
      (o.supplierName ?? '').toLowerCase().includes(s)
    );

    if (this.selectedStatus) {
      list = list.filter(o => o.status === this.selectedStatus);
    }

    if (this.selectedSort) {
      list = [...list].sort((a, b) => {
        switch (this.selectedSort) {
          case 'date_desc': return new Date(b.createdAt ?? '').getTime() - new Date(a.createdAt ?? '').getTime();
          case 'date_asc': return new Date(a.createdAt ?? '').getTime() - new Date(b.createdAt ?? '').getTime();
          case 'amount_desc': return b.totalAmount - a.totalAmount;
          case 'amount_asc': return a.totalAmount - b.totalAmount;
          default: return 0;
        }
      });
    }

    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<PurchaseResponse> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  deliver(o: PurchaseResponse): void {
    if (!confirm('Mark this order as delivered?')) return;
    this.orderSvc.deliver(o.id).subscribe({
      next: () => { this.toast.success('Marked as delivered'); this.load(); },
      error: err => this.toast.error(err?.error?.message ?? 'Failed')
    });
  }

  cancel(o: PurchaseResponse): void {
    if (!confirm('Cancel this order?')) return;

    const user = this.auth.currentUser();
    if (!user) return;

    this.svc.cancel(o.id, user.id).subscribe({
      next: () => {
        this.toast.success('Cancelled');
        this.load();
      },
      error: () => this.toast.error('Failed')
    });
  }

  get pendingCount(): number { return this.orders().filter(o => o.status === 'PENDING').length; }
  get deliveredCount(): number { return this.orders().filter(o => o.status === 'DELIVERED').length; }
  get confirmedCount(): number { return this.orders().filter(o => o.status === 'CONFIRMED').length; }
  get cancelledCount(): number { return this.orders().filter(o => o.status === 'CANCELED').length; }
}