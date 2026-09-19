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
        "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰",
        "😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
        "😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😡",
        "😠","🤬","🤯","😳","🥶","🥵","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥",
        "😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐",
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🔥","⭐","✨","🎮","🤖","👻","🍊","🎉"
    };

    private static final String[] SPECIAL_NAMES = {
        "alien","apple","avocado","balloon","banana","basketball","beach","bee","bell","bike","bird","birthday",
        "book","bow","bread","burger","cake","camera","candy","car","cat","cherry","chess","chicken","cloud",
        "coffee","comet","cookie","crown","crystal","diamond","dog","donut","dragon","earth","egg","firework",
        "flower","football","gamepad","ghost","gift","guitar","hamburger","headphones","icecream","island","key",
        "kiwi","lemon","lightning","lock","magic","melon","microphone","moon","mushroom","music","octopus","orange",
        "panda","party","peach","pear","penguin","pizza","planet","popcorn","rainbow","rocket","rose","sandwich",
        "satellite","shark","shield","soccer","sparkle","sun","sunglasses","sword","taco","teddy","thunder","ticket",
        "trophy","tulip","unicorn","watermelon","whale","wizard","wolf","yo-yo"
    };

    private Bitmap skinSheet;
    private Bitmap specialSheet;

    @Override public View onCreateInputView() {
        skinSheet = loadBitmap(R.drawable.emoji_skins);
        specialSheet = loadBitmap(R.drawable.special_emojis);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(6, 6, 6, 6);
        grid.setBackgroundColor(Color.rgb(24, 24, 28));

        for (int i = 0; i < EMOJIS.length; i++) {
            final int index = i;
            final String emoji = EMOJIS[i];
            addBitmapButton(grid, crop(skinSheet, index, 12), emoji, () -> commitUnicode(emoji));
        }

        TextView header = new TextView(this);
        header.setText("88 ÖZEL EMOJI");
        header.setTextColor(Color.WHITE);
        header.setTextSize(13);
        header.setGravity(Gravity.CENTER);
        header.setPadding(4, 14, 4, 8);
        GridLayout.LayoutParams hp = new GridLayout.LayoutParams();
        hp.width = dp(72);
        hp.height = dp(42);
        grid.addView(header, hp);

        for (int i = 0; i < SPECIAL_NAMES.length; i++) {
            final int index = i;
            addBitmapButton(grid, crop(specialSheet, index, 11), SPECIAL_NAMES[index],
                    () -> commitUnicode("✨"));
        }

        for (String raw : readUris()) addCustomImage(grid, Uri.parse(raw));

        TextView add = new TextView(this);
        add.setText("+\nÖzel Görsel");
        add.setTextColor(Color.WHITE);
        add.setTextSize(13);
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

    private void addBitmapButton(GridLayout grid, Bitmap bitmap, String description, Runnable action) {
        ImageButton b = new ImageButton(this);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setPadding(6, 6, 6, 6);
        b.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        if (bitmap != null) b.setImageBitmap(bitmap);
        else b.setImageResource(R.drawable.skin_smile);
        b.setContentDescription(description);
        b.setOnClickListener(v -> action.run());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = dp(72);
        lp.height = dp(64);
        grid.addView(b, lp);
    }

    private Bitmap crop(Bitmap sheet, int index, int columns) {
        if (sheet == null) return null;
        int cell = sheet.getWidth() / columns;
        int x = (index % columns) * cell;
        int y = (index / columns) * cell;
        if (x + cell > sheet.getWidth() || y + cell > sheet.getHeight()) return null;
        return Bitmap.createBitmap(sheet, x, y, cell, cell);
    }

    private Bitmap loadBitmap(int id) {
        try {
            return BitmapFactory.decodeResource(getResources(), id);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void addCustomImage(GridLayout grid, Uri uri) {
        Bitmap bitmap = safeDecode(uri);
        addBitmapButton(grid, bitmap, "Özel emoji", () -> {
            if (bitmap != null) commitImage(uri);
            else commitUnicode("🖼️");
        });
    }

    private Bitmap safeDecode(Uri uri) {
        try {
            if (uri == null) return null;
            BitmapFactory.Options b = new BitmapFactory.Options();
            b.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, b);
            }
            if (b.outWidth <= 0 || b.outHeight <= 0) return null;
            int max = Math.max(b.outWidth, b.outHeight);
            int sample = 1;
            while (max / sample > 512) sample *= 2;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                return in == null ? null : BitmapFactory.decodeStream(in, null, o);
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
                if (ic.commitContent(info, InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION, new Bundle())) return;
            } catch (Throwable ignored) {}
        }
        commitUnicode("🖼️");
    }

    private List<String> readUris() {
        Set<String> set = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getStringSet(KEY_URIS, new HashSet<>());
        List<String> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}