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


    private Bitmap sourceBackgroundBitmap;
    private Bitmap displayableBackgroundBitmap;
    private Bitmap pBackgroundPlaceholder;
    private Bitmap pBackgroundError;
    private Bitmap pFishPlaceholder;
    private Bitmap pFishError;


    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<FishSprite> fishSprites = new CopyOnWriteArrayList<>();
    private final Handler animationHandler = new Handler(Looper.getMainLooper());


    private String currentBackgroundUrl; // Tracks the URL that is currently loaded or actively being loaded
    private String pendingBackgroundUrl; // Tracks the desired background URL from setAquariumData
    private List<FishPreview> pendingFishDataList = new ArrayList<>(); // Tracks the desired fish list

    public AnimatedAquariumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeDefaultBitmaps();
    }

    private String bitmapToDimString(Bitmap b) {
        if (b == null) return "null";
        if (b.isRecycled()) return "recycled";
        return b.getWidth() + "x" + b.getHeight();
    }

    private static Bitmap safeGetBitmapFromDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            Log.e(TAG, "ContextCompat.getDrawable returned null for drawable ID: " + drawableId);

            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        if (drawable instanceof BitmapDrawable) {
            Bitmap original = ((BitmapDrawable) drawable).getBitmap();
            if (original != null && !original.isRecycled() && original.getWidth() > 0 && original.getHeight() > 0) {

                return original.copy(Bitmap.Config.ARGB_8888, true);
            }
            Log.w(TAG, "BitmapDrawable for " + drawableId + " had null, recycled or zero-dimension bitmap.");
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            Log.w(TAG, "Drawable " + drawableId + " has no intrinsic dimensions or they are zero. Creating 1x1 transparent bitmap.");
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        if (bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
             Log.e(TAG, "safeGetBitmapFromDrawable for " + drawableId + " resulted in a zero-dimension bitmap. Returning 1x1 transparent.");
             if (!bitmap.isRecycled()) bitmap.recycle();
             Bitmap transparentFallback = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
             transparentFallback.eraseColor(Color.TRANSPARENT);
             return transparentFallback;
        }
        return bitmap;
    }

    private void initializeDefaultBitmaps() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null during placeholder initialization.");
            pBackgroundPlaceholder = createFallbackErrorBitmap(Color.YELLOW, "CTX_NULL_BG_PLACEHOLDER");
            pBackgroundError = createFallbackErrorBitmap(Color.BLUE, "CTX_NULL_BG_ERROR");
            pFishPlaceholder = createFallbackErrorBitmap(Color.CYAN, "CTX_NULL_FISH_PLACEHOLDER");
            pFishError = createFallbackErrorBitmap(Color.MAGENTA, "CTX_NULL_FISH_ERROR");
            displayableBackgroundBitmap = pBackgroundPlaceholder;
            return;
        }
        try {
            recycleBitmap("pBackgroundPlaceholder (init)", pBackgroundPlaceholder);
            pBackgroundPlaceholder = safeGetBitmapFromDrawable(getContext(), R.drawable.aquarium_background_placeholder);
            if (pBackgroundPlaceholder == null || pBackgroundPlaceholder.isRecycled() || pBackgroundPlaceholder.getWidth() == 0 || pBackgroundPlaceholder.getHeight() == 0) {
                Log.w(TAG, "Failed to load R.drawable.aquarium_background_placeholder or it's invalid. Using YELLOW fallback.");
                recycleBitmap("invalid pBackgroundPlaceholder", pBackgroundPlaceholder);
                pBackgroundPlaceholder = createFallbackErrorBitmap(Color.YELLOW, "INIT_BG_PLACEHOLDER_FAIL");
            }
            
            recycleBitmap("pBackgroundError (init)", pBackgroundError);
            pBackgroundError = safeGetBitmapFromDrawable(getContext(), R.drawable.aquarium_background_error);
            if (pBackgroundError == null || pBackgroundError.isRecycled() || pBackgroundError.getWidth() == 0 || pBackgroundError.getHeight() == 0) {
                Log.w(TAG, "Failed to load R.drawable.aquarium_background_error or it's invalid. Using BLUE fallback.");
                recycleBitmap("invalid pBackgroundError", pBackgroundError);
                pBackgroundError = createFallbackErrorBitmap(Color.BLUE, "INIT_BG_ERROR_FAIL");
            }

            recycleBitmap("pFishPlaceholder (init)", pFishPlaceholder);
            pFishPlaceholder = safeGetBitmapFromDrawable(getContext(), R.drawable.fish_sprite_placeholder);
            if (pFishPlaceholder == null || pFishPlaceholder.isRecycled() || pFishPlaceholder.getWidth() == 0 || pFishPlaceholder.getHeight() == 0) {
                Log.w(TAG, "Failed to load R.drawable.fish_sprite_placeholder or it's invalid. Using CYAN fallback.");
                recycleBitmap("invalid pFishPlaceholder", pFishPlaceholder);
                pFishPlaceholder = createFallbackErrorBitmap(Color.CYAN, "INIT_FISH_PLACEHOLDER_FAIL");
            }
            
            recycleBitmap("pFishError (init)", pFishError);
            pFishError = safeGetBitmapFromDrawable(getContext(), R.drawable.fish_sprite_error);
             if (pFishError == null || pFishError.isRecycled() || pFishError.getWidth() == 0 || pFishError.getHeight() == 0) {
                Log.w(TAG, "Failed to load R.drawable.fish_sprite_error or it's invalid. Using MAGENTA fallback.");
                recycleBitmap("invalid pFishError", pFishError);
                pFishError = createFallbackErrorBitmap(Color.MAGENTA, "INIT_FISH_ERROR_FAIL");
            }

            if (displayableBackgroundBitmap == null || displayableBackgroundBitmap.isRecycled()) {
                Log.d(TAG, "displayableBackgroundBitmap was invalid on init, setting to pBackgroundPlaceholder: " + bitmapToDimString(pBackgroundPlaceholder));
                displayableBackgroundBitmap = pBackgroundPlaceholder;
            }
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "OutOfMemoryError initializing default bitmaps", oom);
            pBackgroundPlaceholder = createFallbackErrorBitmap(Color.YELLOW, "OOM_BG_PLACEHOLDER");
            pBackgroundError = createFallbackErrorBitmap(Color.BLUE, "OOM_BG_ERROR");
            pFishPlaceholder = createFallbackErrorBitmap(Color.CYAN, "OOM_FISH_PLACEHOLDER");
            pFishError = createFallbackErrorBitmap(Color.MAGENTA, "OOM_FISH_ERROR");
            displayableBackgroundBitmap = pBackgroundPlaceholder;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing default bitmaps", e);
             pBackgroundPlaceholder = createFallbackErrorBitmap(Color.YELLOW, "EX_BG_PLACEHOLDER");
            pBackgroundError = createFallbackErrorBitmap(Color.BLUE, "EX_BG_ERROR");
            pFishPlaceholder = createFallbackErrorBitmap(Color.CYAN, "EX_FISH_PLACEHOLDER");
            pFishError = createFallbackErrorBitmap(Color.MAGENTA, "EX_FISH_ERROR");
            displayableBackgroundBitmap = pBackgroundPlaceholder;
        }
    }

    public void setAquariumData(String backgroundUrl, List<FishPreview> fishDataList) {
        Log.d(TAG, "setAquariumData called. New Pending URL: " + backgroundUrl +
                ", New Pending Fish count: " + (fishDataList != null ? fishDataList.size() : "null") +
                ". Current active URL: " + this.currentBackgroundUrl);
        this.pendingBackgroundUrl = backgroundUrl;
        this.pendingFishDataList.clear();
        if (fishDataList != null) {
            this.pendingFishDataList.addAll(fishDataList);
        }
        processPendingData();
    }

    private synchronized void processPendingData() {
        if (!isViewAttached || getContext() == null) {
            Log.d(TAG, "processPendingData: View not attached or context null. Deferring. Attached: " + isViewAttached);
            return;
        }
        Log.d(TAG, "processPendingData: Processing. Attached: " + isViewAttached + ", Measured: " + isViewMeasured +
                   ", PendingBG: " + pendingBackgroundUrl + ", PendingFish: " + pendingFishDataList.size() +
                   ", CurrentBG URL: " + currentBackgroundUrl + ", SourceBG: " + bitmapToDimString(sourceBackgroundBitmap));

        if (pendingBackgroundUrl == null) {
            if (currentBackgroundUrl != null || sourceBackgroundBitmap != pBackgroundPlaceholder) {
                Log.d(TAG, "processPendingData: Pending URL is null. Requesting placeholder.");
                loadAquariumBackground(null);
            }
        } else if (!pendingBackgroundUrl.equals(currentBackgroundUrl) || sourceBackgroundBitmap == null || sourceBackgroundBitmap.isRecycled() || sourceBackgroundBitmap == pBackgroundPlaceholder || sourceBackgroundBitmap == pBackgroundError) {
            Log.d(TAG, "processPendingData: BG URL changed, source invalid, or source is placeholder/error. Requesting load for: " + pendingBackgroundUrl);
            loadAquariumBackground(pendingBackgroundUrl);
        } else if (isViewMeasured && sourceBackgroundBitmap != null && !sourceBackgroundBitmap.isRecycled()) {
            Log.d(TAG, "processPendingData: Background URL unchanged, source valid, view measured. Re-scaling if necessary. Source: " + bitmapToDimString(sourceBackgroundBitmap));
            scaleAndSetDisplayableBackground(sourceBackgroundBitmap, false);
        }

        if (isViewMeasured) {
            updateFishSprites(this.pendingFishDataList);
        } else {
             Log.d(TAG, "processPendingData: View not measured, fish sprite update deferred to onSizeChanged or later processPendingData call.");
            for (FishSprite sprite : fishSprites) {
                sprite.cleanup();
            }
            fishSprites.clear();
            manageAnimationState(); 
            invalidate(); 
        }
    }


    private void loadAquariumBackground(final String urlToLoad) {
        Log.d(TAG, "loadAquariumBackground: Requested URL: " + urlToLoad +
                ", Current Active URL: " + this.currentBackgroundUrl +
                ", SourceBitmap: " + bitmapToDimString(sourceBackgroundBitmap) +
                ", Attached: " + isViewAttached + ", Measured: " + isViewMeasured);

        if (getContext() == null || !isViewAttached) {
            Log.e(TAG, "loadAquariumBackground: Context null or view not attached. Cannot load. Requested URL: " + urlToLoad);
            return;
        }

        if (urlToLoad == null || urlToLoad.trim().isEmpty()) {
            Log.i(TAG, "loadAquariumBackground: URL is null/empty. Setting placeholder. Old active URL: " + this.currentBackgroundUrl);
            this.currentBackgroundUrl = null; 
            setSourceBackground(null, false);
            return;
        }

        if (urlToLoad.equals(this.currentBackgroundUrl) && 
            sourceBackgroundBitmap != null && !sourceBackgroundBitmap.isRecycled() &&
            !isBitmapAPlaceholderOrError(sourceBackgroundBitmap) ) {
            Log.i(TAG, "loadAquariumBackground: Skipping load. URL unchanged (" + urlToLoad + ") and source bitmap is valid and not a placeholder/error. Source: " + bitmapToDimString(sourceBackgroundBitmap));
            if (isViewMeasured) {
                scaleAndSetDisplayableBackground(sourceBackgroundBitmap, false);
            }
            return;
        }

        Log.i(TAG, "loadAquariumBackground: Proceeding to load. New Active URL: " + urlToLoad + ". Old Active URL: " + this.currentBackgroundUrl);
        this.currentBackgroundUrl = urlToLoad; 

        Glide.with(getContext().getApplicationContext())
                .asBitmap()
                .load(urlToLoad)
                .apply(new RequestOptions().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (!isViewAttached || getContext() == null) {
                            Log.w(TAG, "BG OnResourceReady: View not attached or context null for URL: " + urlToLoad + ". Original resource: " + bitmapToDimString(resource) + (resource.isRecycled() ? " (already recycled by Glide)" : ""));

                            return;
                        }
                        if (!urlToLoad.equals(AnimatedAquariumView.this.currentBackgroundUrl)) {
                            Log.w(TAG, "BG OnResourceReady: Active URL changed (" + AnimatedAquariumView.this.currentBackgroundUrl + " vs " + urlToLoad + "). Discarding. Original resource: " + bitmapToDimString(resource) + (resource.isRecycled() ? " (already recycled by Glide)" : ""));

                            return;
                        }

                        if (resource.isRecycled()) {
                            Log.e(TAG, "BG OnResourceReady: Glide provided an ALREADY RECYCLED bitmap for URL: " + urlToLoad + ". Treating as load failure.");
                            setSourceBackground(null, true); // true for isErrorFromLoad
                            return;
                        }

                        Log.d(TAG, "BG Glide load SUCCESS: " + bitmapToDimString(resource) + " for URL: " + urlToLoad);
                        Bitmap safeCopy = null;
                        try {

                            Bitmap.Config config = resource.getConfig() != null ? resource.getConfig() : Bitmap.Config.ARGB_8888;
                            safeCopy = resource.copy(config, true); // Create a mutable copy
                            Log.d(TAG, "BG OnResourceReady: Created safeCopy: " + bitmapToDimString(safeCopy) + " from resource: " + bitmapToDimString(resource));
                        } catch (Exception e) { // Catch any exception during copy, including from recycled bitmap
                            Log.e(TAG, "BG OnResourceReady: Error copying bitmap for URL: " + urlToLoad + ". Resource state: " + bitmapToDimString(resource), e);
                            safeCopy = null; // Ensure safeCopy is null if copy fails
                        }


                        
                        setSourceBackground(safeCopy, safeCopy == null); // isError if safeCopy is null
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                         if (!isViewAttached || getContext() == null) {
                            Log.w(TAG, "BG OnLoadFailed: View not attached or context null for URL: " + urlToLoad);
                            return;
                        }
                        if (!urlToLoad.equals(AnimatedAquariumView.this.currentBackgroundUrl)) {
                            Log.w(TAG, "BG OnLoadFailed: Active URL changed (" + AnimatedAquariumView.this.currentBackgroundUrl + " vs " + urlToLoad + "). Ignoring failure.");
                            return;
                        }
                        Log.e(TAG, "BG Glide load FAILED for URL: " + urlToLoad);
                        setSourceBackground(null, true); 
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Log.d(TAG, "BG Glide load CLEARED for URL: " + urlToLoad + ". Current Active URL: " + AnimatedAquariumView.this.currentBackgroundUrl);
                        if (isViewAttached && 
                            urlToLoad.equals(AnimatedAquariumView.this.currentBackgroundUrl) &&
                            (sourceBackgroundBitmap == null || sourceBackgroundBitmap.isRecycled() || isBitmapAPlaceholderOrError(sourceBackgroundBitmap))) {
                            Log.d(TAG, "BG OnLoadCleared: Active URL matches, source is invalid or placeholder. Reverting to placeholder.");
                            setSourceBackground(null, false);
                        } else if (isViewAttached && urlToLoad.equals(AnimatedAquariumView.this.currentBackgroundUrl)) {
                             Log.d(TAG, "BG OnLoadCleared: Active URL matches, but source is still valid ("+bitmapToDimString(sourceBackgroundBitmap)+"). No action taken.");
                        }
                    }
                });
    }

    private void setSourceBackground(Bitmap newSource, boolean isErrorFromLoad) {
        Log.i(TAG, "setSourceBackground: NewSource " + bitmapToDimString(newSource) + 
                   ", isErrorFromLoad: " + isErrorFromLoad + 
                   ". Prev sourceBG: " + bitmapToDimString(sourceBackgroundBitmap));

        Bitmap oldSourceBitmap = sourceBackgroundBitmap;
        Bitmap bitmapToUseAsSource;
        boolean isPlaceholderOrErrorActive;

        if (newSource != null && !newSource.isRecycled()) {
            bitmapToUseAsSource = newSource;
            isPlaceholderOrErrorActive = false;
            Log.d(TAG, "setSourceBackground: Using newSource: " + bitmapToDimString(bitmapToUseAsSource));
        } else {
            Log.w(TAG, "setSourceBackground: newSource is " + bitmapToDimString(newSource) + 
                       ". Using " + (isErrorFromLoad ? "ERROR (BLUE)" : "PLACEHOLDER (YELLOW)") + " bitmap. Active URL: " + currentBackgroundUrl);
            bitmapToUseAsSource = isErrorFromLoad ? pBackgroundError : pBackgroundPlaceholder;
            isPlaceholderOrErrorActive = true;
        }
        
        sourceBackgroundBitmap = bitmapToUseAsSource;

        if (oldSourceBitmap != null && oldSourceBitmap != sourceBackgroundBitmap && !oldSourceBitmap.isRecycled() &&
            !isBitmapAPlaceholderOrError(oldSourceBitmap)) {
            Log.d(TAG, "setSourceBackground: Recycling previous oldSourceBitmap: " + bitmapToDimString(oldSourceBitmap));
            oldSourceBitmap.recycle();
        }
        
        if (sourceBackgroundBitmap == null || sourceBackgroundBitmap.isRecycled()) {
            Log.e(TAG, "setSourceBackground: sourceBackgroundBitmap (" + bitmapToDimString(sourceBackgroundBitmap) + ") is invalid! Using emergency fallback (RED).");
            sourceBackgroundBitmap = createFallbackErrorBitmap(Color.RED, "SAVESOURCE_EMERGENCY_RED"); // Absolute fallback
            isPlaceholderOrErrorActive = true;
        }

        scaleAndSetDisplayableBackground(sourceBackgroundBitmap, isPlaceholderOrErrorActive);
    }

    private void scaleAndSetDisplayableBackground(Bitmap bitmapToScaleParam, boolean isSrcPlaceholderOrError) {
        Bitmap bitmapToScale = bitmapToScaleParam;
        Log.d(TAG, "scaleAndSetDisplayableBackground: Input bitmap: " + bitmapToDimString(bitmapToScale) + ", isSrcPlaceholderOrError: " + isSrcPlaceholderOrError);

        if (bitmapToScale == null || bitmapToScale.isRecycled()) {
            Log.e(TAG, "scaleAndSetDisplayableBackground: bitmapToScale is " + bitmapToDimString(bitmapToScale) + ". Fallback to pBackgroundError (BLUE).");
            bitmapToScale = pBackgroundError; 
            isSrcPlaceholderOrError = true; 
            if (bitmapToScale == null || bitmapToScale.isRecycled()) { 
                Log.e(TAG, "CRITICAL: pBackgroundError (BLUE) is also " + bitmapToDimString(bitmapToScale) + ". Using 1x1 RED fallback.");
                bitmapToScale = createFallbackErrorBitmap(Color.RED, "SCALE_EMERGENCY_RED");
            }
        }

        if (!isViewMeasured || getWidth() <= 0 || getHeight() <= 0) {
            Log.w(TAG, "scaleAndSetDisplayableBackground: View not measured or invalid dimensions (" + getWidth() + "x" + getHeight() + "). Setting bitmap ("+bitmapToDimString(bitmapToScale)+") as is.");
            recycleBitmapIfNotPlaceholder("displayableBackgroundBitmap (pre-measure set)", displayableBackgroundBitmap, bitmapToScale);
            displayableBackgroundBitmap = bitmapToScale;
            invalidate();
            return;
        }

        Bitmap newDisplayBitmap;
        if (bitmapToScale.getWidth() == getWidth() && bitmapToScale.getHeight() == getHeight()) {
            Log.d(TAG, "scaleAndSetDisplayableBackground: bitmapToScale (" + bitmapToDimString(bitmapToScale) + ") already matches view dimensions. Using as is.");
            newDisplayBitmap = bitmapToScale;
        } else {
            Log.d(TAG, "Scaling background: " + bitmapToDimString(bitmapToScale) + " to " + getWidth() + "x" + getHeight());
            try {
                newDisplayBitmap = Bitmap.createScaledBitmap(bitmapToScale, getWidth(), getHeight(), true);
                Log.d(TAG, "scaleAndSetDisplayableBackground: Scaled successfully to " + bitmapToDimString(newDisplayBitmap));
                } catch (Exception e) {
                Log.e(TAG, "Error scaling background bitmap (" + bitmapToDimString(bitmapToScale) + ")", e);

                newDisplayBitmap = (pBackgroundError != null && !pBackgroundError.isRecycled()) ? pBackgroundError : createFallbackErrorBitmap(Color.RED, "SCALE_FAIL_RED_FB");
                isSrcPlaceholderOrError = true;

                if (newDisplayBitmap.getWidth() != getWidth() || newDisplayBitmap.getHeight() != getHeight()) {
                    try {
                        Bitmap scaledFallback = Bitmap.createScaledBitmap(newDisplayBitmap, getWidth(), getHeight(), true);
                        Log.d(TAG, "scaleAndSetDisplayableBackground: Scaled pBackgroundError/Red fallback to " + bitmapToDimString(scaledFallback));
                        

                        if (newDisplayBitmap != scaledFallback && !isBitmapAPlaceholderOrError(newDisplayBitmap) ) { 
                           recycleBitmap("temp-fallback-before-final-scale", newDisplayBitmap);
                        }
                        newDisplayBitmap = scaledFallback;
                    } catch (Exception ex) {
                        Log.e(TAG, "Error scaling pBackgroundError/Red fallback bitmap (" + bitmapToDimString(newDisplayBitmap) + ")", ex);
                        Bitmap emergencyBitmap = createFallbackErrorBitmap(Color.RED, "SCALE_FAIL_EMERGENCY_RED");
                        recycleBitmapIfNotPlaceholder("displayableBackgroundBitmap (error scaling fallback)", displayableBackgroundBitmap, emergencyBitmap);
                        displayableBackgroundBitmap = emergencyBitmap;
                        invalidate();
                        return;
                    }
                }
            }
        }
        
        recycleBitmapIfNotPlaceholder("displayableBackgroundBitmap (final set)", displayableBackgroundBitmap, newDisplayBitmap);
        displayableBackgroundBitmap = newDisplayBitmap;
        Log.d(TAG, "scaleAndSetDisplayableBackground: Final displayableBitmap is " + bitmapToDimString(displayableBackgroundBitmap));
        invalidate();
    }
    
    private boolean isBitmapAPlaceholderOrError(Bitmap b) {
        if (b == null) return false;

        return b == pBackgroundPlaceholder || b == pBackgroundError || b == pFishPlaceholder || b == pFishError;
    }

    private void recycleBitmapIfNotPlaceholder(String logName, Bitmap oldBitmap, Bitmap newBitmapCandidate) {
        if (oldBitmap != null && oldBitmap != newBitmapCandidate && !oldBitmap.isRecycled() &&
            !isBitmapAPlaceholderOrError(oldBitmap)) {
            Log.d(TAG, "Recycling old " + logName + " (" + bitmapToDimString(oldBitmap) + ")");
            oldBitmap.recycle();
        } else if (oldBitmap != null && oldBitmap != newBitmapCandidate && !oldBitmap.isRecycled() && isBitmapAPlaceholderOrError(oldBitmap)){
             Log.d(TAG, "NOT recycling old " + logName + " (" + bitmapToDimString(oldBitmap) + ") because it IS a placeholder/error.");
        }
    }
    

    private Bitmap createFallbackErrorBitmap(boolean red) {
        return createFallbackErrorBitmap(red ? Color.RED : 0x4000FF00, red ? "FALLBACK_RED" : "FALLBACK_TRANSPARENT_GREEN");
    }


    private Bitmap createFallbackErrorBitmap(int color, String logTag) {
        Log.d(TAG, "Creating 1x1 fallback error bitmap with color " + String.format("#%08X", color) + " for: " + logTag);
        Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        b.eraseColor(color); 
        return b;
    }

    private void updateFishSprites(List<FishPreview> fishDataListToLoad) {
        if (!isViewAttached || !isViewMeasured || getContext() == null) {
            Log.d(TAG, "updateFishSprites: Pre-conditions not met. Aborting.");
            return;
        }

        Log.d(TAG, "updateFishSprites: Clearing existing " + fishSprites.size() + " sprites.");
        for (FishSprite sprite : fishSprites) {
            sprite.cleanup(); 
        }
        fishSprites.clear();

        if (fishDataListToLoad == null || fishDataListToLoad.isEmpty()) {
            Log.d(TAG, "updateFishSprites: New fish list is null or empty. No sprites to load.");
            manageAnimationState();
            invalidate();
            return;
        }

        Log.d(TAG, "updateFishSprites: Populating " + fishDataListToLoad.size() + " new fish. View: " + getWidth() + "x" + getHeight());
        Bitmap fishPlaceholderToUse = (pFishPlaceholder != null && !pFishPlaceholder.isRecycled()) ? pFishPlaceholder : createFallbackErrorBitmap(Color.CYAN, "FISH_SPRITE_PLACEHOLDER_FB");
        Bitmap fishErrorToUse = (pFishError != null && !pFishError.isRecycled()) ? pFishError : createFallbackErrorBitmap(Color.MAGENTA, "FISH_SPRITE_ERROR_FB");

        for (FishPreview fishData : fishDataListToLoad) {
            if (fishData == null || fishData.getSprite() == null || fishData.getSprite().trim().isEmpty()) {
                Log.w(TAG, "Invalid fish data or sprite URL for: " + fishData);
                continue;
            }
            final FishSprite fishSprite = new FishSprite(
                fishPlaceholderToUse,
                fishErrorToUse,
                getWidth(), getHeight()
            );
            fishSprites.add(fishSprite);
            final String fishSpriteUrl = fishData.getSprite();
            final String fishNameForLogging = fishData.getName();

            Log.d(TAG, "Loading fish sprite: " + fishNameForLogging + " from " + fishSpriteUrl);
            Glide.with(getContext().getApplicationContext())
                    .asBitmap()
                    .load(fishSpriteUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (!isViewAttached || getContext() == null) { 
                                Log.w(TAG, "Fish OnResourceReady: View detached or context null. Recycling for: " + fishNameForLogging + " (" + bitmapToDimString(resource) + ")");
                                resource.recycle();
                                return;
                            }
                            if (!fishSprites.contains(fishSprite)) {
                                Log.w(TAG, "Fish OnResourceReady: Sprite for " + fishNameForLogging + " no longer in active list. Recycling bitmap: " + bitmapToDimString(resource));
                                resource.recycle();
                                return;
                            }
                            Log.d(TAG, "Fish sprite Glide load SUCCESS for " + fishNameForLogging + " (" + bitmapToDimString(resource) + ")");
                            fishSprite.setBitmap(resource); 
                            invalidate();
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (!isViewAttached || getContext() == null) {
                                Log.w(TAG, "Fish OnLoadFailed: View detached or context null for: " + fishNameForLogging);
                                return;
                            }
                             if (!fishSprites.contains(fishSprite)) {
                                Log.w(TAG, "Fish OnLoadFailed: Sprite for " + fishNameForLogging + " no longer in active list. Ignoring failure.");
                                return;
                            }
                            Log.e(TAG, "Fish sprite Glide load FAILED for: " + fishNameForLogging);
                            fishSprite.setBitmap(null);
                            invalidate();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            Log.d(TAG, "Fish sprite Glide load CLEARED for: " + fishNameForLogging);
                            if (isViewAttached && fishSprites.contains(fishSprite)) {
                                fishSprite.setBitmap(null); 
                                invalidate();
                            }
                        }
                    });
        }
        manageAnimationState();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged: " + w + "x" + h + (isViewAttached ? " (Attached)" : " (Not Attached)") + ". Old: " + oldw + "x" + oldh);
        if (w > 0 && h > 0) {
            boolean wasPreviouslyMeasured = isViewMeasured;
            isViewMeasured = true;
            if (!wasPreviouslyMeasured) { 
                Log.d(TAG, "onSizeChanged: View is now measured for the first time or after being unmeasured.");
                processPendingData(); 
            } else { 
                Log.d(TAG, "onSizeChanged: View dimensions changed while already measured. Re-evaluating background and fish sizes.");
                Bitmap bgToScale = sourceBackgroundBitmap; 
                boolean isPlaceholderOrErrorUsedAsSource = isBitmapAPlaceholderOrError(bgToScale);

                if (bgToScale == null || bgToScale.isRecycled()) {
                    Log.w(TAG, "onSizeChanged: sourceBackgroundBitmap (" + bitmapToDimString(bgToScale) + ") invalid. Using placeholder/error. Active URL: " + currentBackgroundUrl);
                    bgToScale = (currentBackgroundUrl == null || currentBackgroundUrl.trim().isEmpty()) ? pBackgroundPlaceholder : pBackgroundError;
                    isPlaceholderOrErrorUsedAsSource = true; 
                }
                if (bgToScale == null || bgToScale.isRecycled()) { // Should use pBackgroundError (BLUE) or pBackgroundPlaceholder (YELLOW)
                     Log.e(TAG, "onSizeChanged: bgToScale is STILL " + bitmapToDimString(bgToScale) + " after primary fallback. Using emergency RED.");
                     bgToScale = createFallbackErrorBitmap(Color.RED, "ONSIZE_EMERGENCY_RED");
                     isPlaceholderOrErrorUsedAsSource = true;
                }
                scaleAndSetDisplayableBackground(bgToScale, isPlaceholderOrErrorUsedAsSource);

                for (FishSprite fishSprite : fishSprites) {
                    fishSprite.setCanvasDimensions(w, h);
                }
            }
        } else {
            isViewMeasured = false;
            Log.w(TAG, "onSizeChanged: new dimensions are invalid (" + w + "x" + h + "). Setting isViewMeasured=false.");
            stopAnimation();
            recycleBitmapIfNotPlaceholder("displayableBackgroundBitmap (unmeasured)", displayableBackgroundBitmap, pBackgroundPlaceholder);
            displayableBackgroundBitmap = pBackgroundPlaceholder; 
            invalidate();
        }
        manageAnimationState(); 
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (displayableBackgroundBitmap != null && !displayableBackgroundBitmap.isRecycled()) {
            canvas.drawBitmap(displayableBackgroundBitmap, 0, 0, null);
        } else {
            Log.w(TAG, "onDraw: displayableBackgroundBitmap is " + bitmapToDimString(displayableBackgroundBitmap) + "! Drawing error. isViewMeasured: " + isViewMeasured + " Size: " + getWidth() + "x" + getHeight());
            Bitmap fallbackToDraw = (pBackgroundError != null && !pBackgroundError.isRecycled()) ? pBackgroundError : createFallbackErrorBitmap(Color.RED, "ONDRAW_EMERGENCY_RED");
            
            if (getWidth() > 0 && getHeight() > 0) {
                 if (fallbackToDraw.getWidth() != getWidth() || fallbackToDraw.getHeight() != getHeight()) {
                     try {
                        Bitmap scaledFallback = Bitmap.createScaledBitmap(fallbackToDraw, getWidth(), getHeight(), true);
                        canvas.drawBitmap(scaledFallback, 0,0,null);
                        if (!isBitmapAPlaceholderOrError(scaledFallback)) {
                            scaledFallback.recycle();
                        }
                     } catch (Exception e) {
                         Log.e(TAG, "onDraw: Exception scaling fallback " + bitmapToDimString(fallbackToDraw) + ": " + e.getMessage());
                         canvas.drawColor(Color.BLACK);
                     }
                 } else { 
                     canvas.drawBitmap(fallbackToDraw, 0,0,null);
                 }
            } else { 

                canvas.drawColor(Color.BLACK); 
            }
        }

        if (isViewMeasured && isViewAttached) { 
            for (FishSprite fishSprite : fishSprites) {
                fishSprite.draw(canvas, fishPaint);
            }
        }
    }

    private void manageAnimationState() {
        boolean shouldAnimate = isViewAttached && isViewMeasured && !fishSprites.isEmpty();
        if (shouldAnimate && !isAnimating) {
            startAnimation();
        } else if (!shouldAnimate && isAnimating) {
            stopAnimation();
        }
    }

    private final Runnable animationLoop = new Runnable() {
        @Override
        public void run() {
            if (!isAnimating) return;
            for (FishSprite fishSprite : fishSprites) {
                fishSprite.update();
            }
            invalidate(); 
            animationHandler.postDelayed(this, 30); 
        }
    };

    public void startAnimation() {
        if (!isAnimating && isViewAttached && isViewMeasured && !fishSprites.isEmpty()) {
            isAnimating = true;
            Log.d(TAG, "Starting animation with " + fishSprites.size() + " sprites.");
            animationHandler.post(animationLoop);
        } else {


        }
    }

    public void stopAnimation() {
        if (isAnimating) {
            isAnimating = false;
            animationHandler.removeCallbacks(animationLoop);
            Log.d(TAG, "Stopped animation.");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isViewAttached = true;
        Log.i(TAG, "onAttachedToWindow. View size: " + getWidth() + "x" + getHeight() + 
                   ". Pending BG: " + pendingBackgroundUrl + ". Current BG: " + currentBackgroundUrl);
        
        initializeDefaultBitmaps(); 

        if (getWidth() > 0 && getHeight() > 0) {
            isViewMeasured = true;
             Log.d(TAG, "onAttachedToWindow: View is already measured upon attachment.");
        } else {
             isViewMeasured = false; 
        }
        processPendingData(); 
        manageAnimationState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow. Cleaning up. Active BG URL: " + currentBackgroundUrl);
        isViewAttached = false;
        stopAnimation();

        pendingBackgroundUrl = null; 
        pendingFishDataList.clear(); 
        
        for (FishSprite sprite : fishSprites) {
            sprite.cleanup(); 
        }
        fishSprites.clear(); 

        recycleBitmapIfNotPlaceholder("sourceBackgroundBitmap (detach)", sourceBackgroundBitmap, null);
        sourceBackgroundBitmap = null; 
        
        recycleBitmapIfNotPlaceholder("displayableBackgroundBitmap (detach)", displayableBackgroundBitmap, null);
        displayableBackgroundBitmap = null;
        
        currentBackgroundUrl = null;
        Log.d(TAG, "Finished onDetachedFromWindow cleanup.");
    }

    private void recycleBitmap(String name, Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            Log.d(TAG, "Recycling " + name + " (" + bitmapToDimString(bitmap) + ")");
            bitmap.recycle();
        }
    }
    

    public void performFullCleanup() {
        Log.i(TAG, "performFullCleanup called. Active BG URL: " + currentBackgroundUrl);
        onDetachedFromWindow();


        Log.d(TAG, "Performing aggressive placeholder cleanup in performFullCleanup.");
        recycleBitmap("pBackgroundPlaceholder (full cleanup)", pBackgroundPlaceholder); pBackgroundPlaceholder = null;
        recycleBitmap("pBackgroundError (full cleanup)", pBackgroundError); pBackgroundError = null;
        recycleBitmap("pFishPlaceholder (full cleanup)", pFishPlaceholder); pFishPlaceholder = null;
        recycleBitmap("pFishError (full cleanup)", pFishError); pFishError = null;
        

        displayableBackgroundBitmap = null; 
        sourceBackgroundBitmap = null;

        Log.i(TAG, "Full cleanup finished. Active BG URL is now: " + currentBackgroundUrl);
    }
}
