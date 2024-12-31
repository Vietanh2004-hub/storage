package com.vanh.du_an_kho_hang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

    public class MainActivity extends BaseActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Kiểm tra trạng thái mở ứng dụng lần đầu
            SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

            if (isFirstRun) {
                // Nếu là lần đầu tiên, chuyển hướng đến WelcomeActivity
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);

                preferences.edit().putBoolean("isFirstRun", false).apply(); // Lưu trạng thái
                finish();
                return;
            }

            // Hiển thị nội dung MainActivity
            setContentView(R.layout.activity_main);

            // Thiết lập Navigation Drawer
            setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);

            // Thêm sự kiện cho các nút
            findViewById(R.id.btnNhapHang).setOnClickListener(v -> startActivity(new Intent(this, Nhaphang.class)));
            findViewById(R.id.btnXuatHang).setOnClickListener(v -> startActivity(new Intent(this, Xuathang.class)));
            findViewById(R.id.btnHangHoa).setOnClickListener(v -> startActivity(new Intent(this, Hanghoa.class)));
            findViewById(R.id.btnThongKe).setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        }
    }