package com.musab.customemojis;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.GridLayout;
import android.graphics.Color;
import android.view.Gravity;

public class CustomEmojiKeyboardService extends InputMethodService {
    @Override public View onCreateInputView() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(12, 12, 12, 12);
        String[] emojis = {"😀","😂","😎","❤️","🔥","⭐","🍊","🤖","👻","🎮","✨","😈"};
        for (String emoji : emojis) {
            Button b = new Button(this);
            b.setText(emoji);
            b.setTextSize(26);
            b.setGravity(Gravity.CENTER);
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) ic.commitText(((Button)v).getText(), 1);
            });
            grid.addView(b, new GridLayout.LayoutParams());
        }
        return grid;
    }
}
