public class SocksTools {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int bytesAmount) {

        int length = Math.min(bytesAmount, bytes.length);

        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte b) {
        byte[] bytes = new byte[1];
        bytes[0] = b;

        return bytesToHex(bytes, 1);
    }

    public static int bytesToPort(byte[] bytes) {
        if(bytes.length != 2) {
            return -1;
        }

        int result = 0;

        int i = bytes[0] & 0xFF;

        result += i*256;

        return result + (bytes[1] & 0xFF);
    }
}
