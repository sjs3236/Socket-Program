import java.io.*;
import java.net.*;
import java.time.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class Server {

    public static BlockingQueue<Request> requests;
    public static DatagramSocket serverSocket;
    public static HashMap<String, Instant> userTimes;
    public static String ip = "127.0.0.1";
    public static int port = 9876;


    //    public static File logFile = new File("C:/temp/MyLogFile.log");
    public static File logFile = new File("MyLogFile.log");
    public static BufferedWriter writer;


    public static void main(String[] args) throws Exception {

        ip = args[0];
        port = Integer.parseInt(args[1]);
        requests = new LinkedBlockingQueue<Request>();
        serverSocket = new DatagramSocket(Integer.parseInt(args[1]), InetAddress.getByName(ip));
        userTimes = new HashMap<>();
        String str=("SERVER LOG FILE\n");
        writer = new BufferedWriter(new FileWriter(logFile));

        writer.write(str);


        Listener listener = new Listener();
        Solver solver = new Solver();

        listener.start();
        solver.start();
    }

    public static void sendMessage(byte[] msg, String dest, String destPort) throws IOException {
        //Prepare connection request
        sendMessage(msg, InetAddress.getByName(dest), Integer.parseInt(destPort));
    }

    public static void sendMessage(byte[] msg, InetAddress dest, int destPort) throws IOException {
        //Prepare packet
        DatagramPacket packet = new DatagramPacket(msg, msg.length, dest, destPort);

        //Send
        serverSocket.send(packet);
    }
}
class Listener extends Thread {
    private boolean isRunning;

    public Listener() {
    }

    public void run() {
        isRunning = true;
        System.out.println("Waiting for packets...");

        byte[] buf = new byte[2048];
        DatagramPacket in = new DatagramPacket(buf,buf.length);

        while(true) {
            try {
                Server.serverSocket.receive(in);
                Request newReq = new Request(new String(in.getData(), 0, in.getLength()));
                Server.requests.add(newReq);
                System.out.println("New request from " + newReq.getName());
                Arrays.fill(buf, (byte)0);
                in = new DatagramPacket(buf,buf.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}



class Solver extends Thread {

    private byte[] buffer = new byte[2048];


    public Solver() {
        System.out.println("Waiting for requests...");
    }

    public void run() {

        while(true) {
            try {
                Request req = Server.requests.take();
                switch(req.type)
                {
                    case "connection":
                        if(Server.userTimes.containsKey(req.getName())) {
                            buffer = "repeat".getBytes();
                        }
                        else {
                            buffer = "ack".getBytes();
                            Instant start= Instant.now();
                            Server.userTimes.put(req.getName(), start);
                            System.out.println("User " + req.getName() + " connected.");
                            Server.writer.append(String.format("Client detail, Name:%s IP:%s Port:%s \n", req.getName(),req.getIp(),req.getPort()));
                            Server.writer.append(String.format("Client %s connected to sever at %s:%s:%s\n", req.getName(), (start.atZone(ZoneOffset.UTC).getHour()+18)%24, start.atZone(ZoneOffset.UTC).getMinute(), start.atZone(ZoneOffset.UTC).getSecond()));
                        }

                        break;
                    case "problem":
                        System.out.println(req.getProblem());
                        buffer = req.solve().getBytes();
                        System.out.println("Solving problem " + req.getProblem() + " for user " + req.getName());
                        break;
                    case "stop":
                        buffer = "ack".getBytes();
                        Instant end= Instant.now();
                        Server.writer.append(String.format("Client %s disconnect at %s:%s:%s\n", req.getName(), (end.atZone(ZoneOffset.UTC).getHour()+18)%24, end.atZone(ZoneOffset.UTC).getMinute(),end.atZone(ZoneOffset.UTC).getSecond()));
                        Server.writer.append(String.format("Client %s connected for %s seconds\n", req.getName(), Duration.between(Server.userTimes.get(req.getName()), end).getSeconds()));
                        Server.userTimes.remove(req.getName());
                        System.out.println("User " + req.getName() + " disconnected.");
                        Server.writer.flush();
                        break;
                    default:
                        System.out.println("Invalid request");
                }
                Server.sendMessage(buffer, req.getIp(), req.getPort());

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
class Request
{
    private String name;
    private InetAddress ip;
    private int port;
    private String problem;
    public String type;

    public Request(String txt) throws UnknownHostException {
        String[] splitTxt = txt.split("#");
        name = splitTxt[0];
        ip = InetAddress.getByName(splitTxt[1]);
        port = Integer.parseInt(splitTxt[2]);


        switch(splitTxt.length) {
            case 3:
                type = "connection";
                break;
            case 4:
                type = splitTxt[3].toLowerCase().contains("stop") ? "stop" : "problem";
                problem = splitTxt[3];
                break;
            default:
                type = "invalid";
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(String ip) throws UnknownHostException {
        this.ip = InetAddress.getByName(ip);
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String solve() {

        String[] operators = {"+", "-", "*", "/", "%"};
        String solution  = "Invalid problem";

        for(String x:operators) {
            if(this.problem.contains(x)) {
                int operatorIndex = this.problem.indexOf(x);
                int answer = 0;
                System.out.println("LHS = " + this.problem.substring(0,operatorIndex));
                System.out.println("RHS = " + this.problem.substring(operatorIndex+1));
                switch(x) {
                    case "+":
                        answer = Integer.parseInt(this.problem.substring(0,operatorIndex).trim()) + Integer.parseInt(this.problem.substring(operatorIndex+1).trim());
                        break;
                    case "-":
                        answer = Integer.parseInt(this.problem.substring(0,operatorIndex).trim()) - Integer.parseInt(this.problem.substring(operatorIndex+1).trim());
                        break;
                    case "*":
                        answer = Integer.parseInt(this.problem.substring(0,operatorIndex).trim()) * Integer.parseInt(this.problem.substring(operatorIndex+1).trim());
                        break;
                    case "/":
                        answer = Integer.parseInt(this.problem.substring(0,operatorIndex).trim()) / Integer.parseInt(this.problem.substring(operatorIndex+1).trim());
                        break;
                    case "%":
                        answer = Integer.parseInt(this.problem.substring(0,operatorIndex).trim()) % Integer.parseInt(this.problem.substring(operatorIndex+1).trim());
                        break;
                }
                return "" + answer;
            }
        }

        return solution;
    }


}
