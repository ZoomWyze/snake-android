package com.example.snake.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 * 用于用户密码的哈希存储
 * 输出 32 位小写十六进制字符串
 */
public final class MD5Util {

    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private MD5Util() {
        // 工具类禁止实例化
    }

    /**
     * 计算字符串的 MD5 摘要
     *
     * @param input 待加密的原始字符串（如明文密码）
     * @return 32 位小写十六进制 MD5 字符串
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为 32 位小写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xff;
            sb.append(HEX_DIGITS[v >>> 4]);
            sb.append(HEX_DIGITS[v & 0x0f]);
        }
        return sb.toString();
    }
}
