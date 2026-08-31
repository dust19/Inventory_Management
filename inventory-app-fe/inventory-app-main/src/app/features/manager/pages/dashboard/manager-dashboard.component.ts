import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { SaleService } from '../../../user/services/sale.service';
import { PurchaseService } from '../../../admin/services/purchase.service';
import { StaffService } from '../../../admin/services/staff.service';
import { ManagerService } from '../../../admin/services/manager.service';
import { SaleResponse } from '../../../common/models/sale.model';
import { StaffResponse } from '../../../common/models/staff.model';
import { PurchaseResponse } from '../../../common/models/purchase.model';
import { fadeIn, fadeInList } from '../../../../shared/animations/fade.animation';
import { paymentModeBadge } from '../../../../core/utils/role.util';
import { initials, } from 'src/app/core/utils/role.util';
import { getAvatarGradient } from 'src/app/core/utils/avatar.util';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';

@Component({
    selector: 'app-manager-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, StatStripComponent, LoaderComponent, AppTableComponent],
    templateUrl: './manager-dashboard.component.html',
    styleUrls: ['./manager-dashboard.component.css'],
    animations: [fadeIn, fadeInList]
})
export class ManagerDashboardComponent implements OnInit {
    auth = inject(AuthService);
    private saleSvc = inject(SaleService);
    private purchaseSvc = inject(PurchaseService);
    private staffSvc = inject(StaffService);
    private managerSvc = inject(ManagerService);

    loading = signal(true);

    staffList = signal<StaffResponse[]>([]);
    pendingDeliveries = signal<PurchaseResponse[]>([]);
    recentSales = signal<SaleResponse[]>([]);
    allSales = signal<SaleResponse[]>([]);

    totalSalesToday = signal(0);
    totalRevenueToday = signal(0);
    totalRevenueAllTime = signal(0);

    initials = initials;
    getAvatarGradient = getAvatarGradient;
    paymentModeBadge = paymentModeBadge;

    today = new Date().toLocaleDateString('default', { weekday: 'long', month: 'long', day: 'numeric' });

    greeting = computed(() => {
        const h = new Date().getHours();
        if (h < 12) return 'Good morning';
        if (h < 17) return 'Good afternoon';
        return 'Good evening';
    });

    /** KPI strip */
    managerStats = computed<StatStripItem[]>(() => [
        {
            icon: 'bi-people-fill',
            value: this.staffList().length,
            label: 'My Staff',
            iconClass: 'icon-users',
            format: 'number'
        },
        {
            icon: 'bi-truck',
            value: this.pendingDeliveries().length,
            label: 'Pending Confirmations',
            iconClass: 'icon-lowstock',
            format: 'number'
        },
        {
            icon: 'bi-receipt-cutoff',
            value: this.totalSalesToday(),
            label: 'Sales Today',
            iconClass: 'icon-sales',
            format: 'number'
        },
        {
            icon: 'bi-graph-up-arrow',
            value: this.totalRevenueAllTime(),
            label: 'Total Revenue',
            iconClass: 'icon-products',
            format: 'currency'
        }
    ]);

    /** Top performer among staff — the one with most sales in allSales */
    topPerformers = computed(() => {
        const sales = this.allSales();
        const staff = this.staffList();
        if (!sales.length || !staff.length) return [];

        const countMap: Record<string, { name: string; count: number; revenue: number }> = {};
        for (const s of sales) {
            const key = s.soldByName || 'Unknown';
            if (!countMap[key]) countMap[key] = { name: key, count: 0, revenue: 0 };
            countMap[key].count++;
            countMap[key].revenue += s.totalAmount;
        }
        return Object.values(countMap)
            .sort((a, b) => b.revenue - a.revenue)
            .slice(0, 5);
    });

    maxPerformerRevenue = computed(() =>
        Math.max(...this.topPerformers().map(p => p.revenue), 1)
    );

    /** 7-day daily sale counts for the mini chart */
    last7Days = computed(() => {
        const sales = this.allSales();
        return Array.from({ length: 7 }, (_, i) => {
            const d = new Date();
            d.setDate(d.getDate() - (6 - i));
            const dateStr = d.toDateString();
            const daySales = sales.filter(s => new Date(s.saleDate).toDateString() === dateStr);
            return {
                label: i === 6 ? 'Today' : d.toLocaleDateString('default', { weekday: 'short' }),
                count: daySales.length,
                revenue: daySales.reduce((sum, s) => sum + s.totalAmount, 0)
            };
        });
    });

    maxDayRevenue = computed(() =>
        Math.max(...this.last7Days().map(d => d.revenue), 1)
    );

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (!user) return;

        // Load staff via manager profile
        this.managerSvc.getByUser(user.id).pipe(catchError(() => of(null))).subscribe(m => {
            if (m) {
                this.staffSvc.getByManager(m.id).pipe(catchError(() => of([]))).subscribe(s => {
                    this.staffList.set(s);
                });
            }
        });

        // Load purchases + sales in parallel
        forkJoin({
            purchases: this.purchaseSvc.getByStatus('DELIVERED').pipe(catchError(() => of([]))),
            sales: this.saleSvc.getAll().pipe(catchError(() => of([])))
        }).subscribe(({ purchases, sales }) => {
            this.pendingDeliveries.set(purchases as PurchaseResponse[]);

            const allSales = sales as SaleResponse[];
            this.allSales.set(allSales);
            this.recentSales.set([...allSales].reverse().slice(0, 5));

            const todayStr = new Date().toDateString();
            const todaySales = allSales.filter(s => new Date(s.saleDate).toDateString() === todayStr);
            this.totalSalesToday.set(todaySales.length);
            this.totalRevenueToday.set(todaySales.reduce((sum, s) => sum + s.totalAmount, 0));
            this.totalRevenueAllTime.set(allSales.reduce((sum, s) => sum + s.totalAmount, 0));

            this.loading.set(false);
        });
    }
}