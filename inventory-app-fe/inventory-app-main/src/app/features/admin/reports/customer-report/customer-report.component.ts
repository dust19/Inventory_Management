import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerReportDTO } from 'src/app/features/common/models/report.model';
import { ReportService } from '../../services/report.service';
import { ExportService } from '../../services/export.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';

type SortBy = 'spent' | 'orders' | 'recent';

@Component({
  selector: 'app-customer-report',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, PaginatorComponent],
  templateUrl: './customer-report.component.html',
  styleUrls: ['./customer-report.component.css']
})
export class CustomerReportComponent implements OnInit {

  all = signal<CustomerReportDTO[]>([]);
  filtered = signal<CustomerReportDTO[]>([]);
  loading = signal(false);
  error = '';

  sortBy: SortBy = 'spent';
  search = '';

  page = signal(1);
  pageSize = signal(10);

  sortOptions: { value: SortBy; label: string; icon: string }[] = [
    { value: 'spent', label: 'Highest Spend', icon: 'bi-currency-rupee' },
    { value: 'orders', label: 'Most Orders', icon: 'bi-cart-fill' },
    { value: 'recent', label: 'Most Recent', icon: 'bi-clock-history' }
  ];

  constructor(
    private reportService: ReportService,
    private exportService: ExportService
  ) { }

  ngOnInit(): void { this.load(); }

  topCustomer = computed(() => {
    const data = this.all();
    return data.length > 0 ? [...data].sort((a, b) => b.totalSpent - a.totalSpent)[0] : null;
  });

  tierBreakdown = computed(() => {
    const data = this.all();
    const total = data.length || 1;
    const buckets = [
      { key: 'platinum', label: 'Platinum', cls: 'tier-platinum', min: 50000 },
      { key: 'gold', label: 'Gold', cls: 'tier-gold', min: 10000 },
      { key: 'silver', label: 'Silver', cls: 'tier-silver', min: 2000 },
      { key: 'bronze', label: 'Bronze', cls: 'tier-bronze', min: 0 },
    ];

    return buckets.map((b, idx) => {
      const max = idx === 0 ? Infinity : buckets[idx - 1].min;
      const count = data.filter(c => c.totalSpent >= b.min && c.totalSpent < max).length;
      return { ...b, count, pct: Math.round((count / total) * 100) };
    });
  });

  load(): void {
    this.loading.set(true);
    this.reportService.getCustomerReport().subscribe({
      next: data => {
        this.all.set(data);
        this.applySort('spent');
        this.loading.set(false);
      },
      error: () => { this.error = 'Failed to load.'; this.loading.set(false); }
    });
  }

  applySort(sort: SortBy): void {
    this.sortBy = sort;
    let result = [...this.all()];
    if (sort === 'spent') result.sort((a, b) => b.totalSpent - a.totalSpent);
    else if (sort === 'orders') result.sort((a, b) => b.totalOrders - a.totalOrders);
    else if (sort === 'recent') result.sort((a, b) => new Date(b.lastPurchase).getTime() - new Date(a.lastPurchase).getTime());
    this.applySearch(result);
  }

  onSearchChange(value: string): void {
    this.search = value;
    this.applySort(this.sortBy);
  }

  private applySearch(base: CustomerReportDTO[]): void {
    const term = this.search.toLowerCase();
    const list = base.filter(c =>
      c.name.toLowerCase().includes(term) || c.email.toLowerCase().includes(term)
    );
    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<CustomerReportDTO> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  globalRank(indexInPage: number): number {
    return (this.paged.currentPage - 1) * this.paged.pageSize + indexInPage + 1;
  }

  initials(name: string): string {
    return name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }

  avatarColor(name: string): string {
    const colors = ['av-teal', 'av-indigo', 'av-rose', 'av-amber', 'av-emerald', 'av-violet', 'av-sky', 'av-orange'];
    const hash = name.split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    return colors[hash % colors.length];
  }

  getCustomerTier(spent: number): { label: string; cls: string } {
    if (spent >= 50000) return { label: 'Platinum', cls: 'tier-platinum' };
    if (spent >= 10000) return { label: 'Gold', cls: 'tier-gold' };
    if (spent >= 2000) return { label: 'Silver', cls: 'tier-silver' };
    return { label: 'Bronze', cls: 'tier-bronze' };
  }

  tierIcon(clsOrKey: string): string {
    if (clsOrKey.includes('platinum')) return 'bi-gem';
    if (clsOrKey.includes('gold')) return 'bi-star-fill';
    if (clsOrKey.includes('silver')) return 'bi-star-half';
    return 'bi-circle-fill';
  }

  exportExcel(): void {
    const data = this.filtered().map((c, i) => ({
      '#': i + 1,
      Customer: c.name,
      Email: c.email,
      'Total Orders': c.totalOrders,
      'Total Spent (₹)': c.totalSpent.toFixed(2),
      'Last Purchase': c.lastPurchase || '—'
    }));
    this.exportService.exportExcel(data, 'customer-report');
  }

  exportPDF(): void {
    const cols = ['#', 'Customer', 'Email', 'Orders', 'Total Spent', 'Last Purchase'];
    const rows = this.filtered().map((c, i) => [
      (i + 1).toString(), c.name, c.email,
      c.totalOrders.toString(),
      '₹' + c.totalSpent.toFixed(2),
      c.lastPurchase || '—'
    ]);
    this.exportService.exportPDF(cols, rows, 'customer-report', 'Customer Report');
  }

  get totalRevenue(): number { return this.all().reduce((s, c) => s + c.totalSpent, 0); }
  get totalOrders(): number { return this.all().reduce((s, c) => s + c.totalOrders, 0); }
}