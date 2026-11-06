package com.nthexperience.desktop;
import android.graphics.*;
/** Original flat Nth icons, drawn in the shared 64-unit icon coordinate space. */
final class TenIcons {
 static boolean draw(Canvas c,String kind){Paint p=new Paint(3);Path path=new Path();p.setStrokeWidth(2);p.setColor(0xff5b6268);
  switch(kind){
   case "browser":p.setColor(0xff0078d7);c.drawCircle(32,32,25,p);p.setColor(0xff32c7b5);path.moveTo(8,37);path.cubicTo(18,6,52,11,56,34);path.cubicTo(42,25,22,29,20,42);path.cubicTo(29,53,46,49,54,41);path.cubicTo(46,65,7,57,8,37);c.drawPath(path,p);p.setColor(0xffe9ffff);c.drawCircle(34,28,9,p);return true;
   case "files":case "documents":case "pictures":case "music":case "videos":case "downloads":case "folderapps":folder(c,kind);return true;
   case "computer":p.setColor(0xff59636a);c.drawRect(4,9,60,47,p);p.setColor(0xff1386bf);c.drawRect(7,12,57,43,p);p.setColor(0xff4cc9e7);path.moveTo(7,12);path.lineTo(57,12);path.lineTo(7,43);path.close();c.drawPath(path,p);p.setColor(0xff929ba1);c.drawRect(27,47,37,54,p);c.drawRect(17,54,47,58,p);return true;
   case "notes":case "sticky":p.setColor(kind.equals("sticky")?0xffffe27e:0xffe9f5ff);c.drawRect(12,5,53,59,p);p.setColor(0xff1588c7);c.drawRect(9,5,15,59,p);p.setColor(0xff8198a8);for(int y=17;y<51;y+=7)c.drawLine(21,y,45,y,p);return true;
   case "appearance":p.setColor(0xff0078d7);c.drawRect(6,9,58,47,p);p.setColor(0xffe4f4ff);c.drawRect(10,13,54,43,p);p.setColor(0xff5ba6d5);c.drawCircle(44,23,5,p);path.moveTo(11,40);path.lineTo(27,23);path.lineTo(42,40);path.close();c.drawPath(path,p);p.setColor(0xff718494);c.drawRect(28,47,36,53,p);c.drawRect(18,53,46,57,p);return true;
   case "run":p.setColor(0xffe7f2fa);c.drawRect(5,12,59,52,p);p.setColor(0xff0078d7);c.drawRect(5,12,59,23,p);p.setColor(0xff536778);c.drawRect(13,31,51,35,p);c.drawRect(34,41,51,46,p);return true;
   case "control":case "settings":p.setStyle(Paint.Style.STROKE);p.setColor(0xff65747e);p.setStrokeWidth(5);c.drawCircle(32,32,17,p);for(int n=0;n<8;n++){double a=n*Math.PI/4;c.drawLine(32+19*(float)Math.cos(a),32+19*(float)Math.sin(a),32+26*(float)Math.cos(a),32+26*(float)Math.sin(a),p);}p.setStrokeWidth(3);c.drawCircle(32,32,6,p);return true;
   case "recycle":case "recycle-full":p.setColor(0xffdcebf3);path.moveTo(14,15);path.lineTo(51,15);path.lineTo(46,57);path.lineTo(19,57);path.close();c.drawPath(path,p);p.setColor(0xff83959f);c.drawRect(12,10,53,16,p);c.drawRect(25,6,39,10,p);p.setColor(0xff0078d7);p.setTextSize(25);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("↻",22,43,p);return true;
   case "pictures-app":p.setColor(0xff0078d7);c.drawRect(7,10,57,55,p);p.setColor(0xff8ee3ff);c.drawCircle(43,22,6,p);p.setColor(0xffd9f2ff);path.moveTo(10,50);path.lineTo(25,28);path.lineTo(35,43);path.lineTo(44,34);path.lineTo(55,50);path.close();c.drawPath(path,p);return true;
   case "mediaplayer":case "mediacenter":p.setColor(0xff0078d7);c.drawCircle(32,32,26,p);p.setColor(Color.WHITE);path.moveTo(26,18);path.lineTo(46,32);path.lineTo(26,46);path.close();c.drawPath(path,p);return true;
   default:return false;
  }
 }
 /** Upright open folder: narrow gold spine, rear cover, white papers and perspective front cover. */
 private static void folder(Canvas c,String kind){Paint p=new Paint(3);Path shape=new Path();
  p.setColor(0x22000000);c.drawOval(7,55,58,60,p);
  shape.moveTo(9,7);shape.lineTo(27,7);shape.lineTo(31,12);shape.lineTo(53,12);shape.lineTo(53,51);shape.lineTo(9,57);shape.close();p.setShader(new LinearGradient(9,8,53,55,0xffffe599,0xffdda82f,Shader.TileMode.CLAMP));c.drawPath(shape,p);p.setShader(null);
  p.setColor(0xffbc8c24);c.drawRect(9,8,12,56,p);p.setColor(0xffffefb8);c.drawRect(12,9,26,11,p);
  shape.reset();shape.moveTo(17,16);shape.lineTo(48,13);shape.lineTo(48,49);shape.lineTo(17,53);shape.close();p.setColor(0xffd3d8dd);c.drawPath(shape,p);
  shape.reset();shape.moveTo(19,13);shape.lineTo(46,16);shape.lineTo(46,50);shape.lineTo(19,52);shape.close();p.setShader(new LinearGradient(19,13,46,50,Color.WHITE,0xffe9eef1,Shader.TileMode.CLAMP));c.drawPath(shape,p);p.setShader(null);
  shape.reset();shape.moveTo(13,24);shape.lineTo(26,26);shape.lineTo(30,30);shape.lineTo(58,26);shape.lineTo(55,55);shape.lineTo(13,60);shape.close();p.setShader(new LinearGradient(13,25,54,59,0xffffe9a7,0xfff6c84e,Shader.TileMode.CLAMP));c.drawPath(shape,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(0xffd5a633);c.drawPath(shape,p);p.setStyle(Paint.Style.FILL);p.setColor(0x88fff7cc);c.drawLine(14,25,26,27,p);c.drawLine(31,31,57,27,p);
  if(!kind.equals("files")&&!kind.equals("folderapps")){c.save();c.skew(0,-.10f);p.setColor(0xff2778a8);p.setStrokeWidth(1.8f);float x=37,y=48;
   if(kind.equals("music")){p.setStyle(Paint.Style.STROKE);c.drawLine(x,y-9,x,y+2,p);c.drawLine(x,y-9,x+7,y-11,p);c.drawLine(x+7,y-11,x+7,y,p);p.setStyle(Paint.Style.FILL);c.drawOval(x-5,y,x+1,y+4,p);c.drawOval(x+2,y-2,x+8,y+2,p);}
   else if(kind.equals("pictures")){p.setColor(0xffeaf5ff);c.drawRect(30,40,48,53,p);p.setColor(0xff6ab8db);c.drawRect(32,42,46,51,p);p.setColor(0xff2b7e9b);Path hill=new Path();hill.moveTo(32,51);hill.lineTo(37,45);hill.lineTo(41,49);hill.lineTo(44,46);hill.lineTo(46,51);hill.close();c.drawPath(hill,p);}
   else if(kind.equals("downloads")){Path arrow=new Path();arrow.moveTo(36,39);arrow.lineTo(41,39);arrow.lineTo(41,47);arrow.lineTo(46,47);arrow.lineTo(38.5f,54);arrow.lineTo(31,47);arrow.lineTo(36,47);arrow.close();c.drawPath(arrow,p);}
   else if(kind.equals("videos")){p.setColor(0xff3885ad);c.drawRect(30,40,48,53,p);p.setColor(Color.WHITE);Path play=new Path();play.moveTo(37,42);play.lineTo(43,46.5f);play.lineTo(37,51);play.close();c.drawPath(play,p);}
   else {p.setColor(0xfff9fcff);c.drawRect(32,38,46,54,p);p.setColor(0xff538bae);for(int y2=42;y2<53;y2+=3)c.drawLine(34,y2,44,y2,p);}c.restore();}
 }
 private TenIcons(){}
}
