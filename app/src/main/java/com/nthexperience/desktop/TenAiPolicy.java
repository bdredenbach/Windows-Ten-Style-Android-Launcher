package com.nthexperience.desktop;
final class TenAiPolicy {
 static boolean fallback(String error){return error.equals("UNAVAILABLE")||error.equals("TIMEOUT")||error.equals("RATE_LIMIT")||error.equals("BUSY")||error.equals("MODEL_NEEDED");}
}
