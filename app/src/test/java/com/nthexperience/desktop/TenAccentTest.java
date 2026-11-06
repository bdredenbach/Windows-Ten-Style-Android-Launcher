package com.nthexperience.desktop;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class TenAccentTest {
    @Test public void everySwatchAndTextVariantHasReadableContrast(){Set<Integer> seen=new HashSet<>();assertEquals(24,TenAccent.COLORS.length);assertEquals(TenAccent.COLORS.length,TenAccent.NAMES.length);for(int color:TenAccent.COLORS){assertTrue(seen.add(color));assertTrue(TenAccent.contrast(TenAccent.on(color),color)>=4.5);for(boolean dark:new boolean[]{false,true}){int text=TenAccent.readable(color,dark);for(int surface:dark?new int[]{0xff1e1e1e,0xff202020,0xff262626,0xff333333,0xff505050}:new int[]{0xffffffff,0xfffafafa,0xffeeeeee,0xffdddddd})assertTrue(TenAccent.contrast(text,surface)>=4.5);}}assertTrue(seen.contains(0xffffffff)&&seen.contains(0xff000000));int[][] gradients={new int[]{0xfff0f7fc,0xffc9dae8,0xffeaf7ff,0xffa9c9df,0xff7397b3,0xffc9e0ee,0xffb6defa},new int[]{0xff1e1e1e,0xff262626,0xff333333,0xff505050}};for(int color:TenAccent.COLORS)for(int[] gradient:gradients){int text=TenAccent.readableOn(color,gradient);for(int stop:gradient)assertTrue(TenAccent.contrast(text,stop)>=4.5);}}
    @Test public void filledControlTextIsReadableAcrossTheRgbCube(){for(int r=0;r<=255;r+=17)for(int g=0;g<=255;g+=17)for(int b=0;b<=255;b+=17){int color=0xff000000|r<<16|g<<8|b;assertTrue(TenAccent.contrast(TenAccent.on(color),color)>=4.5);}}
}
