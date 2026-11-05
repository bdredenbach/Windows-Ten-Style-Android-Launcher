package com.nthexperience.desktop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/** Short, interruptible Ten transitions. Closed tools release resources immediately. */
final class TenWindowMotion {
    static void closing(View window, FrameLayout host) {
        if (!Aero.motion(window.getContext()) || window.getWidth() < 1 || window.getHeight() < 1) return;
        float scale = Math.min(1f, (float)Math.sqrt(1_200_000d / ((double)window.getWidth()*window.getHeight())));
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(Math.max(1,(int)(window.getWidth()*scale)), Math.max(1,(int)(window.getHeight()*scale)), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap); canvas.scale(scale,scale); window.draw(canvas);
        } catch (RuntimeException | OutOfMemoryError unavailable) { return; }
        ImageView ghost = new ImageView(window.getContext());
        ghost.setImageBitmap(bitmap); ghost.setScaleType(ImageView.ScaleType.FIT_XY);
        ghost.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        ghost.setElevation(window.getElevation()); ghost.setClickable(false);
        FrameLayout.LayoutParams bounds = new FrameLayout.LayoutParams(window.getWidth(),window.getHeight());
        host.addView(ghost,bounds); ghost.setX(window.getX()); ghost.setY(window.getY());
        ghost.animate().alpha(0).scaleX(.96f).scaleY(.96f).setDuration(130).withEndAction(()->{
            ghost.setImageDrawable(null); if(ghost.getParent() instanceof ViewGroup)((ViewGroup)ghost.getParent()).removeView(ghost); bitmap.recycle();
        }).start();
    }
}
