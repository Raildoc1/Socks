import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private static final int BUFFER_SIZE = 2_048;

    public enum Status {
        ALIVE,
        FAILED,
        CLOSED
    }

    private int port;
    private Status status;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private byte SocksVersion;
    private InetAddress targetAddress;
    private int targetPort;
    private InputStream targetIn;
    private OutputStream targetOut;
    Socket targetSocket;

    public Status getStatus() {
        return status;
    }

    public Client (Socket socket, int port) {
        this.socket = socket;
        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();
        } catch (IOException e) {
            status = Status.FAILED;
        }
        this.port = port;
        status = Status.ALIVE;
    }

    public boolean initSOCKS5() {
        try {
            byte[] bytes = new byte[BUFFER_SIZE];

            int readBytes = in.read(bytes);

            SocksVersion = bytes[0];

            byte[] answer = new byte[2];

            answer[0] = SocksVersion;
            answer[1] = bytes[2];

            out.write(answer);

            readBytes = in.read(bytes);

            byte[] byteAddr = {bytes[4], bytes[5], bytes[6], bytes[7]};
            targetAddress = InetAddress.getByAddress(byteAddr);

            byte[] bytePort = {bytes[8], bytes[9]};
            targetPort = SocksTools.bytesToPort(bytePort);

            targetSocket = new Socket(targetAddress, targetPort);
            targetIn = targetSocket.getInputStream();
            targetOut = targetSocket.getOutputStream();

            answer = new byte[10];

            answer[0] = SocksVersion;
            answer[1] = 0;
            answer[2] = 0;
            answer[3] = 1;
            answer[4] = 127;
            answer[5] = 0;
            answer[6] = 0;
            answer[7] = 1;
            answer[8] = (byte)(port / 256);
            answer[9] = (byte)(port % 256);

            out.write(answer);

        } catch (IOException e) {
            status = Status.FAILED;
            return false;
        }
        return true;
    }

    public void listenTick() {
        try {
            if(in.available() > 0) {

                byte[] bytes = new byte[BUFFER_SIZE];

                int readBytes = in.read(bytes);

                System.out.println("source read bytes: " + readBytes);

                byte[] toTarget = new byte[readBytes];

                for(int i = 0; i < readBytes; i++) {
                    toTarget[i] = bytes[i];
                    //System.out.print((char)bytes[i]);
                }
                //System.out.println("");

                targetOut.write(toTarget);

            }

            if(targetIn.available() > 0) {

                byte[] bytes = new byte[BUFFER_SIZE];

                int readBytes = targetIn.read(bytes);

                System.out.println("target read bytes: " + readBytes);

                byte[] toSource = new byte[readBytes];

                for (int i = 0; i < readBytes; i++) {
                    toSource[i] = bytes[i];
                    //System.out.print((char) bytes1[i]);
                }
                //System.out.println("");

                out.write(toSource);
            }
        } catch (IOException e) {
            status = Status.FAILED;
            return;
        }
    }

    public void close() {
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(targetOut != null) targetOut.close();
            if(targetIn != null) targetIn.close();
        } catch (IOException e) {  } finally {
            status = Status.CLOSED;
        }
    }

}
