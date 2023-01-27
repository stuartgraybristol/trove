package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.example.stucollyn.nfc_play.R;
import com.svgandroid.SVG;
import com.svgandroid.SVGParser;

/**
 This class extends the SvgImageView Github project but it now let's us dynamically change the shape of the background archive image views and the image views themselves.
 This class cuts ImageViews into custom shapes using local svg vector resources.
 */

public class CustomImageView extends com.meg7.widget.SvgImageView {

    public int mSvgRawResourceId;

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedConstructor(context, attrs);
    }

    private void sharedConstructor(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomShapeImageView);
        mSvgRawResourceId = a.getResourceId(R.styleable.CustomShapeImageView_svg_raw_resource, 0);
        a.recycle();
    }

    //Get local svg resource and convert it to a Bitmap
    public static Bitmap getBitmap(Context context, int width, int height, int svgRawResourceId) {

        Log.i("Building shape", String.valueOf(svgRawResourceId));

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        if (svgRawResourceId > 0) {
            SVG svg = SVGParser.getSVGFromInputStream(
                    context.getResources().openRawResource(svgRawResourceId), width, height);
            canvas.drawPicture(svg.getPicture());
        } else {
            canvas.drawRect(new RectF(0.0f, 0.0f, width, height), paint);
        }

        return bitmap;
    }

    //This can be called to change the active svg resource that is desired
    public void setCustomImageResource(int resource) {

        mSvgRawResourceId= resource;
    }

    //Returns the bitmap
    @Override
    public Bitmap getBitmap() {
        return getBitmap(mContext, getWidth(), getHeight(), mSvgRawResourceId);
    }
}


