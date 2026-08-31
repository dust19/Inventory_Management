import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface StatStripItem {
  icon: string;
  value: number | string;
  label: string;
  iconClass: string;
  format?: 'number' | 'currency' | 'percent' | 'plain';
  trend?: number;
  trendLabel?: string;
}

@Component({
  selector: 'app-stat-strip',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-strip.component.html',
  styleUrls: ['./stat-strip.component.css']
})
export class StatStripComponent {

  @Input() items: StatStripItem[] = [];

  shortValue(value: number | string): string {
    const num = Number(value);

    if (isNaN(num)) {
      return String(value);
    }

    return new Intl.NumberFormat('en', {
      notation: 'compact',
      maximumFractionDigits: 1
    }).format(num);
  }

  getFullValue(item: StatStripItem): string {

    const num = Number(item.value);

    if (isNaN(num)) {
      return String(item.value);
    }

    switch (item.format) {

      case 'currency':
        return '₹' + num.toLocaleString('en-IN');

      case 'percent':
        return num.toFixed(2) + '%';

      case 'number':
        return num.toLocaleString('en-IN');

      default:
        return String(item.value);
    }
  }

  getDisplayValue(item: StatStripItem): string {

    const num = Number(item.value);

    if (isNaN(num)) {
      return String(item.value);
    }

    switch (item.format) {

      case 'currency':
        return '₹' + this.shortValue(num);

      case 'percent':
        return num.toFixed(2) + '%';

      case 'number':
        return this.shortValue(num);

      default:
        return String(item.value);
    }
  }
}