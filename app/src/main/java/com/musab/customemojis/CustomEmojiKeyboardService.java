package com.musab.customemojis;

import android.content.ClipDescription;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.inputmethodservice.InputMethodService;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomEmojiKeyboardService extends InputMethodService {
    private static final String PREFS = "custom_emojis";
    private static final String KEY_URIS = "image_uris";

    private static final String[] EMOJIS = {
        "😀","😃","😄","😁","😆","😅","😂","🤣",
        "😊","😇","🙂","🙃","😉","😌","😍","🥰",
        "😘","😗","😙","😚","😋","😛","😝","😜",
        "🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
        "😒","😞","😔","😟","😕","🙁","☹️","😣",
        "😖","😫","😩","🥺","😢","😭","😤","😡",
        "😠","🤬","🤯","😳","🥶","🥵","😱","😨",
        "😰","😥","😓","🤗","🤔","🤭","🤫","🤥",
        "😶","😐","😑","😬","🙄","😯","😦","😧",
        "😮","😲","🥱","😴","🤤","😪","😵","🤐",
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍",
        "🔥","⭐","✨","🎮","🤖","👻","🍊","🎉"
    };

    private final int[] SKINS = {
        R.drawable.skin_smile, R.drawable.skin_laugh, R.drawable.skin_cool,
        R.drawable.skin_heart, R.drawable.skin_fire, R.drawable.skin_ghost,
        R.drawable.skin_robot, R.drawable.skin_sparkles
    };

    @Override public View onCreateInputView() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(8, 8, 8, 8);
        grid.setBackgroundColor(Color.rgb(24, 24, 28));

        for (int i = 0; i < EMOJIS.length; i++) {
            final String emoji = EMOJIS[i];
            ImageButton b = new ImageButton(this);
            b.setImageResource(SKINS[i % SKINS.length]);
            b.setContentDescription(emoji);
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setPadding(8, 8, 8, 8);
            b.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
            b.setOnClickListener(v -> commitUnicode(emoji));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp(72);
            lp.height = dp(64);
            grid.addView(b, lp);
        }

        List<String> customUris = readUris();
        for (String raw : customUris) {
            addCustomImage(grid, Uri.parse(raw));
        }

        TextView add = new TextView(this);
        add.setText("+\nÖzel");
        add.setTextColor(Color.WHITE);
        add.setTextSize(14);
        add.setGravity(Gravity.CENTER);
        add.setBackgroundColor(Color.rgb(55, 55, 65));
        add.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        GridLayout.LayoutParams addLp = new GridLayout.LayoutParams();
        addLp.width = dp(72);
        addLp.height = dp(64);
        grid.addView(add, addLp);

        return grid;
    }

    private void addCustomImage(GridLayout grid, Uri uri) {
        ImageButton b = new ImageButton(this);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setPadding(8, 8, 8, 8);
        b.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        Bitmap bitmap = safeDecode(uri);
        if (bitmap != null) {
            b.setImageBitmap(bitmap);
            b.setContentDescription("Özel emoji");
            b.setOnClickListener(v -> commitImage(uri));
        } else {
            b.setImageResource(R.drawable.skin_smile);
            b.setContentDescription("Görsel açılamadı");
            b.setOnClickListener(v -> commitUnicode("🙂"));
        }
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = dp(72);
        lp.height = dp(64);
        grid.addView(b, lp);
    }

    private Bitmap safeDecode(Uri uri) {
        try {
            if (uri == null) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            int sample = 1;
            while (maxSide / sample > 1024) sample *= 2;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                return BitmapFactory.decodeStream(in, null, opts);
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void commitUnicode(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(text, 1);
    }

    private void commitImage(Uri uri) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        if (Build.VERSION.SDK_INT >= 25) {
            try {
                String mime = getContentResolver().getType(uri);
                if (mime == null || !mime.startsWith("image/")) mime = "image/png";
                ClipDescription desc = new ClipDescription("Custom Emoji", new String[]{mime});
                InputContentInfo info = new InputContentInfo(uri, desc, null);
                boolean accepted = ic.commitContent(
                        info,
                        InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
                        new Bundle()
                );
                if (accepted) return;
            } catch (Throwable ignored) {}
        }
        commitUnicode("🖼️");
    }

    private List<String> readUris() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        Set<String> set = p.getStringSet(KEY_URIS, new HashSet<>());
        List<String> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
