import com.google.gson.Gson;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static List<Integer> ports = new CopyOnWriteArrayList<>();
    static List<String> ipAddresses = new CopyOnWriteArrayList<>();
    static List<Map<String, String>> mapList = new CopyOnWriteArrayList<>();
    static final String FILE_NAME = "./src/main/resources/TCPPorts.json";
    static final int countThreads = 1;
    static volatile AtomicInteger i = new AtomicInteger(0);
    static volatile AtomicInteger j = new AtomicInteger(0);
    static ExecutorService threadPool = Executors.newFixedThreadPool(countThreads);
    //-h 192.168.88.1-4 -p 139
    // -h 173.194.222.121,192.168.88.1-4 -p 80,139-141
    //173.194.222.121
    //80
    //www.bootdev.ru
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
            inputProcessing(reader.readLine().split("\\s"));
            if (ports.size() > 0 && ipAddresses.size() > 0) {
                    while (j.get() < ipAddresses.size() && i.get() < ports.size()) {
                        mapList.add(threadPool.submit(new ProcessThread(i, j, ipAddresses, ports)).get());
                    }
                threadPool.shutdown();
            } else
                System.out.println("Нет чего-то");

        } catch (IOException e) {}
        try(BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME))) {
            Gson gson = new Gson();
            fileWriter.write(gson.toJson(mapList));
        } catch (IOException ex){
            System.out.println("Запись не удалась");
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
