package com.jumanji.worldoffantasy;

import android.content.Context;
import android.content.SharedPreferences;

final class GameProgress {
    static final int REGION_COUNT = 4;
    static final int TASKS_PER_REGION = 3;

    final boolean[][] tasks = new boolean[REGION_COUNT][TASKS_PER_REGION];
    final boolean[] seals = new boolean[REGION_COUNT];

    float playerX = 2500f;
    float playerY = 1870f;
    int xp = 0;
    int essences = 0;
    boolean talkedToGuide = false;
    boolean completed = false;
    boolean started = false;

    static GameProgress load(Context context) {
        GameProgress state = new GameProgress();
        SharedPreferences prefs = context.getSharedPreferences("jumanji_world_save", Context.MODE_PRIVATE);
        state.playerX = prefs.getFloat("player_x", 2500f);
        state.playerY = prefs.getFloat("player_y", 1870f);
        state.xp = prefs.getInt("xp", 0);
        state.essences = prefs.getInt("essences", 0);
        state.talkedToGuide = prefs.getBoolean("talked_to_guide", false);
        state.completed = prefs.getBoolean("completed", false);
        state.started = prefs.getBoolean("started", false);

        for (int region = 0; region < REGION_COUNT; region++) {
            state.seals[region] = prefs.getBoolean("seal_" + region, false);
            for (int task = 0; task < TASKS_PER_REGION; task++) {
                state.tasks[region][task] = prefs.getBoolean("task_" + region + "_" + task, false);
            }
        }
        return state;
    }

    void save(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                "jumanji_world_save", Context.MODE_PRIVATE).edit();

        editor.putFloat("player_x", playerX);
        editor.putFloat("player_y", playerY);
        editor.putInt("xp", xp);
        editor.putInt("essences", essences);
        editor.putBoolean("talked_to_guide", talkedToGuide);
        editor.putBoolean("completed", completed);
        editor.putBoolean("started", started);

        for (int region = 0; region < REGION_COUNT; region++) {
            editor.putBoolean("seal_" + region, seals[region]);
            for (int task = 0; task < TASKS_PER_REGION; task++) {
                editor.putBoolean("task_" + region + "_" + task, tasks[region][task]);
            }
        }
        editor.apply();
    }

    void reset() {
        for (int region = 0; region < REGION_COUNT; region++) {
            seals[region] = false;
            for (int task = 0; task < TASKS_PER_REGION; task++) {
                tasks[region][task] = false;
            }
        }
        playerX = 2500f;
        playerY = 1870f;
        xp = 0;
        essences = 0;
        talkedToGuide = false;
        completed = false;
        started = true;
    }

    int sealCount() {
        int count = 0;
        for (boolean seal : seals) if (seal) count++;
        return count;
    }

    int taskCount(int region) {
        int count = 0;
        for (boolean task : tasks[region]) if (task) count++;
        return count;
    }

    int level() {
        return 1 + xp / 150;
    }

    int xpIntoLevel() {
        return xp % 150;
    }
}
