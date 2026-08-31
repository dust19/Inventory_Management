import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { SaleService } from '../../../user/services/sale.service';
import { SaleResponse } from '../../../common/models/sale.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { paymentModeBadge } from '../../../../core/utils/role.util';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

@Component({
    selector: 'app-staff-history',
    standalone: true,
    imports: [CommonModule, FormsModule, LoaderComponent, StatStripComponent, AppTableComponent, PaginatorComponent],
    templateUrl: './staff-history.component.html',
    styleUrl: './staff-history.component.css',
    animations: [fadeIn]
})
export class StaffHistoryComponent implements OnInit {
    private auth = inject(AuthService);
    private saleSvc = inject(SaleService);

    sales = signal<SaleResponse[]>([]);
    filtered = signal<SaleResponse[]>([]);
    loading = signal(true);
    detailItem = signal<SaleResponse | null>(null);
    paymentModeBadge = paymentModeBadge;

    page = signal(1);
    pageSize = signal(10);
    search = '';
    selectedPayment = '';
    selectedSort = '';

    paymentFilterOptions = [
        { value: 'CASH', label: 'Cash' },
        { value: 'UPI', label: 'UPI' },
    ];

    sortOptions = [
        { value: 'date_desc', label: 'Date (Newest)' },
        { value: 'date_asc', label: 'Date (Oldest)' },
        { value: 'amount_desc', label: 'Amount (High to Low)' },
        { value: 'amount_asc', label: 'Amount (Low to High)' },
    ];

    dashboardStats = computed<StatStripItem[]>(() => [
        { icon: 'bi-receipt-cutoff', value: this.sales().length, label: 'My Sales', iconClass: 'icon-primary', format: 'number' },
        { icon: 'bi-currency-rupee', value: this.sales().reduce((s, x) => s + x.totalAmount, 0), label: 'Total Revenue', iconClass: 'icon-success', format: 'currency' },
        { icon: 'bi-cash-stack', value: this.sales().filter(s => s.paymentMode === 'CASH').length, label: 'Cash', iconClass: 'icon-accent', format: 'number' },
        { icon: 'bi-phone', value: this.sales().filter(s => s.paymentMode === 'UPI').length, label: 'UPI', iconClass: 'icon-warning', format: 'number' },
    ]);

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (!user) return;
        this.saleSvc.getBySoldBy(user.id).subscribe({
            next: s => { this.sales.set(s); this.applyFilter(); this.loading.set(false); },
            error: () => this.loading.set(false)
        });
    }

    applyFilter(): void {
        const s = this.search.toLowerCase();
        let list = this.sales().filter(sale =>
            sale.id.toString().includes(s) ||
            (sale.customerName ?? '').toLowerCase().includes(s) ||
            (sale.customerPhone ?? '').toLowerCase().includes(s)
        );

        if (this.selectedPayment) {
            list = list.filter(sale => sale.paymentMode === this.selectedPayment);
        }

        if (this.selectedSort) {
            list = [...list].sort((a, b) => {
                switch (this.selectedSort) {
                    case 'date_desc': return new Date(b.saleDate ?? '').getTime() - new Date(a.saleDate ?? '').getTime();
                    case 'date_asc': return new Date(a.saleDate ?? '').getTime() - new Date(b.saleDate ?? '').getTime();
                    case 'amount_desc': return b.totalAmount - a.totalAmount;
                    case 'amount_asc': return a.totalAmount - b.totalAmount;
                    default: return 0;
                }
            });
        }

        this.filtered.set(list);
        this.page.set(1);
    }

    get paged(): PageResult<SaleResponse> {
        return paginate(this.filtered(), this.page(), this.pageSize());
    }

    onPageSize(size: number): void {
        this.pageSize.set(size);
        this.page.set(1);
    }
}