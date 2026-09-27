package com.sikui.emoji;

import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SikuiInputMethodService extends InputMethodService {
    private static final int[] CUSTOM = {
        0xF0000, 0xF0001, 0xF0002, 0xF0003, 0xF0004, 0xF0005
    };

    @Override
    public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(8, 8, 8, 8);
        root.setBackgroundColor(Color.rgb(16, 19, 26));

        LinearLayout customRow = new LinearLayout(this);
        customRow.setGravity(Gravity.CENTER);

        for (int i = 0; i < CUSTOM.length; i++) {
            final int codePoint = CUSTOM[i];
            TextView key = new TextView(this);
            key.setText(String.format("F%04X", codePoint & 0xFFFFF));
            key.setTextSize(16);
            key.setTextColor(Color.WHITE);
            key.setGravity(Gravity.CENTER);
            key.setTypeface(Typeface.DEFAULT_BOLD);
            key.setBackground(keyBg());
            key.setContentDescription("SIKUI U+" + Integer.toHexString(codePoint).toUpperCase());
            key.setOnClickListener(v -> sendCustom(codePoint));
            customRow.addView(key, new LinearLayout.LayoutParams(0, 82, 1));
        }
        root.addView(customRow);

        String[] vanilla = {"😀","😂","😍","😎","😭","😡","👍","❤️","🔥","🎉","✨","💀"};
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setGravity(Gravity.CENTER);

        for (String emoji : vanilla) {
            TextView key = new TextView(this);
            key.setText(emoji);
            key.setTextSize(28);
            key.setGravity(Gravity.CENTER);
            key.setBackground(keyBg());
            key.setOnClickListener(v -> commit(emoji));
            emojiRow.addView(key, new LinearLayout.LayoutParams(0, 70, 1));
        }
        root.addView(emojiRow);

        Button space = new Button(this);
        space.setText("Boşluk");
        space.setOnClickListener(v -> commit(" "));
        root.addView(space, new LinearLayout.LayoutParams(-1, 64));

        return root;
    }

    private GradientDrawable keyBg() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.rgb(32, 38, 51));
        g.setCornerRadius(18);
        return g;
    }

    private void commit(String text) {
        InputConnection c = getCurrentInputConnection();
        if (c != null) c.commitText(text, 1);
    }

    private void sendCustom(int codePoint) {
        InputConnection c = getCurrentInputConnection();
        if (c != null) c.commitText(new String(Character.toChars(codePoint)), 1);
    }
}
