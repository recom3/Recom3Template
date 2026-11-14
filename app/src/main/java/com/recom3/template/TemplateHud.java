package com.recom3.template;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.reconinstruments.ui.FontUtils;

final class TemplateHud extends View {
    private static final float DESIGN_WIDTH = 428f;
    private static final float DESIGN_HEIGHT = 240f;
    private static final int PANEL_COLOR = Color.rgb(15, 45, 58);
    private static final int TEXT_COLOR = Color.rgb(241, 249, 252);
    private static final int SECONDARY_COLOR = Color.rgb(160, 193, 205);
    private static final int READY_COLOR = Color.rgb(76, 227, 155);
    private static final int STATUS_COLOR = Color.rgb(255, 191, 112);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF(12, 42, 416, 206);
    private final Typeface regular;
    private final Typeface bold;
    private final String title;
    private final int background;

    private String status = "";
    private String metric = "";
    private String value = "";
    private String unit = "";
    private String controls = "";
    private boolean ready;

    TemplateHud(Context context) {
        super(context);
        regular = FontUtils.getFontFromRes(context, com.reconinstruments.ui.R.raw.opensans_regular);
        bold = FontUtils.getFontFromRes(context, com.reconinstruments.ui.R.raw.opensans_bold);
        title = context.getString(R.string.hud_title);
        background = getResources().getColor(R.color.hud_background);
    }

    // The activity owns the state. The view draws the latest snapshot on the UI thread.
    void show(String status, boolean ready, String metric, String value, String unit, String controls) {
        this.status = status;
        this.ready = ready;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.controls = controls;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(background);
        float scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        canvas.save();
        canvas.translate((getWidth() - DESIGN_WIDTH * scale) / 2,
                (getHeight() - DESIGN_HEIGHT * scale) / 2);
        canvas.scale(scale, scale);

        text(canvas, title, 17, 27, 17, TEXT_COLOR, bold, Paint.Align.LEFT);
        text(canvas, status, 411, 27, 13, ready ? READY_COLOR : STATUS_COLOR, bold,
                Paint.Align.RIGHT);
        paint.setColor(PANEL_COLOR);
        canvas.drawRoundRect(panel, 13, 13, paint);
        text(canvas, metric, 214, 74, 16, SECONDARY_COLOR, bold, Paint.Align.CENTER);
        text(canvas, value, 214, 145, 60, TEXT_COLOR, bold, Paint.Align.CENTER);
        text(canvas, unit, 214, 176, 19, SECONDARY_COLOR, regular, Paint.Align.CENTER);
        text(canvas, controls, 214, 230, 15, TEXT_COLOR, regular, Paint.Align.CENTER);
        canvas.restore();
    }

    private void text(Canvas canvas, String text, float x, float baseline, float size,
                      int color, Typeface font, Paint.Align align) {
        paint.setColor(color);
        paint.setTypeface(font);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        canvas.drawText(text, x, baseline, paint);
    }
}
