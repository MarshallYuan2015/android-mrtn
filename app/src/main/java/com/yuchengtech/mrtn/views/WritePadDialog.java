package com.yuchengtech.mrtn.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.yuchengtech.mrtn.R;
import com.yuchengtech.mrtn.utils.DialogListener;

public class WritePadDialog extends Dialog {
    static final int BACKGROUND_COLOR = Color.WHITE;
    static final int BRUSH_COLOR = Color.BLACK;
    Context context;
    LayoutParams p;
    DialogListener dialogListener;
    int win_width;
    int win_height;
    PaintView mView;
    /**
     * The index of the current color to use.
     */
    int mColorIndex;
    public WritePadDialog(Context context, DialogListener dialogListener,
                          int width, int height) {
        super(context);
        this.context = context;
        this.dialogListener = dialogListener;
        win_width = width;
        win_height = height;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.write_pad);

        p = getWindow().getAttributes();
        p.height = (int) (win_height * 0.6);
        p.width = (int) (win_width * 0.6);
        getWindow().setAttributes(p);
        mView = new PaintView(context);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.tablet_view);
        frameLayout.addView(mView);
        mView.requestFocus();

        Button btnClear = (Button) findViewById(R.id.tablet_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.clear();
            }
        });
        Button btnOk = (Button) findViewById(R.id.tablet_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dialogListener.refreshActivity(mView.getCachebBitmap());
                    WritePadDialog.this.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Button btnCancel = (Button) findViewById(R.id.tablet_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }


    class PaintView extends View {
        private Paint paint;
        private Canvas cacheCanvas;
        private Bitmap cachebBitmap;
        private Path path;
        private float cur_x, cur_y;

        public PaintView(Context context) {
            super(context);
            init();
        }

        public Bitmap getCachebBitmap() {
            return cachebBitmap;
        }

        private void init() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            path = new Path();
            cachebBitmap = Bitmap.createBitmap(p.width, (int) (p.height * 0.8),
                    Config.ARGB_8888);
            cacheCanvas = new Canvas(cachebBitmap);
            // cacheCanvas.drawColor(Color.WHITE);
            // cacheCanvas.drawColor(R.color.transparent);
        }

        public void clear() {
            if (cacheCanvas != null) {
                paint.setColor(BACKGROUND_COLOR);
                cacheCanvas.drawPaint(paint);
                paint.setColor(Color.BLACK);
                cacheCanvas.drawColor(Color.WHITE);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // canvas.drawColor(BRUSH_COLOR);
            canvas.drawBitmap(cachebBitmap, 0, 0, null);
            canvas.drawPath(path, paint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
            int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }
            if (curW < w)
                curW = w;
            if (curH < h)
                curH = h;
            Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
                    Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (cachebBitmap != null) {
                newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
            }
            cachebBitmap = newBitmap;
            cacheCanvas = newCanvas;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    cur_x = x;
                    cur_y = y;
                    path.moveTo(cur_x, cur_y);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    path.quadTo(cur_x, cur_y, x, y);
                    cur_x = x;
                    cur_y = y;
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    cacheCanvas.drawPath(path, paint);
                    path.reset();
                    break;
                }
            }
            invalidate();
            return true;
        }
    }
}
