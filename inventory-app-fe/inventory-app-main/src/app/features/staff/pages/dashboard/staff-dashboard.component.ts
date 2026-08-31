import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { SaleService } from '../../../user/services/sale.service';
import { SaleResponse } from '../../../common/models/sale.model';
import { paymentModeBadge } from '../../../../core/utils/role.util';
import { fadeIn, fadeInList } from '../../../../shared/animations/fade.animation';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';

@Component({
    selector: 'app-staff-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, StatStripComponent, LoaderComponent, AppTableComponent],
    templateUrl: './staff-dashboard.component.html',
    styleUrls: ['./staff-dashboard.component.css'],
    animations: [fadeIn, fadeInList]
})
export class StaffDashboardComponent implements OnInit {
    auth = inject(AuthService);
    private saleSvc = inject(SaleService);

    loading = signal(true);
    recentSales = signal<SaleResponse[]>([]);
    allSales = signal<SaleResponse[]>([]);
    paymentModeBadge = paymentModeBadge;

    totalToday = signal(0);
    revenueToday = signal(0);
    totalAllTime = signal(0);
    avgOrderValue = signal(0);

    /** Formatted date string for the subtitle */
    today = new Date().toLocaleDateString('default', { weekday: 'long', month: 'long', day: 'numeric' });

    /** Greeting based on hour */
    greeting = computed(() => {
        const h = new Date().getHours();
        if (h < 12) return 'Good morning';
        if (h < 17) return 'Good afternoon';
        return 'Good evening';
    });

    /** Top-level KPI strip reusing StatStripComponent */
    staffStats = computed<StatStripItem[]>(() => [
        {
            icon: 'bi-receipt-cutoff',
            value: this.totalToday(),
            label: 'Sales Today',
            iconClass: 'icon-sales',
            format: 'number'
        },
        {
            icon: 'bi-currency-rupee',
            value: this.revenueToday(),
            label: 'Revenue Today',
            iconClass: 'icon-revenue',
            format: 'currency'
        },
        {
            icon: 'bi-bag-check-fill',
            value: this.totalAllTime(),
            label: 'Total Sales (All Time)',
            iconClass: 'icon-products',
            format: 'number'
        },
        {
            icon: 'bi-graph-up-arrow',
            value: this.avgOrderValue(),
            label: 'Avg. Order Value',
            iconClass: 'icon-users',
            format: 'currency'
        }
    ]);

    /** Payment mode breakdown for the progress bars */
    paymentBreakdown = computed(() => {
        const sales = this.allSales();
        if (!sales.length) return [];
        const counts: Record<string, number> = {};
        for (const s of sales) {
            counts[s.paymentMode] = (counts[s.paymentMode] || 0) + 1;
        }
        return Object.entries(counts)
            .map(([mode, count]) => ({ mode, count, pct: Math.round((count / sales.length) * 100) }))
            .sort((a, b) => b.count - a.count);
    });

    /** 7-day mini sparkline data (sale counts per day) */
    last7Days = computed(() => {
        const sales = this.allSales();
        const days: { label: string; count: number; revenue: number }[] = [];
        for (let i = 6; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            const dateStr = d.toDateString();
            const daySales = sales.filter(s => new Date(s.saleDate).toDateString() === dateStr);
            days.push({
                label: i === 0 ? 'Today' : d.toLocaleDateString('default', { weekday: 'short' }),
                count: daySales.length,
                revenue: daySales.reduce((sum, s) => sum + s.totalAmount, 0)
            });
        }
        return days;
    });

    maxDayRevenue = computed(() =>
        Math.max(...this.last7Days().map(d => d.revenue), 1)
    );

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (!user) return;

        this.saleSvc.getBySoldBy(user.id).subscribe({
            next: sales => {
                this.allSales.set(sales);
                this.totalAllTime.set(sales.length);
                this.recentSales.set([...sales].reverse().slice(0, 8));

                const today = new Date().toDateString();
                const todaySales = sales.filter(s => new Date(s.saleDate).toDateString() === today);
                this.totalToday.set(todaySales.length);
                this.revenueToday.set(todaySales.reduce((sum, s) => sum + s.totalAmount, 0));
                this.avgOrderValue.set(
                    sales.length ? Math.round(sales.reduce((sum, s) => sum + s.totalAmount, 0) / sales.length) : 0
                );
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }
}