package com.vanh.du_an_kho_hang;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class CustomTextWatcher implements TextWatcher {
    private EditText editText;

    public CustomTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Xóa lỗi khi người dùng bắt đầu nhập
        editText.setBackgroundResource(R.drawable.edittext_normal);
        editText.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
