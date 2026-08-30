package com.example.javaqua.myaqua;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

public class FishSprite {
    private static final String TAG = "FishSprite";
    private static final Random random = new Random();
    // Scaling constants
    private static final int TARGET_FISH_HEIGHT_DIVISOR = 6;
    private static final float MIN_FISH_HEIGHT_PX = 30;
    private static final float MAX_FISH_HEIGHT_PX = 200;

    // Bitmap fields
    private Bitmap originalLoadedBitmap;
    private Bitmap scaledAndReadyBitmap;
    private final Bitmap placeholderBitmapRef;
    private final Bitmap errorBitmapRef;
    private boolean isScaledBitmapInternallyCreated = false;

    private float x, y;
    private float speedX, speedY;
    private boolean facingRight;
    private final Matrix matrix;
    private int viewCanvasWidth, viewCanvasHeight;

    private float wobbleAngle = 0;
    private final float wobbleSpeed = 0.1f;
    private final float wobbleMagnitude = 3f;

    public FishSprite(Bitmap placeholderBitmap, Bitmap errorBitmap, int initialCanvasWidth, int initialCanvasHeight) {
        this.placeholderBitmapRef = placeholderBitmap;
        this.errorBitmapRef = errorBitmap;
        this.originalLoadedBitmap = this.placeholderBitmapRef;
        this.matrix = new Matrix();
        this.viewCanvasWidth = initialCanvasWidth;
        this.viewCanvasHeight = initialCanvasHeight;

        Log.d(TAG, "FishSprite created. Initial canvas: " + initialCanvasWidth + "x" + initialCanvasHeight);
        attemptBitmapScalingAndInitialization();
    }


    public void setBitmap(Bitmap newBitmapFromGlide) {
        Log.d(TAG, "setBitmap called. New bitmap from Glide: " + (newBitmapFromGlide != null ? newBitmapFromGlide.getWidth() + "x" + newBitmapFromGlide.getHeight() : "null"));
        if (newBitmapFromGlide != null) {
            this.originalLoadedBitmap = newBitmapFromGlide;
        } else {

            this.originalLoadedBitmap = this.errorBitmapRef != null ? this.errorBitmapRef : this.placeholderBitmapRef;
            Log.d(TAG, "Using fallback bitmap: " + (this.originalLoadedBitmap != null ? this.originalLoadedBitmap.getWidth() + "x" + this.originalLoadedBitmap.getHeight() : "null"));
        }
        attemptBitmapScalingAndInitialization();
    }

    public void setCanvasDimensions(int width, int height) {
        if (this.viewCanvasWidth != width || this.viewCanvasHeight != height) {
            Log.d(TAG, "Canvas dimensions changed: " + width + "x" + height);
            this.viewCanvasWidth = width;
            this.viewCanvasHeight = height;
            attemptBitmapScalingAndInitialization();
        }
    }

    private void attemptBitmapScalingAndInitialization() {

        if (isScaledBitmapInternallyCreated && scaledAndReadyBitmap != null && !scaledAndReadyBitmap.isRecycled()) {
            if (scaledAndReadyBitmap != originalLoadedBitmap &&
                scaledAndReadyBitmap != placeholderBitmapRef &&
                scaledAndReadyBitmap != errorBitmapRef) {
                Log.d(TAG, "Recycling previously internally scaled bitmap.");
                scaledAndReadyBitmap.recycle();
            }
        }
        scaledAndReadyBitmap = null;
        isScaledBitmapInternallyCreated = false;

        Bitmap sourceBitmapToScale = originalLoadedBitmap;
        if (sourceBitmapToScale == null || sourceBitmapToScale.isRecycled()) {
             Log.w(TAG, "Source bitmap for scaling is null or recycled. Falling back to placeholder/error.");
             sourceBitmapToScale = errorBitmapRef != null ? errorBitmapRef : placeholderBitmapRef;
        }

        if (sourceBitmapToScale == null || sourceBitmapToScale.isRecycled()) {
            Log.e(TAG, "Cannot scale, no valid source, placeholder or error bitmap available.");
            initializePositionAndSpeed();
            return;
        }

        if (viewCanvasWidth > 0 && viewCanvasHeight > 0) {
            float targetHeight = (float) viewCanvasHeight / TARGET_FISH_HEIGHT_DIVISOR;
            targetHeight = Math.max(MIN_FISH_HEIGHT_PX, Math.min(targetHeight, MAX_FISH_HEIGHT_PX));

            float scaleFactor = targetHeight / sourceBitmapToScale.getHeight();
            int targetWidth = Math.round(sourceBitmapToScale.getWidth() * scaleFactor);


            if (Math.abs(sourceBitmapToScale.getHeight() - targetHeight) < 2.0f) {
                scaledAndReadyBitmap = sourceBitmapToScale;
                Log.d(TAG, "Using source bitmap directly, close enough to target size. Size: " + targetWidth + "x" + Math.round(targetHeight));

            } else {
                try {
                    Log.d(TAG, "Scaling source bitmap ("+sourceBitmapToScale.getWidth()+"x"+sourceBitmapToScale.getHeight()+") to target: " + targetWidth + "x" + Math.round(targetHeight));
                    scaledAndReadyBitmap = Bitmap.createScaledBitmap(sourceBitmapToScale, targetWidth, Math.round(targetHeight), true);
                    isScaledBitmapInternallyCreated = true;
                } catch (OutOfMemoryError oom) {
                    Log.e(TAG, "OutOfMemoryError scaling fish sprite. Using original.", oom);
                    scaledAndReadyBitmap = sourceBitmapToScale;
                    isScaledBitmapInternallyCreated = false;
                } catch (IllegalArgumentException iae) {
                     Log.e(TAG, "IllegalArgumentException scaling fish sprite (target W/H likely 0). Using original.", iae);
                    scaledAndReadyBitmap = sourceBitmapToScale;
                    isScaledBitmapInternallyCreated = false;
                }
            }
        } else {

            scaledAndReadyBitmap = sourceBitmapToScale;

            Log.d(TAG, "Canvas dimensions not ready, using source bitmap as is.");
        }
        
        if (scaledAndReadyBitmap == null || scaledAndReadyBitmap.isRecycled()){ // Final fallback if scaling failed badly
            Log.w(TAG, "Scaled and ready bitmap is null or recycled after scaling attempt, falling back to placeholder.");
            scaledAndReadyBitmap = placeholderBitmapRef;
            isScaledBitmapInternallyCreated = false;
        }

        initializePositionAndSpeed();
    }

