package net.ekene.jposclient.iso.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.params.DESedeParameters;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UTF8;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PinBlockUtilities {
    private final ConcurrentHashMap<String, String> zpkMap = new ConcurrentHashMap<>();

    public byte[] generatePinBlock(String pin, String zpk, String cardPan) {
        String pinString = "0" + pin.length() + pin;
        pinString = padRight(pinString, 16, 'F');
        byte[] pinBlock1 = hexStringToByteArray(pinString);
        String treatedPan = cardPan.substring(cardPan.length() - 13, cardPan.length() - 1);
        treatedPan = padLeft(treatedPan, 16, '0');
        byte[] pinBlock2 = hexStringToByteArray(treatedPan);
        byte[] xor = xorIt(pinBlock1, pinBlock2);
        byte[] pinBlockZpk = new byte[8];
        byte[] zpkBytes = hexStringToByteArray(zpk);
        DESedeParameters keyParam = new DESedeParameters(zpkBytes);
        DESEngine desEngine = new DESEngine();
        desEngine.init(true, keyParam);
        desEngine.processBlock(xor, 0, pinBlockZpk, 0);
        return pinBlockZpk;
    }

    public static String byteArrayToHexString(byte[] ba) {
        return Hex.toHexString(ba);
    }

//    public static byte[] hexStringToByteArray(String hex) {
//        return Hex.decode(hex);
//    }

    public static byte[] xorIt(byte[] key, byte[] input) {
        byte[] bytes = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            bytes[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return bytes;
    }

    public static String padRight(String str, int length, char padChar) {
        while (str.length() < length) {
            str += padChar;
        }
        return str;
    }

    public static String padLeft(String str, int length, char padChar) {
        while (str.length() < length) {
            str = padChar + str;
        }
        return str;
    }

    public String formulateDE4(double amount) {
        long amountInSmallestUnit = Math.round(amount * 100);

//        "DE89ED53F4DA1CDA
//        C027FD39E8A845B9"; "022DC7";

        String sha384 = "DE89ED53F4DA1CDAC027FD39E8A845B9022DC70000000000000000000000000000000000000000000000000000000000";


        return String.format("%012d", amountInSmallestUnit);
    }



    public void addEZpk(String key, String encryptedZpk) {
        zpkMap.put(key, encryptedZpk);
    }

    public String getEZpk(String key) {
        return zpkMap.get(key);
    }

    public String decryptZpk(String encryptedZpk) throws Exception {
        String zpk16A = encryptedZpk.substring(0, 16);
        String zpk16B = encryptedZpk.substring(16, 32);

        String zmk16A = "63E4880A2D502DD8";
        String zmk16B = "E835C68DD8061BBB";
        String variantZmKA = zmk16B.substring(0, 2);
        String partA = zmk16B.substring(2, 16);
        String variantZmkBOne = ISOUtil.hexor("A6", variantZmKA) + partA;
        String newZmk = zmk16A + variantZmkBOne;
        decrypt(zpk16A, newZmk.getBytes());


        String variantZmkBTwo = ISOUtil.hexor("5A", variantZmKA) + partA;
        String newZmk2 = zmk16A + variantZmkBTwo;
        decrypt(zpk16B, newZmk2.getBytes());

        return "";
    }

    public static void main(String[] args) throws Exception {
        String encryptedZpk = "63E4880A2D502DD8E835C68DD8061BBB";
//        String zpk16A = input.substring(0, 2);
////        String zpk16A = input.substring(0, 16);
////        String zpk16A = input.substring(16, 32);
////
//        System.out.println(zpk16A);
//        System.out.println(ISOUtil.hexor("A6", "E8"));
//        System.out.println(substring1);

        String zpk16A = "98EEA376A14DF585";
        String zpk16B = encryptedZpk.substring(16, 32);

        String zmk16A = "63E4880A2D502DD8";
        String zmk16B = "E835C68DD8061BBB";
        String variantZmKA = zmk16B.substring(0, 2);
        String partA = zmk16B.substring(2, 16);
        String variantZmkBOne = ISOUtil.hexor("A6", variantZmKA) + partA;
        String newZmk = zmk16A + variantZmkBOne;
        log.info("Zmk {}", newZmk);

        System.out.println(byteArrayToHexString(decrypt3DESECB(newZmk.getBytes(), zpk16A.getBytes())));
    }


    private static String decrypt(String ciphertext, byte[] zmkComp) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        SecretKey secretKey = new SecretKeySpec(zmkComp, "DESede");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decryptedBytes, "UTF-8");
    }

    // Utility function to convert hex string to byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static byte[] decrypt3DESECB(byte[] keyBytes, byte[] dataBytes) {
        try {
            if (keyBytes.length == 32) { // short key ? .. extend to 24 byte key
                byte[] tmpKey = new byte[24];
                System.arraycopy(keyBytes, 0, tmpKey, 0, 16);
                System.arraycopy(keyBytes, 0, tmpKey, 16, 8);
                keyBytes = tmpKey;
            }

            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, newKey);
            return cipher.doFinal(dataBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encrypt(String plaintext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String byteArrayToString(byte[] bytes, String charset) {
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}