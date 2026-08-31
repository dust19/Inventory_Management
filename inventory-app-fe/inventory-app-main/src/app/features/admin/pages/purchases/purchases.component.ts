import { Component, OnInit, computed, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PurchaseService } from '../../services/purchase.service';
import { SupplierService } from '../../services/supplier.service';
import { ProductService } from '../../services/product.service';
import { ToastService } from '../../../../shared/components/toast/toast.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { PurchaseResponse, PurchaseRequest } from '../../../common/models/purchase.model';
import { SupplierResponse } from '../../../common/models/supplier.model';
import { SupplierProductResponse } from '../../../common/models/product.model';
import { statusBadge } from '../../../../core/utils/role.util';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';
import { AuthService } from 'src/app/core/services/auth.service';

const PRODUCT_PAGE_SIZE = 10;

@Component({
  selector: 'app-admin-purchases',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, StatStripComponent, AppTableComponent, PaginatorComponent],
  templateUrl: './purchases.component.html',
  styleUrls: ['./purchases.component.css'],
  animations: [fadeIn]
})
export class PurchasesComponent implements OnInit {
  private svc = inject(PurchaseService);
  private supplierSvc = inject(SupplierService);
  private productSvc = inject(ProductService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  purchases = signal<PurchaseResponse[]>([]);
  filtered = signal<PurchaseResponse[]>([]);
  suppliers = signal<SupplierResponse[]>([]);
  supProducts = signal<SupplierProductResponse[]>([]);
  loading = signal(true);
  submitting = signal(false);

  showCreate = signal(false);
  detailItem = signal<PurchaseResponse | null>(null);
  confirmedOrder = signal<PurchaseResponse | null>(null);

  page = signal(1);
  pageSize = signal(10);
  search = '';
  selectedStatus = '';
  selectedSort = '';

  // Modal wizard step
  currentStep = signal<1 | 2 | 3>(1);

  statusFilterOptions = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'CONFIRMED', label: 'Confirmed' },
    { value: 'DELIVERED', label: 'Delivered' },
    { value: 'CANCELED', label: 'Cancelled' },
  ];

  sortOptions = [
    { value: 'date_desc', label: 'Date (Newest)' },
    { value: 'date_asc', label: 'Date (Oldest)' },
    { value: 'amount_desc', label: 'Amount (High → Low)' },
    { value: 'amount_asc', label: 'Amount (Low → High)' },
  ];

  dashboardStats = computed<StatStripItem[]>(() => [
    { icon: 'bi-cart-fill', value: this.purchases().length, label: 'Total Orders', iconClass: 'icon-primary', format: 'number' },
    { icon: 'bi-hourglass-split', value: this.pendingCount, label: 'Pending', iconClass: 'icon-warning', format: 'number' },
    { icon: 'bi-truck', value: this.deliveredCount, label: 'Delivered', iconClass: 'icon-accent', format: 'number' },
    { icon: 'bi-check-circle-fill', value: this.confirmedCount, label: 'Confirmed', iconClass: 'icon-success', format: 'number' },
    { icon: 'bi-x-circle-fill', value: this.cancelledCount, label: 'Cancelled', iconClass: 'icon-danger', format: 'number' },
  ]);

  // Supplier search
  selectedSupplierId = 0;
  selectedSupplierName = '';
  supplierSearch = '';
  supplierDropdownOpen = signal(false);

  // Product search & pagination
  productSearch = signal('');
  productVisibleCount = signal(PRODUCT_PAGE_SIZE);
  selectedProductId = 0;
  orderItems: { productId: number; quantity: number }[] = [];

  statusBadge = statusBadge;

  ngOnInit(): void { this.load(); }

  @HostListener('document:click', ['$event'])
  onDocumentClick(e: MouseEvent): void {
    const target = e.target as HTMLElement;
    if (!target.closest('.supplier-search-wrap')) {
      this.supplierDropdownOpen.set(false);
    }
  }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: p => { this.purchases.set(p); this.applyFilter(); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
    this.supplierSvc.getAll().subscribe({ next: s => this.suppliers.set(s) });
  }

  applyFilter(): void {
    const s = this.search.toLowerCase();
    let list = this.purchases().filter(p =>
      (p.supplierName || '').toLowerCase().includes(s) || p.id.toString().includes(s)
    );
    if (this.selectedStatus) list = list.filter(p => p.status === this.selectedStatus);
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

  onPageSize(size: number): void { this.pageSize.set(size); this.page.set(1); }

  // ── SUPPLIER ──
  get filteredSuppliers(): SupplierResponse[] {
    const q = this.supplierSearch.trim().toLowerCase();
    return q ? this.suppliers().filter(s => s.companyName.toLowerCase().includes(q)) : this.suppliers();
  }

  selectSupplier(s: SupplierResponse): void {
    this.selectedSupplierId = s.id;
    this.selectedSupplierName = s.companyName;
    this.supplierSearch = s.companyName;
    this.supplierDropdownOpen.set(false);
    this.productSvc.getBySupplier(s.id).subscribe({ next: p => { this.supProducts.set(p); this.currentStep.set(2); } });
    this.orderItems = [];
    this.productSearch.set('');
    this.productVisibleCount.set(PRODUCT_PAGE_SIZE);
  }

  // ── PRODUCTS ──
  get filteredProducts(): SupplierProductResponse[] {
    const q = this.productSearch().trim().toLowerCase();
    return q ? this.supProducts().filter(p =>
      p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)
    ) : this.supProducts();
  }

  get visibleProducts(): SupplierProductResponse[] {
    return this.filteredProducts.slice(0, this.productVisibleCount());
  }

  get hasMoreProducts(): boolean {
    return this.productVisibleCount() < this.filteredProducts.length;
  }

  onProductSearchChange(value: string): void {
    this.productSearch.set(value);
    this.productVisibleCount.set(PRODUCT_PAGE_SIZE);
  }

  loadMoreProducts(): void { this.productVisibleCount.update(n => n + PRODUCT_PAGE_SIZE); }

  /** One-click add from product row — adds qty 1, or increments if already in cart */
  quickAddItem(productId: number): void {
    const ex = this.orderItems.find(i => i.productId === productId);
    if (ex) {
      ex.quantity += 1;
      this.orderItems = [...this.orderItems];
    } else {
      this.orderItems = [...this.orderItems, { productId, quantity: 1 }];
    }
    if (this.orderItems.length === 1) this.currentStep.set(3);
  }

  selectProduct(id: number): void { this.selectedProductId = id; }
  isAdded(productId: number): boolean { return this.orderItems.some(i => i.productId === productId); }

  removeItem(id: number): void {
    this.orderItems = this.orderItems.filter(i => i.productId !== id);
    if (this.orderItems.length === 0) this.currentStep.set(2);
  }

  updateQty(id: number, qty: number): void {
    if (qty < 1) return;
    this.orderItems = this.orderItems.map(i => i.productId === id ? { ...i, quantity: qty } : i);
  }

  productName(id: number): string { return this.supProducts().find(p => p.id === id)?.name ?? `Product #${id}`; }
  productSku(id: number): string { return this.supProducts().find(p => p.id === id)?.sku ?? ''; }
  productPrice(id: number): number { return this.supProducts().find(p => p.id === id)?.supplierToAdminPrice ?? 0; }

  get orderTotal(): number {
    return this.orderItems.reduce((s, i) => s + i.quantity * this.productPrice(i.productId), 0);
  }

  createOrder(): void {
    if (this.submitting()) return;
    const req: PurchaseRequest = { supplierId: this.selectedSupplierId, items: this.orderItems };
    this.submitting.set(true);
    this.svc.create(req).subscribe({
      next: created => {
        this.submitting.set(false);
        this.toast.success('Purchase order created');
        this.closeCreate();
        this.confirmedOrder.set(created);
        this.load();
      },
      error: () => { this.submitting.set(false); this.toast.error('Failed to create order'); }
    });
  }

  confirm(p: PurchaseResponse): void {
    if (!confirm('Confirm delivery? Stock will be updated.')) return;
    this.svc.confirm(p.id).subscribe({
      next: () => { this.toast.success('Delivery confirmed — inventory updated'); this.load(); },
      error: err => this.toast.error(err?.error?.message ?? 'Failed to confirm')
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

  openCreate(): void {
    this.showCreate.set(true);
    this.selectedSupplierId = 0;
    this.selectedSupplierName = '';
    this.supplierSearch = '';
    this.supplierDropdownOpen.set(false);
    this.orderItems = [];
    this.supProducts.set([]);
    this.productSearch.set('');
    this.productVisibleCount.set(PRODUCT_PAGE_SIZE);
    this.selectedProductId = 0;
    this.currentStep.set(1);
  }

  closeCreate(): void {
    this.showCreate.set(false);
    this.supProducts.set([]);
    this.orderItems = [];
    this.currentStep.set(1);
  }

  dismissConfirmation(): void { this.confirmedOrder.set(null); }

  get pendingCount(): number { return this.purchases().filter(p => p.status === 'PENDING').length; }
  get confirmedCount(): number { return this.purchases().filter(p => p.status === 'CONFIRMED').length; }
  get deliveredCount(): number { return this.purchases().filter(p => p.status === 'DELIVERED').length; }
  get cancelledCount(): number { return this.purchases().filter(p => p.status === 'CANCELED').length; }
  get totalPurchaseAmount(): number { return this.purchases().reduce((sum, p) => sum + p.totalAmount, 0); }
}