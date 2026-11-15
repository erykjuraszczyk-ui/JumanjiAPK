package com.jumanji.worldoffantasy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private FantasyGameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        applyImmersiveMode();
        gameView = new FantasyGameView(this);
        setContentView(gameView);
    }

    private void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyImmersiveMode();
    }

    @Override
    protected void onPause() {
        if (gameView != null) gameView.pauseGame();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveMode();
        if (gameView != null) gameView.resumeGame();
    }

    @Override
    public void onBackPressed() {
        if (gameView != null && gameView.handleBackPressed()) return;
        super.onBackPressed();
    }
}
