package com.nthexperience.desktop;

import android.net.Uri;
import android.text.Html;
import org.json.*;
import org.xmlpull.v1.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/** Stream only enough XML for twelve cards; large feeds need not be downloaded into a String. */
final class FeedXml {
    static final int MAX_BYTES=8*1024*1024,MAX_ITEMS=12;
    static final class Limited extends FilterInputStream {
        long count;Limited(InputStream in){super(in);}
        private void add(int n)throws IOException{if(n>0&&(count+=n)>MAX_BYTES)throw new IOException("FEED_TOO_LARGE");}
        @Override public int read()throws IOException{int b=super.read();if(b>=0)add(1);return b;}
        @Override public int read(byte[] bytes,int off,int length)throws IOException{int n=in.read(bytes,off,length);add(n);return n;}
    }
    static JSONArray parse(InputStream stream,String base,String contentType)throws Exception{
        // HTML5's lowercase doctype is not XML. Recognize it before the XML parser
        // reports a misleading syntax error; replay this small prefix unchanged.
        if(contentType!=null&&contentType.toLowerCase(Locale.ROOT).matches("\\s*(text/html|application/xhtml\\+xml)(\\s*;.*)?"))throw new IOException("FEED_HTML");
        PushbackInputStream input=new PushbackInputStream(stream,512);byte[] prefix=new byte[512];int count=input.read(prefix);if(count>0){input.unread(prefix,0,count);rejectHtml(new String(prefix,0,count,java.nio.charset.StandardCharsets.UTF_8));}
        XmlPullParser parser=parser();String charset=null;if(contentType!=null){Matcher m=Pattern.compile("(?i)charset\\s*=\\s*[\"']?([^;\\s\"']+)").matcher(contentType);if(m.find())charset=m.group(1);}
        parser.setInput(input,charset);return parse(parser,base);
    }
    static JSONArray parse(String xml,String base)throws Exception{rejectHtml(xml);XmlPullParser parser=parser();parser.setInput(new StringReader(xml));return parse(parser,base);}
    private static void rejectHtml(String prefix)throws IOException{if(Pattern.compile("(?is)^[\\s\\uFEFF]*(?:<\\?xml[^>]*>\\s*)?(?:<!doctype\\s+html(?:\\s|>)|<html(?:\\s|>))").matcher(prefix).find())throw new IOException("FEED_HTML");}
    private static XmlPullParser parser()throws Exception{XmlPullParser parser=XmlPullParserFactory.newInstance().newPullParser();parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true);parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL,false);return parser;}
    private static int next(XmlPullParser parser)throws Exception{int event=parser.nextToken();if(event==XmlPullParser.DOCDECL)throw new IOException(parser.getText()!=null&&parser.getText().trim().toLowerCase(Locale.ROOT).startsWith("html")?"FEED_HTML":"FEED_DOCTYPE");return event;}
    private static JSONArray parse(XmlPullParser parser,String base)throws Exception{
        JSONArray items=new JSONArray();Set<String> seen=new HashSet<>();int entry=-1;boolean root=false;String title="",link="",summary="",image="";
        for(int event=parser.getEventType();event!=XmlPullParser.END_DOCUMENT&&items.length()<MAX_ITEMS;event=next(parser)){
            if(event==XmlPullParser.START_TAG){String tag=parser.getName().toLowerCase(Locale.ROOT);int depth=parser.getDepth();
                if(depth==1){if(tag.equals("html"))throw new IOException("FEED_HTML");if(!Arrays.asList("rss","rdf","feed").contains(tag))throw new IOException("FEED_FORMAT");root=true;}
                if(tag.equals("item")||tag.equals("entry")){entry=depth;title="";link="";summary="";image="";}
                else if(entry>0&&depth==entry+1&&tag.equals("title")){title=text(parser);}
                else if(entry>0&&Arrays.asList("thumbnail","content","enclosure").contains(tag)&&parser.getAttributeValue(null,"url")!=null){String candidate=parser.getAttributeValue(null,"url"),type=parser.getAttributeValue(null,"type");if(image.isEmpty()&&(tag.equals("thumbnail")||type!=null&&type.startsWith("image/")))image=candidate;}
                else if(entry>0&&depth==entry+1&&Arrays.asList("description","summary","content").contains(tag)){if(summary.isEmpty())summary=plain(text(parser));}
                else if(entry>0&&depth==entry+1&&tag.equals("link")){String href=parser.getAttributeValue(null,"href"),rel=parser.getAttributeValue(null,"rel");if(href!=null&&(rel==null||rel.equals("alternate")))link=href;else if(href==null)link=text(parser);}
            }else if(event==XmlPullParser.END_TAG&&parser.getDepth()==entry){entry=-1;if(title.isEmpty()||link.isEmpty())continue;String url=resolve(base,link);if(url.isEmpty()||!seen.add(url))continue;String clean=plain(title);if(clean.isEmpty())continue;String thumbnail=resolve(base,image);items.put(new JSONObject().put("title",cut(clean,180)).put("url",url).put("summary",cut(summary,360)).put("image",FeedClient.validUrl(thumbnail)?thumbnail:""));}
        }
        if(!root)throw new IOException("FEED_FORMAT");return items;
    }
    private static String resolve(String base,String value){if(value==null||value.trim().isEmpty())return "";try{String target=new URL(new URL(base),value.trim()).toString();Uri uri=Uri.parse(target);if(target.length()>2048||!("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))||uri.getHost()==null||uri.getUserInfo()!=null)return "";return target;}catch(Exception invalid){return "";}}
    private static String text(XmlPullParser parser)throws Exception{int depth=parser.getDepth();StringBuilder text=new StringBuilder();for(int event=next(parser);!(event==XmlPullParser.END_TAG&&parser.getDepth()==depth)&&event!=XmlPullParser.END_DOCUMENT;event=next(parser)){if((event==XmlPullParser.TEXT||event==XmlPullParser.CDSECT||event==XmlPullParser.ENTITY_REF)&&text.length()<1200){String token=parser.getText();if(token!=null)text.append(token,0,Math.min(token.length(),1200-text.length()));}else if(event==XmlPullParser.START_TAG&&text.length()>0&&text.length()<1200&&Arrays.asList("p","div","br").contains(parser.getName().toLowerCase(Locale.ROOT)))text.append(' ');}return text.toString().trim();}
    private static String plain(String text){return Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY).toString().trim();}
    private static String cut(String text,int length){return text.substring(0,Math.min(length,text.length()));}
    private FeedXml(){}
}
