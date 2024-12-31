package com.vanh.du_an_kho_hang;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Xuathang extends BaseActivity {

    private RecyclerView recyclerView;
    private SalesAdapter adapter;
    private DatabaseHelper dbHelper;
    private Button btnXoaHang;
    private ImageButton btnScanBarcode;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xuathang);
        setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);

        recyclerView = findViewById(R.id.recyclerViewSales);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ các thành phần
        btnXoaHang = findViewById(R.id.btnXoaHang);
        etSearch = findViewById(R.id.etSearch);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        // Khởi tạo adapter rỗng
        adapter = new SalesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadSalesData(); // Tải dữ liệu ban đầu

        // Tìm kiếm sản phẩm theo thời gian thực
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSalesData(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý nút "Xóa Hàng"
        btnXoaHang.setOnClickListener(v -> deleteSelectedItems());

        // Xử lý quét mã vạch
        btnScanBarcode.setOnClickListener(v -> startBarcodeScan());
    }

    private void searchSalesData(String query) {
        List<Product> salesList = new ArrayList<>();
        Cursor cursor = dbHelper.searchSales(query);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    // Trích xuất dữ liệu từ các cột, đảm bảo các cột đều tồn tại
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String ten = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEN));
                    double gia = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GIA));
                    int soLuong = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SOLUONG));
                    String maVach = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MA_VACH));
                    String loaiHang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOAI_HANG));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath")); // Trích xuất đường dẫn ảnh
                    String importDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String exportDate = cursor.getString(cursor.getColumnIndexOrThrow("exportDate"));
                    // Thêm sản phẩm vào danh sách
                    salesList.add(new Product(id, ten, gia, soLuong, maVach, loaiHang, imagePath, importDate, exportDate));
                } catch (IllegalArgumentException e) {
                    // Xử lý lỗi nếu cột không tồn tại
                    Log.e("Database Error", "Một số cột không tồn tại: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close(); // Đóng Cursor sau khi sử dụng
        }

        // Cập nhật adapter với danh sách mới
        adapter.updateData(salesList);
    }


    private void startBarcodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Quét mã vạch");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    private void deleteSelectedItems() {
        List<Product> selectedItems = adapter.getSelectedItemsWithQuantities();
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allSuccess = true;

        for (Product product : selectedItems) {
            int exportQuantity = product.getTempExportQuantity(); // Lấy số lượng xuất tạm thời
            if (exportQuantity <= 0) {
                Toast.makeText(this, "Số lượng xuất không hợp lệ cho sản phẩm: " + product.getTen(), Toast.LENGTH_SHORT).show();
                allSuccess = false;
                continue;
            }

            // Giảm số lượng tồn kho trong cơ sở dữ liệu
            boolean success = dbHelper.reduceProductQuantity(product.getId(), exportQuantity);
            if (success) {
                dbHelper.updateProductSales(product.getId(), exportQuantity); // Chỉ tăng xuat trong hàm này
            } else {
                allSuccess = false;
                Toast.makeText(this, "Không thể xuất hàng: " + product.getTen() + " - Số lượng không đủ.", Toast.LENGTH_SHORT).show();
            }
        }

        if (allSuccess) {
            Toast.makeText(this, "Xuất hàng thành công", Toast.LENGTH_SHORT).show();
        }

        loadSalesData(); // Tải lại dữ liệu sau khi xuất

        if (allSuccess) {
            Toast.makeText(this, "Xuất hàng thành công", Toast.LENGTH_SHORT).show();
        }

        loadSalesData(); // Tải lại dữ liệu sau khi xuất
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            searchSalesData(result.getContents());
        } else {
            Toast.makeText(this, "Quét mã vạch thất bại hoặc không tìm thấy.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSalesData() {
        List<Product> salesList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllSales();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    // Lấy dữ liệu từ các cột
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String ten = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEN));
                    double gia = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GIA));
                    int soLuong = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SOLUONG));
                    String maVach = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MA_VACH));
                    String loaiHang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOAI_HANG));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));

                    // Xử lý giá trị ngày nhập, xuất
                    String importDate = cursor.getString(cursor.getColumnIndexOrThrow("importDate"));
                    String exportDate = cursor.getString(cursor.getColumnIndexOrThrow("exportDate"));

                    // Kiểm tra giá trị null và gán giá trị mặc định nếu cần
                    if (importDate == null || importDate.isEmpty()) {
                        importDate = "N/A"; // Hoặc định dạng ngày mặc định
                    }

                    if (exportDate == null || exportDate.isEmpty()) {
                        exportDate = "N/A"; // Hoặc định dạng ngày mặc định
                    }

                    // Thêm sản phẩm vào danh sách
                    salesList.add(new Product(id, ten, gia, soLuong, maVach, loaiHang, imagePath, importDate, exportDate));
                } catch (Exception e) {
                    Log.e("Database Error", "Có lỗi trong khi xử lý dữ liệu: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        } else {
            Log.e("Database Error", "Không tìm thấy dữ liệu sản phẩm trong bảng Sales.");
        }

        if (cursor != null) {
            cursor.close(); // Đóng Cursor
        }

        // Cập nhật RecyclerView qua Adapter
        adapter.updateData(salesList);
    }


}

