package com.nthexperience.desktop;

import android.content.*;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;

/** The shared native Nota workspace. Context is reviewed in the composer before Send. */
final class TenNotaPanel extends LinearLayout {
    final TenAssistant assistant;final CloudAI cloud;final DesktopStore store;final EditText input;
    final TextView status,answer,guidance,taskSummary,contextLabel,apply,send,compactSend,copyReply,saveReply;final Spinner task;final LinearLayout actionChoices;final ScrollView replyScroll;
    int guidanceTask=-1;boolean hydrating,commandReply;String desktopId;
    boolean compactLayout,tinyLayout,closed,privateDraft,importedDraft;String pending="",lastReply="",requestText="";TenNotaContext context;
    TenNotaPanel(Context c,DesktopStore store){this(c,store,TenDesktopItems.current(store));}
    TenNotaPanel(Context c,DesktopStore store,String desktopId){this(c,store,desktopId,null);}
    TenNotaPanel(Context c,DesktopStore store,String desktopId,LocalAI.Backend backend){
        super(c);this.store=store;this.desktopId=desktopId;setOrientation(VERTICAL);setBackgroundColor(Color.WHITE);Aero.pad(this,12);setContentDescription("Nth Nota apps and writing assistant");
        LinearLayout top=new LinearLayout(c);TextView title=Aero.text(c,"Nth Nota",22,Aero.INK);top.addView(title,new LayoutParams(0,Aero.dp(c,48),1));top.addView(button("New chat",this::newChat),new LayoutParams(Aero.dp(c,82),Aero.dp(c,44)));top.addView(button("History",this::history),new LayoutParams(Aero.dp(c,72),Aero.dp(c,44)));addView(top);
        taskSummary=Aero.text(c,"",12,0xff666666);taskSummary.setPadding(Aero.dp(c,8),Aero.dp(c,6),Aero.dp(c,8),Aero.dp(c,8));taskSummary.setMaxLines(3);taskSummary.setEllipsize(android.text.TextUtils.TruncateAt.END);taskSummary.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);addView(taskSummary);
        contextLabel=Aero.text(c,"",12,0xff666666);contextLabel.setVisibility(GONE);contextLabel.setPadding(Aero.dp(c,8),Aero.dp(c,6),Aero.dp(c,8),Aero.dp(c,8));contextLabel.setMaxLines(2);contextLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);contextLabel.setContentDescription("Nota context source");contextLabel.setOnClickListener(v->{if(context!=null)Aero.dialog(c).setTitle("Context from "+context.title).setMessage("The text in the composer is the only passage used for this request. Edit it before sending. Clearing context keeps this passage in writing mode. Use New command to open apps.").setPositiveButton("Keep",null).setNeutralButton("Clear context",(d,w)->clearContext()).show();});addView(contextLabel);
        ScrollView scroll=new ScrollView(c);replyScroll=scroll;scroll.setFillViewport(true);LinearLayout reply=Aero.column(c);lastReply="";guidance=Aero.text(c,"",16,Aero.INK);Aero.pad(guidance,12);reply.addView(guidance);answer=Aero.text(c,lastReply,16,Aero.INK);answer.setVisibility(lastReply.isEmpty()?GONE:VISIBLE);answer.setTextIsSelectable(true);Aero.pad(answer,12);actionChoices=Aero.column(c);actionChoices.setContentDescription("Nota desktop actions");reply.addView(actionChoices,new LayoutParams(-1,-2));reply.addView(answer);scroll.addView(reply);addView(scroll,new LayoutParams(-1,0,1));
        LinearLayout replyTools=new LinearLayout(c);copyReply=button("Copy reply",this::copy);replyTools.addView(copyReply,new LayoutParams(0,Aero.dp(c,40),1));saveReply=button("Save to Notepad",this::save);replyTools.addView(saveReply,new LayoutParams(0,Aero.dp(c,40),1));apply=button("Apply to source",this::applyReply);apply.setContentDescription("Apply Nota reply to source");apply.setVisibility(GONE);replyTools.addView(apply,new LayoutParams(0,Aero.dp(c,40),1));addView(replyTools);
        HorizontalScrollView suggestions=new HorizontalScrollView(c);suggestions.setHorizontalScrollBarEnabled(false);LinearLayout chips=new LinearLayout(c);String[] labels={"Draft content","Brainstorm","Proofread","Polish","Summarize"};for(int i=0;i<labels.length;i++){final int index=i;TextView chip=button(labels[i],()->chooseTask(index));LayoutParams p=new LayoutParams(-2,Aero.dp(c,42));p.setMargins(Aero.dp(c,3),0,Aero.dp(c,3),0);chips.addView(chip,p);}for(String example:TenNotaActions.EXAMPLES){TextView chip=button(example,()->example(example));chip.setContentDescription("Try command: "+example);chips.addView(chip,0,new LayoutParams(-2,Aero.dp(c,42)));}suggestions.addView(chips);suggestions.setContentDescription("Nota writing and desktop suggestions");addView(suggestions);
        task=new Spinner(c);task.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,new String[]{"Ask or write","Brainstorm","Proofread","Polish","Summarize","Apps & settings"}));task.setContentDescription("Nota task");LinearLayout modes=new LinearLayout(c);modes.setGravity(Gravity.CENTER_VERTICAL);modes.addView(task,new LayoutParams(0,Aero.dp(c,44),1));modes.addView(button("New command",this::newCommand),new LayoutParams(-2,Aero.dp(c,44)));compactSend=button("Send",this::send);compactSend.setContentDescription("Send Nota message in compact window");compactSend.setVisibility(GONE);modes.addView(compactSend,new LayoutParams(Aero.dp(c,64),Aero.dp(c,32)));addView(modes);
        input=new EditText(c);input.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI|android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN);input.setHint("Message Nth Nota");input.setTextSize(15);input.setMinLines(2);input.setMaxLines(5);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(TenWritingPrompts.MAX_COMPOSER)});selectTask(0);input.setContentDescription("Message Nth Nota");addView(input,new LayoutParams(-1,-2));
        status=Aero.text(c,"Commands on this device · local-first writing",12,0xff555555);status.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);addView(status,new LayoutParams(-1,Aero.dp(c,32)));
        assistant=new TenAssistant(c,event->post(()->result(event)),backend);cloud=assistant.cloud;
        LinearLayout commands=new LinearLayout(c);send=button("Send",this::send);commands.addView(send,new LayoutParams(0,Aero.dp(c,44),1));commands.addView(button("Stop",()->{if(!pending.isEmpty())assistant.cancel(pending);}),new LayoutParams(0,Aero.dp(c,44),1));commands.addView(button("Tools",this::tools),new LayoutParams(0,Aero.dp(c,44),1));addView(commands);
        task.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){updateTaskSummary();persist();if(pending.isEmpty()&&lastReply.isEmpty())status.setText(position==5?(importedDraft?"Writing passage kept · use New command to open apps":"Ready for a new command · nothing has been sent"):"Ready · nothing has been sent");}public void onNothingSelected(android.widget.AdapterView<?> parent){}});
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){actionChoices.removeAllViews();persist();}public void afterTextChanged(Editable e){}});
        addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            boolean tiny=b-t<Aero.dp(c,280),compact=b-t<Aero.dp(c,500);
            suggestions.setVisibility(b-t<Aero.dp(c,360)?GONE:VISIBLE);contextLabel.setVisibility(tiny||contextLabel.getText().length()==0?GONE:VISIBLE);
            if(tiny==tinyLayout&&compact==compactLayout)return;tinyLayout=tiny;compactLayout=compact;
            taskSummary.setVisibility(tiny?GONE:VISIBLE);replyTools.setVisibility(tiny?GONE:VISIBLE);replyScroll.setVisibility(tiny?GONE:VISIBLE);status.setVisibility(tiny?GONE:VISIBLE);
            top.setVisibility(compact?GONE:VISIBLE);commands.setVisibility(tiny?GONE:VISIBLE);compactSend.setVisibility(tiny?VISIBLE:GONE);
            for(int i=0;i<modes.getChildCount();i++){View mode=modes.getChildAt(i);int h=Aero.dp(c,tiny?32:44);if(mode.getLayoutParams().height!=h){mode.getLayoutParams().height=h;mode.requestLayout();}mode.setMinimumHeight(Aero.dp(c,tiny?32:40));if(mode instanceof TextView)((TextView)mode).setMinHeight(Aero.dp(c,tiny?32:40));}
            compactLayout=compact;Aero.pad(this,tiny?4:compact?8:12);taskSummary.setTextSize(compact?11:12);contextLabel.setTextSize(compact?11:12);
            status.getLayoutParams().height=Aero.dp(c,compact?24:32);for(int i=0;i<replyTools.getChildCount();i++)replyTools.getChildAt(i).getLayoutParams().height=Aero.dp(c,compact?32:40);
            input.setMinLines(compact?1:2);input.setMaxLines(tiny?1:compact?2:5);
        });
        restoreState(TenNotaDrafts.read(store,desktopId));
    }
    private void clearContext(){context=null;importedDraft=true;contextLabel.setText("Writing passage · use New command to open apps");contextLabel.setVisibility(VISIBLE);apply.setVisibility(GONE);persist();}
    JSONObject state(){
        if(privateDraft)return TenNotaState.redacted();
        try{return TenNotaState.ordinary(input.getText().toString(),Math.max(0,task.getSelectedItemPosition()),importedDraft,lastReply,contextLabel.getText().toString(),guidance.getVisibility()==VISIBLE,commandReply);}catch(JSONException e){throw new IllegalStateException(e);}
    }
    private void persist(){if(!hydrating&&!privateDraft&&!closed)TenNotaDrafts.write(store,desktopId,state());}
    JSONObject sessionState(){
        if(privateDraft)return TenNotaState.redacted();
        // Full text lives in the scoped store; keep Android's saved-state transaction small.
        persist();JSONObject ref=new JSONObject();GadgetLayout.put(ref,"version",1);GadgetLayout.put(ref,"desktop",desktopId);return ref;
    }
    void restoreSessionState(JSONObject saved){
        if(saved!=null&&saved.length()==2&&Integer.valueOf(1).equals(saved.opt("version"))&&desktopId.equals(saved.opt("desktop")))restoreState(TenNotaDrafts.read(store,desktopId));
        else restoreState(saved);
    }
    void moveToDesktop(String id){if(desktopId.equals(id))return;if(!privateDraft)TenNotaDrafts.move(store,desktopId,id,state());desktopId=id;}
    private void setSendEnabled(boolean enabled){send.setEnabled(enabled);compactSend.setEnabled(enabled);}
    TextView visibleSend(){return compactSend.getVisibility()==VISIBLE?compactSend:send;}
    private void cancelRequest(){String previous=pending;pending="";setSendEnabled(true);if(!previous.isEmpty())assistant.cancel(previous);}
    void restoreState(JSONObject saved){
        final JSONObject data;try{data=TenNotaState.checked(saved);}catch(JSONException ignored){return;}
        hydrating=true;try{
            cancelRequest();context=null;privateDraft=data.optBoolean("private");importedDraft=privateDraft||data.optBoolean("imported");
            clearReply();selectTask(data.optInt("task",0));input.setText(data.optString("draft"));
            String source=data.optString("source");if(privateDraft)source="Private passage cleared · choose New command for an app request";else if(source.isEmpty()&&importedDraft)source="Restored writing passage · use New command to open apps";
            contextLabel.setText(source);contextLabel.setVisibility(source.isEmpty()?GONE:VISIBLE);
            displayReply(data.optString("reply"),data.optBoolean("command"));guidance.setVisibility(data.optBoolean("guidance",true)||lastReply.isEmpty()?VISIBLE:GONE);
            apply.setVisibility(GONE);status.setText(privateDraft?"Private passage excluded from saved state":"Restored · nothing has been sent");
        }finally{hydrating=false;}
    }
    private void selectTask(int index){task.setSelection(index);updateTaskSummary();}
    private void updateTaskSummary(){
        String[] summaries={
            "Ask questions, write new content, or give a simple app command.",
            "Explore ideas, suggestions and new directions for a topic.",
            "Correct spelling, grammar and punctuation while keeping your wording.",
            "Improve clarity, tone and flow while keeping your meaning.",
            "Condense a passage into its key points and important details.",
            "Open apps and settings, switch apps, or search the web and linked files."
        };
        int selected=Math.max(0,Math.min(summaries.length-1,task.getSelectedItemPosition()));
        String[] instructions={
            "Type a question or describe what you want to write, then press Send. Include the topic, tone and any details you want in the reply.",
            "Type a topic or goal, then press Send for ideas. Include any limits or preferences to help shape the suggestions.",
            "Paste or type the text you want checked, then press Send. I’ll correct spelling, grammar and punctuation while keeping your wording.",
            "Paste or type the text you want improved, then press Send. Describe the tone or style you’d like, and I’ll make it clearer and smoother.",
            "Paste or type a passage, then press Send for a shorter version. Mention any length or format you prefer, such as a paragraph or bullet points.",
            "Type open followed by an installed app name, then press Send. One match opens immediately; multiple matches show choices."
        };
        String[] hints={"Ask a question or describe what to write","Enter a topic or goal for ideas","Paste or type text to proofread","Paste or type text to polish","Paste or type text to summarize","Try open ChatGPT or open Calculator"};
        taskSummary.setText(summaries[selected]);
        guidance.setText(instructions[selected]);input.setHint(hints[selected]);
        if(guidanceTask!=selected){guidanceTask=selected;guidance.setVisibility(VISIBLE);replyScroll.post(()->replyScroll.scrollTo(0,0));}
        updateReplyActions();
    }
    private void updateReplyActions(){boolean available=!lastReply.isEmpty();for(TextView action:new TextView[]{copyReply,saveReply}){action.setEnabled(available);action.setAlpha(available?1f:.45f);}}
    private void clearReply(){lastReply="";commandReply=false;answer.setText("");answer.setVisibility(GONE);actionChoices.removeAllViews();guidance.setVisibility(VISIBLE);apply.setVisibility(GONE);updateReplyActions();}
    private void displayReply(String text,boolean command){lastReply=text;commandReply=command;replyStyle(command);answer.setText(text);answer.setVisibility(text.isEmpty()?GONE:VISIBLE);guidance.setVisibility(text.isEmpty()?VISIBLE:GONE);updateReplyActions();}
    private void chooseTask(int index){selectTask(index);input.requestFocus();}
    private TextView button(String label,Runnable run){TextView v=Aero.text(getContext(),label,13,TenStyle.accent(getContext()));TenAppearance.accentText(v);v.setGravity(Gravity.CENTER);v.setPadding(Aero.dp(getContext(),10),0,Aero.dp(getContext(),10),0);v.setMinHeight(Aero.dp(getContext(),40));v.setBackground(Aero.selection(getContext()));v.setFocusable(true);v.setContentDescription(label);v.setOnClickListener(w->run.run());return v;}
    void context(TenNotaContext value){hydrating=true;try{cancelRequest();context=value;privateDraft=value.privateText;importedDraft=true;input.setText(value.text);clearReply();selectTask(4);contextLabel.setVisibility(VISIBLE);String label="From "+value.title+(privateDraft?" · private, excluded from history":" · review before Send")+(value.text.length()>TenWritingPrompts.MAX_COMPOSER?" · shortened to 15,000 characters":"");contextLabel.setText(label.substring(0,Math.min(512,label.length())));status.setText("Ready · nothing has been sent");}finally{hydrating=false;}persist();}
    private void newChat(){hydrating=true;try{cancelRequest();context=null;privateDraft=false;importedDraft=false;clearReply();selectTask(0);input.setText("");contextLabel.setText("");contextLabel.setVisibility(GONE);status.setText("Commands on this device · local-first writing");}finally{hydrating=false;}persist();}
    private void newCommand(){newChat();selectTask(5);persist();input.requestFocus();status.setText("Ready for a new command · nothing has been sent");}
    private void tools(){Aero.dialog(getContext()).setTitle("Nth Nota tools").setItems(new String[]{"Prepare on-device AI","Cloud assistance settings","Share reply","Clear local chat history","Open Notepad","Find an app or setting","New chat","Recent chats","What can Nota do?"},(d,w)->{switch(w){case 0:prepare();break;case 1:consent(false);break;case 2:if(!lastReply.isEmpty())getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,lastReply),"Share Nota reply"));break;case 3:Aero.dialog(getContext()).setTitle("Clear local chat history?").setMessage("This removes the saved writing requests and replies on this launcher.").setPositiveButton("Clear",(dialog,which)->store.prefs.edit().remove("ten_nota_history").apply()).setNegativeButton("Cancel",null).show();break;case 4:open("@notes");break;case 5:open("@search");break;case 6:newChat();break;case 7:history();break;case 8:showPlan(TenNotaActions.help());break;}}).show();}
    private void open(String id){if(getContext() instanceof MainActivity)((MainActivity)getContext()).open(id);}
    private void prepare(){if(!pending.isEmpty())return;Aero.dialog(getContext()).setTitle("Prepare on-device AI").setMessage("Supported phones use Android AICore. Preparing may download a model through Google services. Model preparation and SDK diagnostics may contact Google. Your writing is processed on this device.").setPositiveButton("Prepare",(d,w)->{pending="prepare_"+System.nanoTime();setSendEnabled(false);assistant.prepare(pending);}).setNegativeButton("Cancel",null).show();}
    void consent(boolean sending){Aero.dialog(getContext()).setTitle("Free cloud writing assistance").setMessage("For writing assistance, only the text in the composer and chosen writing task go to Nth Nota's HTTPS gateway when the local model is unavailable. Local desktop commands stay on this device. Cloudflare Workers AI, or the configured Groq fallback, writes the reply. Free limits apply. Other files, desktop activity, notifications and browser pages are not attached.\n\nAllow optional cloud assistance? You can turn it off in Tools.").setPositiveButton("Allow",(d,w)->{cloud.allow(true);if(sending)send();else status.setText("Cloud assistance allowed · press Send when ready");}).setNegativeButton("Turn off",(d,w)->{cloud.allow(false);status.setText("Cloud assistance off");}).setNeutralButton("Cancel",null).show();}
    private void send(){if(closed||!pending.isEmpty())return;if(input.getText().toString().trim().isEmpty()){input.setError("Enter a message or desktop command");return;}if(tinyLayout){android.view.inputmethod.InputMethodManager keyboard=getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);if(keyboard!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);}try{requestText=input.getText().toString();actionChoices.removeAllViews();int selected=task.getSelectedItemPosition();if(selected==5&&(context!=null||privateDraft||importedDraft)){guidance.setText("This is a writing passage. Choose New command to start an app request without using imported text.");guidance.setVisibility(VISIBLE);status.setText("Writing passage kept · New command opens a fresh composer");return;}if(context==null&&!privateDraft&&!importedDraft&&(selected==0||selected==5)){MainActivity host=getContext() instanceof MainActivity?(MainActivity)getContext():null;TenNotaActions.Plan plan=TenNotaActions.resolve(requestText,host==null?java.util.Collections.emptyList():host.notaAppTargets());if(plan!=null){showPlan(plan);if(plan.autoOpen)runAction(plan.actions.get(0));return;}if(selected==5){showPlan(TenNotaActions.help());return;}}pending="ten_"+java.util.UUID.randomUUID().toString().replace("-","");setSendEnabled(false);status.setText("Connecting…");String[] tasks={"generate","brainstorm","proofread","polish","summarize"};assistant.request(pending,tasks[Math.min(4,task.getSelectedItemPosition())],requestText);}catch(RuntimeException e){pending="";setSendEnabled(true);status.setText("The request could not be prepared.");}}
    void result(JSONObject e){if(closed||!e.optString("id").equals(pending))return;if(e.optString("event").equals("progress")){status.setText(e.optString("message"));return;}pending="";setSendEnabled(true);if(e.has("error")){status.setText(e.optString("message","Assistance unavailable. Try again later."));if(e.optBoolean("needsCloud")&&isShown())consent(false);return;}if(e.optString("status").equals("ready")){status.setText("On-device AI prepared");return;}displayReply(e.optString("text"),false);if(!privateDraft){persist();remember();}apply.setVisibility(context!=null&&context.target!=null&&context.text.length()<=TenWritingPrompts.MAX_COMPOSER?VISIBLE:GONE);status.setText("Reply received · "+(e.optBoolean("local")?"on-device":e.optString("provider","cloud")));}
    private void example(String text){newCommand();input.setText(text);input.setSelection(input.length());input.requestFocus();status.setText("Press Send to open a matching app or show action shortcuts");}
    private void replyStyle(boolean command){answer.setTextSize(command?18:16);answer.setTypeface(Aero.face(getContext()),command?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);}
    private void showPlan(TenNotaActions.Plan plan){
        android.view.inputmethod.InputMethodManager keyboard=getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);if(keyboard!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);
        displayReply(plan.message,true);apply.setVisibility(GONE);actionChoices.removeAllViews();
        for(TenNotaActions.Action action:plan.actions){
            LinearLayout choice=new LinearLayout(getContext());choice.setGravity(Gravity.CENTER_VERTICAL);choice.setPadding(Aero.dp(getContext(),12),Aero.dp(getContext(),12),Aero.dp(getContext(),12),Aero.dp(getContext(),12));choice.setMinimumHeight(Aero.dp(getContext(),64));choice.setBackground(Aero.buttonBackground(getContext()));choice.setFocusable(true);choice.setContentDescription("Nota action: "+action.label);choice.setOnClickListener(v->runAction(action));
            View icon=getContext() instanceof MainActivity?((MainActivity)getContext()).notaActionIcon(action):new AeroIcon(getContext(),"programs");choice.addView(icon,new LayoutParams(Aero.dp(getContext(),32),Aero.dp(getContext(),32)));
            TextView label=Aero.text(getContext(),action.label,18,Aero.INK);label.setTypeface(Aero.face(getContext()),android.graphics.Typeface.BOLD);label.setMaxLines(3);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LayoutParams text=new LayoutParams(0,-2,1);text.leftMargin=Aero.dp(getContext(),12);choice.addView(label,text);TextView arrow=Aero.text(getContext(),"›",23,Aero.INK);arrow.setGravity(Gravity.CENTER);choice.addView(arrow,new LayoutParams(Aero.dp(getContext(),24),-1));LayoutParams row=new LayoutParams(-1,-2);row.setMargins(0,0,0,Aero.dp(getContext(),8));TenAppearance.apply(choice);actionChoices.addView(choice,row);
        }
        replyScroll.post(()->replyScroll.scrollTo(0,0));status.setText("Desktop command · stays on this device");persist();
    }
    private void runAction(TenNotaActions.Action action){
        if(closed||!pending.isEmpty())return;
        String message=getContext() instanceof MainActivity?((MainActivity)getContext()).performNotaAction(action):"Open Nth Nota from the desktop to use this shortcut.";
        status.setText(message);displayReply(message,true);persist();
    }
    private void remember(){JSONArray rows=new JSONArray();try{rows.put(new JSONObject().put("request",requestText.substring(0,Math.min(15000,requestText.length()))).put("reply",lastReply.substring(0,Math.min(15000,lastReply.length()))).put("task",task.getSelectedItemPosition()));JSONArray old=new JSONArray(store.prefs.getString("ten_nota_history","[]"));for(int i=0;i<Math.min(11,old.length());i++)rows.put(old.getJSONObject(i));store.prefs.edit().putString("ten_nota_history",rows.toString()).apply();}catch(JSONException ignored){}}
    private void history(){try{JSONArray rows=new JSONArray(store.prefs.getString("ten_nota_history","[]"));if(rows.length()==0){Toast.makeText(getContext(),"No saved conversations yet",Toast.LENGTH_SHORT).show();return;}String[] titles=new String[rows.length()];for(int i=0;i<titles.length;i++){String request=rows.getJSONObject(i).optString("request").replace('\n',' ');titles[i]=request.substring(0,Math.min(70,request.length()));}Aero.dialog(getContext()).setTitle("Recent chats").setItems(titles,(d,w)->{JSONObject row=rows.optJSONObject(w);try{restoreState(TenNotaState.ordinary(row.optString("request"),Math.max(0,Math.min(5,row.optInt("task"))),true,row.optString("reply"),"From local chat history",false,false));persist();}catch(JSONException ignored){}}).show();}catch(JSONException ignored){}}
    private void copy(){if(lastReply.isEmpty())return;String reply=lastReply;getContext().getSystemService(android.content.ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Nth Nota reply",reply));Toast.makeText(getContext(),"Reply copied",Toast.LENGTH_SHORT).show();}
    private void applyReply(){if(context==null||context.target==null||lastReply.isEmpty())return;TenNotaContext source=context;String reply=lastReply;Aero.dialog(getContext()).setTitle("Apply reply to "+source.title+"?").setMessage("Replace the passage you chose with this reply? Your source must still match the original text.").setPositiveButton("Apply",(d,w)->{boolean ok=source.apply(reply);status.setText(ok?"Reply applied to source":"Source changed or closed. Copy the reply to keep both versions.");if(ok)apply.setVisibility(GONE);}).setNegativeButton("Cancel",null).show();}
    private void save(){if(lastReply.isEmpty())return;String text=lastReply;String old=store.prefs.getString("note","");String combined=old+(old.isEmpty()?"":"\n\n")+text;store.prefs.edit().putString("note",combined).apply();syncNotepad(getRootView(),combined);Toast.makeText(getContext(),"Saved to Notepad",Toast.LENGTH_SHORT).show();}
    private void syncNotepad(View view,String text){if(view instanceof EditText&&"Notepad text".contentEquals(view.getContentDescription()==null?"":view.getContentDescription())){((EditText)view).setText(text);return;}if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)syncNotepad(((ViewGroup)view).getChildAt(i),text);}
    private int presentationTransfers;
    void beginPresentationTransfer(){presentationTransfers++;}
    void endPresentationTransfer(){presentationTransfers=Math.max(0,presentationTransfers-1);if(presentationTransfers==0)assistant.foreground(isShown());}
    void foreground(boolean active){if(presentationTransfers==0)assistant.foreground(active);}void close(){if(closed)return;persist();closed=true;assistant.close();}
    @Override protected void onWindowVisibilityChanged(int visible){super.onWindowVisibilityChanged(visible);if(assistant!=null&&presentationTransfers==0)assistant.foreground(visible==VISIBLE);}
    @Override public void onVisibilityAggregated(boolean visible){super.onVisibilityAggregated(visible);if(assistant!=null&&presentationTransfers==0)assistant.foreground(visible);}
    @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(presentationTransfers==0)close();}
}
