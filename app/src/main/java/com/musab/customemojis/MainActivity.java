package com.musab.customemojis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);

        TextView title = new TextView(this);
        title.setText("Custom Emojis");
        title.setTextSize(28);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Özel emoji klavyesini seçmek için aşağıdaki düğmeye dokun.");
        info.setTextSize(16);
        root.addView(info);

        Button change = new Button(this);
        change.setText("Değiştir");
        change.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        root.addView(change);
        setContentView(root);
    }
}
