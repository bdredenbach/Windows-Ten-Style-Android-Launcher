package com.nthexperience.desktop;
import android.graphics.*;
/** Original flat Nth icons, drawn in the shared 64-unit icon coordinate space. */
final class TenIcons {
 static boolean draw(Canvas c,String kind){Paint p=new Paint(3);Path path=new Path();p.setStrokeWidth(2);p.setColor(0xff5b6268);
  switch(kind){
   case "browser":p.setColor(0xff0078d7);c.drawCircle(32,32,25,p);p.setColor(0xff32c7b5);path.moveTo(8,37);path.cubicTo(18,6,52,11,56,34);path.cubicTo(42,25,22,29,20,42);path.cubicTo(29,53,46,49,54,41);path.cubicTo(46,65,7,57,8,37);c.drawPath(path,p);p.setColor(0xffe9ffff);c.drawCircle(34,28,9,p);return true;
   case "files":case "documents":case "pictures":case "music":case "videos":case "downloads":case "folderapps":p.setColor(0xffe3a624);c.drawRect(5,13,29,23,p);c.drawRect(5,20,59,54,p);p.setColor(0xffffd454);c.drawRect(5,25,59,54,p);if(!kind.equals("files")&&!kind.equals("folderapps")){p.setColor(0xffe5f1fc);c.drawRect(23,30,47,48,p);p.setColor(0xff0078d7);if(kind.equals("pictures")){path.moveTo(25,46);path.lineTo(32,36);path.lineTo(38,42);path.lineTo(43,38);path.lineTo(46,46);path.close();c.drawPath(path,p);}else if(kind.equals("music")){c.drawRect(34,33,37,44,p);c.drawCircle(32,44,4,p);}else{p.setStrokeWidth(2);for(int y=35;y<46;y+=5)c.drawLine(27,y,43,y,p);}}return true;
   case "computer":p.setColor(0xff59636a);c.drawRect(4,9,60,47,p);p.setColor(0xff1386bf);c.drawRect(7,12,57,43,p);p.setColor(0xff4cc9e7);path.moveTo(7,12);path.lineTo(57,12);path.lineTo(7,43);path.close();c.drawPath(path,p);p.setColor(0xff929ba1);c.drawRect(27,47,37,54,p);c.drawRect(17,54,47,58,p);return true;
   case "notes":case "sticky":p.setColor(kind.equals("sticky")?0xffffe27e:0xffe9f5ff);c.drawRect(12,5,53,59,p);p.setColor(0xff1588c7);c.drawRect(9,5,15,59,p);p.setColor(0xff8198a8);for(int y=17;y<51;y+=7)c.drawLine(21,y,45,y,p);return true;
   case "control":case "settings":p.setStyle(Paint.Style.STROKE);p.setColor(0xff65747e);p.setStrokeWidth(5);c.drawCircle(32,32,17,p);for(int n=0;n<8;n++){double a=n*Math.PI/4;c.drawLine(32+19*(float)Math.cos(a),32+19*(float)Math.sin(a),32+26*(float)Math.cos(a),32+26*(float)Math.sin(a),p);}p.setStrokeWidth(3);c.drawCircle(32,32,6,p);return true;
   case "recycle":case "recycle-full":p.setColor(0xffdcebf3);path.moveTo(14,15);path.lineTo(51,15);path.lineTo(46,57);path.lineTo(19,57);path.close();c.drawPath(path,p);p.setColor(0xff83959f);c.drawRect(12,10,53,16,p);c.drawRect(25,6,39,10,p);p.setColor(0xff0078d7);p.setTextSize(25);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("↻",22,43,p);return true;
   case "pictures-app":p.setColor(0xff0078d7);c.drawRect(7,10,57,55,p);p.setColor(0xff8ee3ff);c.drawCircle(43,22,6,p);p.setColor(0xffd9f2ff);path.moveTo(10,50);path.lineTo(25,28);path.lineTo(35,43);path.lineTo(44,34);path.lineTo(55,50);path.close();c.drawPath(path,p);return true;
   case "mediaplayer":case "mediacenter":p.setColor(0xff0078d7);c.drawCircle(32,32,26,p);p.setColor(Color.WHITE);path.moveTo(26,18);path.lineTo(46,32);path.lineTo(26,46);path.close();c.drawPath(path,p);return true;
   default:return false;
  }
 }
 private TenIcons(){}
}
