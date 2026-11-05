package com.nthexperience.desktop;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Android share/selection target. Importing text never submits an AI request. */
public final class NotaTextActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);Intent source=getIntent();String action=source.getAction();CharSequence text=null;if(Intent.ACTION_SEND.equals(action))text=source.getCharSequenceExtra(Intent.EXTRA_TEXT);else if(Intent.ACTION_PROCESS_TEXT.equals(action))text=source.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);if(text!=null&&text.length()>0){String passage=text.subSequence(0,Math.min(TenWritingPrompts.MAX_COMPOSER,text.length())).toString();startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("nota_import",passage));}else startActivity(new Intent(this,MainActivity.class).putExtra("open_tool","@nota"));setResult(RESULT_CANCELED);finish();}
}
