package com.nthexperience.desktop;

/** Literal choices, with separate contrast-safe colors for text and filled controls. */
final class TenAccent {
    static final String[] NAMES={"White","Black","Graphite","Gray","Silver","Navy","Blue","Sky","Cyan","Teal","Emerald","Green","Lime","Sage","Yellow","Gold","Amber","Orange","Red","Ruby","Pink","Magenta","Purple","Violet"};
    static final int[] COLORS={0xffffffff,0xff000000,0xff333333,0xff808080,0xffc0c0c0,0xff003e73,0xff0078d7,0xff00a2ed,0xff0099bc,0xff008272,0xff008a54,0xff107c10,0xff7fba00,0xff7c9862,0xffffd800,0xffd6a100,0xffffb900,0xffd83b01,0xffe81123,0xffa80000,0xffe3008c,0xffc239b3,0xff744da9,0xff8764b8};
    static int mix(int a,int b,float t){return 0xff000000|Math.round(((a>>16)&255)*(1-t)+((b>>16)&255)*t)<<16|Math.round(((a>>8)&255)*(1-t)+((b>>8)&255)*t)<<8|Math.round((a&255)*(1-t)+(b&255)*t);}
    private static double channel(int x){double c=x/255.0;return c<=.04045?c/12.92:Math.pow((c+.055)/1.055,2.4);}
    static double luminance(int c){return .2126*channel((c>>16)&255)+.7152*channel((c>>8)&255)+.0722*channel(c&255);}
    static double contrast(int a,int b){double x=luminance(a),y=luminance(b);return (Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
    static int on(int fill){return contrast(0xff000000,fill)>=contrast(0xffffffff,fill)?0xff000000:0xffffffff;}
    static int readable(int color,boolean dark){return readableOn(color,dark?0xff505050:0xffdddddd);}
    static int readableOn(int color,int surface){int target=on(surface);if(contrast(color,surface)>=4.5)return color;for(int i=1;i<=100;i++){int candidate=mix(color,target,i/100f);if(contrast(candidate,surface)>=4.5)return candidate;}return target;}
    static int readableOn(int color,int[] surfaces){if(surfaces.length==0)return color;int target=minimumContrast(0xff000000,surfaces)>=minimumContrast(0xffffffff,surfaces)?0xff000000:0xffffffff;if(minimumContrast(color,surfaces)>=4.5)return color;for(int i=1;i<=100;i++){int candidate=mix(color,target,i/100f);if(minimumContrast(candidate,surfaces)>=4.5)return candidate;}return target;}
    private static double minimumContrast(int color,int[] surfaces){double result=Double.MAX_VALUE;for(int surface:surfaces)result=Math.min(result,contrast(color,surface));return result;}
    private TenAccent(){}
}
