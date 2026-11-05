package com.nthexperience.desktop;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.function.Predicate;

/** Windows-style Run for installed apps, launcher tools and website addresses. */
final class TenRun {
    static void show(Context context,DesktopStore store,Predicate<String> launch){
        LinearLayout body=Aero.column(context);Aero.pad(body,16);LinearLayout intro=new LinearLayout(context);intro.setGravity(Gravity.CENTER_VERTICAL);intro.addView(new TenBrandMark(context),new LinearLayout.LayoutParams(Aero.dp(context,42),Aero.dp(context,42)));TextView hint=Aero.text(context,"Type an app name, Nth tool, or website address.",13,Aero.INK);hint.setPadding(Aero.dp(context,12),0,0,0);intro.addView(hint,new LinearLayout.LayoutParams(0,-2,1));body.addView(intro);TextView label=Aero.text(context,"Open:",13,Aero.INK);label.setPadding(0,Aero.dp(context,12),0,0);body.addView(label);AutoCompleteTextView entry=new AutoCompleteTextView(context);entry.setSingleLine();entry.setThreshold(0);entry.setHint("notepad, calc, explorer, https://…");entry.setContentDescription("Run command");List<String> recent=history(store);entry.setAdapter(new ArrayAdapter<>(context,android.R.layout.simple_dropdown_item_1line,recent));entry.setOnFocusChangeListener((v,focused)->{if(focused&&!recent.isEmpty())entry.showDropDown();});body.addView(entry,new LinearLayout.LayoutParams(-1,Aero.dp(context,48)));
        AlertDialog dialog=Aero.dialog(context).setTitle("Run").setView(body).setPositiveButton("OK",null).setNegativeButton("Cancel",null).setNeutralButton("Browse apps",(d,w)->{if(context instanceof MainActivity)((MainActivity)context).open("@search");}).show();Runnable submit=()->{String command=entry.getText().toString().trim();if(command.isEmpty()||command.length()>256){entry.setError("Enter an app, Nth tool, or address (up to 256 characters)");return;}if(!launch.test(command)){entry.setError("Could not find that app or command. Try its name in Start.");return;}List<String> next=new ArrayList<>();next.add(command);for(String old:recent)if(!old.equalsIgnoreCase(command)&&next.size()<10)next.add(old);store.prefs.edit().putString("ten_run_history",new JSONArray(next).toString()).apply();dialog.dismiss();};dialog.getButton(-1).setOnClickListener(v->submit.run());entry.setOnEditorActionListener((v,action,event)->{submit.run();return true;});
    }
    static List<String> history(DesktopStore store){List<String> out=new ArrayList<>();try{JSONArray rows=new JSONArray(store.prefs.getString("ten_run_history","[]"));for(int i=0;i<Math.min(10,rows.length());i++)if(rows.opt(i) instanceof String&&rows.getString(i).length()<=256)out.add(rows.getString(i));}catch(JSONException ignored){}return out;}
}
