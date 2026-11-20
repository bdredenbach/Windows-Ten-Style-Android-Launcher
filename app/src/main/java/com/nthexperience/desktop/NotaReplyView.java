package com.nthexperience.desktop;

// Adapted from Nth Desktop 3D b7926709ae2584527933f5246bbd5fdb499cadcf.

import android.content.*;
import android.graphics.Typeface;
import android.text.*;
import android.text.style.*;
import android.widget.*;
import java.util.regex.*;

/** Native selectable prose and scrollable code. Provider text is never interpreted as HTML. */
@android.annotation.SuppressLint("ViewConstructor")
final class NotaReplyView extends LinearLayout {
    TextView primary;
    NotaReplyView(Context c,String content){super(c);setOrientation(VERTICAL);for(NotaMarkdown.Block block:NotaMarkdown.blocks(content)){
        if(block.kind.equals("table")){HorizontalScrollView horizontal=new HorizontalScrollView(c);horizontal.setContentDescription("Comparison table; scroll horizontally for more columns");TableLayout table=new TableLayout(c);int rowIndex=0;for(String line:block.text.split("\n")){String trimmed=line.replaceFirst("^\\|","").replaceFirst("\\|$","");TableRow row=new TableRow(c);row.setBackground(NotaDesign.surface(c,rowIndex==0?0xffe5e5e5:NotaDesign.SURFACE));for(String cell:trimmed.split("\\|",-1)){TextView value=text(c,"",14,NotaDesign.TEXT);value.setText(inline(cell.trim()));value.setPadding(Aero.dp(c,12),Aero.dp(c,12),Aero.dp(c,12),Aero.dp(c,12));value.setTextIsSelectable(true);if(rowIndex==0)value.setTypeface(null,Typeface.BOLD);row.addView(value,new TableRow.LayoutParams(Aero.dp(c,168),-2));if(primary==null)primary=value;}table.addView(row);rowIndex++;}horizontal.addView(table);LayoutParams p=new LayoutParams(-1,-2);p.bottomMargin=Aero.dp(c,16);addView(horizontal,p);continue;}
        if(block.kind.equals("rule")){android.view.View line=new android.view.View(c);line.setBackgroundColor(NotaDesign.LINE);LayoutParams p=new LayoutParams(-1,Aero.dp(c,1));p.setMargins(0,Aero.dp(c,10),0,Aero.dp(c,14));addView(line,p);continue;}
        if(block.kind.equals("code")){LinearLayout box=Aero.column(c);box.setBackground(NotaDesign.surface(c,NotaDesign.SURFACE));LinearLayout bar=new LinearLayout(c);TextView language=text(c,block.label.isEmpty()?"Code":block.label,12,NotaDesign.MUTED);language.setPadding(Aero.dp(c,12),0,0,0);bar.addView(language,new LayoutParams(0,-2,1));bar.addView(NotaDesign.action(c,"Copy code",()->copy(c,block.text)),new LayoutParams(-2,Aero.dp(c,44)));box.addView(bar);HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.setFillViewport(true);TextView code=text(c,block.text,14,NotaDesign.TEXT);code.setTypeface(Typeface.MONOSPACE);code.setPadding(Aero.dp(c,12),0,Aero.dp(c,16),Aero.dp(c,14));code.setHorizontallyScrolling(true);code.setTextIsSelectable(true);scroll.addView(code);box.addView(scroll,new LayoutParams(-1,-2));LayoutParams p=new LayoutParams(-1,-2);p.bottomMargin=Aero.dp(c,14);addView(box,p);if(primary==null)primary=code;continue;}
        int size=block.kind.equals("heading")?(block.label.equals("1")?23:19):16;
        TextView text=text(c,"",size,NotaDesign.TEXT);text.setText(inline(block.text));text.setTextIsSelectable(true);text.setLineSpacing(Aero.dp(c,5),1.05f);
        if(block.kind.equals("heading"))text.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        if(block.kind.equals("quote")){text.setTextColor(NotaDesign.MUTED);text.setPadding(Aero.dp(c,12),Aero.dp(c,9),Aero.dp(c,12),Aero.dp(c,9));text.setBackground(NotaDesign.surface(c,NotaDesign.SURFACE));}
        LayoutParams p=new LayoutParams(-1,-2);p.bottomMargin=Aero.dp(c,block.kind.equals("list")?5:13);addView(text,p);if(primary==null)primary=text;
    }if(primary==null){primary=text(c,content,16,NotaDesign.TEXT);addView(primary);}}
    static TextView text(Context c,String value,int size,int color){TextView v=Aero.text(c,value,size,color);v.setShadowLayer(0,0,0,0);v.setIncludeFontPadding(false);return v;}
    static void copy(Context c,String value){android.content.ClipboardManager clipboard=(android.content.ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);if(clipboard!=null)clipboard.setPrimaryClip(ClipData.newPlainText("Nth Nota AI reply",value));Toast.makeText(c,"Copied",Toast.LENGTH_SHORT).show();}
    static CharSequence inline(String value){
        SpannableStringBuilder result=new SpannableStringBuilder();Pattern tokens=Pattern.compile("`([^`\\n]+)`|\\*\\*([^*\\n]+)\\*\\*|__([^_\\n]+)__|\\*([^*\\n]+)\\*");Matcher matcher=tokens.matcher(value);int position=0;
        while(matcher.find()){result.append(value,position,matcher.start());int start=result.length();boolean code=matcher.group(1)!=null;String words=code?matcher.group(1):matcher.group(2)!=null?matcher.group(2):matcher.group(3)!=null?matcher.group(3):matcher.group(4);result.append(words);
            if(code){result.setSpan(new TypefaceSpan("monospace"),start,result.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);result.setSpan(new BackgroundColorSpan(0x297e84af),start,result.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}else result.setSpan(new StyleSpan(matcher.group(4)!=null?Typeface.ITALIC:Typeface.BOLD),start,result.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);position=matcher.end();
        }result.append(value,position,value.length());return result;
    }
}
