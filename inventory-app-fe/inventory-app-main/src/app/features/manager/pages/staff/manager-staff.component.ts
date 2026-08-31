import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { StaffService } from '../../../admin/services/staff.service';
import { ManagerService } from '../../../admin/services/manager.service';
import { SaleService } from '../../../user/services/sale.service';
import { StaffResponse } from '../../../common/models/staff.model';
import { SaleResponse } from '../../../common/models/sale.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';
import { paymentModeBadge } from '../../../../core/utils/role.util';

type DateRange = 'today' | 'last_week' | 'custom';

@Component({
    selector: 'app-manager-staff',
    standalone: true,
    imports: [CommonModule, FormsModule, LoaderComponent, StatStripComponent],
    templateUrl: './manager-staff.component.html',
    styleUrls: ['./manager-staff.component.css'],
    animations: [fadeIn]
})
export class ManagerStaffComponent implements OnInit {
    private auth = inject(AuthService);
    private staffSvc = inject(StaffService);
    private managerSvc = inject(ManagerService);
    private saleSvc = inject(SaleService);

    staffList = signal<StaffResponse[]>([]);
    salesCount = signal<Record<number, number>>({});
    allSalesByStaff = signal<Record<number, SaleResponse[]>>({});
    loading = signal(true);
    error = signal('');

    selectedStaff = signal<StaffResponse | null>(null);
    dateRange = signal<DateRange>('today');
    customFrom = '';
    customTo = '';
    paymentModeBadge = paymentModeBadge;

    dashboardStats = computed<StatStripItem[]>(() => [
        { icon: 'bi-people-fill', value: this.staffList().length, label: 'Assigned staff', iconClass: 'icon-primary', format: 'number' },
        { icon: 'bi-person-check-fill', value: this.activeCount, label: 'Active', iconClass: 'icon-success', format: 'number' },
        { icon: 'bi-person-x-fill', value: this.inactiveCount, label: 'Inactive', iconClass: 'icon-danger', format: 'number' },
    ]);

    filteredSales = computed<SaleResponse[]>(() => {
        const staff = this.selectedStaff();
        if (!staff) return [];

        const all = this.allSalesByStaff()[staff.id] ?? [];
        const range = this.dateRange();
        const now = new Date();

        if (range === 'today') {
            return all.filter(s => {
                const d = new Date(s.saleDate ?? '');
                return d.getFullYear() === now.getFullYear()
                    && d.getMonth() === now.getMonth()
                    && d.getDate() === now.getDate();
            });
        }

        if (range === 'last_week') {
            const from = new Date(now);
            from.setDate(now.getDate() - 7);
            return all.filter(s => new Date(s.saleDate ?? '') >= from);
        }

        if (range === 'custom' && this.customFrom && this.customTo) {
            const from = new Date(this.customFrom);
            const to = new Date(this.customTo);
            to.setHours(23, 59, 59, 999);
            return all.filter(s => {
                const d = new Date(s.saleDate ?? '');
                return d >= from && d <= to;
            });
        }

        return all;
    });

    popupRevenue = computed(() =>
        this.filteredSales().reduce((sum, s) => sum + s.totalAmount, 0)
    );

    /** Percentage of total sales this staff member holds in the selected range. */
    staffSharePct = computed(() => {
        const staff = this.selectedStaff();
        if (!staff) return 0;
        const total = Object.values(this.allSalesByStaff())
            .reduce((s, arr) => s + arr.length, 0);
        return total === 0 ? 0 : Math.round((this.filteredSales().length / total) * 100);
    });

    ngOnInit(): void {
        const user = this.auth.currentUser();

        if (!user) {
            this.auth.fetchMe().pipe(
                switchMap(u => this.managerSvc.getByUser(u.id)),
                switchMap(m => this.staffSvc.getByManager(m.id)),
                catchError(() => {
                    this.error.set('Could not load staff. Make sure your manager profile is set up.');
                    this.loading.set(false);
                    return of([]);
                })
            ).subscribe(staff => this.handleStaff(staff as StaffResponse[]));
            return;
        }

        this.managerSvc.getByUser(user.id).pipe(
            switchMap(m => this.staffSvc.getByManager(m.id)),
            catchError(() => {
                this.error.set('Could not load staff. Make sure your manager profile is set up.');
                this.loading.set(false);
                return of([]);
            })
        ).subscribe(staff => this.handleStaff(staff));
    }

    private handleStaff(staff: StaffResponse[]): void {
        this.staffList.set(staff);
        this.loading.set(false);

        staff.forEach(s => {
            this.saleSvc.getBySoldBy(s.userId)
                .pipe(catchError(() => of([])))
                .subscribe(sales => {
                    this.salesCount.update(c => ({ ...c, [s.id]: sales.length }));
                    this.allSalesByStaff.update(m => ({ ...m, [s.id]: sales }));
                });
        });
    }

    /** Returns initials (up to 2 chars) from a name. */
    initials(name: string): string {
        return name.trim().slice(0, 2).toUpperCase();
    }

    /**
     * Returns a colour key cycled from a fixed palette so each card in the
     * list gets a distinct accent colour regardless of staff count.
     * Keys map to CSS modifier classes: .card--color-{key}
     */
    cardColor(index: number): string {
        const palette = ['indigo', 'rose', 'amber', 'teal', 'violet'];
        return palette[index % palette.length];
    }

    openSalesPopup(staff: StaffResponse): void {
        this.selectedStaff.set(staff);
        this.dateRange.set('today');
        this.customFrom = '';
        this.customTo = '';
    }

    closeSalesPopup(): void { this.selectedStaff.set(null); }
    setRange(range: DateRange): void { this.dateRange.set(range); }

    get activeCount(): number { return this.staffList().filter(s => s.active).length; }
    get inactiveCount(): number { return this.staffList().filter(s => !s.active).length; }
}