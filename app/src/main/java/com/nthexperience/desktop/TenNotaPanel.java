package com.nthexperience.desktop;
import android.content.*;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;

/** Nota's free writing gateway in a native launcher window. Only the composer text is sent. */
final class TenNotaPanel extends LinearLayout {
 final CloudAI cloud;final DesktopStore store;final EditText input;final TextView status,answer;final Spinner task;final TextView send;String pending="",lastReply="";boolean closed;
 TenNotaPanel(Context c,DesktopStore store){
  super(c);this.store=store;setOrientation(VERTICAL);Aero.pad(this,14);setContentDescription("Nth Nota writing assistant");
  TextView title=Aero.text(c,"What can I help you write?",22,Aero.INK);addView(title,new LayoutParams(-1,Aero.dp(c,54)));
  TextView hint=Aero.text(c,"Draft, polish, summarize, or explore ideas with Nth Nota.",13,0xff666666);addView(hint,new LayoutParams(-1,-2));
  ScrollView scroll=new ScrollView(c);answer=Aero.text(c,store.prefs.getString("ten_nota_reply","Your reply will appear here."),15,Aero.INK);answer.setTextIsSelectable(true);Aero.pad(answer,10);scroll.addView(answer);addView(scroll,new LayoutParams(-1,0,1));
  task=new Spinner(c);task.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,new String[]{"Write","Brainstorm","Proofread","Polish","Summarize"}));task.setContentDescription("Writing task");addView(task,new LayoutParams(-1,Aero.dp(c,44)));
  input=new EditText(c);input.setHint("Message Nth Nota");input.setTextSize(15);input.setMinLines(2);input.setMaxLines(5);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16000)});input.setText(store.prefs.getString("ten_nota_draft",""));input.setContentDescription("Message Nth Nota");addView(input,new LayoutParams(-1,-2));
  status=Aero.text(c,"Free cloud writing · optional",12,0xff555555);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);addView(status,new LayoutParams(-1,Aero.dp(c,36)));
  cloud=new CloudAI(c,event->post(()->result(event)));
  LinearLayout commands=new LinearLayout(c);send=Aero.button(c,"Send",this::send);commands.addView(send,new LayoutParams(0,Aero.dp(c,48),1));commands.addView(Aero.button(c,"Stop",()->{if(!pending.isEmpty())cloud.cancel(pending);}),new LayoutParams(0,Aero.dp(c,48),1));commands.addView(Aero.button(c,"Save to Notepad",this::save),new LayoutParams(0,Aero.dp(c,48),1));addView(commands);
  TextView privacy=Aero.button(c,"Cloud assistance settings",()->consent(false));addView(privacy,new LayoutParams(-1,Aero.dp(c,44)));
  input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){store.prefs.edit().putString("ten_nota_draft",s.toString()).apply();}public void afterTextChanged(Editable e){}});
 }
 void consent(boolean sending){Aero.dialog(getContext()).setTitle("Free cloud writing assistance").setMessage("When you press Send, the text in this message is sent over HTTPS to Nth Nota's Cloudflare gateway. Cloudflare Workers AI, or its configured Groq fallback, writes the reply. Free daily limits apply. Other notes, files, app activity and your desktop are not included.\n\nAllow cloud assistance on this launcher? You can turn it off here at any time.").setPositiveButton("Allow",(d,w)->{cloud.allow(true);if(sending)send();else status.setText("Cloud assistance allowed");}).setNegativeButton("Turn off",(d,w)->{cloud.allow(false);status.setText("Cloud assistance off");}).setNeutralButton("Cancel",null).show();}
 private void send(){if(closed||!pending.isEmpty())return;if(input.getText().toString().trim().isEmpty()){input.setError("Enter a writing request");return;}try{if(!new JSONObject(cloud.state()).optBoolean("allowed")){consent(true);return;}pending="ten_"+java.util.UUID.randomUUID().toString().replace("-","");send.setEnabled(false);status.setText("Connecting…");String[] tasks={"generate","brainstorm","proofread","polish","summarize"};cloud.request(pending,new JSONObject().put("task",tasks[task.getSelectedItemPosition()]).put("prompt",input.getText().toString()).toString());}catch(JSONException e){pending="";send.setEnabled(true);status.setText("The request could not be prepared.");}}
 void result(JSONObject e){if(closed||!e.optString("id").equals(pending))return;if(e.optString("event").equals("progress")){status.setText(e.optString("message"));return;}pending="";send.setEnabled(true);if(e.has("error")){status.setText(e.optString("message","Assistance unavailable. Try again later."));return;}lastReply=e.optString("text");answer.setText(lastReply);store.prefs.edit().putString("ten_nota_reply",lastReply).apply();status.setText("Reply received · "+e.optString("provider","cloud"));}
 private void save(){String text=lastReply.isEmpty()?store.prefs.getString("ten_nota_reply",""):lastReply;if(text.isEmpty())return;String old=store.prefs.getString("note","");store.prefs.edit().putString("note",old+(old.isEmpty()?"":"\n\n")+text).apply();Toast.makeText(getContext(),"Saved to Notepad",Toast.LENGTH_SHORT).show();}
 void foreground(boolean active){cloud.foreground(active);}
 void close(){closed=true;cloud.close();}
 @Override protected void onWindowVisibilityChanged(int visible){super.onWindowVisibilityChanged(visible);if(cloud!=null)cloud.foreground(visible==VISIBLE);}
 @Override public void onVisibilityAggregated(boolean visible){super.onVisibilityAggregated(visible);if(cloud!=null)cloud.foreground(visible);}
 @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();close();}
}
