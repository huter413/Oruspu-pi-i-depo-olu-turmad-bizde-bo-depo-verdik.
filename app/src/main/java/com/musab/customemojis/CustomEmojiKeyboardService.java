package com.musab.customemojis;

import android.content.ClipDescription;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private boolean emojiMode = false;

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
        "book","bow","bread","burger","cake","camera","candy","car","cat","cherry","chicken","cloud",
        "coffee","comet","cookie","crown","diamond","dog","donut","dragon","earth","egg","firework",
        "flower","football","gamepad","ghost","gift","guitar","headphones","icecream","island","key",
        "kiwi","lemon","lightning","lock","magic","melon","microphone","moon","mushroom","music","octopus","orange",
        "panda","party","peach","pear","penguin","pizza","planet","popcorn","rainbow","rocket","rose","sandwich",
        "satellite","shark","shield","soccer","sparkle","sun","sunglasses","sword","taco","teddy","thunder","ticket",
        "trophy","tulip","unicorn","watermelon","whale","wizard","wolf"
    };

    private static final String[] SPECIAL_UNICODE = {
        "👽","🍎","🥑","🎈","🍌","🏀","🏖️","🐝","🔔","🚲","🐦","🎂",
        "📖","🏹","🍞","🍔","🍰","📷","🍬","🚗","🐱","🍒","🐔","☁️",
        "☕","☄️","🍪","👑","💎","🐶","🍩","🐉","🌍","🥚","🎆",
        "🌸","🏈","🎮","👻","🎁","🎸","🎧","🍦","🏝️","🔑",
        "🥝","🍋","⚡","🔒","✨","🍈","🎤","🌙","🍄","🎵","🐙","🍊",
        "🐼","🎉","🍑","🍐","🐧","🍕","🪐","🍿","🌈","🚀","🌹","🥪",
        "🛰️","🦈","🛡️","⚽","✨","☀️","😎","⚔️","🌮","🧸","🌩️","🎟️",
        "🏆","🌷","🦄","🍉","🐋","🧙","🐺"
    };

    private Bitmap skinSheet;
    private Bitmap specialSheet;

    @Override public View onCreateInputView() {
        skinSheet = loadBitmap(R.drawable.emoji_skins);
        specialSheet = loadBitmap(R.drawable.special_emojis);
        return buildKeyboard();
    }

    private View buildKeyboard() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(24,24,28));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(4), dp(2), dp(4), dp(2));

        TextView title = new TextView(this);
        title.setText(emojiMode ? "😊 Emojiler" : "ABC Klavye");
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(38), 1));

        Button toggle = smallButton(emojiMode ? "ABC" : "😊");
        toggle.setOnClickListener(v -> {
            emojiMode = !emojiMode;
            setInputView(buildKeyboard());
        });
        top.addView(toggle, new LinearLayout.LayoutParams(dp(54), dp(38)));
        root.addView(top);

        if (emojiMode) buildEmojiPanel(root);
        else buildAbcPanel(root);

        return root;
    }

    private void buildAbcPanel(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout inside = new LinearLayout(this);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setPadding(dp(3),dp(2),dp(3),dp(3));

        String[] rows = {"QWERTYUIOP","ASDFGHJKL","ZXCVBNM"};
        for (String row : rows) {
            GridLayout g = new GridLayout(this);
            g.setColumnCount(row.length());
            for (int i=0;i<row.length();i++) {
                final String key=String.valueOf(row.charAt(i));
                addKey(g,key,() -> commitUnicode(key));
            }
            inside.addView(g,new LinearLayout.LayoutParams(-1,dp(48)));
        }

        GridLayout bottom = new GridLayout(this);
        bottom.setColumnCount(4);
        addKey(bottom,"⌫",this::deleteOne);
        addKey(bottom,"SPACE",() -> commitUnicode(" "));
        addKey(bottom,".",() -> commitUnicode("."));
        addKey(bottom,"↵",() -> commitUnicode("\n"));
        inside.addView(bottom,new LinearLayout.LayoutParams(-1,dp(50)));

        TextView hint = new TextView(this);
        hint.setText("Türkçe harfler: ç ğ ı ö ş ü");
        hint.setTextColor(Color.LTGRAY);
        hint.setGravity(Gravity.CENTER);
        hint.setTextSize(12);
        inside.addView(hint,new LinearLayout.LayoutParams(-1,dp(28)));

        GridLayout tr = new GridLayout(this);
        tr.setColumnCount(6);
        for (String key : new String[]{"Ç","Ğ","İ","Ö","Ş","Ü"}) addKey(tr,key,() -> commitUnicode(key));
        inside.addView(tr,new LinearLayout.LayoutParams(-1,dp(48)));

        scroll.addView(inside);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void buildEmojiPanel(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout inside = new LinearLayout(this);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setPadding(dp(3),dp(2),dp(3),dp(3));

        TextView header = new TextView(this);
        header.setText("96 SKIN • 88 ÖZEL");
        header.setTextColor(Color.WHITE);
        header.setTextSize(12);
        header.setGravity(Gravity.CENTER);
        inside.addView(header,new LinearLayout.LayoutParams(-1,dp(26)));

        GridLayout skinGrid = new GridLayout(this);
        skinGrid.setColumnCount(6);
        for (int i=0;i<EMOJIS.length;i++) {
            final int index=i;
            addBitmapButton(skinGrid,crop(skinSheet,index,12),EMOJIS[i],() -> commitUnicode(EMOJIS[index]));
        }
        inside.addView(skinGrid,new LinearLayout.LayoutParams(-1,-2));

        TextView specialHeader = new TextView(this);
        specialHeader.setText("88 ÖZEL EMOJI");
        specialHeader.setTextColor(Color.WHITE);
        specialHeader.setTextSize(12);
        specialHeader.setGravity(Gravity.CENTER);
        specialHeader.setPadding(0,dp(8),0,dp(3));
        inside.addView(specialHeader,new LinearLayout.LayoutParams(-1,dp(32)));

        GridLayout specialGrid = new GridLayout(this);
        specialGrid.setColumnCount(6);
        for (int i=0;i<SPECIAL_NAMES.length;i++) {
            final int index=i;
            addBitmapButton(specialGrid,crop(specialSheet,index,11),SPECIAL_NAMES[i],
                    () -> commitUnicode(SPECIAL_UNICODE[index]));
        }
        inside.addView(specialGrid,new LinearLayout.LayoutParams(-1,-2));

        for (String raw : readUris()) addCustomImage(specialGrid,Uri.parse(raw));

        Button add=smallButton("+ Özel PNG");
        add.setOnClickListener(v -> {
            Intent i=new Intent(this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        inside.addView(add,new LinearLayout.LayoutParams(-1,dp(42)));

        scroll.addView(inside);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void addKey(GridLayout grid,String label,Runnable action) {
        Button b=smallButton(label);
        b.setTextSize(label.length()>1?12:16);
        b.setOnClickListener(v -> action.run());
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(46); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        grid.addView(b,lp);
    }

    private Button smallButton(String text) {
        Button b=new Button(this);
        b.setText(text); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setPadding(0,0,0,0);
        b.setBackgroundColor(Color.rgb(52,52,62));
        return b;
    }

    private void addBitmapButton(GridLayout grid,Bitmap bitmap,String description,Runnable action) {
        ImageButton b=new ImageButton(this);
        b.setBackgroundColor(Color.rgb(42,42,50));
        b.setPadding(dp(2),dp(2),dp(2),dp(2));
        b.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        if(bitmap!=null) b.setImageBitmap(bitmap); else b.setImageResource(R.drawable.skin_smile);
        b.setContentDescription(description);
        b.setOnClickListener(v -> action.run());
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(52); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        grid.addView(b,lp);
    }

    private Bitmap crop(Bitmap sheet,int index,int columns) {
        if(sheet==null) return null;
        int cell=sheet.getWidth()/columns;
        int x=(index%columns)*cell, y=(index/columns)*cell;
        if(cell<=0 || x+cell>sheet.getWidth() || y+cell>sheet.getHeight()) return null;
        try{return Bitmap.createBitmap(sheet,x,y,cell,cell);}catch(Throwable e){return null;}
    }

    private Bitmap loadBitmap(int id) {
        try{return BitmapFactory.decodeResource(getResources(),id);}catch(Throwable e){return null;}
    }

    private void addCustomImage(GridLayout grid,Uri uri) {
        Bitmap bitmap=safeDecode(uri);
        addBitmapButton(grid,bitmap,"Özel PNG",() -> { if(bitmap!=null) commitImage(uri); });
    }

    private Bitmap safeDecode(Uri uri) {
        try {
            BitmapFactory.Options b=new BitmapFactory.Options(); b.inJustDecodeBounds=true;
            try(InputStream in=getContentResolver().openInputStream(uri)){
                if(in==null)return null; BitmapFactory.decodeStream(in,null,b);
            }
            if(b.outWidth<=0||b.outHeight<=0)return null;
            int sample=1,max=Math.max(b.outWidth,b.outHeight);
            while(max/sample>512)sample*=2;
            BitmapFactory.Options o=new BitmapFactory.Options(); o.inSampleSize=sample; o.inPreferredConfig=Bitmap.Config.ARGB_8888;
            try(InputStream in=getContentResolver().openInputStream(uri)){return in==null?null:BitmapFactory.decodeStream(in,null,o);}
        }catch(Throwable e){return null;}
    }

    private void commitUnicode(String text) {
        InputConnection ic=getCurrentInputConnection();
        if(ic!=null)ic.commitText(text,1);
    }

    private void deleteOne() {
        InputConnection ic=getCurrentInputConnection();
        if(ic!=null)ic.deleteSurroundingText(1,0);
    }

    private void commitImage(Uri uri) {
        InputConnection ic=getCurrentInputConnection();
        if(ic==null)return;
        if(Build.VERSION.SDK_INT>=25) {
            try {
                String mime=getContentResolver().getType(uri);
                if(mime==null||!mime.startsWith("image/"))mime="image/png";
                InputContentInfo info=new InputContentInfo(uri,new ClipDescription("Custom Emoji",new String[]{mime}),null);
                if(ic.commitContent(info,InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,new Bundle()))return;
            }catch(Throwable ignored){}
        }
    }

    private List<String> readUris() {
        Set<String> set=getSharedPreferences(PREFS,MODE_PRIVATE).getStringSet(KEY_URIS,new HashSet<>());
        List<String> result=new ArrayList<>(set); Collections.sort(result); return result;
    }

    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
