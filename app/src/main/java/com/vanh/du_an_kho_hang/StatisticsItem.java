package com.vanh.du_an_kho_hang;

public class StatisticsItem {
    private String name;
    private String barcode;
    private String loaiHang;
    private int imports;
    private int sales;
    private double price;
    private String imagePath; // Thêm thuộc tính imagePath
    private String importDate; // Ngày nhập hàng
    private String exportDate; // Ngày xuất hàng

    // Constructor đầy đủ tham số
    public StatisticsItem(String name, String barcode, String loaiHang, int imports, int sales, double price, String imagePath, String importDate, String exportDate) {
        this.name = name;
        this.barcode = barcode;
        this.loaiHang = loaiHang;
        this.imports = imports;
        this.sales = sales;
        this.price = price;
        this.imagePath = imagePath; // Gán giá trị cho imagePath
        this.importDate = importDate;
        this.exportDate = exportDate;
    }

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getLoaiHang() { return loaiHang; }
    public void setLoaiHang(String loaiHang) { this.loaiHang = loaiHang; }

    public int getImports() { return imports; }
    public void setImports(int imports) { this.imports = imports; }

    public int getSales() { return sales; }
    public void setSales(int sales) { this.sales = sales; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImagePath() { return imagePath; } // Getter cho imagePath
    public void setImagePath(String imagePath) { this.imagePath = imagePath; } // Setter cho imagePath
    public String getImportDate() {
        return importDate;
    }

    public String getExportDate() {
        return exportDate;
    }

    // Phương thức toString để hỗ trợ debug
    @Override
    public String toString() {
        return "StatisticsItem{" +
                "name='" + name + '\'' +
                ", barcode='" + barcode + '\'' +
                ", loaiHang='" + loaiHang + '\'' +
                ", imports=" + imports +
                ", sales=" + sales +
                ", price=" + price +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
