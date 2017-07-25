package com.mak.util;

import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/7/23 0023.
 */
public class MD5Util {

    public static String md5(String source) {
        String des;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] result = md.digest(source.getBytes());
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                byte b = result[i];
                buf.append(String.format("%02X", b));
            }
            des = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("md5 failure");
        }
        return des;
    }
}
