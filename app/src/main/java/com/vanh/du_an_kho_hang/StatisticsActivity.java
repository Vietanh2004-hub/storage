package com.vanh.du_an_kho_hang;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends BaseActivity {

    private TextView tvTotalImports, tvTotalSales;
    private RecyclerView recyclerViewStatistics;
    private BarChart barChart;
    private StatisticsAdapter adapter;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageButton btnClearSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);

        // Ánh xạ view
        tvTotalImports = findViewById(R.id.tvTotalImports);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        recyclerViewStatistics = findViewById(R.id.recyclerViewStatistics);
        barChart = findViewById(R.id.barChart);
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        TextView tvChartTitle = findViewById(R.id.tvChartTitle);
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo adapter với danh sách rỗng
        adapter = new StatisticsAdapter(new ArrayList<>(), this::showProductDetails);
        recyclerViewStatistics.setAdapter(adapter);

        // Tải dữ liệu ban đầu
        loadData();

        // Tìm kiếm sản phẩm
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

        // Xử lý nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            searchProducts(query);
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText(""); // Xóa nội dung tìm kiếm
            loadData(); // Tải lại dữ liệu ban đầu
        });
        tvChartTitle.setText("Thống kê hàng nhập và xuất");
    }

    private void loadData() {
        int totalImports = dbHelper.getTotalImports();
        int totalSales = dbHelper.getTotalSales();

        tvTotalImports.setText("Tổng hàng nhập: " + totalImports);
        tvTotalSales.setText("Tổng hàng xuất: " + totalSales);

        List<StatisticsItem> statisticsList = dbHelper.getStatisticsByItem();
        if (!statisticsList.isEmpty()) {
            recyclerViewStatistics.setVisibility(View.VISIBLE);
            adapter.updateData(statisticsList);

            // Hiển thị biểu đồ
            setupBarChart(statisticsList); // Gọi hàm để vẽ biểu đồ
        } else {
            recyclerViewStatistics.setVisibility(View.GONE);
            barChart.clear(); // Xóa dữ liệu biểu đồ nếu không có thống kê
            Toast.makeText(this, "Không có dữ liệu thống kê.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBarChart(List<StatisticsItem> statisticsList) {
        ArrayList<BarEntry> importEntries = new ArrayList<>();
        ArrayList<BarEntry> salesEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < statisticsList.size(); i++) {
            StatisticsItem item = statisticsList.get(i);
            importEntries.add(new BarEntry(i * 2, item.getImports())); // Nhập: cách xa để phân biệt nhóm
            salesEntries.add(new BarEntry(i * 2 + 1, item.getSales())); // Xuất: cách nhóm nhập
            labels.add(item.getName());
        }

        // Tạo dataset cho hàng nhập
        BarDataSet importDataSet = new BarDataSet(importEntries, "Hàng nhập");
        importDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);

        // Tạo dataset cho hàng xuất
        BarDataSet salesDataSet = new BarDataSet(salesEntries, "Hàng xuất");
        salesDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);

        // Tạo BarData từ các dataset
        BarData data = new BarData(importDataSet, salesDataSet);
        data.setBarWidth(0.4f); // Đặt độ rộng của cột (nên nhỏ hơn 0.5f để không chồng)

        barChart.setData(data);

        // Cài đặt trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f); // Hiển thị từng nhãn một
        xAxis.setCenterAxisLabels(true); // Căn chỉnh nhãn ở giữa nhóm cột
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Cài đặt khác cho biểu đồ
        barChart.getAxisRight().setEnabled(false); // Ẩn trục Y bên phải
        barChart.setFitBars(true); // Điều chỉnh các cột vừa vặn biểu đồ
        barChart.getDescription().setEnabled(false); // Ẩn mô tả trong biểu đồ
        barChart.animateY(1000);
        barChart.invalidate(); // Cập nhật biểu đồ
    }

    private void searchProducts(String query) {
        List<StatisticsItem> filteredList = dbHelper.searchStatistics(query);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy kết quả phù hợp.", Toast.LENGTH_SHORT).show();
        } else {
            adapter.updateData(filteredList);
            setupBarChart(filteredList); // Cập nhật biểu đồ theo tìm kiếm
        }
    }

    private void showProductDetails(StatisticsItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product_details, null);
        builder.setView(view);

        TextView tvName = view.findViewById(R.id.tvProductName);
        TextView tvBarcode = view.findViewById(R.id.tvProductBarcode);
        TextView tvImports = view.findViewById(R.id.tvTotalImports);
        TextView tvSales = view.findViewById(R.id.tvTotalSales);
        TextView tvPrice = view.findViewById(R.id.tvProductPrice);

        tvName.setText("Tên sản phẩm: " + item.getName());
        tvBarcode.setText("Mã vạch: " + item.getBarcode());
        tvImports.setText("Số lượng nhập: " + item.getImports());
        tvSales.setText("Số lượng xuất: " + item.getSales());
        tvPrice.setText("Giá: " + item.getPrice() + " VNĐ");

        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Tải lại dữ liệu khi quay lại Activity
    }
}
