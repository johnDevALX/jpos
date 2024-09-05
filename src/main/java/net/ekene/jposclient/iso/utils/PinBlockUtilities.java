package net.ekene.jposclient.iso.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.params.DESedeParameters;
import org.bouncycastle.util.encoders.Hex;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
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
        log.info("Zpk {}", zpk);
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

    public static byte[] hexStringToByteArray(String hex) {
        return Hex.decode(hex);
    }

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

        return String.format("%012d", amountInSmallestUnit);
    }

    public static String getKCV(byte[] zpk)
    {
       byte[] result = new byte[8];
        DESedeEngine desEngine = new DESedeEngine();

        desEngine.init(true, new DESedeParameters(zpk));
        byte[] kcvBytes = new byte[8];

        desEngine.processBlock(result, 0, kcvBytes, 0);


        return byteArrayToHexString(kcvBytes).substring(0, 6);

    }

    public void addEZpk(String key, String encryptedZpk) {
        log.info("Data {}", encryptedZpk);
        zpkMap.put(key, encryptedZpk);
    }

    public String getEZpk(String key) {
        return zpkMap.get(key);
    }

    public void decryptZpk(String encryptedZpk) {
        String zpk16A = encryptedZpk.substring(0, 16);
        String zpk16B = encryptedZpk.substring(16, 32);

        String zmk16A = "63E4880A2D502DD8";
        String zmk16B = "E835C68DD8061BBB";
        String variantZmKA = zmk16B.substring(0, 2);
        String partA = zmk16B.substring(2, 16);

        String variantZmkBOne = ISOUtil.hexor("A6", variantZmKA) + partA;
        String newZmk = zmk16A + variantZmkBOne;
        String clearZpkA = byteArrayToHexString(decrypt3DESECB(hexStringToByteArray(newZmk), hexStringToByteArray(zpk16A)));

        String variantZmkBTwo = ISOUtil.hexor("5A", variantZmKA) + partA;
        String newZmk2 = zmk16A + variantZmkBTwo;
        String clearZpkB = byteArrayToHexString(decrypt3DESECB(hexStringToByteArray(newZmk2), hexStringToByteArray(zpk16B)));

        String clearZpk = clearZpkA.substring(0, 16) + clearZpkB.substring(0, 16);
        addEZpk("zpk", clearZpk);
    }

    public static byte[] decrypt3DESECB(byte[] keyBytes, byte[] dataBytes) {
        try {
            if (keyBytes.length == 16) { // short key ? .. extend to 24 byte key
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
}