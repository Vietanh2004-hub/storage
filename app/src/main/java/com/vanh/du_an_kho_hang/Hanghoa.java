package com.vanh.du_an_kho_hang;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

public class Hanghoa extends BaseActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private ImageButton btnScanBarcode, btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hanghoa);

        // Thiết lập thanh menu
        setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.etSearch);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnSearch = findViewById(R.id.btnSearch);

        loadData();

        // Thêm TextWatcher cho EditText để lọc sản phẩm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            searchProducts(query);
        });

        // Nút quét mã vạch
        btnScanBarcode.setOnClickListener(v -> startBarcodeScan());

    }

    private void searchProducts(String query) {
        // Sử dụng dbHelper.searchProducts trả về danh sách sản phẩm
        List<Product> productList = dbHelper.searchProducts(query);

        if (productList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm nào.", Toast.LENGTH_SHORT).show();
        } else {
            adapter.updateData(productList);
        }
    }

    private void startBarcodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Quét mã vạch");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Đã hủy quét mã vạch", Toast.LENGTH_SHORT).show();
            } else {
                searchProducts(result.getContents());
            }
        } else {
            Toast.makeText(this, "Quét mã vạch không thành công", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadData() {
        // Lấy toàn bộ sản phẩm từ cơ sở dữ liệu
        List<Product> productList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllProducts();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    // Trích xuất dữ liệu từ các cột, sử dụng getColumnIndexOrThrow để đảm bảo an toàn
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String ten = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEN));
                    double gia = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GIA));
                    int soLuong = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SOLUONG));
                    String maVach = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MA_VACH));
                    String loaiHang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOAI_HANG));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath")); // Lấy đường dẫn ảnh
                    String importDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String exportDate = cursor.getString(cursor.getColumnIndexOrThrow("exportDate"));
                    // Thêm sản phẩm vào danh sách
                    productList.add(new Product(id, ten, gia, soLuong, maVach, loaiHang, imagePath, importDate, exportDate));
                } catch (IllegalArgumentException e) {
                    // Xử lý lỗi nếu cột không tồn tại
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi: Một số cột không tồn tại trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
            cursor.close(); // Đóng cursor sau khi sử dụng
        } else {
            Toast.makeText(this, "Không có dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show();
        }

        // Kiểm tra danh sách sản phẩm
        if (productList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(this, "Không có sản phẩm nào để hiển thị.", Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            // Khởi tạo adapter và cập nhật RecyclerView
            adapter = new ProductAdapter(this, productList);
            recyclerView.setAdapter(adapter);
        }
    }

    void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);
        builder.setView(view);

        EditText etNewPrice = view.findViewById(R.id.etNewPrice);
        EditText etNewQuantity = view.findViewById(R.id.etNewQuantity);
        Button btnSave = view.findViewById(R.id.btnSave);

        etNewPrice.setText(String.valueOf(product.getGia()));
        etNewQuantity.setText(String.valueOf(product.getSoLuong()));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            double newPrice;
            int newQuantity;

            try {
                newPrice = Double.parseDouble(etNewPrice.getText().toString().trim());
                newQuantity = Integer.parseInt(etNewQuantity.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá hoặc số lượng không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            int currentQuantity = product.getSoLuong();
            int addedQuantity = newQuantity - currentQuantity;

            Log.d("CurrentQuantity", "Số lượng hiện tại: " + currentQuantity);
            Log.d("NewQuantity", "Số lượng mới: " + newQuantity);
            Log.d("AddedQuantity", "Số lượng thêm: " + addedQuantity);

            // Nếu có số lượng nhập thêm, cập nhật cột nhap và soLuong
            if (addedQuantity > 0) {
                dbHelper.updateNhapHang(product.getId(), addedQuantity);
            }

            // Cập nhật giá và số lượng mới
            boolean success = dbHelper.updateProduct(product.getId(), newPrice, newQuantity);

            if (success) {
                Toast.makeText(this, "Cập nhật sản phẩm thành công.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData(); // Tải lại dữ liệu trong màn hình hiện tại
            } else {
                Toast.makeText(this, "Cập nhật sản phẩm thất bại.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialogInterface, which) -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Tải lại dữ liệu khi quay lại Activity
    }
}
