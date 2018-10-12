/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author river.liao
 */
public class TestClass {

    public static void main(String args[]) {


    }

    public static long getSerialNum(String str) {
        if (str == null) {
            return 0;
        }
        byte[] bt = str.getBytes();
        int leng = bt.length;
        long sn = 0;
        for (int i = 0; i < leng; i++) {
            sn += bt[i] * getPowerNum(leng - i - 1);
        }
        return sn;
    }

    private static long getPowerNum(int n) {
        long num = 1;
        for (int i = 0; i < n; i++) {
            num *= 10;
        }
        return num;
    }


    public String getSerial(String userId, String licenseNum) {
        String LL = "Shanghai";
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(1, 3);
        cal.add(6, -1);
        java.text.NumberFormat nf = new java.text.DecimalFormat("000");
        licenseNum = nf.format(Integer.valueOf(licenseNum));
        String verTime = new StringBuilder("-").append(new java.text.
                SimpleDateFormat("yyMMdd").format(cal.getTime())).append("0").
                toString();
        String type = "YE3MP-";
        String need = new StringBuilder(userId.substring(0, 1)).append(type).
                append("300").append(licenseNum).append(verTime).toString();
        String dx = new StringBuilder(need).append(LL).append(userId).toString();
        int suf = this.decode(dx);
        String code = new StringBuilder(need).append(String.valueOf(suf)).
                toString();
        return this.change(code);
    }

    private int decode(String s) {
        int i;
        char[] ac;
        int j;
        int k;
        i = 0;
        ac = s.toCharArray();
        j = 0;
        k = ac.length;
        while (j < k) {
            i = (31 * i) + ac[j];
            j++;
        }
        return Math.abs(i);
    }

    private String change(String s) {
        byte[] abyte0;
        char[] ac;
        int i;
        int k;
        int j;
        abyte0 = s.getBytes();
        ac = new char[s.length()];
        i = 0;
        k = abyte0.length;
        while (i < k) {
            j = abyte0[i];
            if ((j >= 48) && (j <= 57)) {
                j = (((j - 48) + 5) % 10) + 48;
            } else if ((j >= 65) && (j <= 90)) {
                j = (((j - 65) + 13) % 26) + 65;
            } else if ((j >= 97) && (j <= 122)) {
                j = (((j - 97) + 13) % 26) + 97;
            }
            ac[i] = (char) j;
            i++;
        }
        return String.valueOf(ac);
    }

}
