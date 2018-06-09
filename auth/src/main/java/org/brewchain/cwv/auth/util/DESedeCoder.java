package org.brewchain.cwv.auth.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class DESedeCoder {
    /**
     * 秘钥算法
    * java6只支持56位秘钥
    */
    public static final String KEY_ALGORITHM = "DESede";

    /**
     * 加密/加密算法
    */
    public static final String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";

    /**
     * 转换秘钥
    * @param key  二进制秘钥（key就是秘密秘钥二进制字节数组的形式，但我们要使用它需要将它转换成秘钥对象，
    *             首先需要将二进制秘钥转换成秘钥材料对象ps:这里是DESKeySpec的dks，再使用秘钥工厂生产秘钥SecretKeyFactory）
    * @return Key 秘钥
    * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception {
        // 实例化DES秘钥材料
        DESedeKeySpec dks = new DESedeKeySpec(key);
        // 实例化秘密秘钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        // 生产密码秘钥
        SecretKey secretKey = keyFactory.generateSecret(dks);
        return secretKey;
    }

    /**解密
    * @param data  待解密数据  
     * @param key   秘钥
    * @return byte[]  解密数据
    * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 还原秘钥
        Key k = toKey(key);
        // 实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 加密
    * @param data  待加密数据
    * @param key   秘钥
    * @return byte[]  加密数据
    * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        Key k = toKey(key);
        // 实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化 设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 生产秘钥
    * java 6只支持56位秘钥
    * @return byte[] 二进制秘钥
    * @throws Exception
     */
    public static byte[] initKey() throws Exception {
        /**
         * 实例化秘钥生产器
        * 目前java 6 支持的是56位
        */
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        /**
         * 初始化秘钥生产
        * 若要使用64位秘钥生产器直接把56换成64即可。
        * 秘钥长度和安全性成正比
        */
        kg.init(168);
        // 生产秘密秘钥
        SecretKey sky = kg.generateKey();
        // 获得秘钥的二进制编码格式
        return sky.getEncoded();
    }

    public static void main(String[] args) throws Exception {
        String str = "DES";
        System.out.println("原文：  \t" + str);
        byte[] data = str.getBytes();

        // 获得二进制秘钥
        byte[] key = DESedeCoder.initKey();
        System.out.println("秘钥: \t" + Base64.encodeBase64(key));

        // 使用DES进行加密
        byte[] enData = DESedeCoder.encrypt(data, key);
        System.out.println("加密后：\t" + Base64.encodeBase64(enData));

        byte[] outData = DESedeCoder.decrypt(enData, key);
        String outputStr = new String(outData);
        System.out.println("解密后：\t" + outputStr);
        
        
//        String key = "201702232017031420170223";
//        String result = "{\"action_code\":\"A01\",\"user_name\":\"NBYH\",\"pwd\":\"20170223_rWfB\",\"logserial\":\"201702230002\",\"mobile\":\"13486087845\",\"city\":[{\"citycode\":\"0574\"}]}";
//        String encMssg = encrypt(result,key);
//        System.out.println(encMssg);
//        
//        
//        
//        String encmssge = encMssg;
//        String decmessg = decrypt(encmssge);
//        System.out.println(decmessg);
//        String aa ="Ctkh7vp9hyFw/fc7L+bF7uapTDZM0f9rHjEDXMNnO2jhDjuw21hHtaDOyfcWh1xI10FQ4eWYOeL1/XuLr3s7/e+p1Nrl38lw";
//        String key = "201703232017031420170323";
//        String encMssg1 = decrypt(encMssg,key);
//        System.out.println(encMssg1);
    }
    
    public static String encrypt(String jsonStr ,String key) {
        try {
            SecureRandom random = new SecureRandom();
            
//            String key = "20170223";
//            key = key + "20170314" + key;
            // ConfYd.getInstance().DES_KEY.getBytes()
            DESKeySpec desKey = new DESKeySpec(key.getBytes());
            // 私钥工厂获取私钥
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKey);

            // 加密
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
            return new String(Base64.encodeBase64(cipher.doFinal(jsonStr.getBytes("UTF-8"))));
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String jsonStr,String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException{

        SecureRandom random = new SecureRandom();
        // ConfYd.getInstance().DES_KEY.getBytes()
//        String key = "20170223";
//        key = key + "20170223" + key;
        DESKeySpec desKey = new DESKeySpec(key.getBytes());
        // 私钥工厂获取私钥
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKey);

        // 加密
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
        return new String(cipher.doFinal(Base64.decodeBase64(jsonStr.getBytes("UTF-8"))), "UTF-8");
    }
}
