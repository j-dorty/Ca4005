import java.util.Scanner;
import java.io.File;
import java.math.BigInteger;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets; 
import java.io.FileWriter;
import java.util.Random;
import javax.crypto.spec.IvParameterSpec;
import java.util.Arrays;
import java.util.BitSet;

//Jack Doherty
//17351591

public class Assignment1 {

    public static final int BASE = 16;
    public static final String PRIME_P = "b59dd79568817b4b9f6789822d22594f376e6a9abc0241846de426e5dd8f6eddef00b465f38f509b2b18351064704fe75f012fa346c5e2c442d7c99eac79b2bc8a202c98327b96816cb8042698ed3734643c4c05164e739cb72fba24f6156b6f47a7300ef778c378ea301e1141a6b25d48f1924268c62ee8dd3134745cdf7323";
    public static final String GENERATOR_G = "44ec9d52c8f9189e49cd7c70253c2eb3154dd4f08467a64a0267c9defe4119f2e373388cfa350a4e66e432d638ccdc58eb703e31d4c84e50398f9f91677e88641a2d2f6157e2f4ec538088dcf5940b053c622e53bab0b4e84b1465f5738f549664bd7430961d3e5a2e7bceb62418db747386a58ff267a9939833beefb7a6fd68";
    public static final String PUBLIC_KEY_A = "5af3e806e0fa466dc75de60186760516792b70fdcd72a5b6238e6f6b76ece1f1b38ba4e210f61a2b84ef1b5dc4151e799485b2171fcf318f86d42616b8fd8111d59552e4b5f228ee838d535b4b987f1eaf3e5de3ea0c403a6c38002b49eade15171cb861b367732460e3a9842b532761c16218c4fea51be8ea0248385f6bac0d";
    public static byte[] Message = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public static final int PRIVATE_B_LENGHT = 1023;

    public static void main (String[] args ){

        String filename = "";
        

        if(args.length != 1){
            
            return;
        
        } else {

            filename = args[0];
        }


        Message = readFile(filename);
        Message = addPadding(Message);

    
        byte[] IVbytes = generateIV();
		BigInteger IV = new BigInteger(IVbytes);        


        BigInteger primeP = new BigInteger(PRIME_P,BASE);
        BigInteger generatorG = new BigInteger(GENERATOR_G,BASE);
        BigInteger publickeyA = new BigInteger(PUBLIC_KEY_A, BASE);


        SecureRandom randomGen = new SecureRandom();
        BigInteger privateB = BigInteger.probablePrime(PRIVATE_B_LENGHT, randomGen);


        BigInteger publicB = modularPower(generatorG, privateB, primeP);
        BigInteger sharedS =  modularPower(publickeyA, privateB, primeP);
       

        byte[] encryption = encrypt(Message, sharedS, IV);

        
        System.out.print(bytesToHex(encryption));

        writeValTo(IV.toByteArray(), "IV.txt");
        writeValTo(publicB.toByteArray(), "DH.txt");
        
    }

    public static void writeValTo(byte[] val, String To) {
        try {

            PrintWriter out = new PrintWriter(To);
            out.print(bytesToHex(val));
            out.close();

            } catch (FileNotFoundException e) {}
    }
    

    

    public static BigInteger modularPower(BigInteger base, BigInteger exponent, BigInteger modulus){
        BigInteger r = new BigInteger("1");
        for(int i = exponent.bitLength(); i>=0; i--){
            if(exponent.testBit(i))
                r = ((r.multiply(r)).multiply(base)).mod(modulus);
            else
                r = (r.multiply(r)).mod(modulus);
            }
        return r;
    }

    public static byte[] encrypt(byte[] message, BigInteger key, BigInteger IV)
    {

        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
        }

        byte[] keyDigest = sha256.digest(key.toByteArray());

        SecretKeySpec cipherSettings = new SecretKeySpec(keyDigest, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.toByteArray());


        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        }

        try { 
            cipher.init(Cipher.ENCRYPT_MODE, cipherSettings, ivSpec);
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        }

        byte[] result = null;
        try {
            result = cipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }

        return result;

    }

    private static byte[] generateIV() {
		byte[] _16byteValue = new byte[16];

		Random rnd = new SecureRandom();
		rnd.nextBytes(_16byteValue);
		return _16byteValue;
	}

    private static byte[] readFile(String filename) {
     File fileIn = new File(filename);
        byte[] fileBytes = new byte[(int) fileIn.length()];

        try {

            FileInputStream fileInStream = new FileInputStream(fileIn);
            fileInStream.read(fileBytes);
            fileInStream.close();

        } catch (IOException e) {

        }

        return fileBytes;
    }

    private static byte[] addPadding(byte[] message) {
        BitSet bits = BitSet.valueOf(message);
        int len = bits.length();
        int padPosition = (message.length * 8) + 7;
        bits.set(padPosition, true);
        len = bits.length();
        int arrSize = (16*(len/128)) + 16;
        byte[] newBytes = Arrays.copyOf(bits.toByteArray(), arrSize);

        return newBytes;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int j = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[j >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[j & 0x0F];
        }
        return new String(hexChars);
    }
}
