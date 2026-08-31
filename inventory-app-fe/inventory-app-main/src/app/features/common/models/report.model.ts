export interface SalesReportDTO {
    label: string;
    totalOrders: number;
    revenue: number;
    profit: number;
    cost: number;
    marginPercent: number;
}

export interface SalesSummaryDTO {
    label: string;
    totalOrders: number;
    revenue: number;
    profit: number;
}

export interface InventoryReportDTO {
    productId: number;
    productName: string;
    sku: string;
    unitPrice: number;
    soldQty: number;
    revenue: number;
    currentStock: number;
    stockValue: number;
    status: string;           // IN_STOCK | LOW_STOCK | OUT_OF_STOCK | INACTIVE
    lastUpdated: string;
}

export interface SupplierReportDTO {
    supplierId: number;
    companyName: string;
    totalProducts: number;
    totalStockSupplied: number;
    totalPurchaseValue: number;
}

export interface CustomerReportDTO {
    userId: number;
    name: string;
    email: string;
    totalOrders: number;
    totalSpent: number;
    lastPurchase: string;
}

export interface UserSummaryDTO {
    id: number;
    name: string;
    email: string;
    role: string;
    active: boolean;
    createdAt: string;
}