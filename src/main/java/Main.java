import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {

    /**
     * ports - список введенных портов
     * ipAddresses - список введенных ip-адресов
     * mapList - список из трех мап, который далее преобразуется в контейнер в json
     * FILE_NAME - адрес файла, куда ведется запись результата сканирования
     * countThreads - количество потоков, по дефолту 1, но далее принимает введенное значение
     * startTime - отметка времени старта приложения
     * endTime - отметчка времени конца приложения
     * threadPool - пул потоков
     * callList - список Callable
     */
    static List<Integer> ports = new CopyOnWriteArrayList<>();
    static List<String> ipAddresses = new CopyOnWriteArrayList<>();
    static List<Map<String, String>> mapList = new CopyOnWriteArrayList<>();
    static final String FILE_NAME = "./src/main/resources/TCPPorts.json";
    static int countThreads = 1;
    static long startTime = System.currentTimeMillis();
    static long endTime = 0L;
    static ExecutorService threadPool;
    static List<Callable<Map<String, String>>> callList = new ArrayList<>();
    // -h 173.194.222.121,192.168.88.1-4 -p 80,139-141 -t 5  пример

    public static void main(String[] args) throws InterruptedException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
            inputProcessing(reader.readLine().split("\\s"));
            threadPool = Executors.newFixedThreadPool(countThreads);
            if (ports.size() > 0 && ipAddresses.size() > 0) {
                for (int i = 0; i < ports.size(); i++){
                    for (int j = 0; j < ipAddresses.size(); j++){
                        int finalI1 = i;
                        int finalJ1 = j;
                        callList.add(new Callable<Map<String, String>>() {
                            @Override
                            public Map<String, String> call() {
                                Map<String, String> result = new HashMap<>();
                                result.put("ip", ipAddresses.get(finalJ1));
                                result.put("port", String.valueOf(ports.get(finalI1)));
                                result.put("Is connected", Boolean.toString(isActivePort(ipAddresses.get(finalJ1), ports.get(finalI1))));
                                mapList.add(result);
                                return result;
                            }
                        });
                    }
                }
                threadPool.invokeAll(callList);
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
        endTime = System.currentTimeMillis();
        System.out.println("Время работы программы: " + (endTime - startTime)/1000 + " сек.");
    }

    /**
     * Метод для обработки введенной строки, здесь заполняются списки с адресами и портами,
     * вычисляется желаемое количество потоков
     * @param strings введенная строка, представленная в виде массива слов
     */
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
            } if(strings[i].equals("-t")){
                i++;
                countThreads = Integer.parseInt(strings[i]);
            }
        }
    }

    /**
     * Метод, который проверяет подключение по переданному адресу
     * @param host
     * и порту
     * @param port
     * и возвращается true или false
     * @return
     */
    public static boolean isActivePort(String host, int port){
        try(Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(10);
            if(socket.isConnected())
                return true;
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
