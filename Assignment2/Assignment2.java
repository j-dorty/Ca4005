import java.math.BigInteger;
import java.security.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.File;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Arrays;




//Jack Doherty
//17351591

public class Assignment2 {

    public static void main (String[] args ) {

        BigInteger exponentE = new BigInteger("65537");


        SecureRandom randomGen = new SecureRandom();

        BigInteger primeP = BigInteger.probablePrime(512, randomGen);
        BigInteger primeQ = BigInteger.probablePrime(512, randomGen);

        BigInteger modulusN = primeP.multiply(primeQ);

        BigInteger phiN = primeP.subtract(BigInteger.ONE). multiply(primeQ.subtract(BigInteger.ONE));
        BigInteger[] xEuclid = gcdExtended(exponentE, phiN);

        while(!(xEuclid[2].equals(BigInteger.ONE))) {
            primeP = BigInteger.probablePrime(512, randomGen);
            primeQ = BigInteger.probablePrime(512, randomGen);
        

            modulusN = primeP.multiply(primeQ);
            phiN = primeP.subtract(BigInteger.ONE). multiply(primeQ.subtract(BigInteger.ONE));

            xEuclid = gcdExtended(exponentE, phiN);
        }

        BigInteger exponentD = xEuclid[0];

        try{
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] inputFile = md.digest(readFile(args[0]));

        BigInteger c = new BigInteger(1, inputFile);

        BigInteger signedDigest = decryption(c, exponentD, primeP, primeQ);

        writeValTo(modulusN.toString(16), "Modulus.txt");

        System.out.print(bytesToHex(signedDigest.toByteArray()));

        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
      

    }


    public static BigInteger modularPower(BigInteger base, BigInteger exponent, BigInteger modulus){
        BigInteger r = BigInteger.ONE;
        for(int i = exponent.bitLength(); i>=0; i--){
            if(exponent.testBit(i))
                r = ((r.multiply(r)).multiply(base)).mod(modulus);
            else
                r = (r.multiply(r)).mod(modulus);
            }
        return r;
}

    public static void writeValTo(String val, String To) {

        try{
        Charset utf8 = StandardCharsets.UTF_8;
        List<String> out = Arrays.asList(new String[]{val});
        Files.write(Paths.get(To), out, utf8);
        } catch ( IOException exception) {
            exception.printStackTrace();
        }
}

    public static BigInteger[] gcdExtended(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return new BigInteger[] {BigInteger.ONE, BigInteger.ZERO, a};
        }

        BigInteger[] gcd = gcdExtended(b, a.mod(b));
        return new BigInteger[] {gcd[1], gcd[0].subtract((a.divide(b)).multiply(gcd[1])), gcd[2]};

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

    public static BigInteger decryption(BigInteger c, BigInteger exponentD, BigInteger primeP, BigInteger primeQ) {

        BigInteger cprimeP = modularPower(c, exponentD.mod(primeP.subtract(BigInteger.ONE)), primeP);
        BigInteger cprimeQ = modularPower(c, exponentD.mod(primeQ.subtract(BigInteger.ONE)), primeQ);

        BigInteger qInv = (gcdExtended(primeQ,primeP))[0];

        return cprimeQ.add(primeQ.multiply((qInv.multiply(cprimeP.subtract(cprimeQ))).mod(primeP)));
        
    }

    private static byte[] readFile(String filename) {
        File fileIn = new File(filename);
        byte[] fileBytes = new byte[(int) fileIn.length()];
   
        try {
            FileInputStream fileInStream = new FileInputStream(fileIn);
            fileInStream.read(fileBytes);
            fileInStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return fileBytes;
       }

}