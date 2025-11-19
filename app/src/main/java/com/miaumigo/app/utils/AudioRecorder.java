package com.miaumigo.app.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private boolean isRecording = false;
    private Context context;

    public AudioRecorder(Context context) {
        this.context = context.getApplicationContext();
    }

    public void startRecording() throws IOException {
        if (isRecording) {
            return;
        }

        File audioDir = new File(context.getExternalFilesDir(null), "audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        outputFile = new File(audioDir, "audio_" + System.currentTimeMillis() + ".m4a").getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder = new MediaRecorder(context);
        } else {
            mediaRecorder = new MediaRecorder();
        }

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(outputFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Gravação iniciada: " + outputFile);
        } catch (IOException e) {
            Log.e(TAG, "Erro ao iniciar gravação", e);
            releaseRecorder();
            throw e;
        }
    }

    public void stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            return;
        }

        try {
            mediaRecorder.stop();
            Log.d(TAG, "Gravação finalizada: " + outputFile);
        } catch (RuntimeException e) {
            Log.e(TAG, "Erro ao parar gravação", e);
            outputFile = null; // Arquivo inválido
        } finally {
            releaseRecorder();
            isRecording = false;
        }
    }

    public void cancelRecording() {
        stopRecording();
        if (outputFile != null) {
            try {
                File file = new File(outputFile);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao deletar arquivo", e);
            }
            outputFile = null;
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao liberar recorder", e);
            }
            mediaRecorder = null;
        }
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getDuration() {
        // Duração aproximada baseada no tamanho do arquivo
        if (outputFile == null) {
            return 0;
        }
        File file = new File(outputFile);
        if (!file.exists()) {
            return 0;
        }
        // Estimativa: 1 segundo ≈ 16KB (128kbps / 8 bits)
        return file.length() / 16000;
    }
}

