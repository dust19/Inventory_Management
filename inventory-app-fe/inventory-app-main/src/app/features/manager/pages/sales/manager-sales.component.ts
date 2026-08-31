import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SaleService } from '../../../user/services/sale.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { SaleResponse } from '../../../common/models/sale.model';
import { paymentModeBadge } from '../../../../core/utils/role.util';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { StatStripComponent, StatStripItem } from 'src/app/shared/components/stats-strip/stat-strip.component';

@Component({
    selector: 'app-manager-sales',
    standalone: true,
    imports: [CommonModule, FormsModule, LoaderComponent, StatStripComponent, AppTableComponent, PaginatorComponent],
    templateUrl: './manager-sales.component.html',
    styleUrls: ['./manager-sales.component.css'],
    animations: [fadeIn]
})
export class ManagerSalesComponent implements OnInit {
    private saleSvc = inject(SaleService);
    private auth = inject(AuthService);

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
        { icon: 'bi-receipt-cutoff', value: this.sales().length, label: 'Total Sales', iconClass: 'icon-primary', format: 'number' },
        { icon: 'bi-currency-rupee', value: this.sales().reduce((sum, s) => sum + s.totalAmount, 0), label: 'Gross Revenue', iconClass: 'icon-success', format: 'currency' },
        { icon: 'bi-cash-stack', value: this.sales().filter(s => s.paymentMode === 'CASH').length, label: 'Cash Payments', iconClass: 'icon-accent', format: 'number' },
        { icon: 'bi-phone', value: this.sales().filter(s => s.paymentMode === 'UPI').length, label: 'UPI Payments', iconClass: 'icon-warning', format: 'number' },
    ]);

    ngOnInit(): void {
        const user = this.auth.currentUser();
        // Fetch only sales made by this manager's staff (scoped by managerId)
        this.saleSvc.getByManager(user!.id).subscribe({
            next: s => { this.sales.set(s); this.applyFilter(); this.loading.set(false); },
            error: () => this.loading.set(false)
        });
    }

    applyFilter(): void {
        const s = this.search.toLowerCase();
        let list = this.sales().filter(sale =>
            sale.id.toString().includes(s) ||
            (sale.customerName ?? '').toLowerCase().includes(s) ||
            (sale.soldByName ?? '').toLowerCase().includes(s)
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