package com.time.fit.fittime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final int RUNNING = 1;
    private final int STOP = 2;

    private int state = STOP;

    private MediaPlayer m = new MediaPlayer();
    private final MainActivity context = this;
    private Integer SET_TIME = 5; // seconds
    private Integer REST_TIME = 5; // seconds
    private Integer PREPARE_TIME = 5; // seconds
    private TextView time_left;
    private RelativeLayout activityMainView;
    private CountDownTimer cdt;

    public int getIntegerPref(String key, int defaultValue) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String value = prefs.getString(key, null);
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return super.onOptionsItemSelected(item);
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                // newGame();
                Intent settingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                settingsActivityIntent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT,
                        SettingsActivity.GeneralPreferenceFragment.class.getName() );
                settingsActivityIntent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );
                startActivity(settingsActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMainView = (RelativeLayout) findViewById(R.id.activity_main);
        time_left = (TextView) findViewById(R.id.time_left);

        final Button startStopButton = (Button) findViewById(R.id.start_stop_button);

        startStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (context.state == RUNNING) {
                    startStopButton.setText("START");
                    cdt.cancel();
                    time_left.setText("0000");
                    state = STOP;

                    if (m.isPlaying()) {
                        m.stop();
                        m.release();
                        m = new MediaPlayer();
                    }
                } else {
                    startStopButton.setText("STOP");
                    startPrepare();
                    state = RUNNING;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        SET_TIME = getIntegerPref("SET_TIME", SET_TIME);
        REST_TIME = getIntegerPref("REST_TIME", REST_TIME);
        PREPARE_TIME = getIntegerPref("PREPARE_TIME", PREPARE_TIME);
    }

    private void startPrepare() {
        activityMainView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrepare));

        cdt = new CountDownTimer(PREPARE_TIME * 1000, 10) {

            public void onTick(long millisUntilFinished) {
                mOnTick(millisUntilFinished);
            }

            public void onFinish() {
                context.startSet();
            }
        }.start();
    }

    private void startSet() {
        playRithm("set.wav");
        activityMainView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));

        cdt = new CountDownTimer(SET_TIME * 1000, 10) {

            public void onTick(long millisUntilFinished) {
                mOnTick(millisUntilFinished);
            }

            public void onFinish() {
                context.startRest();
            }
        }.start();
    }

    private void startRest() {
        playRithm("rest.wav");
        activityMainView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        cdt = new CountDownTimer(REST_TIME * 1000, 10) {

            public void onTick(long millisUntilFinished) {
                mOnTick(millisUntilFinished);
            }

            public void onFinish() {
                context.startPrepare();
            }
        }.start();
    }

    private void mOnTick(long millisUntilFinished) {
        time_left.setText(Long.toString(millisUntilFinished));
    }

    private void playRithm(String assetName) {
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd(assetName);
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(true);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
