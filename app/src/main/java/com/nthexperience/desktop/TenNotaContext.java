package com.nthexperience.desktop;

import android.widget.EditText;

/** An explicitly chosen passage; applying a reply requires an unchanged source. */
final class TenNotaContext {
    final String title,text,original;
    final boolean privateText;
    final EditText target;
    final int start,end;
    TenNotaContext(String title,String text,boolean privateText) {this(title,text,privateText,null,0,0,"");}
    private TenNotaContext(String title,String text,boolean privateText,EditText target,int start,int end,String original){
        this.title=title;this.text=text;this.privateText=privateText;this.target=target;this.start=start;this.end=end;this.original=original;
    }
    static TenNotaContext editor(String title,EditText editor){
        String original=editor.getText().toString();int start=Math.max(0,editor.getSelectionStart()),end=Math.max(0,editor.getSelectionEnd());
        if(start>end){int swap=start;start=end;end=swap;}if(start==end){start=0;end=original.length();}
        return new TenNotaContext(title,original.substring(start,end),false,editor,start,end,original);
    }
    boolean unchanged(){return text.length()<=TenWritingPrompts.MAX_COMPOSER&&target!=null&&target.isAttachedToWindow()&&original.equals(target.getText().toString())&&(!(target instanceof SharedNoteEditor)||original.equals(((SharedNoteEditor)target).prefs.getString("note","")));}
    boolean apply(String reply){if(!unchanged())return false;target.getText().replace(start,end,reply);return true;}
}
