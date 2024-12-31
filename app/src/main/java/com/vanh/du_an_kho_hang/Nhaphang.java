package com.vanh.du_an_kho_hang;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Nhaphang extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private EditText etTenHang, etGia, etMaVach, etSoLuong;
    private ImageView imgSanPham;
    private Button btnSave, btnChonAnh, btnChupAnh;
    private ImageButton btnQuetMaVach;
    private Spinner spinnerLoaiHang;
    private DatabaseHelper dbHelper;
    private Bitmap selectedImageBitmap;
    private String selectedLoaiHang;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhaphang);

        // Thiết lập thanh menu
        setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);

        // Ánh xạ các thành phần giao diện
        etTenHang = findViewById(R.id.etTenHang);
        etSoLuong = findViewById(R.id.etSoLuong);
        etGia = findViewById(R.id.etGia);
        etMaVach = findViewById(R.id.etMaVach);
        imgSanPham = findViewById(R.id.imgSanPham);
        btnSave = findViewById(R.id.btnSave);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        btnChupAnh = findViewById(R.id.btnChupAnh);
        btnQuetMaVach = findViewById(R.id.btnQuetMaVach);
        spinnerLoaiHang = findViewById(R.id.spinnerLoaiHang);
        dbHelper = new DatabaseHelper(this);

        // Gán sự kiện cho các nút và spinner
        setupSpinner();
        btnChonAnh.setOnClickListener(v -> openImageChooser());
        btnChupAnh.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
        btnSave.setOnClickListener(v -> handleProductSave());
        btnQuetMaVach.setOnClickListener(v -> startBarcodeScan());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Chọn loại hàng", "Thực phẩm", "Đồ gia dụng", "Thời trang", "Khác"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoaiHang.setAdapter(adapter);

        spinnerLoaiHang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLoaiHang = position == 0 ? null : (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLoaiHang = "Khác";
            }
        });
    }

    private void startBarcodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Quét mã vạch");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo file ảnh.", Toast.LENGTH_SHORT).show();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                imgSanPham.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi chọn ảnh.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                selectedImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                imgSanPham.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xử lý ảnh.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleProductSave() {
        if (!validateInput()) return;

        String imagePath = saveImageToInternalStorage(selectedImageBitmap);
        if (imagePath == null) {
            Toast.makeText(this, "Lỗi khi lưu ảnh sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        String barcode = etMaVach.getText().toString().trim();
        String tenHang = etTenHang.getText().toString().trim();
        double gia = Double.parseDouble(etGia.getText().toString().trim());
        int soLuong = Integer.parseInt(etSoLuong.getText().toString().trim());
        String importDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (dbHelper.isBarcodeExists(barcode)) {
            boolean updated = dbHelper.updateProductByBarcode(barcode, tenHang, gia, soLuong, selectedLoaiHang, imagePath, importDate);
            if (updated) {
                Toast.makeText(this, "Cập nhật sản phẩm thành công.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật sản phẩm.", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveNewProduct();
        }
    }

    private void saveNewProduct() {
        if (!validateInput()) return;

        String imagePath = saveImageToInternalStorage(selectedImageBitmap);
        if (imagePath == null) {
            Toast.makeText(this, "Lỗi khi lưu ảnh sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenHang = etTenHang.getText().toString().trim();
        double gia = Double.parseDouble(etGia.getText().toString().trim());
        int soLuong = Integer.parseInt(etSoLuong.getText().toString().trim());
        String maVach = etMaVach.getText().toString().trim();
        String importDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        long productId = dbHelper.addProduct(tenHang, gia, soLuong, maVach, selectedLoaiHang, imagePath, importDate);
        if (productId != -1) {
            Log.d("saveNewProduct", "Thêm sản phẩm mới thành công. ID: " + productId);
            Toast.makeText(this, "Thêm hàng hóa thành công.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi khi thêm hàng hóa. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }




    private boolean validateInput() {
        boolean isValid = true;

        if (etTenHang.getText().toString().trim().isEmpty()) {
            etTenHang.setError("Tên hàng không được để trống");
            isValid = false;
        }

        if (etSoLuong.getText().toString().trim().isEmpty()) {
            etSoLuong.setError("Số lượng không được để trống");
            isValid = false;
        } else {
            try {
                int soLuong = Integer.parseInt(etSoLuong.getText().toString().trim());
                if (soLuong <= 0) {
                    etSoLuong.setError("Số lượng phải lớn hơn 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etSoLuong.setError("Số lượng không hợp lệ");
                isValid = false;
            }
        }

        if (etGia.getText().toString().trim().isEmpty()) {
            etGia.setError("Giá không được để trống");
            isValid = false;
        } else {
            try {
                double gia = Double.parseDouble(etGia.getText().toString().trim());
                if (gia <= 0) {
                    etGia.setError("Giá phải lớn hơn 0");
                    isValid = false;
                } else if (gia > 1_000_000_000) { // Giới hạn giá tối đa là 1 tỷ VNĐ
                    etGia.setError("Giá không được vượt quá 1 tỷ VNĐ");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etGia.setError("Giá không hợp lệ");
                isValid = false;
            }
        }

        if (selectedLoaiHang == null || selectedLoaiHang.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại hàng", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }


    private String saveImageToInternalStorage(Bitmap bitmap) {
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("productImages", Context.MODE_PRIVATE);
        File imagePath = new File(directory, "IMG_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return imagePath.getAbsolutePath();
    }
}
