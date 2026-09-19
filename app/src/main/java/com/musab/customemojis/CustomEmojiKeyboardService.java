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
    private static final String PREFS="custom_emojis", KEY_URIS="image_uris";
    private boolean emojiMode=false;
    private boolean numberMode=false;

    private static final String[] EMOJIS={
        "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰",
        "😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
        "😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😡",
        "😠","🤬","🤯","😳","🥶","🥵","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥",
        "😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐",
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🔥","⭐","✨","🎮","🤖","👻","🍊","🎉"
    };

    private static final String[] SPECIAL_UNICODE={
        "👽","🍎","🥑","🎈","🍌","🏀","🏖️","🐝","🔔","🚲","🐦","🎂","📖","🏹","🍞","🍔",
        "🍰","📷","🍬","🚗","🐱","🍒","🐔","☁️","☕","☄️","🍪","👑","💎","🐶","🍩","🐉",
        "🌍","🥚","🎆","🌸","🏈","🎮","👻","🎁","🎸","🎧","🍦","🏝️","🔑","🥝","🍋","⚡",
        "🔒","✨","🍈","🎤","🌙","🍄","🎵","🐙","🍊","🐼","🎉","🍑","🍐","🐧","🍕","🪐",
        "🍿","🌈","🚀","🌹","🥪","🛰️","🦈","🛡️","⚽","✨","☀️","😎","⚔️","🌮","🧸","🌩️",
        "🎟️","🏆","🌷","🦄","🍉","🐋","🧙","🐺"
    };

    @Override public View onCreateInputView(){
        return buildKeyboard();
    }

    private View buildKeyboard(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(24,24,28));

        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this);
        title.setText(emojiMode?"EMOJİLER":(numberMode?"SAYILAR":"ABC"));
        title.setTextColor(Color.WHITE); title.setTextSize(13);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(30),1));

        Button emojiToggle=smallButton(emojiMode?"ABC":"😊");
        emojiToggle.setOnClickListener(v->{emojiMode=!emojiMode; numberMode=false; setInputView(buildKeyboard());});
        bar.addView(emojiToggle,new LinearLayout.LayoutParams(dp(52),dp(30)));
        Button numbers=smallButton(numberMode?"ABC":"123");
        numbers.setOnClickListener(v->{numberMode=!numberMode; emojiMode=false; setInputView(buildKeyboard());});
        bar.addView(numbers,new LinearLayout.LayoutParams(dp(52),dp(30)));
        Button settings=smallButton("⚙");
        settings.setOnClickListener(v->{
            Intent i=new Intent(this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        bar.addView(settings,new LinearLayout.LayoutParams(dp(42),dp(30)));
        root.addView(bar);

        if(emojiMode) buildEmojiPanel(root); else if(numberMode) buildNumberPanel(root); else buildAbcPanel(root);
        return root;
    }

    private void buildNumberPanel(LinearLayout root){
        ScrollView scroll=new ScrollView(this);
        LinearLayout inside=new LinearLayout(this);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setPadding(dp(2),dp(1),dp(2),dp(2));
        addTextRow(inside,"1234567890");
        addTextRow(inside,"-+=*/%()[]");
        addTextRow(inside,".,!?;:#@");
        GridLayout bottom=new GridLayout(this);
        bottom.setColumnCount(4);
        addKey(bottom,"⌫",this::deleteOne);
        addKey(bottom,"SPACE",()->commitUnicode(" "));
        addKey(bottom,"ABC",()->{numberMode=false; setInputView(buildKeyboard());});
        addKey(bottom,"↵",()->commitUnicode("\n"));
        inside.addView(bottom,new LinearLayout.LayoutParams(-1,dp(40)));
        scroll.addView(inside);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void buildAbcPanel(LinearLayout root){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout inside=new LinearLayout(this);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setPadding(dp(2),dp(1),dp(2),dp(2));

        addTextRow(inside,"1234567890");
        addTextRow(inside,"QWERTYUIOP");
        addTextRow(inside,"ASDFGHJKL");
        addTextRow(inside,"ZXCVBNM");
        addTextRow(inside,"ÇĞİÖŞÜ");

        GridLayout bottom=new GridLayout(this);
        bottom.setColumnCount(4);
        addKey(bottom,"⌫",this::deleteOne);
        addKey(bottom,"SPACE",()->commitUnicode(" "));
        addKey(bottom,",",()->commitUnicode(","));
        addKey(bottom,"↵",()->commitUnicode("\n"));
        inside.addView(bottom,new LinearLayout.LayoutParams(-1,dp(40)));

        scroll.addView(inside);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void addTextRow(LinearLayout parent,String row){
        GridLayout g=new GridLayout(this);
        g.setColumnCount(row.length());
        for(int i=0;i<row.length();i++){
            final String key=String.valueOf(row.charAt(i));
            addKey(g,key,()->commitUnicode(key));
        }
        parent.addView(g,new LinearLayout.LayoutParams(-1,dp(38)));
    }

    private void buildEmojiPanel(LinearLayout root){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout inside=new LinearLayout(this);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setPadding(dp(2),dp(1),dp(2),dp(2));

        TextView header=label("96 SKIN + 88 ÖZEL • KAYDIR");
        inside.addView(header,new LinearLayout.LayoutParams(-1,dp(24)));

        GridLayout skinGrid=new GridLayout(this);
        skinGrid.setColumnCount(8);
        for(String raw:readUris()) addCustomImage(skinGrid,Uri.parse(raw));
        for(int i=0;i<EMOJIS.length;i++){
            final int index=i;
            Bitmap b=loadNamedPng("skin_",index);
            if(b!=null) addBitmapButton(skinGrid,b,EMOJIS[i],()->commitUnicode(EMOJIS[index]));
        }
        inside.addView(skinGrid,new LinearLayout.LayoutParams(-1,-2));

        TextView sh=label("88 ÖZEL");
        sh.setPadding(0,dp(4),0,0);
        inside.addView(sh,new LinearLayout.LayoutParams(-1,dp(26)));

        GridLayout specialGrid=new GridLayout(this);
        specialGrid.setColumnCount(8);
        for(int i=0;i<SPECIAL_UNICODE.length;i++){
            final int index=i;
            Bitmap b=loadNamedPng("special_",index);
            if(b!=null) addBitmapButton(specialGrid,b,SPECIAL_UNICODE[i],()->commitUnicode(SPECIAL_UNICODE[index]));
        }
        inside.addView(specialGrid,new LinearLayout.LayoutParams(-1,-2));

        Button add=smallButton("+ PNG EKLE");
        add.setOnClickListener(v->{
            Intent i=new Intent(this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        inside.addView(add,new LinearLayout.LayoutParams(-1,dp(34)));

        scroll.addView(inside);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private TextView label(String text){
        TextView t=new TextView(this);
        t.setText(text); t.setTextColor(Color.WHITE); t.setTextSize(11); t.setGravity(Gravity.CENTER);
        return t;
    }

    private void addKey(GridLayout grid,String text,Runnable action){
        Button b=smallButton(text);
        b.setTextSize(text.length()>1?10:14);
        b.setOnClickListener(v->{animateKeyPress(b); action.run();});
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(36);
        lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        grid.addView(b,lp);
    }

    private void animateKeyPress(View v){
        v.setBackgroundColor(Color.rgb(20,78,145));
        v.animate().translationY(-dp(8)).alpha(0.88f).setDuration(90).withEndAction(() ->
            v.animate().translationY(0).alpha(1f).setDuration(140).withEndAction(() ->
                v.setBackgroundColor(Color.rgb(52,52,62))
            ).start()
        ).start();
    }

    private Button smallButton(String text){
        Button b=new Button(this);
        b.setText(text); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setPadding(0,0,0,0); b.setMinHeight(0); b.setMinWidth(0);
        b.setBackgroundColor(Color.rgb(52,52,62));
        return b;
    }

    private void addBitmapButton(GridLayout grid,Bitmap bitmap,String description,Runnable action){
        ImageButton b=new ImageButton(this);
        b.setBackgroundColor(Color.rgb(42,42,50)); b.setPadding(dp(1),dp(1),dp(1),dp(1));
        b.setScaleType(ImageButton.ScaleType.CENTER_INSIDE); b.setImageBitmap(bitmap);
        b.setContentDescription(description); b.setOnClickListener(v->{animateKeyPress(b); action.run();});
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(36);
        lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        grid.addView(b,lp);
    }

    private Bitmap loadNamedPng(String prefix,int index){
        try{
            String name=prefix+String.format(java.util.Locale.US,"%03d",index);
            int id=getResources().getIdentifier(name,"drawable",getPackageName());
            return id==0?null:BitmapFactory.decodeResource(getResources(),id);
        }catch(Throwable e){return null;}
    }

    private void addCustomImage(GridLayout grid,Uri uri){
        Bitmap bitmap=safeDecode(uri);
        if(bitmap!=null) addBitmapButton(grid,bitmap,"Özel PNG",()->commitImage(uri));
    }

    private Bitmap safeDecode(Uri uri){
        try{
            BitmapFactory.Options bounds=new BitmapFactory.Options(); bounds.inJustDecodeBounds=true;
            try(InputStream in=getContentResolver().openInputStream(uri)){
                if(in==null)return null; BitmapFactory.decodeStream(in,null,bounds);
            }
            if(bounds.outWidth<=0||bounds.outHeight<=0)return null;
            int sample=1,max=Math.max(bounds.outWidth,bounds.outHeight);
            while(max/sample>512)sample*=2;
            BitmapFactory.Options o=new BitmapFactory.Options();
            o.inSampleSize=sample; o.inPreferredConfig=Bitmap.Config.ARGB_8888;
            try(InputStream in=getContentResolver().openInputStream(uri)){
                return in==null?null:BitmapFactory.decodeStream(in,null,o);
            }
        }catch(Throwable e){return null;}
    }

    private void commitUnicode(String text){
        InputConnection ic=getCurrentInputConnection();
        if(ic!=null) ic.commitText(text,1);
    }

    private void deleteOne(){
        InputConnection ic=getCurrentInputConnection();
        if(ic!=null) ic.deleteSurroundingText(1,0);
    }

    private void commitImage(Uri uri){
        InputConnection ic=getCurrentInputConnection();
        if(ic==null||Build.VERSION.SDK_INT<25)return;
        try{
            String mime=getContentResolver().getType(uri);
            if(mime==null||!mime.startsWith("image/"))mime="image/png";
            InputContentInfo info=new InputContentInfo(uri,new ClipDescription("Custom Emoji",new String[]{mime}),null);
            ic.commitContent(info,InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,new Bundle());
        }catch(Throwable ignored){}
    }

    private List<String> readUris(){
        Set<String> set=getSharedPreferences(PREFS,MODE_PRIVATE).getStringSet(KEY_URIS,new HashSet<>());
        List<String> result=new ArrayList<>(set); Collections.sort(result); return result;
    }

    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
