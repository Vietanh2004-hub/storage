package com.vanh.du_an_kho_hang;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    private Button tvPolicy, tvHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Thiết lập thanh menu
        setupNavigationDrawer(R.id.toolbar, R.id.drawer_layout, R.id.navigation_view);

        tvPolicy = findViewById(R.id.tvPolicy);
        tvHelp = findViewById(R.id.tvHelp);

        tvPolicy.setOnClickListener(v -> showPolicyDialog());
        tvHelp.setOnClickListener(v -> showHelpDialog());
    }

    // Hiển thị dialog cho chính sách bảo mật
    private void showPolicyDialog() {
        String policyMessage = "Chính sách bảo mật:\n\n"
                + "1. Mục đích thu thập thông tin:\n"
                + "- Quản lý và theo dõi hàng hóa trong kho.\n"
                + "- Cung cấp các chức năng thống kê, báo cáo và quản lý dữ liệu liên quan.\n"
                + "- Cải thiện chất lượng dịch vụ và trải nghiệm người dùng.\n\n"
                + "2. Thông tin được thu thập:\n"
                + "- Thông tin cá nhân: Tên, địa chỉ email, số điện thoại (nếu cần).\n"
                + "- Dữ liệu liên quan đến hàng hóa: Tên, mã vạch, số lượng, giá cả.\n"
                + "- Nhật ký hoạt động: Thời gian nhập, xuất hoặc chỉnh sửa dữ liệu.\n\n"
                + "3. Cách sử dụng thông tin:\n"
                + "- Quản lý và tối ưu hóa hoạt động kho.\n"
                + "- Hiển thị báo cáo và thống kê hàng hóa.\n"
                + "- Bảo vệ thông tin người dùng và dữ liệu kho.\n\n"
                + "4. Quyền của người dùng:\n"
                + "- Truy cập, chỉnh sửa hoặc xóa dữ liệu cá nhân và thông tin liên quan đến kho.\n"
                + "- Liên hệ để đặt câu hỏi hoặc khiếu nại về xử lý dữ liệu.\n\n"
                + "Liên hệ hỗ trợ:\n"
                + "- Email: vvannhh1969@gmail.com\n";


        new AlertDialog.Builder(this)
                .setTitle("Chính sách bảo mật")
                .setMessage(policyMessage)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Hiển thị dialog cho yêu cầu trợ giúp
    private void showHelpDialog() {
        String helpMessage = "Trợ giúp:\n\n"
                + "Nếu bạn gặp vấn đề trong việc sử dụng ứng dụng, vui lòng liên hệ tôi qua Email hỗ trợ:\n"
                + "- Email: vvannhh1969@gmail.com\n"
                + "Chúng tôi sẽ hỗ trợ bạn sớm nhất có thể.";

        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu trợ giúp")
                .setMessage(helpMessage)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

