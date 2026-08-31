import { Component, ElementRef, OnInit, ViewChild, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';
import { InventoryReportDTO } from 'src/app/features/common/models/report.model';
import { ReportService } from '../../services/report.service';
import { ExportService } from '../../services/export.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

Chart.register(...registerables);

type StockFilter = 'all' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

@Component({
  selector: 'app-inventory-report',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, AppTableComponent, PaginatorComponent, StatStripComponent],
  templateUrl: './inventory-report.component.html',
  styleUrls: ['./inventory-report.component.css']
})
export class InventoryReportComponent implements OnInit {

  all = signal<InventoryReportDTO[]>([]);
  filtered = signal<InventoryReportDTO[]>([]);
  loading = signal(false);
  error = '';

  activeFilter = signal<StockFilter>('all');
  search = '';

  page = signal(1);
  pageSize = signal(10);

  private chart1: Chart | null = null;
  private chart2: Chart | null = null;

  @ViewChild('stockBarChart') stockBarChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusPieChart') statusPieChart!: ElementRef<HTMLCanvasElement>;
  stockChart!: ElementRef<HTMLCanvasElement>;

  filterOptions: { value: StockFilter; label: string }[] = [
    { value: 'all', label: 'All' },
    { value: 'IN_STOCK', label: 'In Stock' },
    { value: 'LOW_STOCK', label: 'Low Stock' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' }
  ];

  constructor(
    private reportService: ReportService,
    private exportService: ExportService
  ) { }

  ngOnInit(): void {
    this.load();
  }

  dashboardStats = computed<StatStripItem[]>(() => {
    const data = this.all();
    const totalValue = data.reduce((s, i) => s + i.stockValue, 0);
    const totalRevenue = data.reduce((s, i) => s + i.revenue, 0);
    const totalSold = data.reduce((s, i) => s + i.soldQty, 0);

    return [
      { icon: 'bi-box-seam-fill', value: data.length, label: 'Total Products', iconClass: 'icon-primary', format: 'number' },
      { icon: 'bi-currency-rupee', value: totalValue, label: 'Stock Value', iconClass: 'icon-success', format: 'currency' },
      { icon: 'bi-graph-up-arrow', value: totalRevenue, label: 'Total Revenue', iconClass: 'icon-accent', format: 'currency' },
      { icon: 'bi-cart-check-fill', value: totalSold, label: 'Units Sold', iconClass: 'icon-danger', format: 'number' },
    ];
  });

  load(): void {
    this.loading.set(true);
    this.error = '';
    this.reportService.getInventoryReport().subscribe({
      next: data => {
        this.all.set(data);
        this.applyFilter('all');
        this.loading.set(false);
        setTimeout(() => this.renderChart(), 150);
      },
      error: () => {
        this.error = 'Failed to load inventory report.';
        this.loading.set(false);
      }
    });
  }

  applyFilter(f: StockFilter): void {
    this.activeFilter.set(f);
    this.applySearch();
  }

  applySearch(): void {
    const s = this.search.toLowerCase();
    const f = this.activeFilter();

    const list = this.all().filter(i => {
      const matchSearch = !s || i.productName.toLowerCase().includes(s) || i.sku.toLowerCase().includes(s);
      const matchFilter = f === 'all' || i.status === f;
      return matchSearch && matchFilter;
    });

    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<InventoryReportDTO> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  get totalStockValue(): number { return this.filtered().reduce((s, i) => s + i.stockValue, 0); }
  get totalRevenue(): number { return this.filtered().reduce((s, i) => s + i.revenue, 0); }
  get totalSoldQty(): number { return this.filtered().reduce((s, i) => s + i.soldQty, 0); }

  getStatusBadge(status: string): { label: string; cls: string } {
    switch (status) {
      case 'IN_STOCK': return { label: 'In Stock', cls: 'badge badge-success' };
      case 'LOW_STOCK': return { label: 'Low Stock', cls: 'badge badge-warning' };
      case 'OUT_OF_STOCK': return { label: 'Out of Stock', cls: 'badge badge-danger' };
      default: return { label: status, cls: 'badge' };
    }
  }

  getRowClass(status: string): string {
    switch (status) {
      case 'OUT_OF_STOCK': return 'row-critical';
      case 'LOW_STOCK': return 'row-low';
      case 'INACTIVE': return 'row-inactive';
      default: return '';
    }
  }

  renderChart(): void {
    this.renderBarChart();
    this.renderPieChart();
  }

  renderBarChart(): void {
    if (!this.stockBarChart) return;
    if (this.chart1) { this.chart1.destroy(); }

    const top10 = [...this.all()]
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 10);

    const palette = ['#2563eb', '#3b82f6', '#60a5fa', '#93c5fd', '#1d4ed8',
      '#1e40af', '#0ea5e9', '#38bdf8', '#0284c7', '#0369a1'];

    this.chart1 = new Chart(this.stockBarChart.nativeElement, {
      type: 'bar',
      data: {
        labels: top10.map(i =>
          i.productName.length > 12 ? i.productName.slice(0, 12) + '…' : i.productName
        ),
        datasets: [
          {
            label: 'Stock Value (₹)',
            data: top10.map(i => i.stockValue),
            backgroundColor: top10.map((_, i) => palette[i % palette.length]),
            borderRadius: 10,
            borderSkipped: false,
            order: 2,
            yAxisID: 'y'
          },
          {
            type: 'line' as const,
            label: 'Units Sold',
            data: top10.map(i => i.soldQty),
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245,158,11,0.12)',
            borderWidth: 3,
            pointRadius: 5,
            pointHoverRadius: 7,
            pointBackgroundColor: '#f59e0b',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            fill: false,
            tension: 0.4,
            yAxisID: 'y2',
            order: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: {
            labels: { color: '#64748b', usePointStyle: true, pointStyle: 'circle' }
          },
          tooltip: {
            callbacks: {
              label: ctx => ctx.datasetIndex === 0
                ? ` Stock Value: ₹${(ctx.parsed.y ?? 0).toLocaleString('en-IN')}`
                : ` Units Sold: ${ctx.parsed.y ?? 0}`
            }
          }
        },
        scales: {
          y: {
            position: 'left',
            beginAtZero: true,
            grid: { color: '#e2e8f0' },
            ticks: {
              color: '#94a3b8',
              callback: v => '₹' + Number(v).toLocaleString('en-IN')
            }
          },
          y2: {
            position: 'right',
            beginAtZero: true,
            grid: { drawOnChartArea: false },
            ticks: { color: '#94a3b8', callback: v => v + ' u' }
          },
          x: {
            grid: { display: false },
            ticks: { color: '#94a3b8', maxRotation: 25 }
          }
        }
      }
    });
  }

  renderPieChart(): void {
    if (!this.statusPieChart) return;
    if (this.chart2) { this.chart2.destroy(); }

    const data = this.all();
    const inStock = data.filter(i => i.status === 'IN_STOCK').length;
    const lowStock = data.filter(i => i.status === 'LOW_STOCK').length;
    const outOfStock = data.filter(i => i.status === 'OUT_OF_STOCK').length;

    this.chart2 = new Chart(this.statusPieChart.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['In Stock', 'Low Stock', 'Out of Stock'],
        datasets: [{
          data: [inStock, lowStock, outOfStock],
          backgroundColor: ['#10b981', '#f59e0b', '#f43f5e'],
          hoverBackgroundColor: ['#059669', '#d97706', '#e11d48'],
          borderColor: '#ffffff',
          borderWidth: 3,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '68%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: '#64748b',
              usePointStyle: true,
              pointStyle: 'circle',
              padding: 16,
              font: { size: 13 }
            }
          },
          tooltip: {
            callbacks: {
              label: ctx => {
                const val = ctx.parsed ?? 0;
                const total = data.length;
                const pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0';
                return ` ${ctx.label}: ${val} products (${pct}%)`;
              }
            }
          }
        }
      }
    });
  }

  exportExcel(): void {
    const data = this.filtered().map(i => ({
      'Product': i.productName,
      'SKU': i.sku,
      'Unit Price (₹)': i.unitPrice.toFixed(2),
      'Units Sold': i.soldQty,
      'Revenue (₹)': i.revenue.toFixed(2),
      'Current Stock': i.currentStock,
      'Stock Value (₹)': i.stockValue.toFixed(2),
      'Status': i.status,
      'Last Updated': new Date(i.lastUpdated).toLocaleDateString()
    }));
    this.exportService.exportExcel(data, 'inventory-report');
  }

  exportPDF(): void {
    const cols = ['Product', 'SKU', 'Unit Price', 'Sold Qty', 'Revenue', 'Stock', 'Stock Value', 'Status'];
    const rows = this.filtered().map(i => [
      i.productName,
      i.sku,
      '₹' + i.unitPrice.toFixed(2),
      i.soldQty.toString(),
      '₹' + i.revenue.toFixed(2),
      i.currentStock.toString(),
      '₹' + i.stockValue.toFixed(2),
      i.status.replace('_', ' ')
    ]);
    this.exportService.exportPDF(cols, rows, 'inventory-report', 'Inventory Report');
  }
}