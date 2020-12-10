import javax.sound.midi.Soundbank;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {


    private static final int BUFFER_SIZE = 2_147_000_000;
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

    public static void main(String args[]) {

        if(args.length != 1) {
            System.out.println("Wrong argument!");
            return;
        }

        byte version;
        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket socket = new ServerSocket(port);

            Socket client = socket.accept();

            byte[] bytes = new byte[BUFFER_SIZE];

            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();


            int readBytes = in.read(bytes);

            System.out.print("Bytes read = " + readBytes + ": ");

            for(int i = 0; i < readBytes; i++) {
                System.out.print(bytesToHex(bytes[i]) + " ");
            }

            version = bytes[0];

            byte[] answer = new byte[2];

            answer[0] = version;
            answer[1] = bytes[2];

            out.write(answer);
            readBytes = in.read(bytes);

            System.out.print("\nBytes read = " + readBytes + ": ");

            for(int i = 0; i < readBytes; i++) {
                System.out.print(bytesToHex(bytes[i]) + " ");
            }

            byte[] byteAddr = {bytes[4], bytes[5], bytes[6], bytes[7]};
            InetAddress targetAddress = InetAddress.getByAddress(byteAddr);
            byte[] bytePort = {bytes[8], bytes[9]};
            int targetPort = bytesToPort(bytePort);

            System.out.println("target: " + targetAddress.toString() + " " + targetPort);

            Socket socket1 = new Socket(targetAddress, targetPort);

            InputStream targetIn = socket1.getInputStream();
            OutputStream targetOut = socket1.getOutputStream();


            answer = new byte[10];

            answer[0] = version;
            answer[1] = 0;
            answer[2] = 0;
            answer[3] = 1;
            answer[4] = 127;
            answer[5] = 0;
            answer[6] = 0;
            answer[7] = 1;
            answer[8] = 0x13;//116;//116;0x13
            answer[9] = (byte)0x88;//10;//10; 0x88

            out.write(answer);

            while(true) {
                if(in.available() > 0) {

                    readBytes = in.read(bytes);

                    System.out.println("source read bytes: " + readBytes);

                    byte[] toTarget = new byte[readBytes];

                    for(int i = 0; i < readBytes; i++) {
                        toTarget[i] = bytes[i];
                    }

                    targetOut.write(toTarget);

                    readBytes = targetIn.read(bytes);

                    System.out.println("target read bytes: " + readBytes);

                    byte[] toSource = new byte[readBytes];

                    for(int i = 0; i < readBytes; i++) {
                        toSource[i] = bytes[i];
                    }

                    out.write(toSource);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
