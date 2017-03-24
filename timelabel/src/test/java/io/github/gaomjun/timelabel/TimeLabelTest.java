package io.github.gaomjun.timelabel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by qq on 15/12/2016.
 */
public class TimeLabelTest {
    private TimeLabel timeLabel;
    @Before
    public void setUp() throws Exception {
        timeLabel = new TimeLabel();
    }

    @Test
    public void start() throws Exception {
        timeLabel.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeLabel.stop();
            }
        }).start();
    }

}