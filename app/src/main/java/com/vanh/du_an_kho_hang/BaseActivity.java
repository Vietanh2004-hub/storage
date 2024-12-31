package com.vanh.du_an_kho_hang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupNavigationDrawer(int toolbarId, int drawerLayoutId, int navigationViewId) {
        Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);

        // Tắt tiêu đề mặc định
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(drawerLayoutId);
        NavigationView navigationView = findViewById(navigationViewId);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_trang_chu) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_nhap_hang) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Nhaphang.class));
        } else if (id == R.id.nav_xuat_hang) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Xuathang.class));
        } else if (id == R.id.nav_hang_hoa) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Hanghoa.class));
        } else if (id == R.id.nav_thong_ke) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_cai_dat) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_danh_gia) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Đánh giá ứng dụng", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_chia_se) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
        } else if (id == R.id.nav_thong_tin) {
            Toast.makeText(this, "Thông tin liên hệ: vvannhh1969@gmail.com", Toast.LENGTH_SHORT).show();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}
