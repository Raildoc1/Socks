import javax.sound.midi.Soundbank;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    private static final int BUFFER_SIZE = 2_048_000;
    private static ArrayList<Client> clientList;

    public static void main(String args[]) {

        if(args.length != 1) {
            System.out.println("Wrong argument!");
            return;
        }

        clientList = new ArrayList<>();

        byte version;
        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket socket = new ServerSocket(port);

            socket.setSoTimeout(1);


            while(true) {

                try {
                    Socket newClientSocket = socket.accept();
                    Client newClient = new Client(newClientSocket, port);
                    newClient.initSOCKS5();
                    if(newClient.getStatus() == Client.Status.ALIVE) {
                        System.out.println("new client accepted!");
                        clientList.add(newClient);
                    } else {
                        System.out.println("client accept failed :c");
                        newClient.close();
                        newClientSocket.close();
                    }
                } catch (IOException e) { /* IGNORE */}

                if(clientList.size() == 0) {
                    continue;
                }

                Iterator<Client> clientIterator = clientList.iterator();

                while(clientIterator.hasNext()) {
                    var client = clientIterator.next();
                    if(client.getStatus() != Client.Status.ALIVE) {
                        client.close();
                        clientIterator.remove();
                    }
                }

                for (var client : clientList) {
                    client.listenTick();
                }

            }

//            byte[] bytes = new byte[BUFFER_SIZE];
//
//            InputStream in = client.getInputStream();
//            OutputStream out = client.getOutputStream();
//
//
//            int readBytes = in.read(bytes);
//
//            System.out.print("Bytes read = " + readBytes + ": ");
//
//            for(int i = 0; i < readBytes; i++) {
//                System.out.print(SocksTools.bytesToHex(bytes[i]) + " ");
//            }
//
//            version = bytes[0];
//
//            byte[] answer = new byte[2];
//
//            answer[0] = version;
//            answer[1] = bytes[2];
//
//            out.write(answer);
//            readBytes = in.read(bytes);
//
//            System.out.print("\nBytes read = " + readBytes + ": ");
//
//            for(int i = 0; i < readBytes; i++) {
//                System.out.print(SocksTools.bytesToHex(bytes[i]) + " ");
//            }
//
//            byte[] byteAddr = {bytes[4], bytes[5], bytes[6], bytes[7]};
//            InetAddress targetAddress = InetAddress.getByAddress(byteAddr);
//            byte[] bytePort = {bytes[8], bytes[9]};
//            int targetPort = SocksTools.bytesToPort(bytePort);
//
//            System.out.println("target: " + targetAddress.toString() + " " + targetPort);
//
//            Socket socket1 = new Socket(targetAddress, targetPort);
//
//            InputStream targetIn = socket1.getInputStream();
//            OutputStream targetOut = socket1.getOutputStream();
//
//
//            ServerSocket SourceDataListenSocket = new ServerSocket();
//            byte[] SourceDataListenSocketAddress = InetAddress.getLocalHost().getAddress();
//            int SourceDataListenSocketPort = SourceDataListenSocket.getLocalPort();
//
//            answer = new byte[10];
//
//            SourceDataListenSocketPort = 5000;
//
//            answer[0] = version;
//            answer[1] = 0;
//            answer[2] = 0;
//            answer[3] = 1;
//            answer[4] = SourceDataListenSocketAddress[0];
//            answer[5] = SourceDataListenSocketAddress[1];
//            answer[6] = SourceDataListenSocketAddress[2];
//            answer[7] = SourceDataListenSocketAddress[3];
//            answer[8] = (byte)(SourceDataListenSocketPort / 256);
//            answer[9] = (byte)(SourceDataListenSocketPort % 256);
//
//            System.out.println("PORT = " + SourceDataListenSocketPort);
//
//            out.write(answer);
//
//            byte[] bytes1 = new byte[BUFFER_SIZE];
//
//            while(true) {
//                if(in.available() > 0) {
//
//                    readBytes = in.read(bytes);
//
//                    System.out.println("source read bytes: " + readBytes);
//
//                    byte[] toTarget = new byte[readBytes];
//
//                    for(int i = 0; i < readBytes; i++) {
//                        toTarget[i] = bytes[i];
//                        //System.out.print((char)bytes[i]);
//                    }
//                    //System.out.println("");
//
//                    targetOut.write(toTarget);
//
//                }
//
//                if(targetIn.available() > 0) {
//                    readBytes = targetIn.read(bytes1);
//
//                    System.out.println("target read bytes: " + readBytes);
//
//                    byte[] toSource = new byte[readBytes];
//
//                    for (int i = 0; i < readBytes; i++) {
//                        toSource[i] = bytes1[i];
//                        //System.out.print((char) bytes1[i]);
//                    }
//                    //System.out.println("");
//
//                    out.write(toSource);
//                }
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
