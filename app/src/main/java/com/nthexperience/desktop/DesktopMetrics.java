package com.nthexperience.desktop;

/** Pixel geometry shared by the desktop grid and the settings preview. */
final class DesktopMetrics {
    static int cell(int available,int requested,int automatic,int minimum){
        if(requested<=0)return Math.max(1,Math.min(Math.max(1,available),automatic));
        int count=Math.max(1,Math.min(requested,Math.max(1,available/Math.max(1,minimum))));
        return Math.max(1,available/count);
    }
    static int fittedRow(int reference,int visible,int normal){
        normal=Math.max(1,normal);if(visible>=reference)return normal;
        int rows=Math.max(1,reference/normal);
        return Math.max(1,Math.min(normal,visible/rows));
    }
    static int fittedRow(int reference,int visible,int normal,boolean snap){
        int fit=fittedRow(reference,visible,normal);
        return !snap&&reference>0&&visible<reference?Math.min(fit,Math.max(1,(int)((long)Math.max(1,normal)*visible/reference))):fit;
    }
    static float rowScale(int reference,int visible,int normal,int fitted,boolean snap){
        if(visible>=reference)return 1;
        if(snap||reference<=normal)return fitted/(float)Math.max(1,normal);
        return Math.max(1f/Math.max(1,reference),Math.min(1f,(visible-fitted)/(float)(reference-normal)));
    }
    static int icon(int requested,int width,int height,int horizontalPadding,int verticalPadding,int labelHeight){
        return Math.max(1,Math.min(requested,Math.min(Math.max(1,width-horizontalPadding),Math.max(1,height-verticalPadding-labelHeight))));
    }
}
