import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { CustomerReportDTO, InventoryReportDTO, SalesReportDTO, SupplierReportDTO } from "../../common/models/report.model";
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class ReportService {
    private base = 'http://localhost:8080/api/reports';
    constructor(private http: HttpClient) { }

    getSalesReport(start?: string, end?: string): Observable<SalesReportDTO[]> {
        let params = new HttpParams();
        if (start) params = params.set('start', start);
        if (end) params = params.set('end', end);
        return this.http.get<SalesReportDTO[]>(`${this.base}/sales`, { params });
    }

    getSalesSummary(start?: string, end?: string): Observable<SalesReportDTO> {
        let params = new HttpParams();
        if (start) params = params.set('start', start);
        if (end) params = params.set('end', end);
        return this.http.get<SalesReportDTO>(`${this.base}/sales/summary`, { params });
    }


    getInventoryReport() { return this.http.get<InventoryReportDTO[]>(`${this.base}/inventory`); }
    getSupplierReport() { return this.http.get<SupplierReportDTO[]>(`${this.base}/suppliers`); }
    getCustomerReport() { return this.http.get<CustomerReportDTO[]>(`${this.base}/customers`); }
}