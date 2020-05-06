import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static List<Integer> ports = new ArrayList<>();
    static List<String> ipAddresses = new ArrayList<>();
    static Map<String, String> jsonMap;
    static List<Map<String, String>> mapList = new ArrayList<>();
    static final String FILE_NAME = "./src/main/resources/TCPPorts.json";
    //-h 192.168.88.1-4 -p 139
    // -h 173.194.222.121,192.168.88.1-4 -p 80,139-141
    //173.194.222.121
    //80
    //www.bootdev.ru
    public static void main(String[] args){

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
            inputProcessing(reader.readLine().split("\\s"));
            if (ports.size() > 0 && ipAddresses.size() > 0) {
                for (int i = 0; i < ports.size(); i++){
                    for (int j = 0; j < ipAddresses.size(); j++) {
                     jsonMap = new HashMap<>();
                     jsonMap.put("ip", ipAddresses.get(j));
                     jsonMap.put("port", String.valueOf(ports.get(i)));
                     jsonMap.put("Is connected", Boolean.toString(isActivePort(ipAddresses.get(j), ports.get(i))));
                     mapList.add(jsonMap);
                    }
                }
            } else
                System.out.println("Нет чего-то");
            for (Map<String, String> map : mapList)
                System.out.println(map.toString());

        } catch (IOException e) {}
        try(BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME))) {
            Gson gson = new Gson();
            fileWriter.write(gson.toJson(mapList));
        } catch (IOException ex){
            System.out.println("Запись не удалась");
        }
    }

    private static boolean isActivePort(String host, int port){
        try(Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(10);
                if(socket.isConnected())
                    return true;
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static void inputProcessing(String[] strings){
        for (int i = 0; i < strings.length; i++){
            if (strings[i].equals("-h")){
                i++;
                if(strings[i].contains(",")){
                    String[] addresses = strings[i].split(",");
                    for (int j = 0; j < addresses.length; j++){
                        if(addresses[j].contains("-")){
                            String number0 = addresses[j].split("-")[0].split("\\.")[3];
                            String number1 = addresses[j].split("-")[1];
                            String[] preNumber = strings[j].split("\\.");
                            for (int k = Integer.parseInt(number0); k <= Integer.parseInt(number1); k++)
                                ipAddresses.add(preNumber[0] + "." + preNumber[1] + "." + preNumber[2] + "." + String.valueOf(k));
                        } else
                            ipAddresses.add(addresses[j]);
                    }

                } else if(strings[i].contains("-")){
                    String number0 = strings[i].split("-")[0].split("\\.")[3];
                    String number1 = strings[i].split("-")[1];
                    String[] preNumber = strings[i].split("\\.");
                    for (int k = Integer.parseInt(number0); k <= Integer.parseInt(number1); k++)
                        ipAddresses.add(preNumber[0] + "." + preNumber[1] + "." + preNumber[2] + "." + String.valueOf(k));

                } else
                    ipAddresses.add(strings[i]);
            }
            if (strings[i].equals("-p")){
                i++;
                if (strings[i].contains(",")){
                 String[] sPorts = strings[i].split(",");
                 for (int j = 0; j < sPorts.length; j++){
                     if (sPorts[j].contains("-")){
                         String number0 = sPorts[j].split("-")[0];
                         String number1 = sPorts[j].split("-")[1];
                         for(int k = Integer.parseInt(number0); k <= Integer.parseInt(number1); k++)
                             ports.add(k);
                     } else
                         ports.add(Integer.parseInt(sPorts[j]));
                 }

                } else if(strings[i].contains("-")){
                    String number0 = strings[i].split("-")[0];
                    String number1 = strings[i].split("-")[1];
                    for(int k = Integer.parseInt(number0); k <= Integer.parseInt(number1); k++)
                        ports.add(k);
                } else
                    ports.add(Integer.parseInt(strings[i]));
            }
        }
    }
}
