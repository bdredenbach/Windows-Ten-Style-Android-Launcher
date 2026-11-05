package com.nthexperience.desktop;
import android.content.*;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;
import org.json.*;

/** Ten Start: narrow rail, alphabetic apps and persistent resizable tiles. */
final class StartMenu extends LinearLayout {
 interface Actions{void open(String id);void options(View anchor,String id);void home();void settings();boolean isHome();}
 final Actions actions;final DesktopStore store;final List<AppCatalog.Entry> all=new ArrayList<>();
 final EditText search;final LinearLayout appList,tileGroups;final ScrollView apps,tiles;
 boolean showApps;int lastWidth;
 StartMenu(Context c,Actions actions,DesktopStore store,AeroWallpaper wallpaper){
  super(c);this.actions=actions;this.store=store;setOrientation(VERTICAL);setElevation(Aero.dp(c,18));setBackgroundColor(TenStyle.surface(c));setContentDescription("Windows 10 style Start menu");
  LinearLayout content=new LinearLayout(c);addView(content,new LayoutParams(-1,0,1));
  LinearLayout rail=Aero.column(c);rail.setGravity(Gravity.BOTTOM);content.addView(rail,new LayoutParams(Aero.dp(c,48),-1));
  rail.addView(rail("☰","Toggle all apps",()->{showApps=!showApps;fit();}),new LayoutParams(-1,Aero.dp(c,48)));rail.addView(new View(c),new LayoutParams(1,0,1));
  for(String[] link:new String[][]{{"○","Account","setting:page:accounts"},{"▤","Documents","@documents"},{"▧","Pictures","@pictures"},{"⚙","Settings","@settings"},{"⏻","Power","@power"}})rail.addView(rail(link[0],link[1],()->actions.open(link[2])),new LayoutParams(-1,Aero.dp(c,48)));
  apps=new ScrollView(c);apps.setContentDescription("All apps");appList=Aero.column(c);apps.addView(appList);content.addView(apps,new LayoutParams(Aero.dp(c,220),-1));
  tiles=new ScrollView(c);tiles.setContentDescription("Pinned Start tiles");tileGroups=Aero.column(c);tileGroups.setPadding(Aero.dp(c,8),0,Aero.dp(c,10),Aero.dp(c,12));tiles.addView(tileGroups);content.addView(tiles,new LayoutParams(0,-1,1));
  search=new EditText(c);search.setSingleLine();search.setTextSize(14);search.setTypeface(Aero.face(c));search.setHint("Type here to search");search.setTextColor(Aero.INK);search.setHintTextColor(0xff666666);search.setBackgroundColor(0xfff4f4f4);search.setPadding(Aero.dp(c,14),0,Aero.dp(c,10),0);search.setContentDescription("Search apps, settings and linked files");addView(search,new LayoutParams(-1,Aero.dp(c,48)));
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void afterTextChanged(Editable e){}public void onTextChanged(CharSequence s,int a,int b,int d){renderApps();fit();}});
  addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=lastWidth){lastWidth=r-l;fit();renderTiles();}});refresh();
 }
 private TextView rail(String glyph,String label,Runnable action){TextView v=TenStyle.command(getContext(),glyph,action);v.setTextSize(21);v.setContentDescription(label);v.setTooltipText(label);return v;}
 private void fit(){boolean wide=getWidth()>=Aero.dp(getContext(),600),q=search.length()>0;apps.setVisibility(wide||showApps||q?VISIBLE:GONE);tiles.setVisibility(!wide&&(showApps||q)?GONE:VISIBLE);LayoutParams p=(LayoutParams)apps.getLayoutParams();int width=wide?Aero.dp(getContext(),220):0;float weight=wide?0:1;if(p.width!=width||p.weight!=weight){p.width=width;p.weight=weight;apps.setLayoutParams(p);}}
 void entries(List<AppCatalog.Entry> values){all.clear();all.addAll(values);refresh();}
 void refreshName(){}void refreshHome(){}
 private List<StartSearch.Result> builtins(){List<StartSearch.Result> result=new ArrayList<>();for(String[] b:new String[][]{{"@nota","Nth Nota","notes"},{"@files","File Explorer","files"},{"@settings","Settings","control"},{"@browser",BrowserPanel.NAME,"browser"},{"@notes","Notepad","notes"},{"@gadget:calculator","Calculator","calculator"},{"@calendar","Calendar","calendar"},{"@photos","Photos","pictures"},{MediaLibrary.PLAYER,"Nth Media Player","musicfile"},{"@taskmanager","Task Manager","taskmanager"},{"@recycle","Recycle Bin","recycle"}})result.add(new StartSearch.Result(b[0],b[1],"Apps",b[2],null,true));return result;}
 private StartSearch.Result resolve(String id){for(StartSearch.Result b:builtins())if(b.id.equals(id))return b;for(AppCatalog.Entry a:all)if(a.id.equals(id))return new StartSearch.Result(a.id,store.name(a.id,a.label),"Apps","programs",a.icon,true);JSONObject f=FolderLinks.get(store,id);if(f!=null)return new StartSearch.Result(id,f.optString("title","Folder"),"Folders","files",null,true);if(store.isFolder(id))return new StartSearch.Result(id,store.name(id,"Folder"),"Folders","folderapps",null,true);return null;}
 private void refresh(){renderApps();renderTiles();}
 private View icon(StartSearch.Result i){if(i.id.equals("@nota"))return new TenStyle.Glyph(getContext(),"nota",TenStyle.accent(getContext()));if(i.icon==null)return new AeroIcon(getContext(),i.kind);ImageView v=new ImageView(getContext());v.setImageDrawable(i.icon);return v;}
 private void renderApps(){
  appList.removeAllViews();String q=search.getText().toString().trim();List<StartSearch.Result> rows=new ArrayList<>();
  if(q.isEmpty()){rows.addAll(builtins());for(AppCatalog.Entry a:all)rows.add(new StartSearch.Result(a.id,store.name(a.id,a.label),"Apps","programs",a.icon,true));}
  else{rows.addAll(StartSearch.find(store,all,q));for(StartSearch.Result b:builtins())if(b.title.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))&&rows.stream().noneMatch(r->r.id.equals(b.id)))rows.add(b);}
  rows.sort(Comparator.comparing(a->a.title.toLowerCase(Locale.ROOT)));String previous="";
  for(StartSearch.Result item:rows){String letter=item.title.substring(0,1).toUpperCase(Locale.ROOT);if(q.isEmpty()&&!letter.equals(previous)){TextView heading=Aero.text(getContext(),letter,15,TenStyle.text(getContext()));heading.setPadding(Aero.dp(getContext(),14),0,0,0);appList.addView(heading,new LayoutParams(-1,Aero.dp(getContext(),38)));previous=letter;}
   LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Aero.dp(getContext(),10),0,Aero.dp(getContext(),4),0);row.setBackground(TenStyle.hover(getContext(),Color.TRANSPARENT));row.addView(icon(item),new LayoutParams(Aero.dp(getContext(),28),Aero.dp(getContext(),28)));TextView name=Aero.text(getContext(),item.title,13,TenStyle.text(getContext()));name.setPadding(Aero.dp(getContext(),10),0,0,0);name.setMaxLines(2);row.addView(name,new LayoutParams(0,-1,1));row.setFocusable(true);row.setContentDescription("Open "+item.title);row.setOnClickListener(v->actions.open(item.id));ContextMenus.bind(row,(x,y)->appOptions(row,item));appList.addView(row,new LayoutParams(-1,Aero.dp(getContext(),46)));
  }
  if(!q.isEmpty()){appList.addView(TenStyle.command(getContext(),"Search linked files and folders",()->actions.open("start-search:"+android.net.Uri.encode(q))));appList.addView(TenStyle.command(getContext(),"Search the web",()->actions.open("web-search:"+android.net.Uri.encode(q))));}
 }
 private void appOptions(View anchor,StartSearch.Result item){PopupMenu m=new PopupMenu(getContext(),anchor);boolean pinned=store.startPins().contains(item.id);m.getMenu().add(pinned?"Unpin from Start":"Pin to Start").setOnMenuItemClickListener(v->{List<String> p=new ArrayList<>(store.startPins());if(pinned)p.remove(item.id);else p.add(item.id);store.startPins(p);renderTiles();return true;});m.getMenu().add("More options").setOnMenuItemClickListener(v->{actions.options(anchor,item.id);return true;});m.show();}
 private void renderTiles(){
  tileGroups.removeAllViews();int available=Math.max(Aero.dp(getContext(),160),getWidth()-Aero.dp(getContext(),68)-(getWidth()>=Aero.dp(getContext(),600)?Aero.dp(getContext(),220):0));int unit=available/4;
  for(String group:new String[]{"Productivity","Explore"}){
   List<StartSearch.Result> items=new ArrayList<>();for(String id:store.startPins()){StartSearch.Result i=resolve(id);if(i!=null&&group.equals(store.prefs.getString("ten_group_"+id,"Productivity")))items.add(i);}if(items.isEmpty())continue;
   TextView title=Aero.text(getContext(),group,13,TenStyle.text(getContext()));tileGroups.addView(title,new LayoutParams(-1,Aero.dp(getContext(),42)));FrameLayout grid=new FrameLayout(getContext());List<String> sizes=new ArrayList<>();for(StartSearch.Result i:items)sizes.add(store.prefs.getString("ten_size_"+i.id,"Medium"));List<TenTileLayout.Cell> cells=TenTileLayout.place(sizes);int height=0;
   for(int i=0;i<items.size();i++){StartSearch.Result item=items.get(i);TenTileLayout.Cell cell=cells.get(i);FrameLayout tile=new FrameLayout(getContext());tile.setBackground(TenStyle.hover(getContext(),TenStyle.light(getContext())?0xffdedede:0xff353535));tile.setFocusable(true);tile.setContentDescription(item.title+", "+sizes.get(i)+" Start tile");tile.addView(icon(item),new FrameLayout.LayoutParams(Aero.dp(getContext(),cell.w==1?26:38),Aero.dp(getContext(),cell.w==1?26:38),Gravity.CENTER));
    if(cell.w>1){TextView name=Aero.text(getContext(),item.id.equals("@calendar")?new java.text.SimpleDateFormat("EEEE d",Locale.getDefault()).format(new Date()):item.title,11,TenStyle.text(getContext()));name.setMaxLines(2);name.setPadding(Aero.dp(getContext(),7),0,Aero.dp(getContext(),5),Aero.dp(getContext(),5));tile.addView(name,new FrameLayout.LayoutParams(-1,Aero.dp(getContext(),33),Gravity.BOTTOM));}
    tile.setOnClickListener(v->actions.open(item.id));ContextMenus.desktopIcon(tile,(x,y)->tileOptions(tile,item),()->tile.startDragAndDrop(ClipData.newPlainText("Start tile",item.id),new View.DragShadowBuilder(tile),new TileDrag(item.id),0));tile.setOnDragListener((v,e)->{if(!(e.getLocalState() instanceof TileDrag))return false;if(e.getAction()==DragEvent.ACTION_DROP){String source=((TileDrag)e.getLocalState()).id;List<String> pins=new ArrayList<>(store.startPins());if(!source.equals(item.id)&&pins.contains(item.id)){pins.remove(source);pins.add(pins.indexOf(item.id),source);store.startPins(pins);post(this::renderTiles);}}return true;});
    FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(cell.w*unit-Aero.dp(getContext(),4),cell.h*unit-Aero.dp(getContext(),4));p.leftMargin=cell.col*unit;p.topMargin=cell.row*unit;grid.addView(tile,p);height=Math.max(height,(cell.row+cell.h)*unit);
   }tileGroups.addView(grid,new LayoutParams(-1,height));
  }
  if(tileGroups.getChildCount()==0){TextView h=Aero.text(getContext(),"Hold an app in All apps to pin it here.",14,TenStyle.text(getContext()));Aero.pad(h,16);tileGroups.addView(h);}
 }
 private static final class TileDrag{final String id;TileDrag(String id){this.id=id;}}
 private void tileOptions(View tile,StartSearch.Result item){PopupMenu m=new PopupMenu(getContext(),tile);m.getMenu().add("Unpin from Start").setOnMenuItemClickListener(v->{List<String> p=new ArrayList<>(store.startPins());p.remove(item.id);store.startPins(p);renderTiles();return true;});for(String size:TenTileLayout.SIZES)m.getMenu().add("Resize: "+size).setOnMenuItemClickListener(v->{store.prefs.edit().putString("ten_size_"+item.id,size).apply();renderTiles();return true;});m.getMenu().add("Move tile").setOnMenuItemClickListener(v->{tile.startDragAndDrop(ClipData.newPlainText("Start tile",item.id),new View.DragShadowBuilder(tile),new TileDrag(item.id),0);return true;});m.getMenu().add("Move to other group").setOnMenuItemClickListener(v->{store.prefs.edit().putString("ten_group_"+item.id,store.prefs.getString("ten_group_"+item.id,"Productivity").equals("Explore")?"Productivity":"Explore").apply();renderTiles();return true;});m.getMenu().add("More options").setOnMenuItemClickListener(v->{actions.options(tile,item.id);return true;});m.show();}
 void show(boolean keyboard){setBackgroundColor(TenStyle.surface(getContext()));setVisibility(VISIBLE);bringToFront();showApps=keyboard;search.setText("");refresh();fit();if(keyboard){search.requestFocus();search.post(()->((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(search,InputMethodManager.SHOW_IMPLICIT));}else requestFocus();}
 void hide(){((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(),0);search.clearFocus();setVisibility(GONE);}
 JSONObject state(){JSONObject r=new JSONObject();GadgetLayout.put(r,"visible",getVisibility()==VISIBLE);GadgetLayout.put(r,"query",search.getText().toString());GadgetLayout.put(r,"apps",showApps);return r;}
 void restore(JSONObject s){if(s==null||!s.optBoolean("visible"))return;show(false);showApps=s.optBoolean("apps");search.setText(s.optString("query"));fit();}
}
