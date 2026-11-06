package com.nthexperience.desktop;

import android.content.Context;
import android.content.pm.*;
import android.view.Gravity;
import android.widget.*;
import android.text.*;
import java.util.*;
import java.util.function.Consumer;

/** Settings inventory backed by this device, with no simulated Windows packages. */
final class TenDeviceFeatures extends LinearLayout {
    final EditText query;
    final LinearLayout rows;
    final TextView count;
    final List<Entry> entries=new ArrayList<>();
    static final class Entry {
        final String title,detail;final ApplicationInfo app;
        Entry(String title,String detail,ApplicationInfo app){this.title=title;this.detail=detail;this.app=app;}
    }
    TenDeviceFeatures(Context c,boolean features,Consumer<String> open){
        super(c);setOrientation(VERTICAL);
        setContentDescription(features?"Device optional features":"Installed system components");
        TextView note=Aero.text(c,features?"Features available on this device. Android and the device manufacturer manage their installation.":"Android system apps visible to Nth Desktop. Select a component to view its Android App info.",12,0xff666666);
        note.setPadding(0,0,0,Aero.dp(c,12));addView(note);
        query=new EditText(c);query.setSingleLine();query.setTextSize(13);query.setTypeface(Aero.face(c));query.setHint("Search this list");query.setContentDescription("Search "+(features?"optional features":"system components"));
        addView(query,new LayoutParams(-1,Aero.dp(c,44)));
        count=Aero.text(c,"",12,0xff666666);count.setPadding(0,Aero.dp(c,10),0,Aero.dp(c,10));addView(count);
        rows=Aero.column(c);addView(rows,new LayoutParams(-1,-2));PackageManager pm=c.getPackageManager();
        if(features){
            for(FeatureInfo feature:pm.getSystemAvailableFeatures())if(feature.name!=null)entries.add(new Entry(feature.name,"Version "+feature.version,null));
        }else{
            for(ApplicationInfo app:pm.getInstalledApplications(0))if((app.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0){
                String version="";try{PackageInfo info=pm.getPackageInfo(app.packageName,0);if(info.versionName!=null)version=" · "+info.versionName;}catch(PackageManager.NameNotFoundException ignored){}
                entries.add(new Entry(app.loadLabel(pm).toString(),app.packageName+version+(app.enabled?"":" · Disabled"),app));
            }
        }
        entries.sort(Comparator.comparing((Entry e)->e.title,String.CASE_INSENSITIVE_ORDER).thenComparing(e->e.detail));
        query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int n,int after){}public void onTextChanged(CharSequence s,int start,int before,int n){render(open);}public void afterTextChanged(Editable value){}});
        render(open);
    }
    private void render(Consumer<String> open){
        rows.removeAllViews();String key=query.getText().toString().trim().toLowerCase(Locale.ROOT);int shown=0;
        for(Entry entry:entries){
            if(!key.isEmpty()&&!(entry.title+" "+entry.detail).toLowerCase(Locale.ROOT).contains(key))continue;
            shown++;LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,Aero.dp(getContext(),8),0,Aero.dp(getContext(),8));
            if(entry.app!=null){ImageView icon=new ImageView(getContext());icon.setImageDrawable(entry.app.loadIcon(getContext().getPackageManager()));icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LayoutParams p=new LayoutParams(Aero.dp(getContext(),32),Aero.dp(getContext(),32));p.rightMargin=Aero.dp(getContext(),12);row.addView(icon,p);}
            LinearLayout text=Aero.column(getContext());TextView title=Aero.text(getContext(),entry.title,13,Aero.INK);title.setTextIsSelectable(entry.app==null);text.addView(title);TextView detail=Aero.text(getContext(),entry.detail,11,0xff666666);text.addView(detail);row.addView(text,new LayoutParams(0,-2,1));
            if(entry.app!=null){row.setFocusable(true);row.setBackground(Aero.selection(getContext()));row.setContentDescription("App info: "+entry.app.packageName);row.setOnClickListener(v->open.accept("app-info:"+entry.app.packageName));}
            rows.addView(row,new LayoutParams(-1,-2));
        }
        count.setText(shown+" of "+entries.size()+" items · Sorted by name");
        if(shown==0)rows.addView(Aero.text(getContext(),"No matching items",13,0xff666666));
        TenAppearance.apply(this);
    }
}
