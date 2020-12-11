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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
