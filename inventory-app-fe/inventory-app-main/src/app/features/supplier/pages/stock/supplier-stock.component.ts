import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupplierService } from '../../../admin/services/supplier.service';
import { SupplierProductService } from '../../services/supplier-product.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { SupplierProductResponse } from '../../../common/models/product.model';
import { fadeIn } from '../../../../shared/animations/fade.animation';

type StockFilter = 'all' | 'healthy' | 'low' | 'out';

@Component({
  selector: 'app-supplier-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, StatStripComponent, PaginatorComponent],
  templateUrl: './supplier-stock.component.html',
  styleUrls: ['./supplier-stock.component.css'],
  animations: [fadeIn]
})
export class SupplierStockComponent implements OnInit {
  auth = inject(AuthService);
  supplierSvc = inject(SupplierService);
  productSvc = inject(SupplierProductService);

  products = signal<SupplierProductResponse[]>([]);
  loading = signal(true);

  search = signal('');
  activeFilter = signal<StockFilter>('all');

  page = signal(1);
  pageSize = signal(10);

  readonly LOW_THRESHOLD = 10;

  ngOnInit(): void {
    const user = this.auth.currentUser();
    if (!user) return;
    this.supplierSvc.getByUser(user.id).subscribe({
      next: s => {
        this.productSvc.getBySupplier(s.id).subscribe({
          next: p => { this.products.set(p); this.loading.set(false); },
          error: () => this.loading.set(false)
        });
      },
      error: () => this.loading.set(false)
    });
  }

  totalStock = computed(() => this.products().reduce((s, p) => s + p.availableStock, 0));
  lowStockCount = computed(() => this.products().filter(p => p.availableStock > 0 && p.availableStock < this.LOW_THRESHOLD).length);
  outOfStockCount = computed(() => this.products().filter(p => p.availableStock <= 0).length);
  healthyCount = computed(() => this.products().filter(p => p.availableStock >= this.LOW_THRESHOLD).length);

  dashboardStats = computed<StatStripItem[]>(() => [
    { icon: 'bi-box-seam-fill', value: this.products().length, label: 'Total Products', iconClass: 'icon-products', format: 'number' },
    { icon: 'bi-boxes', value: this.totalStock(), label: 'Total Units', iconClass: 'icon-categories', format: 'number' },
    { icon: 'bi-check-circle-fill', value: this.healthyCount(), label: 'Healthy Stock', iconClass: 'icon-success', format: 'number' },
    { icon: 'bi-exclamation-triangle-fill', value: this.lowStockCount(), label: 'Low Stock', iconClass: 'icon-lowstock', format: 'number' },
    { icon: 'bi-x-octagon-fill', value: this.outOfStockCount(), label: 'Out of Stock', iconClass: 'icon-danger', format: 'number' },
  ]);

  filteredProducts = computed<SupplierProductResponse[]>(() => {
    const q = this.search().trim().toLowerCase();
    const f = this.activeFilter();

    return this.products().filter(p => {
      if (f === 'healthy' && p.availableStock < this.LOW_THRESHOLD) return false;
      if (f === 'low' && !(p.availableStock > 0 && p.availableStock < this.LOW_THRESHOLD)) return false;
      if (f === 'out' && p.availableStock > 0) return false;

      if (!q) return true;
      return (p.name ?? '').toLowerCase().includes(q) || (p.sku ?? '').toLowerCase().includes(q);
    });
  });

  get paged(): PageResult<SupplierProductResponse> {
    return paginate(this.filteredProducts(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  setFilter(f: StockFilter): void {
    this.activeFilter.set(f);
    this.page.set(1);
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.page.set(1);
  }

  stockStatus(p: SupplierProductResponse): 'healthy' | 'low' | 'out' {
    if (p.availableStock <= 0) return 'out';
    if (p.availableStock < this.LOW_THRESHOLD) return 'low';
    return 'healthy';
  }

  stockPct(p: SupplierProductResponse): number {
    const max = Math.max(...this.products().map(x => x.availableStock), 1);
    return Math.round((p.availableStock / max) * 100);
  }
}