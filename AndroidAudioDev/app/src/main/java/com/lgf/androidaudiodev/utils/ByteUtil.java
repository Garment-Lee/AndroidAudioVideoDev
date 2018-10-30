package com.lgf.androidaudiodev.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by garment on 2018/10/26.
 */

public class ByteUtil {

    /**
     * short类型数组转换成byte类型数组
     * @param shortArr
     * @return
     */
    public static byte[] shortArrToByteArr(short[] shortArr){
        ByteBuffer bb = ByteBuffer.allocate(shortArr.length * 2);
        bb.asShortBuffer().put(shortArr);
        return bb.order(ByteOrder.LITTLE_ENDIAN).array(); // this returns the "raw" array, it's shared and not copied!
    }

    /**
     * short类型数组转换成byte类型数组（使用小端模式，先读低位，再读高位）
     * @param shortArr
     * @param length
     * @return
     */
    public static byte[] shortArrToByteArrLittleEndian(short[] shortArr, int length){
        byte[] lebyte = new byte[length * 2];
        for (int i = 0; i < length; i ++){
            lebyte[i * 2] = (byte)(shortArr[i] & 0xFF);
            lebyte[i * 2 + 1] = (byte)((shortArr[i] >> 8) & 0xFF);
        }
        return lebyte;
    }


}
