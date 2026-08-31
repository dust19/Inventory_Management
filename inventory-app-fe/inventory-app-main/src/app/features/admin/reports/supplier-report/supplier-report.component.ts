import { Component, ElementRef, OnInit, ViewChild, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';
import { SupplierReportDTO } from 'src/app/features/common/models/report.model';
import { ReportService } from '../../services/report.service';
import { ExportService } from '../../services/export.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

Chart.register(...registerables);

@Component({
  selector: 'app-supplier-report',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, AppTableComponent, PaginatorComponent, StatStripComponent],
  templateUrl: './supplier-report.component.html',
  styleUrls: ['./supplier-report.component.css']
})
export class SupplierReportComponent implements OnInit {

  all = signal<SupplierReportDTO[]>([]);
  filtered = signal<SupplierReportDTO[]>([]);
  loading = signal(false);
  error = '';
  search = '';

  page = signal(1);
  pageSize = signal(10);

  private chart: Chart | null = null;

  @ViewChild('supplierChart') supplierChart!: ElementRef<HTMLCanvasElement>;

  constructor(
    private reportService: ReportService,
    private exportService: ExportService
  ) { }

  ngOnInit(): void {
    this.load();
  }

  dashboardStats = computed<StatStripItem[]>(() => {
    const data = this.all();
    const totalPurchase = data.reduce((s, r) => s + r.totalPurchaseValue, 0);
    const totalStock = data.reduce((s, r) => s + r.totalStockSupplied, 0);
    const totalProducts = data.reduce((s, r) => s + r.totalProducts, 0);
    const avgPerSupplier = data.length > 0 ? totalPurchase / data.length : 0;

    return [
      { icon: 'bi-building', value: data.length, label: 'Total Suppliers', iconClass: 'icon-primary', format: 'number' },
      { icon: 'bi-tag-fill', value: totalProducts, label: 'Products Listed', iconClass: 'icon-danger', format: 'number' },
      { icon: 'bi-boxes', value: totalStock, label: 'Units Supplied', iconClass: 'icon-accent', format: 'number' },
      { icon: 'bi-currency-rupee', value: totalPurchase, label: 'Total Purchase Value', iconClass: 'icon-success', format: 'currency' },
      { icon: 'bi-bar-chart-fill', value: avgPerSupplier, label: 'Avg. per Supplier', iconClass: 'icon-warning', format: 'currency' },
    ];
  });

  load(): void {
    this.loading.set(true);
    this.error = '';
    this.reportService.getSupplierReport().subscribe({
      next: data => {
        this.all.set(data);
        this.applySearch();
        this.loading.set(false);
        setTimeout(() => this.renderChart(), 150);
      },
      error: () => {
        this.error = 'Failed to load supplier report.';
        this.loading.set(false);
      }
    });
  }

  applySearch(): void {
    const s = this.search.toLowerCase();
    const list = this.all().filter(r =>
      !s || r.companyName.toLowerCase().includes(s)
    );
    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<SupplierReportDTO> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(1);
  }

  get totalPurchaseValue(): number { return this.filtered().reduce((s, r) => s + r.totalPurchaseValue, 0); }
  get totalStockSupplied(): number { return this.filtered().reduce((s, r) => s + r.totalStockSupplied, 0); }
  get totalProducts(): number { return this.filtered().reduce((s, r) => s + r.totalProducts, 0); }

  renderChart(): void {
    if (!this.supplierChart) return;
    if (this.chart) { this.chart.destroy(); }

    const data = this.all();
    const palette = ['#2563eb', '#7c3aed', '#0ea5e9', '#10b981'];

    this.chart = new Chart(this.supplierChart.nativeElement, {
      type: 'bar',
      data: {
        labels: data.map(s => s.companyName),
        datasets: [
          {
            label: 'Purchase Value (₹)',
            data: data.map(s => s.totalPurchaseValue),
            backgroundColor: data.map((_, i) => palette[i % palette.length]),
            borderRadius: 10,
            borderSkipped: false,
            order: 2,
            yAxisID: 'y'
          },
          {
            type: 'line' as const,
            label: 'Units Supplied',
            data: data.map(s => s.totalStockSupplied),
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245,158,11,0.12)',
            borderWidth: 3,
            pointRadius: 6,
            pointHoverRadius: 8,
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
                ? ` Purchase Value: ₹${(ctx.parsed.y ?? 0).toLocaleString('en-IN')}`
                : ` Units Supplied: ${ctx.parsed.y ?? 0}`
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
            ticks: { color: '#94a3b8', maxRotation: 0 }
          }
        }
      }
    });
  }

  exportExcel(): void {
    const data = this.filtered().map(s => ({
      'Supplier': s.companyName,
      'Total Products': s.totalProducts,
      'Units Supplied': s.totalStockSupplied,
      'Purchase Value (₹)': s.totalPurchaseValue.toFixed(2),
      'Avg. Product Value': (s.totalProducts > 0 ? s.totalPurchaseValue / s.totalProducts : 0).toFixed(2)
    }));
    this.exportService.exportExcel(data, 'supplier-report');
  }

  exportPDF(): void {
    const cols = ['Supplier', 'Products', 'Units Supplied', 'Purchase Value (₹)', 'Avg. Value'];
    const rows = this.filtered().map(s => [
      s.companyName,
      s.totalProducts.toString(),
      s.totalStockSupplied.toString(),
      '₹' + s.totalPurchaseValue.toFixed(2),
      '₹' + (s.totalProducts > 0 ? s.totalPurchaseValue / s.totalProducts : 0).toFixed(2)
    ]);
    this.exportService.exportPDF(cols, rows, 'supplier-report', 'Supplier Report');
  }
}