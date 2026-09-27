package com.sikui.emoji;

import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SikuiInputMethodService extends InputMethodService {
    private static final int[] CUSTOM = {0xF0000,0xF0001,0xF0002,0xF0003,0xF0004,0xF0005};

    // Fixed, real PNG bytes. The uploaded files had JPEG bytes with a .png name;
    // these are converted valid PNG snapshots embedded directly so the keyboard
    // does not depend on missing/broken drawable files.
    private static final String[] PNG = {
        "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"
    };

    @Override public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(8,8,8,8);
        root.setBackgroundColor(Color.rgb(16,19,26));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        for (int i=0;i<CUSTOM.length;i++) {
            final int n=i;
            ImageView key = new ImageView(this);
            byte[] bytes = Base64.decode(PNG[i], Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            key.setImageBitmap(bitmap);
            key.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            key.setBackground(keyBg());
            key.setContentDescription("SIKUI U+" + Integer.toHexString(CUSTOM[i]).toUpperCase());
            key.setOnClickListener(v -> sendCustom(CUSTOM[n]));
            row.addView(key,new LinearLayout.LayoutParams(0,82,1));
        }
        root.addView(row);

        String[] vanilla={"😀","😂","😍","😎","😭","😡","👍","❤️","🔥","🎉","✨","💀"};
        LinearLayout row2=new LinearLayout(this);
        row2.setGravity(Gravity.CENTER);
        for(String emoji:vanilla){
            Button key=new Button(this);
            key.setText(emoji);
            key.setTextSize(24);
            key.setOnClickListener(v->commit(emoji));
            row2.addView(key,new LinearLayout.LayoutParams(0,70,1));
        }
        root.addView(row2);

        Button space=new Button(this);
        space.setText("Boşluk");
        space.setOnClickListener(v->commit(" "));
        root.addView(space,new LinearLayout.LayoutParams(-1,64));
        return root;
    }

    private GradientDrawable keyBg(){
        GradientDrawable g=new GradientDrawable();
        g.setColor(Color.rgb(32,38,51));
        g.setCornerRadius(18);
        return g;
    }
    private void commit(String s){
        InputConnection c=getCurrentInputConnection();
        if(c!=null)c.commitText(s,1);
    }
    private void sendCustom(int cp){
        InputConnection c=getCurrentInputConnection();
        if(c!=null)c.commitText(new String(Character.toChars(cp)),1);
    }
}
