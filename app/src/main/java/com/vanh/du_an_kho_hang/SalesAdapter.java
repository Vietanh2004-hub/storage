package com.vanh.du_an_kho_hang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {
    private List<Product> salesList;
    private Set<Product> selectedItems;

    public SalesAdapter(List<Product> salesList) {
        this.salesList = salesList != null ? salesList : new ArrayList<>();
        this.selectedItems = new HashSet<>();
    }

    public List<Product> getSelectedItemsWithQuantities() {
        List<Product> selectedWithQuantities = new ArrayList<>();
        for (Product product : selectedItems) {
            if (product.getSoLuong() > 0) {
                selectedWithQuantities.add(product);
            }
        }
        return selectedWithQuantities;
    }

    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        Product product = salesList.get(position);

        // Hiển thị thông tin sản phẩm
        holder.productName.setText(product.getTen());
        holder.productPrice.setText("Giá: " + product.getGia());
        holder.productStock.setText("Tồn kho: " + product.getSoLuong());

        // Hiển thị hình ảnh sản phẩm nếu có
        if (product.getImagePath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(product.getImagePath());
            holder.productImage.setImageBitmap(bitmap);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_image_placeholder);
        }


        // Đồng bộ trạng thái của CheckBox
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedItems.contains(product));

        // Lắng nghe sự kiện chọn/bỏ chọn sản phẩm
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(product);
            } else {
                selectedItems.remove(product);
            }
        });

        // Lắng nghe thay đổi số lượng xuất
        holder.exportQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int quantity = Integer.parseInt(s.toString().trim());

                    if (quantity > 0 && quantity <= product.getSoLuong()) {
                        product.setTempExportQuantity(quantity); // Lưu số lượng tạm thời
                        holder.exportQuantity.setError(null);
                    } else if (quantity > product.getSoLuong()) {
                        holder.exportQuantity.setError("Số lượng vượt quá tồn kho!");
                    } else {
                        holder.exportQuantity.setError("Số lượng phải lớn hơn 0!");
                    }
                } catch (NumberFormatException e) {
                    holder.exportQuantity.setError("Số lượng không hợp lệ!");
                    product.setTempExportQuantity(0); // Đặt số lượng tạm thời về 0 nếu không hợp lệ
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public void updateData(List<Product> newSalesList) {
        if (newSalesList != null) {
            salesList.clear();
            salesList.addAll(newSalesList);
            selectedItems.clear();
            notifyDataSetChanged();
        }
    }

    static class SalesViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productStock;
        ImageView productImage;
        EditText exportQuantity;
        CheckBox checkBox;

        public SalesViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
            productImage = itemView.findViewById(R.id.productImage);
            exportQuantity = itemView.findViewById(R.id.exportQuantity);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
