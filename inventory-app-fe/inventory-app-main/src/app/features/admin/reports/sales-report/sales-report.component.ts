import { Component, ElementRef, OnInit, ViewChild, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';
import { SalesReportDTO } from 'src/app/features/common/models/report.model';
import { ReportService } from '../../services/report.service';
import { ExportService } from '../../services/export.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

Chart.register(...registerables);

type FilterPreset = 'all' | 'today' | 'yesterday' | 'week' | 'month' | 'custom';

@Component({
  selector: 'app-sales-report',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, AppTableComponent, PaginatorComponent, StatStripComponent],
  templateUrl: './sales-report.component.html',
  styleUrls: ['./sales-report.component.css']
})
export class SalesReportComponent implements OnInit {

  // ── all data as a signal so computed() reacts ──
  rows = signal<SalesReportDTO[]>([]);
  filtered = signal<SalesReportDTO[]>([]);
  loading = signal(false);
  error = '';

  activePreset: FilterPreset = 'month';
  startDate = '';
  endDate = '';
  search = '';

  page = signal(1);
  pageSize = signal(10);

  presetOptions: { value: FilterPreset; label: string }[] = [
    { value: 'all', label: 'All' },
    { value: 'today', label: 'Today' },
    { value: 'yesterday', label: 'Yesterday' },
    { value: 'week', label: 'This Week' },
    { value: 'month', label: 'This Month' },
    { value: 'custom', label: 'Custom' }
  ];

  private chart: Chart | null = null;

  @ViewChild('salesChart')
  salesChart!: ElementRef<HTMLCanvasElement>;

  constructor(
    private reportService: ReportService,
    private exportService: ExportService
  ) { }

  ngOnInit(): void {
    this.applyPreset('month');
  }

  // ── computed stats — reactive because rows() is a signal ──
  dashboardStats = computed<StatStripItem[]>(() => {
    const data = this.rows();
    const revenue = data.reduce((s, r) => s + r.revenue, 0);
    const profit = data.reduce((s, r) => s + r.profit, 0);
    const cost = data.reduce((s, r) => s + r.cost, 0);
    const orders = data.reduce((s, r) => s + r.totalOrders, 0);
    const avg = orders > 0 ? revenue / orders : 0;
    const margin = revenue > 0 ? (profit / revenue) * 100 : 0;

    return [
      { icon: 'bi-cart-fill', value: orders, label: 'Total Orders', iconClass: 'icon-primary', format: 'number' },
      { icon: 'bi-currency-rupee', value: revenue, label: 'Total Revenue', iconClass: 'icon-success', format: 'currency' },
      { icon: 'bi-graph-up-arrow', value: profit, label: 'Est. Profit', iconClass: 'icon-accent', format: 'currency' },
      { icon: 'bi-box-seam', value: cost, label: 'Total Cost', iconClass: 'icon-danger', format: 'currency' },
      { icon: 'bi-percent', value: margin, label: 'Avg. Margin', iconClass: 'icon-warning', format: 'percent' },
    ];
  });

  applyPreset(preset: FilterPreset): void {
    this.activePreset = preset;
    const today = new Date();
    const fmt = (d: Date) => d.toISOString().split('T')[0];

    switch (preset) {
      case 'all':
        this.startDate = '';
        this.endDate = '';
        break;
      case 'today':
        this.startDate = fmt(today);
        this.endDate = fmt(today);
        break;
      case 'yesterday': {
        const y = new Date(today);
        y.setDate(y.getDate() - 1);
        this.startDate = fmt(y);
        this.endDate = fmt(y);
        break;
      }
      case 'week': {
        const w = new Date(today);
        w.setDate(w.getDate() - 6);
        this.startDate = fmt(w);
        this.endDate = fmt(today);
        break;
      }
      case 'month': {
        const m = new Date(today.getFullYear(), today.getMonth(), 1);
        this.startDate = fmt(m);
        this.endDate = fmt(today);
        break;
      }
      case 'custom':
        return;
    }
    this.generate();
  }

  generate(): void {
    if (this.activePreset !== 'all' && (!this.startDate || !this.endDate)) {
      this.error = 'Please select a date range.';
      return;
    }
    this.loading.set(true);
    this.error = '';

    const start = this.startDate || undefined;
    const end = this.endDate || undefined;

    this.reportService.getSalesReport(start, end).subscribe({
      next: data => {
        this.rows.set(data);          // signal update → computed re-runs
        this.applyFilter();
        this.loading.set(false);
        setTimeout(() => this.renderChart(), 150);
      },
      error: () => {
        this.error = 'Failed to load report.';
        this.loading.set(false);
      }
    });
  }

  applyFilter(): void {
    const s = this.search.toLowerCase();
    const list = this.rows().filter(r => r.label.toLowerCase().includes(s));
    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<SalesReportDTO> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  // ── totals for table footer — read from signal ──
  get totalRevenue(): number { return this.rows().reduce((s, r) => s + r.revenue, 0); }
  get totalProfit(): number { return this.rows().reduce((s, r) => s + r.profit, 0); }
  get totalCost(): number { return this.rows().reduce((s, r) => s + r.cost, 0); }
  get totalOrders(): number { return this.rows().reduce((s, r) => s + r.totalOrders, 0); }
  get avgOrderValue(): number { return this.totalOrders > 0 ? this.totalRevenue / this.totalOrders : 0; }

  renderChart(): void {
    if (!this.salesChart) return;
    if (this.chart) { this.chart.destroy(); }

    this.chart = new Chart(this.salesChart.nativeElement, {
      type: 'bar',
      data: {
        labels: this.rows().map(r => r.label),
        datasets: [
          {
            label: 'Revenue (₹)',
            data: this.rows().map(r => r.revenue),
            backgroundColor: 'rgba(67,97,238,0.8)',
            borderRadius: 4
          },
          {
            label: 'Cost (₹)',
            data: this.rows().map(r => r.cost),
            backgroundColor: 'rgba(239,68,68,0.7)',
            borderRadius: 4
          },
          {
            label: 'Profit (₹)',
            data: this.rows().map(r => r.profit),
            backgroundColor: 'rgba(39,174,96,0.8)',
            borderRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'top' } },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  exportExcel(): void {
    const data = this.rows().map(r => ({
      Date: r.label,
      'Total Orders': r.totalOrders,
      'Revenue (₹)': r.revenue.toFixed(2),
      'Cost (₹)': r.cost.toFixed(2),
      'Profit (₹)': r.profit.toFixed(2),
      'Margin (%)': r.marginPercent.toFixed(1)
    }));
    this.exportService.exportExcel(data, `sales-report-${this.startDate || 'all'}-to-${this.endDate || 'all'}`);
  }

  exportPDF(): void {
    const cols = ['Date', 'Orders', 'Revenue (₹)', 'Cost (₹)', 'Profit (₹)', 'Margin'];
    const rows = this.rows().map(r => [
      r.label,
      r.totalOrders.toString(),
      '₹' + r.revenue.toFixed(2),
      '₹' + r.cost.toFixed(2),
      '₹' + r.profit.toFixed(2),
      r.marginPercent.toFixed(1) + '%'
    ]);
    this.exportService.exportPDF(cols, rows, `sales-report`, 'Sales Report');
  }
}