package io.github.gaomjun.live.h264Encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import io.github.gaomjun.libyuv.YUVNative;
import io.github.gaomjun.live.encodeConfiguration.VideoConfiguration;
import io.github.gaomjun.live.rtmpClient.RTMPClient;
import io.github.gaomjun.live.rtmpFrame.RTMPVideoFrame;
import io.github.gaomjun.utils.TypeConversion.HEXString;

/**
 * Created by qq on 16/1/2017.
 */

public class H264Encoder {
    private MediaCodec codec;

    private final int width;
    private final int height;
    private final int bitrate;
    private final int fps;
    private final int keyframeInterval;

    private int frameCount = 0;

    private byte[] sps;
    private byte[] pps;

    private boolean codecWorking = false;
    private final int CACHE_UNIT_SIZE = 3153920;
    private final int CACHE_SIZE = 10;

    private HandlerThread h264EncodingThread = new HandlerThread("h264EncodingThread");
    private Handler h264EncodingHandler;

    private ArrayBlockingQueue<byte[]> cache = new ArrayBlockingQueue<byte[]>(CACHE_SIZE);
    private byte[] i420Buffer;
    private byte[] i420BufferWithPadding;

    private BufferedOutputStream bufferedOutputStream;
    private boolean saveToFile = false;

    private RTMPClient rtmpClient = RTMPClient.instance();
    private RTMPVideoFrame videoFrame = RTMPVideoFrame.instance();

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public H264Encoder(VideoConfiguration configuration) {
        this.width = configuration.getWidth();
        this.height = configuration.getHeight();
        this.bitrate = configuration.getBitrate();
        this.fps = configuration.getFps();
        this.keyframeInterval = configuration.getKeyframeInterval();

        initEncoder();

        h264EncodingThread.start();
        h264EncodingHandler = new Handler(h264EncodingThread.getLooper());
    }

    private void initEncoder() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
//            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel13);
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
//            codec.setCallback(new EncoderCallback());
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encoding(byte[] data, int width, int height, long timestamp) {
//        System.out.println("encoding");

        if ((data == null) || (data.length <= 0)) return;

        h264EncodingHandler.post(new H264EncodingRunnable(data, width, height, timestamp));
    }

    public void stopEncoding() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
    }

    class EncoderCallback extends MediaCodec.Callback {

        @Override
        public void onInputBufferAvailable(final MediaCodec codec, final int index) {
            System.out.println("onInputBufferAvailable ");
            byte[] data = cache.poll();

            if ((data != null) && (data.length > 0)) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);

                System.out.println("onInputBufferAvailable " + data.length + " " + inputBuffer.capacity());
//                inputBuffer.clear();
                inputBuffer.put(data);

                codec.queueInputBuffer(index, 0, data.length, 0, 0);
            }
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {

            System.out.println("onOutputBufferAvailable " + info.size);

//            ByteBuffer outputBuffer = codec.getOutputBuffer(index);
//
//            byte[] data = new byte[info.size];
//
//            outputBuffer.get(data);

            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            e.printStackTrace();
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
            System.out.println("onOutputFormatChanged");
        }
    }

    private class H264EncodingRunnable implements Runnable {
        private final byte[] data;
        private final int width;
        private final int height;
        private final long timestamp;

        public H264EncodingRunnable(byte[] data, int width, int height, long timestamp) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            if (i420Buffer == null) {
                i420Buffer = new byte[getYUVBufferSize(H264Encoder.this.width, H264Encoder.this.height)];
                System.out.println("i420Buffer " + i420Buffer.length);
            }


            YUVNative.SCALE(data, width, height, i420Buffer, H264Encoder.this.width, H264Encoder.this.height);
//            System.out.println("YUVNative.SCALE");

            int inputBufferIndex = codec.dequeueInputBuffer(1000);

            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);

//                if (codec.getCodecInfo().getName().contains("OMX.qcom")) {
//                    FixQCOMENCODE(i420Buffer);
//
//                    inputBuffer.put(i420BufferWithPadding);
//                    codec.queueInputBuffer(inputBufferIndex, 0, i420BufferWithPadding.length, (frameCount++)*1000*1000/fps, 0);
//
//                } else {
                    inputBuffer.put(i420Buffer);
                    codec.queueInputBuffer(inputBufferIndex, 0, i420Buffer.length, (frameCount++)*1000*1000/fps, 0);
//                }
            }

            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0 * 1000);

//            System.out.println("outputBufferIndex " + outputBufferIndex + " " + bufferInfo.flags);

            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);

                final byte[] h264Data = new byte[bufferInfo.size];
                outputBuffer.get(h264Data);

//                System.out.println(HEXString.bytes2HexString(Arrays.copyOf(h264Data, 5)));

                videoFrame.setTimestamp(timestamp);
                videoFrame.setData(h264Data);
                videoFrame.setSps(sps);
                videoFrame.setPps(pps);
                videoFrame.setKeyFrame(bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME);

                rtmpClient.sendFrame(videoFrame);

                if (saveToFile) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (bufferedOutputStream == null) {
                                try {
                                    File f = new File(Environment.getExternalStorageDirectory(), "Download/video_encoded.264");
                                    if (f.exists()) {
                                        f.delete();
                                        System.out.println("rm " + f.getAbsolutePath());
                                    }
                                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(f));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                bufferedOutputStream.write(h264Data);
                                bufferedOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

//                System.out.println("H264EncodingRunnable out data " + bufferInfo.size);

                codec.releaseOutputBuffer(outputBufferIndex, false);
            } else if ((outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) && (sps == null)) {
                MediaFormat outputFormat = codec.getOutputFormat();
                byte[] sps_t = outputFormat.getByteBuffer("csd-0").array();
                byte[] pps_t = outputFormat.getByteBuffer("csd-1").array();

                sps = Arrays.copyOfRange(sps_t, 4, sps_t.length);
                pps = Arrays.copyOfRange(pps_t, 4, pps_t.length);

                System.out.println("sps_t " + HEXString.bytes2HexString(sps_t));
                System.out.println("pps_t " + HEXString.bytes2HexString(pps_t));

                System.out.println("sps " + HEXString.bytes2HexString(sps));
                System.out.println("pps " + HEXString.bytes2HexString(pps));
            }
        }
    }

    private void FixQCOMENCODE(byte[] i420Buffer) {

        int ySize = width * height;
        int padding = ySize % 2048;

        if (i420BufferWithPadding == null) {
            i420BufferWithPadding = new byte[i420Buffer.length + padding];
        }

        System.arraycopy(i420Buffer, 0, i420BufferWithPadding, 0, ySize);

        System.arraycopy(i420Buffer, ySize, i420BufferWithPadding, ySize + padding, i420Buffer.length-ySize);
    }

    public static int getYUVBufferSize(int width, int height) {

        int stride = (int)(Math.ceil(width / 16.0) * 16);

        int y_size = stride * height;

        int c_stride = (int)(Math.ceil((stride>>1) / 16.0) * 16);

        int c_size = c_stride * height >> 1;

        return y_size + c_size * 2;
    }
}
