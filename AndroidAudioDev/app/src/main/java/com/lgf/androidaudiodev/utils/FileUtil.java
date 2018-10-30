package com.lgf.androidaudiodev.utils;

import android.os.Environment;
import android.util.Log;

import com.lgf.androidaudiodev.audio.AudioTrackPlayer;
import com.lgf.androidaudiodev.audio.WavFileWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by garment on 2018/10/26.
 */

public class FileUtil {

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * 获取外部存储的路径
     * @param childPath 子路径
     * @return
     */
    public static String getExternalStoragePath(String childPath){
        File externalDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            externalDir = new File(Environment.getExternalStorageDirectory(), childPath);
        }
        return externalDir.getAbsolutePath();
    }

    /**
     * 创建文件
     * @param file 待创建的文件（注意，创建的是文件，非文件夹）
     */
    public static void createFile(File file){
        if (!file.exists()){
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把pcm文件转换为wav文件
     * @param pcmPath
     * @param destinationPath
     * @param deleteFlag
     * @return
     */
    public static boolean transformPcmToWav(String pcmPath, String destinationPath, boolean deleteFlag){
        byte buffer[] = null;
        File file = new File(pcmPath);
        if (!file.exists()) {
            return false;
        }

        //先删除目标文件
        File destfile = new File(destinationPath);
        if (destfile.exists())
            destfile.delete();
        WavFileWriter wavFileWriter = new WavFileWriter();
        try {
            wavFileWriter.openFile(destinationPath, AudioTrackPlayer.DEF_SAMPLE_RATE, 1, 16);
            buffer = new byte[1024 * 4]; // Length of All Files, Total Size
            InputStream inStream = null;

            inStream = new BufferedInputStream(new FileInputStream(file));
            int size = inStream.read(buffer);
            while (size != -1) {
                wavFileWriter.writeData(buffer, 0, size);
                size = inStream.read(buffer);
            }
            inStream.close();
            wavFileWriter.closeFile();
        } catch (FileNotFoundException e) {
            Log.e("PcmToWav", e.getMessage());
            return false;
        } catch (IOException ioe) {
            Log.e("PcmToWav", ioe.getMessage());
            return false;
        }
        if (deleteFlag) {
            file.delete();
        }
        return true;
    }

}
