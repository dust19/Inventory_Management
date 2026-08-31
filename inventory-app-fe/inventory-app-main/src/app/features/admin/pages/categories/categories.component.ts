import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { ToastService } from '../../../../shared/components/toast/toast.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { CategoryResponse } from '../../../common/models/category.model';
import { fadeIn } from '../../../../shared/animations/fade.animation';
import { AppTableComponent } from 'src/app/shared/components/app-table/app-table.component';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';
import { paginate, PageResult } from '../../../../core/utils/paginate.util';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, AppTableComponent, PaginatorComponent],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css'],
  animations: [fadeIn]
})
export class CategoriesComponent implements OnInit {
  private svc = inject(CategoryService);
  private toast = inject(ToastService);

  categories = signal<CategoryResponse[]>([]);
  filtered = signal<CategoryResponse[]>([]);
  loading = signal(true);
  showModal = signal(false);
  editTarget = signal<CategoryResponse | null>(null);
  formName = '';

  page = signal(1);
  pageSize = signal(10);
  search = '';
  selectedSort = '';

  sortOptions = [
    { value: 'name_asc', label: 'Name (A-Z)' },
    { value: 'name_desc', label: 'Name (Z-A)' },
    { value: 'products_desc', label: 'Products (High to Low)' },
    { value: 'products_asc', label: 'Products (Low to High)' },
  ];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getCategoryCount().subscribe({
      next: c => { this.categories.set(c); this.applyFilter(); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  applyFilter(): void {
    const s = this.search.toLowerCase();
    let list = this.categories().filter(c => c.name.toLowerCase().includes(s));
    if (this.selectedSort) {
      list = [...list].sort((a, b) => {
        switch (this.selectedSort) {
          case 'name_asc': return a.name.localeCompare(b.name);
          case 'name_desc': return b.name.localeCompare(a.name);
          case 'products_desc': return (b.totalProducts || 0) - (a.totalProducts || 0);
          case 'products_asc': return (a.totalProducts || 0) - (b.totalProducts || 0);
          default: return 0;
        }
      });
    }
    this.filtered.set(list);
    this.page.set(1);
  }

  get paged(): PageResult<CategoryResponse> {
    return paginate(this.filtered(), this.page(), this.pageSize());
  }

  onPageSize(size: number): void { this.pageSize.set(size); this.page.set(1); }

  openCreate(): void { this.editTarget.set(null); this.formName = ''; this.showModal.set(true); }
  openEdit(c: CategoryResponse): void { this.editTarget.set(c); this.formName = c.name; this.showModal.set(true); }
  closeModal(): void { this.showModal.set(false); }

  save(): void {
    if (!this.formName.trim()) { this.toast.warning('Name is required'); return; }
    const t = this.editTarget();
    const obs = t ? this.svc.update(t.id, { name: this.formName }) : this.svc.create({ name: this.formName });
    obs.subscribe({
      next: () => { this.toast.success(t ? 'Updated' : 'Created'); this.closeModal(); this.load(); },
      error: () => this.toast.error('Save failed')
    });
  }

  delete(c: CategoryResponse): void {
    if (!confirm(`Delete category "${c.name}"?`)) return;
    this.svc.delete(c.id).subscribe({
      next: () => { this.toast.success('Deleted'); this.load(); },
      error: () => this.toast.error('Delete failed')
    });
  }

  /** Hash name → one of 8 color themes */
  cardColor(name: string): string {
    const palettes = ['cc-violet', 'cc-teal', 'cc-rose', 'cc-amber', 'cc-sky', 'cc-emerald', 'cc-orange', 'cc-indigo'];
    const hash = name.split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    return palettes[hash % palettes.length];
  }

  /** Pick icon from a keyword-matched set, fall back to hash */
  categoryIcon(name: string): string {
    const n = name.toLowerCase();
    const map: [string[], string][] = [
      [['electron', 'tech', 'gadget', 'device', 'mobile', 'phone', 'laptop', 'computer'], 'bi-cpu'],
      [['cloth', 'wear', 'fashion', 'apparel', 'shirt', 'jeans', 'dress'], 'bi-bag-heart'],
      [['food', 'grocery', 'fruit', 'vegetable', 'drink', 'beverage', 'snack'], 'bi-basket2'],
      [['furniture', 'home', 'decor', 'interior', 'sofa', 'chair', 'table'], 'bi-house-heart'],
      [['book', 'stationery', 'office', 'study', 'education', 'school'], 'bi-book'],
      [['toy', 'game', 'sport', 'fitness', 'outdoor', 'play'], 'bi-controller'],
      [['beauty', 'cosmetic', 'skincare', 'health', 'wellness', 'medical', 'pharmacy'], 'bi-heart-pulse'],
      [['auto', 'vehicle', 'car', 'bike', 'motor', 'accessory'], 'bi-car-front'],
      [['pet', 'animal', 'dog', 'cat'], 'bi-shield-heart'],
      [['tool', 'hardware', 'equip', 'machine', 'industrial'], 'bi-tools'],
      [['jewel', 'watch', 'accessory', 'bag', 'purse'], 'bi-gem'],
      [['music', 'audio', 'instrument', 'camera', 'photo'], 'bi-music-note-beamed'],
    ];
    for (const [keywords, icon] of map) {
      if (keywords.some(k => n.includes(k))) return icon;
    }
    // deterministic fallback from icon pool
    const fallbacks = ['bi-tag', 'bi-box-seam', 'bi-grid', 'bi-collection', 'bi-layers', 'bi-archive', 'bi-inboxes', 'bi-stack'];
    const hash = name.split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    return fallbacks[hash % fallbacks.length];
  }
}