package com.musab.customemojis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int PICK_EMOJI = 42;
    private static final String PREFS = "custom_emojis";
    private static final String KEY_URIS = "image_uris";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 40, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Custom Emojis");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Özel emoji klavyesini seç ve standart emojilerin özel skinlerini kullan.");
        info.setTextSize(16);
        info.setPadding(0, 18, 0, 18);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button change = new Button(this);
        change.setText("Klavyeyi etkinleştir / değiştir");
        change.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) return;
            if (isCustomImeEnabled(imm)) {
                imm.showInputMethodPicker();
            } else {
                try {
                    startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                } catch (Exception ignored) {}
            }
        });
        root.addView(change, new LinearLayout.LayoutParams(-1, -2));

        Button add = new Button(this);
        add.setText("Özel emoji ekle (görsel)");
        add.setOnClickListener(v -> pickEmojiImage());
        root.addView(add, new LinearLayout.LayoutParams(-1, -2));

        Button settings = new Button(this);
        settings.setText("Klavye ayarlarını aç");
        settings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception ignored) {}
        });
        root.addView(settings, new LinearLayout.LayoutParams(-1, -2));

        TextView count = new TextView(this);
        count.setText("Eklenen özel emoji: " + getUriCount());
        count.setTextSize(15);
        count.setPadding(0, 18, 0, 0);
        root.addView(count);

        setContentView(root);
    }

    private boolean isCustomImeEnabled(InputMethodManager imm) {
        String target = getPackageName() + "/.CustomEmojiKeyboardService";
        try {
            for (android.view.inputmethod.InputMethodInfo info : imm.getEnabledInputMethodList()) {
                if (target.equals(info.getId())) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private int getUriCount() {
        Set<String> set = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getStringSet(KEY_URIS, new HashSet<>());
        return set.size();
    }

    private void pickEmojiImage() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(i, PICK_EMOJI);
        } catch (Exception ignored) {}
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_EMOJI || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            final int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, flags);
        } catch (Exception ignored) {}

        Set<String> old = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getStringSet(KEY_URIS, new HashSet<>());
        Set<String> copy = new HashSet<>(old);
        copy.add(uri.toString());
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putStringSet(KEY_URIS, copy).apply();
        sendBroadcast(new Intent("com.musab.customemojis.EMOJI_CHANGED").setPackage(getPackageName()));
        recreate();
    }
}
