package com.example.javaqua.myaqua;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnimatedAquariumView extends View {
    private static final String TAG = "AnimatedAquariumView";

    private boolean isViewAttached = false;
    private boolean isViewMeasured = false;
    private boolean isAnimating = false;
    private boolean isAnimationEnabled = true;

    private Bitmap displayableBackgroundBitmap;
    private Bitmap pBackgroundPlaceholder;
    private Bitmap pBackgroundError;
    private Bitmap pFishPlaceholder;
    private Bitmap pFishError;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<FishSprite> fishSprites = new CopyOnWriteArrayList<>();
    private final Handler animationHandler = new Handler(Looper.getMainLooper());

    private String currentBackgroundUrl;
    private String pendingBackgroundUrl;
    private final List<FishPreview> pendingFishDataList = new ArrayList<>();

    public AnimatedAquariumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeDefaultBitmaps();
    }

    private void initializeDefaultBitmaps() {
        if (getContext() == null) return;
        try {
            pBackgroundPlaceholder = safeGetBitmapFromDrawable(getContext(), R.drawable.aquarium_background_placeholder);
            pBackgroundError = safeGetBitmapFromDrawable(getContext(), R.drawable.aquarium_background_error);
            pFishPlaceholder = safeGetBitmapFromDrawable(getContext(), R.drawable.fish_sprite_placeholder);
            pFishError = safeGetBitmapFromDrawable(getContext(), R.drawable.fish_sprite_error);
            displayableBackgroundBitmap = pBackgroundPlaceholder;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bitmaps", e);
        }
    }

    private static Bitmap safeGetBitmapFromDrawable(Context context, int id) {
        Drawable d = ContextCompat.getDrawable(context, id);
        if (d == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        if (d instanceof BitmapDrawable) return ((BitmapDrawable) d).getBitmap();
        Bitmap b = Bitmap.createBitmap(Math.max(1, d.getIntrinsicWidth()), Math.max(1, d.getIntrinsicHeight()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return b;
    }

    public void setAquariumData(String backgroundUrl, List<FishPreview> fishDataList) {
        this.pendingBackgroundUrl = backgroundUrl;
        this.pendingFishDataList.clear();
        if (fishDataList != null) this.pendingFishDataList.addAll(fishDataList);
        processPendingData();
    }

    public void setAnimationEnabled(boolean enabled) {
        this.isAnimationEnabled = enabled;
        manageAnimationState();
    }

    private synchronized void processPendingData() {
        if (!isViewAttached || !isViewMeasured) return;

        if (pendingBackgroundUrl != null && !pendingBackgroundUrl.equals(currentBackgroundUrl)) {
            loadBackground(pendingBackgroundUrl);
        }
        updateFishSprites(pendingFishDataList);
    }

    private void loadBackground(String url) {
        this.currentBackgroundUrl = url;
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .override(getWidth(), getHeight())
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        displayableBackgroundBitmap = resource;
                        invalidate();
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void updateFishSprites(List<FishPreview> data) {
        for (FishSprite s : fishSprites) s.cleanup();
        fishSprites.clear();
        if (data == null || data.isEmpty()) {
            manageAnimationState();
            invalidate();
            return;
        }

        // Limit sprites for performance, especially in grid
        int limit = isAnimationEnabled ? 15 : 5;
        List<FishPreview> sub = data.size() > limit ? data.subList(0, limit) : data;

        for (FishPreview f : sub) {
            FishSprite sprite = new FishSprite(pFishPlaceholder, pFishError, getWidth(), getHeight());
            fishSprites.add(sprite);
            
            int targetH = getHeight() / 6;
            if (targetH <= 0) targetH = 50;

            Glide.with(getContext())
                    .asBitmap()
                    .load(f.getSprite())
                    .override(Target.SIZE_ORIGINAL, targetH)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            sprite.setBitmap(resource);
                            invalidate();
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
        manageAnimationState();
    }

    private final Runnable animationLoop = new Runnable() {
        @Override
        public void run() {
            if (!isAnimating) return;
            for (FishSprite s : fishSprites) s.update();
            invalidate();
            animationHandler.postDelayed(this, 33);
        }
    };

    private void manageAnimationState() {
        boolean should = isViewAttached && isViewMeasured && !fishSprites.isEmpty() && isAnimationEnabled;
        if (should && !isAnimating) {
            isAnimating = true;
            animationHandler.post(animationLoop);
        } else if (!should && isAnimating) {
            isAnimating = false;
            animationHandler.removeCallbacks(animationLoop);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (displayableBackgroundBitmap != null && !displayableBackgroundBitmap.isRecycled()) {
            canvas.drawBitmap(displayableBackgroundBitmap, 0, 0, backgroundPaint);
        }
        for (FishSprite s : fishSprites) s.draw(canvas, fishPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        isViewMeasured = (w > 0 && h > 0);
        if (isViewMeasured) processPendingData();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isViewAttached = true;
        manageAnimationState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isViewAttached = false;
        manageAnimationState();
    }
}