package com.nthexperience.desktop;
import java.util.*;
public final class TenTileLayoutCheck {
 public static void main(String[] args){
  Random random=new Random(10);for(int pass=0;pass<300;pass++){List<String> sizes=new ArrayList<>();for(int i=0;i<32;i++)sizes.add(TenTileLayout.SIZES[random.nextInt(4)]);List<TenTileLayout.Cell> cells=TenTileLayout.place(sizes);Set<String> used=new HashSet<>();for(int i=0;i<cells.size();i++){TenTileLayout.Cell c=cells.get(i);if(c.col<0||c.col+c.w>TenTileLayout.COLUMNS||c.row<0||c.w!=TenTileLayout.width(sizes.get(i))||c.h!=TenTileLayout.height(sizes.get(i)))throw new AssertionError("Tile outside grid");for(int y=c.row;y<c.row+c.h;y++)for(int x=c.col;x<c.col+c.w;x++)if(!used.add(y+":"+x))throw new AssertionError("Overlapping tiles");}}
  if(CloudConsent.allowed(true,0,true)||CloudConsent.allowed(false,1,true)||CloudConsent.allowed(true,1,false)||!CloudConsent.allowed(true,1,true))throw new AssertionError("Cloud consent gate");
  System.out.println("PASS: 9,600 mixed-size tiles fit without overlap; cloud consent defaults off");
 }
}
