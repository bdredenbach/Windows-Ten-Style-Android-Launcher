package com.nthexperience.desktop;
import org.junit.Test;
import static org.junit.Assert.*;

public class DesktopGridFitTest {
 @Test public void normalRowsFitAboveTheExtraTaskbarRow(){for(int reference=240;reference<=1000;reference+=17)for(int normal:new int[]{72,80,94,110}){int visible=reference-48,rows=Math.max(1,reference/normal),fit=DesktopMetrics.fittedRow(reference,visible,normal);assertTrue(fit<=normal);assertTrue(rows*fit<=visible);float scale=DesktopMetrics.rowScale(reference,visible,normal,fit,true);for(int row=0;row<rows;row++){int top=Math.round(row*normal*scale);assertEquals(row*fit,top);assertTrue(top+fit<=visible);}}}
 @Test public void normalModeAndExtraSpaceKeepTheOriginalGeometry(){assertEquals(94,DesktopMetrics.fittedRow(700,700,94));assertEquals(94,DesktopMetrics.fittedRow(700,780,94));assertEquals(1f,DesktopMetrics.rowScale(700,700,94,94,true),0);}
 @Test public void freePositionsAndDragCoordinatesRoundTrip(){int ref=725,visible=677,normal=90,fit=DesktopMetrics.fittedRow(ref,visible,normal,false);float scale=DesktopMetrics.rowScale(ref,visible,normal,fit,false);assertEquals(visible-fit,(ref-normal)*scale,.001);assertTrue(normal*scale>=fit);for(float y:new float[]{0,60,200,510,635,800})assertEquals(y,y*scale/scale,.001);}
}
