package com.nthexperience.desktop;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

/** Complete theme presets: background, shell mode, accent, glass and sounds. */
final class TenThemes {
    static final class Preset {
        final String name;final int wallpaper,accent,aeroColor;final boolean light,aero,transparent;final String sound;final int[] more;
        Preset(String name,int wallpaper,int accent,boolean light,boolean aero,boolean transparent,int aeroColor,String sound,int... more){this.name=name;this.wallpaper=wallpaper;this.accent=accent;this.light=light;this.aero=aero;this.transparent=transparent;this.aeroColor=aeroColor;this.sound=sound;this.more=more;}
        List<String> backgrounds(){List<String> rows=new ArrayList<>();rows.add(DesktopBackgrounds.stock(wallpaper));for(int id:more)rows.add(DesktopBackgrounds.stock(id));return rows;}
        boolean appsDark(){return !aero&&(wallpaper==13||wallpaper==14);}
        boolean matches(DesktopStore store){SharedPreferences p=store.prefs;return backgrounds().equals(DesktopBackgrounds.current(p))&&appsDark()==p.getBoolean("ten_apps_dark",false)&&light==p.getBoolean("ten_light",false)&&(aero?"Aero":"Ten").equals(p.getString("ten_shell_style","Ten"))&&accent==p.getInt("ten_accent",TenStyle.ACCENT)&&transparent==p.getBoolean("ten_transparency",false)&&DesktopSounds.scheme(store).equals(sound)&&!p.getBoolean("ten_accent_shell",false)&&!p.getBoolean("ten_accent_titles",false)&&(!aero||(aeroColor==p.getInt("aero_color",0)&&p.getInt("aero_opacity",85)==85&&p.getInt("aero_intensity",45)==45));}
        void apply(DesktopStore store){SharedPreferences.Editor edit=store.prefs.edit();DesktopBackgrounds.choose(edit,backgrounds(),wallpaper);edit.putBoolean("ten_apps_dark",appsDark()).putString("ten_shell_style",aero?"Aero":"Ten").putInt("ten_accent",accent).putBoolean("ten_light",light).putBoolean("ten_transparency",transparent).putBoolean("aero_transparency",transparent).putInt("aero_color",aeroColor).putInt("aero_intensity",45).putInt("aero_opacity",85).putBoolean("ten_accent_shell",false).putBoolean("ten_accent_titles",false).putString(DesktopSounds.SCHEME,sound).putString("wallpaper_position","Fill").apply();}
    }
    static final List<Preset> TEN=Collections.unmodifiableList(Arrays.asList(
        new Preset("Ten Blue",11,0xff0078d7,false,false,false,0,"seven"),
        new Preset("Ten Light",12,0xff0078d7,true,false,false,0,"seven"),
        new Preset("Ten Midnight",13,0xff4cc2ff,false,false,true,1,"twilight"),
        new Preset("Ten Spectrum",14,0xff8764c5,false,false,true,4,"twilight"),
        new Preset("Coastal Teal",6,0xff008272,false,false,true,2,"meadow"),
        new Preset("Warm Horizons",7,0xffd15a28,false,false,false,5,"meadow"),
        new Preset("Landscape Collection",5,0xff0078d7,false,false,true,0,"meadow",6,7),
        new Preset("Graphite",13,0xff7a8998,false,false,false,1,"classic")
    ));
    static final List<Preset> AERO=Collections.unmodifiableList(Arrays.asList(
        aero("Seven Blue",3,0,"seven"),aero("Twilight",1,4,"twilight"),aero("Meadow",2,3,"meadow"),aero("Harmony",4,0,"seven"),aero("Landscapes",5,0,"meadow",6,7),aero("Architecture",8,7,"seven"),aero("Midnight Circuit",9,2,"twilight"),aero("Paper Skies",10,5,"meadow"),new Preset("Seven Basic",3,0xff0078d7,false,true,false,0,"classic")
    ));
    private static Preset aero(String name,int wallpaper,int color,String sound,int... more){return new Preset(name,wallpaper,AeroGlass.TINTS[color],false,true,true,color,sound,more);}
    static String currentName(DesktopStore store){for(Preset p:TEN)if(p.matches(store))return p.name;for(Preset p:AERO)if(p.matches(store))return p.name;return "Custom theme";}
    static boolean aero(SharedPreferences prefs){return "Aero".equals(prefs.getString("ten_shell_style","Ten"));}
    static void validateExtras(JSONObject profile)throws JSONException{
        if(profile.has("ten_shell_style")&&!Arrays.asList("Ten","Aero").contains(profile.optString("ten_shell_style")))throw new JSONException("Invalid shell style");
        for(String key:new String[]{"ten_apps_dark","ten_light","ten_transparency","ten_accent_shell","ten_accent_titles"})if(profile.has(key)&&!(profile.get(key) instanceof Boolean))throw new JSONException("Invalid Ten theme switch");
        if(profile.has("ten_accent")&&!(profile.get("ten_accent") instanceof Integer))throw new JSONException("Invalid accent color");
    }
    private TenThemes(){}
}
