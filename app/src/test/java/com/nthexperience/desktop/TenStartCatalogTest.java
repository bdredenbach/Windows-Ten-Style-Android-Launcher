package com.nthexperience.desktop;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class TenStartCatalogTest {
 @Test public void completeCatalogHasCategoriesColorsAndLiveChoices(){
  Set<String> ids=new HashSet<>(),groups=new HashSet<>(),sizes=new HashSet<>();Set<Integer> colors=new HashSet<>();int live=0;
  for(TenStartCatalog.App app:TenStartCatalog.APPS){assertTrue(ids.add(app.id));assertFalse(app.title.isBlank());assertFalse(app.icon.isBlank());assertTrue(Arrays.asList(TenStartCatalog.GROUPS).contains(app.group));groups.add(app.group);sizes.add(app.size);colors.add(app.color);if(app.live)live++;}
  assertEquals(43,ids.size());assertEquals(6,groups.size());assertTrue(colors.size()>=10);assertTrue(live>=8);assertTrue(sizes.containsAll(Arrays.asList("Normal","Wide","Large")));assertTrue(ids.containsAll(Arrays.asList("@nota","@files","@mediacenter","@mediaplayer","@settings","@photos")));
  for(String id:GadgetCatalog.IDS)if(GadgetCatalog.extra(id))assertTrue("Missing Nth tool "+id,ids.contains("@gadget:"+id));
 }
 @Test public void oldMediumAndNewNormalUseTheSameGeometry(){assertEquals(TenTileLayout.width("Medium"),TenTileLayout.width("Normal"));assertEquals(TenTileLayout.height("Medium"),TenTileLayout.height("Normal"));assertEquals("Normal",TenTileLayout.label("Medium"));}
 @Test public void expandedPinCapacityPacksWithoutOverlap(){
  for(int columns:new int[]{4,6}){List<String> sizes=new ArrayList<>();for(int i=0;i<128;i++)sizes.add(TenTileLayout.SIZES[i%4]);Set<String> occupied=new HashSet<>();for(TenTileLayout.Cell c:TenTileLayout.place(sizes,columns)){assertTrue(c.col>=0&&c.col+c.w<=columns);for(int y=c.row;y<c.row+c.h;y++)for(int x=c.col;x<c.col+c.w;x++)assertTrue(occupied.add(y+":"+x));}}
 }
}
