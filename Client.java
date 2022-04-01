import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Client
{
    static String name = "Default Client Name";

    static String clientIp = "127.0.0.1";
    static String serverIp = "127.0.0.1";
    static InetAddress serverAddress = InetAddress.getLoopbackAddress();

    static int portNum = 9875;
    static int serverPort = 9876;


    public static void main(String[] args) {
        try{
            clientIp = args[0];
            portNum = Integer.parseInt(args[1]);

            serverIp = args[2];
            serverAddress = InetAddress.getByName(serverIp);
            serverPort = Integer.parseInt(args[3]);

            DatagramSocket socket = new DatagramSocket(portNum, InetAddress.getByName(clientIp));

            Scanner input = new Scanner(System.in);

            do {
                System.out.print("Enter your name: ");
                name = input.nextLine().trim();
                if(name.contains("#"))
                {
                    System.out.println("Name cannot contain '#'");
                }

            }while(name.contains("#"));

            //Prepare connection request
            byte[] buf = String.format("%s#%s#%s##", name, clientIp, portNum).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);

            //Send connection request
            socket.send(packet);

            //Wait for ACK
            buf = new byte[1024];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String response = new String(packet.getData(), 0, packet.getLength());

            System.out.println("Waiting for response...");

            if(!response.contains("ack")) {
                System.out.println("Connection failed.");
                System.exit(1);
            }
            else {
                System.out.println("Connection established!");
            }

            String problem;
            boolean active = true;
            int calcCount = 0;

            while(active || calcCount < 3)
            {
                active = true;
                System.out.print("Enter math problem or 'stop': ");
                problem = input.nextLine().trim();

                if(problem.trim().toLowerCase().contains("stop")){
                    active = false;
                    problem = "STOP";

                }
                else
                {
                    calcCount++;
                }

                //Send the formatted data
                buf = String.format("%s#%s#%s#%s#", name, serverIp, portNum, problem).getBytes();
                packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
                socket.send(packet);

                //Receive the response packet
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String solution = new String(packet.getData(), 0, packet.getLength());
                if(active)
                {
                    System.out.println("Answer: " + solution);
                }

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }



    }




}