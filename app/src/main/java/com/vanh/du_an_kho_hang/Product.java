package com.vanh.du_an_kho_hang;

public class Product {
    private int id;
    private String ten;
    private double gia;
    private int soLuong;
    private String maVach;
    private String loaiHang; // Thêm loại hàng
    private String imagePath;
    private String importDate;
    private String exportDate;
    private int tempExportQuantity;

    public Product(int id, String ten, double gia, int soLuong, String maVach, String loaiHang, String imagePath, String importDate, String exportDate) {
        this.id = id;
        this.ten = ten;
        this.gia = gia;
        this.soLuong = soLuong;
        this.maVach = maVach;
        this.loaiHang = loaiHang;
        this.imagePath = imagePath;
        this.importDate = importDate;
        this.exportDate = exportDate;
    }

    // Thêm getter và setter cho loaiHang
    public String getLoaiHang() {
        return loaiHang;
    }

    public void setLoaiHang(String loaiHang) {
        this.loaiHang = loaiHang;
    }


    // Getter cho từng thuộc tính
    public int getId() {
        return id;
    }

    public String getTen() {
        return ten;
    }

    public double getGia() {
        return gia;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getMaVach() {
        return maVach;
    }

    // Getter và Setter cho imagePath
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public int getTempExportQuantity() {
        return tempExportQuantity;
    }

    public void setTempExportQuantity(int tempExportQuantity) {
        this.tempExportQuantity = tempExportQuantity;
    }
    public String getImportDate() {
        return importDate;
    }
    public String getExportDate() {
        return exportDate;
    }
}