    private void initializePositionAndSpeed() {
        if (scaledAndReadyBitmap == null || scaledAndReadyBitmap.isRecycled() || viewCanvasWidth == 0 || viewCanvasHeight == 0) {
            Log.d(TAG, "Cannot initialize sprite position/speed: scaled bitmap invalid or canvas dimensions zero.");

            x = -getWidth(); 
            y = -getHeight();
            return;
        }
        this.x = random.nextFloat() * Math.max(0, (viewCanvasWidth - getWidth()));
        this.y = random.nextFloat() * Math.max(0, (viewCanvasHeight - getHeight()));
        this.speedX = (random.nextFloat() * 1.5f + 0.5f) * (random.nextBoolean() ? 1 : -1);
        this.speedY = (random.nextFloat() * 0.8f + 0.2f) * (random.nextBoolean() ? 1 : -1);
        this.facingRight = speedX > 0;
        Log.d(TAG, "Initialized sprite pos/speed. Pos: (" + String.format("%.1f", x) + "," + String.format("%.1f", y) +
                     "), Speed: (" + String.format("%.1f", speedX) + "," + String.format("%.1f", speedY) +
                     "), Scaled Size: (" + getWidth() + "x" + getHeight() + ") on view canvas (" + viewCanvasWidth + "x" + viewCanvasHeight + ")");
    }

    public void update() {
        if (scaledAndReadyBitmap == null || scaledAndReadyBitmap.isRecycled() || viewCanvasWidth == 0 || viewCanvasHeight == 0) return;

        x += speedX;
        y += speedY;

        if (x <= 0) {
            x = 0;
            if (speedX < 0) speedX = -speedX;
            facingRight = true;
        } else if (x + getWidth() >= viewCanvasWidth) {
            x = viewCanvasWidth - getWidth();
            if (speedX > 0) speedX = -speedX;
            facingRight = false;
        }

        if (y <= 0) {
            y = 0;
            if (speedY < 0) speedY = -speedY;
        } else if (y + getHeight() >= viewCanvasHeight) {
            y = viewCanvasHeight - getHeight();
            if (speedY > 0) speedY = -speedY;
        }
        wobbleAngle += wobbleSpeed * Math.signum(speedX) * (Math.abs(speedX) / 1.5f);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (scaledAndReadyBitmap == null || scaledAndReadyBitmap.isRecycled() || canvas == null) return;

        float drawY = y + (float) Math.sin(wobbleAngle) * wobbleMagnitude;
        matrix.reset();
        if (!facingRight) {
            matrix.postScale(-1, 1, getWidth() / 2f, getHeight() / 2f);
        }
        matrix.postTranslate(x, drawY);
        canvas.drawBitmap(scaledAndReadyBitmap, matrix, paint);
    }

    public boolean isBitmapReady() {
        return scaledAndReadyBitmap != null && !scaledAndReadyBitmap.isRecycled();
    }

    public int getWidth() {
        return (scaledAndReadyBitmap != null && !scaledAndReadyBitmap.isRecycled()) ? scaledAndReadyBitmap.getWidth() : 0;
    }

    public int getHeight() {
        return (scaledAndReadyBitmap != null && !scaledAndReadyBitmap.isRecycled()) ? scaledAndReadyBitmap.getHeight() : 0;
    }

    public void cleanup() {
        Log.d(TAG, "Cleanup called for fish sprite.");
        if (isScaledBitmapInternallyCreated && scaledAndReadyBitmap != null && !scaledAndReadyBitmap.isRecycled()) {

            if (scaledAndReadyBitmap != originalLoadedBitmap &&
                scaledAndReadyBitmap != placeholderBitmapRef &&
                scaledAndReadyBitmap != errorBitmapRef) {
                Log.d(TAG, "Recycling internally created scaledBitmap in cleanup.");
                scaledAndReadyBitmap.recycle();
            }
        }
        scaledAndReadyBitmap = null;
        originalLoadedBitmap = null;
        isScaledBitmapInternallyCreated = false;
    }
}
