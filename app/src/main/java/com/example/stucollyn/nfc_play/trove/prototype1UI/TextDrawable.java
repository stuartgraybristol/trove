package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by StuCollyn on 20/06/2018.
 */

public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private final Paint paint2;
    Canvas canvas;
    int[] colourCode;
    Random random;


    public TextDrawable(String text) {

        random = new Random();
//        int[] colourCode = {Color.parseColor("#756bc7"), Color.parseColor("#ffb491"),  Color.parseColor("#54b8a9")};

        this.text = text;

        this.paint = new Paint();
        paint.setColor(random.nextInt(2));
        paint.setTextSize(22f);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);

        this.paint2 = new Paint();
        paint2.setColor(Color.BLUE);
        paint2.setTextSize(22f);
        paint2.setAntiAlias(true);
        paint2.setFakeBoldText(true);
        paint2.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setTextAlign(Paint.Align.LEFT);
    }
//
//    public BitmapDrawable draw() {
//        Bitmap bm = BitmapFactory.decodeResource(getResources(), text).copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(bm);
//        canvas.drawText(text, 0, bm.getHeight()/2, paint);
//        return new BitmapDrawable(bm);
//    }

    @Override
    public void draw(Canvas canvas) {
        Log.d("HODY", "HODY");
        this.canvas = canvas;
        //canvas.drawText(text, 200, 200, paint);
//        canvas.drawCircle(100, 100, 100, paint);
        canvas.drawText(text, 100, 100, paint2);


    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void saveBitmap(Bitmap bitmap) {
        File file = new File(Environment.getExternalStorageDirectory() + "/sign.png");

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}