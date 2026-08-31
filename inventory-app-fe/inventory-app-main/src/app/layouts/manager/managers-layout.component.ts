import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';
import { SidebarComponent, NavSection } from '../../shared/components/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { initials } from '../../core/utils/role.util';
import { ChatbotComponent } from 'src/app/shared/components/chatbot/chatbot.component';
import { ThemeToggleComponent } from '../../shared/components/theme-toggle/theme-toggle.component';
import { LowStockSidebarComponent } from 'src/app/shared/components/low-stock-sidebar/low-stock-sidebar.component';

@Component({
  selector: 'app-manager-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, SidebarComponent, ChatbotComponent, ThemeToggleComponent, LowStockSidebarComponent], templateUrl: './managers-layout.component.html',
  styleUrls: ['./managers-layout.component.css']
})
export class ManagerLayoutComponent {
  auth = inject(AuthService);
  sidebarOpen = signal(false);
  collapsed = signal(false);
  toggleSidebar() { this.collapsed.update(v => !v); }

  onMenuClick(): void {
    if (window.innerWidth <= 1024) {
      this.sidebarOpen.update(v => !v);
    } else {
      this.collapsed.update(v => !v);
    }
  }

  navSections: NavSection[] = [
    {
      title: 'Overview',
      items: [{ label: 'Dashboard', icon: 'bi-grid-1x2-fill', route: '/manager/dashboard' }]
    },
    {
      title: 'Operations',
      items: [
        { label: 'Confirm Deliveries', icon: 'bi-truck', route: '/manager/purchases' },
        { label: 'My Staff', icon: 'bi-people-fill', route: '/manager/staff' },
        { label: 'Sales', icon: 'bi-receipt-cutoff', route: '/manager/sales' },
      ]
    }, {
      title: 'Data',
      items: [
        { label: 'Inventory', icon: 'bi-archive-fill', route: '/manager/inventory' },
        { label: 'All Sales', icon: 'bi-receipt-cutoff', route: '/manager/sales/all' },
        { label: 'Transactions', icon: 'bi-arrow-left-right', route: '/manager/transactions' },
      ]
    },
    {
      title: 'Reports',
      items: [
        { label: 'Sales Report', icon: 'bi-graph-up-arrow', route: '/manager/reports/sales' },
        { label: 'Inventory Report', icon: 'bi-clipboard-data-fill', route: '/manager/reports/inventory' },
        { label: 'Supplier Report', icon: 'bi-truck-flatbed', route: '/manager/reports/suppliers' },
        { label: 'Customer Report', icon: 'bi-person-vcard-fill', route: '/manager/reports/customers' },
      ]
    },
  ];

  get userInitials(): string { return initials(this.auth.currentUser()?.name ?? 'M'); }
}