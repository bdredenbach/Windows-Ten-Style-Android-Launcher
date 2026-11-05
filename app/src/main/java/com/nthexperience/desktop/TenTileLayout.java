package com.nthexperience.desktop;
import java.util.*;
/** First-fit placement on responsive four- or six-cell tile grids. */
final class TenTileLayout {
    static final int COLUMNS=6;
    static final String[] SIZES={"Small","Medium","Wide","Large"};
    static int width(String s){return s.equals("Small")?1:s.equals("Medium")?2:4;}
    static int height(String s){return s.equals("Small")?1:s.equals("Large")?4:2;}
    static final class Cell {final int row,col,w,h;Cell(int r,int c,int w,int h){row=r;col=c;this.w=w;this.h=h;}}
    static List<Cell> place(List<String> sizes){return place(sizes,COLUMNS);}
    static List<Cell> place(List<String> sizes,int columns){if(columns<4)throw new IllegalArgumentException("A tile grid needs at least four cells");Set<Integer> used=new HashSet<>();List<Cell> result=new ArrayList<>();for(String size:sizes){int w=width(size),h=height(size),r,c=0;search:for(r=0;;r++)for(c=0;c<=columns-w;c++){boolean free=true;for(int y=r;y<r+h;y++)for(int x=c;x<c+w;x++)if(used.contains(y*columns+x))free=false;if(free)break search;}for(int y=r;y<r+h;y++)for(int x=c;x<c+w;x++)used.add(y*columns+x);result.add(new Cell(r,c,w,h));}return result;}
}
