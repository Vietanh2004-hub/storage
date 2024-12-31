package com.vanh.du_an_kho_hang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageButton btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            // Lưu trạng thái rằng người dùng đã xem màn hình chào mừng
            SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstRun", false); // Đặt giá trị false sau khi xem màn hình chào mừng
            editor.apply();

            // Chuyển đến MainActivity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}


