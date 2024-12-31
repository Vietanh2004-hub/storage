package com.vanh.du_an_kho_hang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private List<StatisticsItem> statisticsList;
    private OnItemClickListener onItemClickListener;

    // Interface xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(StatisticsItem item);
    }

    // Constructor
    public StatisticsAdapter(List<StatisticsItem> statisticsList, OnItemClickListener onItemClickListener) {
        this.statisticsList = statisticsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistics, parent, false);
        return new StatisticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        StatisticsItem item = statisticsList.get(position);

        // Thiết lập dữ liệu
        holder.tvItemName.setText(item.getName());
        holder.tvItemPrice.setText("Giá: " + item.getPrice() + " VNĐ");
        holder.tvImports.setText("Nhập: " + item.getImports());
        holder.tvSales.setText("Xuất: " + item.getSales());
        holder.tvImportDate.setText("Ngày nhập: " + item.getImportDate());
        holder.tvExportDate.setText("Ngày xuất: " + item.getExportDate());

        // Hiển thị hình ảnh sản phẩm
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(item.getImagePath());
            if (bitmap != null) {
                holder.ivProductImage.setImageBitmap(bitmap);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder); // Placeholder nếu không đọc được ảnh
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder); // Placeholder nếu không có đường dẫn ảnh
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }


    @Override
    public int getItemCount() {
        return statisticsList.size();
    }

    // Cập nhật dữ liệu
    public void updateData(List<StatisticsItem> newStatisticsList) {
        this.statisticsList = newStatisticsList;
        notifyDataSetChanged();
    }

    static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemPrice, tvImports, tvSales, tvImportDate, tvExportDate;
        ImageView ivProductImage;

        public StatisticsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvImports = itemView.findViewById(R.id.tvImports);
            tvSales = itemView.findViewById(R.id.tvSales);
            tvImportDate = itemView.findViewById(R.id.tvImportDate);
            tvExportDate = itemView.findViewById(R.id.tvExportDate);
        }
    }

}
