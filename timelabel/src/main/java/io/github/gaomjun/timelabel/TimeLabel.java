package io.github.gaomjun.timelabel;

import android.os.Handler;
import android.os.HandlerThread;

import java.text.DecimalFormat;

/**
 * Created by qq on 15/12/2016.
 */

public class TimeLabel {
    public String timeLabel;

    private HandlerThread thread = new HandlerThread("TimeLabelThread");
    private Handler threadHandler;

    private int second = 0;
    private int minute = 0;
    private int hour = 0;
    private Runnable threadrunnable = new Runnable() {
        @Override
        public void run() {
            second++;
            if (second >= 60) {
                second = 0;
                minute++;
            }

            if (minute >= 60) {
                minute = 0;
                hour++;
            }

            if (hour >= 24) {
                hour = 0;
            }

            DecimalFormat formatter = new DecimalFormat("00");
            timeLabel = formatter.format(hour) + ":" +
                    formatter.format(minute) + ":" +
                    formatter.format(second);

            timeChangedCallback.timeChanged(timeLabel);

            threadHandler.postDelayed(threadrunnable, 1000);
        }
    };

    private TimeChangedCallback timeChangedCallback;

    public void setTimeChangedCallback(TimeChangedCallback timeChangedCallback) {
        this.timeChangedCallback = timeChangedCallback;
    }

    public interface TimeChangedCallback {
        void timeChanged(String timeString);
    }

    public TimeLabel() {
        thread.start();

        threadHandler = new Handler(thread.getLooper());
    }

    public void start() {
        second = 0;
        minute = 0;
        hour = 0;

        resume();
    }

    public void stop() {
        second = 0;
        minute = 0;
        hour = 0;

        pause();
    }

    public void pause() {
        threadHandler.removeCallbacks(threadrunnable);
    }

    public void resume() {
        threadHandler.post(threadrunnable);
    }

    public static String secondsToTimeString(int seconds) {
        String timeString;

        int hour = seconds / 3600;
        int minute = seconds % 3600 / 60;
        int second = seconds % 3600 % 60;

        DecimalFormat formatter = new DecimalFormat("00");
        timeString = formatter.format(hour) + ":" +
                formatter.format(minute) + ":" +
                formatter.format(second);

        return timeString;
    }
}
