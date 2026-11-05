package com.nthexperience.desktop;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/** One N-to-the-tenth mark shared by shell and Android application resources. */
final class TenBrandMark extends View {
    final Drawable mark;
    TenBrandMark(Context context){super(context);mark=context.getDrawable(R.drawable.ic_launcher).mutate();setContentDescription("Nth Desktop Ten N to the tenth logo");}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int edge=Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight()-getPaddingTop()-getPaddingBottom());if(edge<1)return;int left=getPaddingLeft()+(getWidth()-getPaddingLeft()-getPaddingRight()-edge)/2,top=getPaddingTop()+(getHeight()-getPaddingTop()-getPaddingBottom()-edge)/2;mark.setBounds(left,top,left+edge,top+edge);mark.draw(canvas);}
}
