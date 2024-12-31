package com.vanh.du_an_kho_hang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "warehouse.db";
    private static final int DATABASE_VERSION = 6;

    // Bảng sản phẩm
    public static final String TABLE_PRODUCT = "Product";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEN = "ten";
    public static final String COLUMN_LOAI_HANG = "loaiHang";
    public static final String COLUMN_GIA = "gia";
    public static final String COLUMN_SOLUONG = "soLuong";
    public static final String COLUMN_MA_VACH = "maVach";

    private static final String COLUMN_IMAGE_PATH = "imagePath";

    // Bảng xuất hàng
    public static final String TABLE_SALES = "Sales";
    public static final String COLUMN_SALE_ID = "sale_id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_DATE = "date";

    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TEN + " TEXT, "
            + COLUMN_GIA + " REAL, "
            + COLUMN_SOLUONG + " INTEGER, "
            + COLUMN_MA_VACH + " TEXT, "
            + "nhap INTEGER DEFAULT 0, "
            + "xuat INTEGER DEFAULT 0, "
            + COLUMN_IMAGE_PATH + " TEXT, "
            + COLUMN_LOAI_HANG + " TEXT, "
            + "importDate TEXT, " // Thêm cột ngày nhập
            + "exportDate TEXT"   // Thêm cột ngày xuất
            + ")";

    private static final String CREATE_TABLE_SALES = "CREATE TABLE " + TABLE_SALES + "("
            + COLUMN_SALE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PRODUCT_ID + " INTEGER, "
            + COLUMN_QUANTITY + " INTEGER, "
            + COLUMN_DATE + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCT + "(" + COLUMN_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PRODUCT);
        db.execSQL(CREATE_TABLE_SALES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            try {
                // Kiểm tra và thêm cột ngày nhập
                db.execSQL("ALTER TABLE " + TABLE_PRODUCT + " ADD COLUMN importDate TEXT DEFAULT 'Unknown'");
                // Kiểm tra và thêm cột ngày xuất
                db.execSQL("ALTER TABLE " + TABLE_PRODUCT + " ADD COLUMN exportDate TEXT DEFAULT 'Unknown'");
                Log.d("DatabaseUpgrade", "Cột importDate và exportDate đã được thêm.");
            } catch (Exception e) {
                Log.e("DatabaseUpgrade", "Cột importDate hoặc exportDate đã tồn tại hoặc lỗi khi nâng cấp", e);
            }
        }
    }

    // Thêm sản phẩm
    public long addProduct(String ten, double gia, int soLuong, String maVach, String loaiHang, String imagePath, String importDate) {
        SQLiteDatabase db = null;
        long id = -1;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TEN, ten);
            values.put(COLUMN_GIA, gia);
            values.put(COLUMN_SOLUONG, soLuong); // Cập nhật số lượng tồn
            values.put(COLUMN_MA_VACH, maVach);
            values.put(COLUMN_LOAI_HANG, loaiHang);
            values.put(COLUMN_IMAGE_PATH, imagePath);
            values.put("nhap", soLuong); // Gán giá trị nhập khi thêm mới
            values.put("importDate", importDate); // Thêm ngày nhập
            id = db.insert(TABLE_PRODUCT, null, values);

            Log.d("addProduct", "Thêm sản phẩm mới: " + ten + ", Nhập: " + soLuong);
        } catch (Exception e) {
            Log.e("DatabaseError", "Lỗi khi thêm sản phẩm: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
        return id;
    }

    // Tính tổng hàng nhập
    public int getTotalImports() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(nhap) FROM " + TABLE_PRODUCT, null);
        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return total;
    }

    // Tính tổng hàng xuất
    public int getTotalSales() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(xuat) FROM " + TABLE_PRODUCT, null);
        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return total;
    }
    // Cập nhật số lượng bán
    // Cập nhật số lượng xuất
    public void updateProductSales(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Lấy ngày hiện tại
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Cập nhật số lượng đã xuất và ngày xuất
            String updateQuery = "UPDATE " + TABLE_PRODUCT +
                    " SET xuat = xuat + ?, exportDate = ? WHERE id = ?";
            db.execSQL(updateQuery, new Object[]{quantity, currentDate, productId});

            Log.d("updateProductSales", "Cập nhật số lượng xuất và ngày xuất: " + currentDate);
        } catch (Exception e) {
            Log.e("updateProductSales", "Lỗi khi cập nhật số lượng xuất: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    // Tìm kiếm thống kê
    public List<StatisticsItem> searchStatistics(String query) {
        List<StatisticsItem> statisticsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT ten, maVach, loaiHang, nhap AS totalImports, xuat AS totalSales, gia, imagePath, importDate, exportDate " +
                "FROM Product WHERE ten LIKE ? OR maVach LIKE ? OR loaiHang LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("ten"));
                String barcode = cursor.getString(cursor.getColumnIndexOrThrow("maVach"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("loaiHang"));
                int imports = cursor.getInt(cursor.getColumnIndexOrThrow("totalImports"));
                int sales = cursor.getInt(cursor.getColumnIndexOrThrow("totalSales"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("gia"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
                String importDate = cursor.getString(cursor.getColumnIndexOrThrow("importDate"));
                String exportDate = cursor.getString(cursor.getColumnIndexOrThrow("exportDate"));
                if (exportDate == null || exportDate.isEmpty()) {
                    exportDate = "Unknown"; // Gán giá trị mặc định nếu bị null
                }
                statisticsList.add(new StatisticsItem(name, barcode, category, imports, sales, price, imagePath, importDate, exportDate));
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        db.close();
        return statisticsList;
    }



    public List<StatisticsItem> getStatisticsByItem() {
        List<StatisticsItem> statisticsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_TEN + ", " + COLUMN_MA_VACH + ", " + COLUMN_LOAI_HANG +
                ", nhap AS totalImports, xuat AS totalSales, " + COLUMN_GIA + ", " + COLUMN_IMAGE_PATH +
                ", importDate, exportDate FROM " + TABLE_PRODUCT;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN));
                String barcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MA_VACH));
                String loaiHang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI_HANG));
                int imports = cursor.getInt(cursor.getColumnIndexOrThrow("totalImports"));
                int sales = cursor.getInt(cursor.getColumnIndexOrThrow("totalSales"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GIA));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                String importDate = cursor.getString(cursor.getColumnIndexOrThrow("importDate"));
                String exportDate = cursor.getString(cursor.getColumnIndexOrThrow("exportDate"));

                Log.d("getStatisticsByItem", "Sản phẩm: " + name + ", Nhập: " + imports);
                statisticsList.add(new StatisticsItem(name, barcode, loaiHang, imports, sales, price, imagePath, importDate, exportDate));
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        db.close();
        return statisticsList;
    }
    // Phương thức tiện ích lấy String an toàn
    private String getStringSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getString(index);
        }
        return "Không xác định"; // Giá trị mặc định
    }
    // Phương thức tiện ích lấy int an toàn
    private int getIntSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getInt(index);
        }
        return 0; // Giá trị mặc định
    }
    // Phương thức tiện ích lấy double an toàn
    private double getDoubleSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getDouble(index);
        }
        return 0.0; // Giá trị mặc định
    }
    public Cursor searchSales(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Truy vấn tìm kiếm theo tên hoặc mã vạch
        String sql = "SELECT * FROM " + TABLE_PRODUCT +
                " WHERE " + COLUMN_TEN + " LIKE ? OR " + COLUMN_MA_VACH + " LIKE ?";
        String wildcardQuery = "%" + query + "%";
        return db.rawQuery(sql, new String[]{wildcardQuery, wildcardQuery});
    }
    public Cursor getAllSales() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TABLE_SALES + "." + COLUMN_SALE_ID + ", " +
                TABLE_PRODUCT + "." + COLUMN_TEN + ", " +
                TABLE_PRODUCT + "." + COLUMN_GIA + ", " +
                TABLE_SALES + "." + COLUMN_QUANTITY + ", " +
                TABLE_SALES + "." + COLUMN_DATE +
                " FROM " + TABLE_SALES +
                " INNER JOIN " + TABLE_PRODUCT +
                " ON " + TABLE_SALES + "." + COLUMN_PRODUCT_ID + " = " + TABLE_PRODUCT + "." + COLUMN_ID;
        return db.rawQuery(query, null);
    }
    // Refactor: Tìm kiếm sản phẩm
    public List<Product> searchProducts(String query) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_PRODUCT +
                " WHERE " + COLUMN_TEN + " LIKE ? OR " + COLUMN_LOAI_HANG + " LIKE ? OR " + COLUMN_MA_VACH + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                productList.add(extractProductFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        closeCursorSafely(cursor); // Đóng Cursor an toàn
        db.close(); // Đóng cơ sở dữ liệu
        return productList;
    }
    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_PRODUCT;
        return db.rawQuery(sql, null);
    }
    // Thêm phương thức tiện ích đóng Cursor an toàn
    private void closeCursorSafely(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
    private void closeDatabaseSafely(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    // Thêm phương thức trích xuất chung
    private Product extractProductFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String ten = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN));
        double gia = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GIA));
        int soLuong = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLUONG));
        String maVach = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MA_VACH));
        String loaiHang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI_HANG)); // Lấy loại hàng
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
        String importDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String exportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return new Product(id, ten, gia, soLuong, maVach, loaiHang, imagePath, importDate, exportDate);
    }
    public boolean reduceProductQuantity(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Lấy số lượng tồn hiện tại
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_SOLUONG + " FROM " + TABLE_PRODUCT + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(productId)});
            if (cursor != null && cursor.moveToFirst()) {
                int currentQuantity = cursor.getInt(0); // Số lượng tồn kho hiện tại
                if (currentQuantity >= quantity) {
                    // Giảm số lượng tồn kho
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_SOLUONG, currentQuantity - quantity);
                    db.update(TABLE_PRODUCT, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productId)});
                    cursor.close();
                    return true;
                } else {
                    Log.e("reduceProductQuantity", "Không đủ số lượng tồn để xuất.");
                }
            }
        } catch (Exception e) {
            Log.e("reduceProductQuantity", "Lỗi khi giảm số lượng: " + e.getMessage());
        } finally {
            db.close();
        }
        return false; // Trả về false nếu không thể giảm
    }



    public boolean isBarcodeExists(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCT + " WHERE " + COLUMN_MA_VACH + " = ?", new String[]{barcode});
        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return exists;
    }
    // Cập nhật số lượng nhập
    public void updateNhapHang(int productId, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String updateQuery = "UPDATE " + TABLE_PRODUCT +
                    " SET nhap = nhap + ?, soLuong = soLuong + ?, importDate = ? WHERE id = ?";
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            db.execSQL(updateQuery, new Object[]{quantity, quantity, currentDate, productId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }



    private void updateProductQuantity(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String query = "UPDATE " + TABLE_PRODUCT + " SET " + COLUMN_SOLUONG + " = " + COLUMN_SOLUONG + " + ? WHERE " + COLUMN_ID + " = ?";
            db.execSQL(query, new Object[]{quantity, productId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public boolean updateProduct(int productId, double newPrice, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GIA, newPrice);
        values.put(COLUMN_SOLUONG, newQuantity);
        int rowsAffected = db.update(TABLE_PRODUCT, values, COLUMN_ID + " = ?", new String[]{String.valueOf(productId)});
        db.close();
        return rowsAffected > 0;
    }
    public boolean updateProductByBarcode(String barcode, String name, double price, int quantity, String category, String imagePath, String importDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            // Lấy số lượng hiện tại
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_SOLUONG + " FROM " + TABLE_PRODUCT +
                    " WHERE " + COLUMN_MA_VACH + " = ?", new String[]{barcode});
            if (cursor != null && cursor.moveToFirst()) {
                int currentQuantity = cursor.getInt(0); // Số lượng hiện tại
                ContentValues values = new ContentValues();
                values.put(COLUMN_TEN, name);
                values.put(COLUMN_GIA, price);
                values.put(COLUMN_SOLUONG, currentQuantity + quantity); // Cộng dồn số lượng
                values.put(COLUMN_LOAI_HANG, category);
                values.put(COLUMN_IMAGE_PATH, imagePath);
                values.put("nhap", quantity); // Chỉ cộng số lượng mới nhập vào cột nhap
                values.put("importDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date())); // Thêm ngày nhập
                success = db.update(TABLE_PRODUCT, values, COLUMN_MA_VACH + " = ?", new String[]{barcode}) > 0;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseError", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        } finally {
            db.close();
        }
        return success;
    }

    public int getProductQuantityByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        int quantity = 0;
        // Truy vấn để lấy số lượng sản phẩm theo mã vạch
        String query = "SELECT " + COLUMN_SOLUONG + " FROM " + TABLE_PRODUCT + " WHERE " + COLUMN_MA_VACH + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{barcode});
        if (cursor != null && cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLUONG));
            cursor.close();
        }
        db.close();
        return quantity;
    }
    public void updateStatistics() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String updateQuery = "UPDATE " + TABLE_PRODUCT +
                    " SET nhap = nhap + ?, xuat = xuat + ? WHERE id = ?";
            db.execSQL(updateQuery);
        } catch (Exception e) {
            Log.e("DatabaseError", "Lỗi khi cập nhật thống kê: " + e.getMessage());
        } finally {
            db.close();
        }
    }
    public int getProductIdByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        int productId = -1; // Giá trị mặc định nếu không tìm thấy
        Cursor cursor = null;
        try {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_PRODUCT + " WHERE " + COLUMN_MA_VACH + " = ?";
            cursor = db.rawQuery(query, new String[]{barcode});
            if (cursor != null && cursor.moveToFirst()) {
                productId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Lỗi khi lấy Product ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return productId;
    }
    public void logAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCT, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN));
                int nhap = cursor.getInt(cursor.getColumnIndexOrThrow("nhap"));
                Log.d("ProductLog", "Tên: " + name + ", Nhập: " + nhap);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
    }
    public boolean deleteProductById(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCT, COLUMN_ID + " = ?", new String[]{String.valueOf(productId)});
        db.close();
        return rowsAffected > 0;
    }
}
