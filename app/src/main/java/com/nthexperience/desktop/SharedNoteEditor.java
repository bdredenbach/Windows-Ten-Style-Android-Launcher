package com.nthexperience.desktop;
import android.content.*;
import android.text.*;
import android.widget.EditText;
/** Notepad is one shared local document; multiple desktop views stay in sync. */
final class SharedNoteEditor extends EditText {
 final android.content.SharedPreferences prefs;boolean listening;
 final android.content.SharedPreferences.OnSharedPreferenceChangeListener changed=(p,key)->{if(key==null||key.equals("note"))post(this::refresh);};
 SharedNoteEditor(Context context,DesktopStore store){super(context);prefs=store.prefs;setText(prefs.getString("note",""));addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable e){}public void onTextChanged(CharSequence s,int a,int b,int c){String value=s.toString();if(!value.equals(prefs.getString("note","")))prefs.edit().putString("note",value).apply();}});}
 private void refresh(){String value=prefs.getString("note","");if(!value.contentEquals(getText())){int at=Math.max(0,getSelectionStart());setText(value);setSelection(Math.min(at,value.length()));}}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();if(!listening){prefs.registerOnSharedPreferenceChangeListener(changed);listening=true;}refresh();}
 @Override protected void onDetachedFromWindow(){if(listening){prefs.unregisterOnSharedPreferenceChangeListener(changed);listening=false;}super.onDetachedFromWindow();}
}
