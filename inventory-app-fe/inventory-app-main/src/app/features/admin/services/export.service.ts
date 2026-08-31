import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ExportService {

    exportExcel(data: any[], filename: string): void {
        import('xlsx').then(XLSX => {
            const worksheet = XLSX.utils.json_to_sheet(data);
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, 'Report');
            const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
            const blob = new Blob([excelBuffer], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${filename}.xlsx`;
            a.click();
            window.URL.revokeObjectURL(url);
        });
    }

    exportPDF(columns: string[], rows: any[][], filename: string, title: string): void {
        import('jspdf').then(({ jsPDF }) => {
            import('jspdf-autotable').then(({ default: autoTable }) => {
                const doc = new jsPDF({ orientation: 'landscape' });

                // Title
                doc.setFontSize(16);
                doc.setTextColor(40, 40, 40);
                doc.text(title, 14, 16);

                // Date generated
                doc.setFontSize(9);
                doc.setTextColor(120, 120, 120);
                doc.text(`Generated: ${new Date().toLocaleString()}`, 14, 22);

                autoTable(doc, {
                    head: [columns],
                    body: rows,
                    startY: 28,
                    headStyles: {
                        fillColor: [67, 97, 238],
                        textColor: 255,
                        fontStyle: 'bold',
                        fontSize: 10
                    },
                    bodyStyles: { fontSize: 9 },
                    alternateRowStyles: { fillColor: [248, 249, 252] },
                    styles: { cellPadding: 4 }
                });

                doc.save(`${filename}.pdf`);
            });
        });
    }
}