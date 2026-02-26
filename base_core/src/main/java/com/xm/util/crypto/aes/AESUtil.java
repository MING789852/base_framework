package com.xm.util.crypto.aes;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.xm.advice.exception.exception.CommonException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URISyntaxException;

@Slf4j
public class AESUtil {
    private static final SymmetricCrypto aes;


    static {
        try(InputStream stream = ResourceUtil.getStream("privateKey")) {
            aes = new SymmetricCrypto(SymmetricAlgorithm.AES, IoUtil.readBytes(stream));
        } catch (Exception e) {
            String errorMsg=ExceptionUtil.stacktraceToString(e);
            log.error("AES异常=>{}",errorMsg);
            throw new CommonException(errorMsg);
        }
    }

    public static String  encrypt(String content){
        return aes.encryptHex(content);
    }

    public static String decrypt(String content){
        return aes.decryptStr(HexUtil.decodeHex(content));
    }


    public static void main(String[] args) throws URISyntaxException {
        String encryptHex=AESUtil.decrypt("4bbf0bc941779ad15add745d72b21e7f2d9706c986009e278b7efaea912b7ac8");
        System.out.println(AESUtil.encrypt("admin12%…………#@￥13456"));
        System.out.println(encryptHex);

//        System.out.println(SecureUtil.md5("123456"));
    }
}
