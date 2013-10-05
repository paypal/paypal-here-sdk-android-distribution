/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.BitmapUtils;

/**
 * This class is meant to collect the signature from the customer. It creates a
 * bitmap image on which the customer provides a signature, which is used by the
 * transaction manager to complete the transaction.
 * <p/>
 */
public class SignatureActivity extends Activity {

    private static final String LOG = "PayPalHere.SignatureActivity";
    LinearLayout mLayout;
    View mView;
    Bitmap mBitmap;
    Signature mSignature;
    Button mClearSign;
    Button mUseSign;

    /**
     * Initialize the elements in the layout.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        // Layout of this UI screen.
        mLayout = (LinearLayout) findViewById(R.id.signature_layout);
        // Assign this layout to a view.
        mView = mLayout;
        // Create a new signature object that would be used to take the
        // customer's signature.
        mSignature = new Signature(getApplicationContext(), null);
        // Add this signature bitmap view to this screen layout.
        mLayout.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        mUseSign = (Button) findViewById(R.id.use_sign);
        mUseSign.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(LOG, "Saving the signature");
                // Save the view in a bitmap image.
                mView.setDrawingCacheEnabled(true);
                mSignature.save(mView);
                // Store the bitmap into a common variable, which could be accessed later
                // by the CreditCardPeripheralActivity.
                MyActivity.setBitmap(mBitmap);
                Intent intent = new Intent(SignatureActivity.this, CreditCardPeripheralActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mClearSign = (Button) findViewById(R.id.clear_sign);
        mClearSign.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Clearing the view and disable the "use signature" button.
                Log.d(LOG, "Clearing the signature");
                mSignature.clear();
                mUseSign.setEnabled(false);
            }
        });

    }

    /**
     * This helper class contains all the signature related actions.
     */
    public class Signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private final RectF dirtyRect = new RectF();
        private Paint paint = new Paint();
        private Path path = new Path();
        private float lastTouchX;
        private float lastTouchY;

        // create a constructor by setting the color, stroke properties etc.
        public Signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        /**
         * This method is called when the customer is completed with providing
         * their signature.
         *
         * @param v
         */
        public void save(View v) {
            Log.v(LOG, "Width: " + v.getWidth());
            Log.v(LOG, "Height: " + v.getHeight());

            if (mBitmap == null) {
                // Create a bitmap based off of the view that has the signature.
                mBitmap = Bitmap.createBitmap(mLayout.getWidth(), mLayout.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(mBitmap);
            try {
                // Draw it on the canvas to show the same in the UI.
                v.draw(canvas);

            } catch (Exception e) {
                Log.v(LOG, e.toString());
            }

        }

        /**
         * This method is called when the customer wants to clear the provided
         * signature on the UI and start over again.
         */
        public void clear() {
            path.reset();
            invalidate();
        }

        /**
         * This method is used draw the sketch on the UI view.
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        /**
         * This method is used to draw the hand driven motion on the screen.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            // Enable the "use sign" button if the user has drawn something on
            // the screen as a signature.
            mUseSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    Log.d(LOG, event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH), (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH), (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

}
