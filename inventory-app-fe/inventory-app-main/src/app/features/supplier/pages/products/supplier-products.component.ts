import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupplierService } from '../../../admin/services/supplier.service';
import { SupplierProductService } from '../../services/supplier-product.service';
import { ToastService } from '../../../../shared/components/toast/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { SupplierProductResponse, ProductRequest } from '../../../common/models/product.model';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { ViewDetailModalComponent, ViewField } from 'src/app/shared/components/view-detail-modal/view-detail-modal.component';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

@Component({
  selector: 'app-supplier-products',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LoaderComponent,
    StatStripComponent,
    AppTableComponent,
    PaginatorComponent,
    ViewDetailModalComponent
  ],
  templateUrl: './supplier-products.component.html',
  styleUrls: ['./supplier-products.component.css'],
  animations: [fadeIn]
})
export class SupplierProductsComponent implements OnInit {
  private auth = inject(AuthService);
  private supplierSvc = inject(SupplierService);
  private productSvc = inject(SupplierProductService);
  private toast = inject(ToastService);

  products = signal<SupplierProductResponse[]>([]);
  filtered = signal<SupplierProductResponse[]>([]);
  loading = signal(true);
  supplierId = signal(0);

  search = '';
  selectedStatus = '';
  selectedSort = '';

  statusFilterOptions = [
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
  ];

  sortOptions = [
    { value: 'name_asc', label: 'Name (A-Z)' },
    { value: 'name_desc', label: 'Name (Z-A)' },
    { value: 'price_asc', label: 'Price (Low to High)' },
    { value: 'price_desc', label: 'Price (High to Low)' },
    { value: 'stock_desc', label: 'Stock (High to Low)' },
    { value: 'stock_asc', label: 'Stock (Low to High)' },
  ];

  page = signal(1);
  pageSize = signal(10);

  showCreate = signal(false);
  stockTarget = signal<SupplierProductResponse | null>(null);
  priceTarget = signal<SupplierProductResponse | null>(null);
  showViewModal = signal(false);
  selectedProduct: SupplierProductResponse | null = null;

  addQty = 1;
  newPrice = 0;

  createForm: ProductRequest = {
    name: '',
    description: '',
    supplierToAdminPrice: 0,
    quantity: 0,
    supplierId: 0,
    sku: ''
  };

  // ── Computed stats ────────────────────────────────────────────
  dashboardStats = computed<StatStripItem[]>(() => [
    { icon: 'bi-box-seam-fill', value: this.products().length, label: 'Total Products', iconClass: 'icon-primary', format: 'number' },
    { icon: 'bi-check-circle-fill', value: this.products().filter(p => p.active).length, label: 'Active', iconClass: 'icon-success', format: 'number' },
    { icon: 'bi-exclamation-triangle-fill', value: this.products().filter(p => p.availableStock <= 10).length, label: 'Low Stock', iconClass: 'icon-danger', format: 'number' },
    { icon: 'bi-boxes', value: this.products().reduce((s, p) => s + p.availableStock, 0), label: 'Total Units', iconClass: 'icon-accent', format: 'number' },
  ]);

  // ── View detail fields ────────────────────────────────────────
  viewFields: ViewField[] = [
    { key: 'name', label: 'Product Name', width: 'half' },
    { key: 'sku', label: 'SKU', width: 'half' },
    { key: 'supplierToAdminPrice', label: 'Price to Admin', width: 'half' },
    { key: 'availableStock', label: 'Available Stock', width: 'half' },
    { key: 'active', label: 'Status', width: 'half' },
    { key: 'description', label: 'Description', width: 'full' },
    { key: 'createdAt', label: 'Created At', width: 'half' },
  ];

  ngOnInit(): void {
    const user = this.auth.currentUser();
    if (!user) return;
    this.supplierSvc.getByUser(user.id).subscribe({
      next: s => { this.supplierId.set(s.id); this.load(); },
      error: () => this.loading.set(false)
    });
  }

  load(): void {
    this.loading.set(true);
    this.productSvc.getBySupplier(this.supplierId()).subscribe({
      next: p => {
        this.products.set(p);
        this.filter();
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  filter(): void {
    const s = this.search.toLowerCase();
    let list = this.products().filter(p =>
      p.name.toLowerCase().includes(s) ||
      p.sku.toLowerCase().includes(s) ||
      (p.description ?? '').toLowerCase().includes(s)
    );

    if (this.selectedStatus) {
      const active = this.selectedStatus === 'active';
      list = list.filter(p => p.active === active);
    }

    if (this.selectedSort) {
      list = [...list].sort((a, b) => {
        switch (this.selectedSort) {
          case 'name_asc': return a.name.localeCompare(b.name);
          case 'name_desc': return b.name.localeCompare(a.name);
          case 'price_asc': return (a.supplierToAdminPrice || 0) - (b.supplierToAdminPrice || 0);
          case 'price_desc': return (b.supplierToAdminPrice || 0) - (a.supplierToAdminPrice || 0);
          case 'stock_desc': return b.availableStock - a.availableStock;
          case 'stock_asc': return a.availableStock - b.availableStock;
          default: return 0;
        }
      });
    }

    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<SupplierProductResponse> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  // ── Create ────────────────────────────────────────────────────
  openCreate(): void {
    this.createForm = {
      name: '',
      description: '',
      supplierToAdminPrice: 0,
      quantity: 0,
      supplierId: this.supplierId(),
      sku: ''
    };
    this.showCreate.set(true);
  }
  closeCreate(): void { this.showCreate.set(false); }

  create(): void {
    this.productSvc.create(this.createForm).subscribe({
      next: () => { this.toast.success('Product created'); this.closeCreate(); this.load(); },
      error: () => this.toast.error('Create failed')
    });
  }

  // ── View ──────────────────────────────────────────────────────
  viewProduct(p: SupplierProductResponse): void {
    this.selectedProduct = p;
    this.showViewModal.set(true);
  }
  closeViewDetailModal(): void {
    this.showViewModal.set(false);
    this.selectedProduct = null;
  }

  // ── Stock ─────────────────────────────────────────────────────
  openAddStock(p: SupplierProductResponse): void { this.stockTarget.set(p); this.addQty = 1; }
  closeStock(): void { this.stockTarget.set(null); }

  addStock(): void {
    const t = this.stockTarget();
    if (!t || this.addQty < 1) { this.toast.warning('Enter a valid quantity'); return; }
    this.productSvc.addStock(t.id, this.addQty).subscribe({
      next: () => { this.toast.success(`Added ${this.addQty} units`); this.closeStock(); this.load(); },
      error: () => this.toast.error('Failed to add stock')
    });
  }

  // ── Price ─────────────────────────────────────────────────────
  openEditPrice(p: SupplierProductResponse): void { this.priceTarget.set(p); this.newPrice = p.supplierToAdminPrice; }
  closePrice(): void { this.priceTarget.set(null); }

  updatePrice(): void {
    const t = this.priceTarget();
    if (!t || this.newPrice <= 0) { this.toast.warning('Enter a valid price'); return; }
    this.productSvc.updatePrice(t.id, this.newPrice).subscribe({
      next: () => { this.toast.success('Price updated'); this.closePrice(); this.load(); },
      error: () => this.toast.error('Failed to update price')
    });
  }
}