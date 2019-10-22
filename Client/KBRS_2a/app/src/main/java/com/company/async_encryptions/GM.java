package com.company.async_encryptions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class GM implements AsyncEncription<GM.GMPublicKey, GM.GMPrivateKey> {

    private static GMPublicKey pk_1 = null;
    private static GMPrivateKey pk_2 = null;

    public class GMPublicKey {
        BigInteger n;
        BigInteger a;

        GMPublicKey(BigInteger x, BigInteger y) {
            n = x;
            a = y;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(n);
            s.append(" ");
            s.append(a);
            return s.toString();
        }
    }

    public class GMPrivateKey {
        BigInteger p;
        BigInteger q;

        GMPrivateKey(BigInteger x, BigInteger y) {
            p = x;
            q = y;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(p);
            s.append(" ");
            s.append(q);
            return s.toString();
        }
    }

    private static int computeJacobiSymbol(BigInteger initial_a, BigInteger n) {
        // Step 1: a = a mod n
        BigInteger a = initial_a.mod(n);
        // Step 2: if a = 1 or n = 1 return 1
        if (a.equals(BigInteger.ONE) || n.equals(BigInteger.ONE)) {
            return 1;
        }
        // Step 3: if a = 0 return 0
        if (a.equals(BigInteger.ZERO)) {
            return 0;
        }
        // Step 4: define e and a_1 such that a = 2^e * a_1 where a_1 is odd
        int e = 0;
        BigInteger a_1 = a;
        while (a_1.remainder(BigIntegers.TWO).equals(BigInteger.ZERO)) {
            e++;
            a_1 = a_1.divide(BigIntegers.TWO);
        }
        // Step 5: if e is even, then s = 1;
        //          else if n mod 8 = 1 or n mod 8 = 7, then s = 1
        //          else if n mod 8 = 3 or n mod 8 = 5, then s = -1
        int s;
        if (e % 2 == 0) {
            s = 1;
        } else {
            BigInteger n_mod_eight = n.mod(BigIntegers.EIGHT);
            if (n_mod_eight.equals(BigInteger.ONE) || n_mod_eight.equals(BigIntegers.SEVEN)) {
                s = 1;
            } else { // n_mod_eight.equals(THREE) || n_mod_eight.equals(FIVE)
                s = -1;
            }
        }
        // Step 6: if n mod 4 = 3 and a_1 mod 4 = 3, then s = -s
        if (n.mod(BigIntegers.FOUR).equals(BigIntegers.THREE) && a_1.mod(BigIntegers.FOUR).equals(BigIntegers.THREE)) {
            s = -s;
        }
        // Step 7: n_1 = n mod a_1
        BigInteger n_1 = n.mod(a_1);
        // Step 8: return s * JacobiSymbol(n_1, a_1)
        return s * computeJacobiSymbol(n_1, a_1);
    }

    private static String toBinaryString(String s) {
        byte[] bytes = s.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    private static String toStringFromBinary(String s) {
        StringBuilder  str = new StringBuilder();
        for (int i = 0; i < s.length(); i+=8) {
            int b = 0;
            for (int j = i; j < i + 8; j++) {
                b = (b << 1) | ((s.charAt(j) == '0') ? 0 : 1);
            }
            str.append((char)b);
        }
        return str.toString();
    }

    private static ArrayList<BigInteger> stringToBigInteger(String str) {
        String[] strs =  str.split(" ");
        ArrayList<BigInteger> c = new ArrayList<>();
        for (String s : strs) {
            if (s != null && s != "")
            c.add(new BigInteger(s));
        }
        return c;
    }

    public GM() {
        init();
    }

    public GM(String pk_1_) {
        String[] s = pk_1_.split(" ");
        pk_1 = new GMPublicKey(new BigInteger(s[0]), new BigInteger(s[1]));
    }

    public void init() {
        BigInteger p = BigInteger.probablePrime(20, new Random());
        BigInteger q = BigInteger.probablePrime(20, new Random());
        BigInteger n = p.multiply(q);
        BigInteger a = new BigInteger(32, new Random());
        while (computeJacobiSymbol(a, p) != -1 || computeJacobiSymbol(a, q) != -1) {
            a = new BigInteger(32, new Random());
        }
        pk_1 = new GMPublicKey(n, a);
        pk_2 = new GMPrivateKey(p, q);

        System.out.println("GM: Public and Private Keys are updated!");
    }

    public String getPublicKey() {
        return pk_1.toString();
    }
    public String getPrivateKey() {
        return pk_2.toString();
    }
    public String encrypt(String data) {
        String byStr = toBinaryString(data);

        StringBuilder ans = new StringBuilder();

        for (int i = 0; i < byStr.length(); i++) {
            BigInteger r = new BigInteger(32, new Random());
            if (byStr.charAt(i) == '0') {
                ans.append(r.multiply(r).mod(pk_1.n));
                ans.append(" ");
            } else
            {
                ans.append(r.multiply(r).mod(pk_1.n).multiply(pk_1.a).mod(pk_1.n));
                ans.append(" ");
            }
        }
        return ans.toString();
    }

    public String decrypt(String data) {
        if (pk_2 == null) {
            System.out.println("Nothing to decrypt with. Set up Private Key first!");
            return null;
        }
        StringBuilder decBinary = new StringBuilder();
        ArrayList<BigInteger> c = stringToBigInteger(data);

        for (int i = 0; i < c.size(); i++) {
            if (computeJacobiSymbol(c.get(i), pk_2.p) == 1)
                decBinary.append('0');
            else
                decBinary.append('1');
        }

        return toStringFromBinary(decBinary.toString());
    }
}
