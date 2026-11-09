package com.nthexperience.desktop;
import android.content.Context;
/** Existing shell regressions start after optional permissions have been handled. */
final class ShellTestSetup {
 static void complete(Context context){context.getSharedPreferences(InitializationState.PREFS,0).edit().clear().putInt("flow_version",2).putInt("step",InitializationState.Step.DONE.ordinal()).commit();}
 private ShellTestSetup(){}
}
