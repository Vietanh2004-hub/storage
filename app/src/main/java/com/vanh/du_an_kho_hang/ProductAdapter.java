package com.vanh.du_an_kho_hang;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;
    private DatabaseHelper dbHelper;

    // Constructor
    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvTen.setText(product.getTen());
        holder.tvGia.setText("Giá: " + product.getGia());
        holder.tvSoLuong.setText("Số lượng: " + product.getSoLuong());
        holder.tvMaVach.setText("Mã vạch: " + product.getMaVach());
        holder.tvLoaiHang.setText("Loại hàng: " + product.getLoaiHang());

        // Kiểm tra đường dẫn ảnh trước khi decode
        if (product.getImagePath() != null && new File(product.getImagePath()).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(product.getImagePath());
            holder.imgSanPham.setImageBitmap(bitmap);
        } else {
            holder.imgSanPham.setImageResource(R.drawable.ic_image_placeholder); // Ảnh mặc định
        }

        // Sự kiện click vào item để chỉnh sửa sản phẩm
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof Hanghoa) {
                ((Hanghoa) context).showEditProductDialog(product);
            } else {
                Toast.makeText(context, "Không thể mở trình chỉnh sửa.", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện click vào nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Xóa sản phẩm khỏi Database
                        boolean success = dbHelper.deleteProductById(product.getId());
                        if (success) {
                            productList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, productList.size());
                            Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Cập nhật dữ liệu
    public void updateData(List<Product> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvGia, tvSoLuong, tvMaVach, tvLoaiHang;
        ImageView imgSanPham;
        ImageView btnDelete;

        ProductViewHolder(View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTen);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvMaVach = itemView.findViewById(R.id.tvMaVach);
            tvLoaiHang = itemView.findViewById(R.id.tvLoaiHang);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
