import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessThread implements Callable<Map<String, String>> {

    List<String> ipAddresses;
    List<Integer> ports;
    Map<String, String> jsonMap;
    AtomicInteger i;
    AtomicInteger j;

    public ProcessThread(AtomicInteger finalI, AtomicInteger finalJ, List<String> ipAddresses, List<Integer> ports){
        i = finalI;
        j = finalJ;
        this.ipAddresses = ipAddresses;
        this.ports = ports;
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

    @Override
    public Map<String, String> call() {
        final int finalI = i.get();
        final int finalJ = j.get();
        if(j.get() < ipAddresses.size() - 1) {
            j.addAndGet(1);
        } else if(i.get() < ports.size()) {
            j.set(0);
            i.addAndGet(1);
        }
        jsonMap = new ConcurrentHashMap<>();
        jsonMap.put("ip", ipAddresses.get(finalJ));
        jsonMap.put("port", String.valueOf(ports.get(finalI)));
        jsonMap.put("Is connected", Boolean.toString(isActivePort(ipAddresses.get(finalJ), ports.get(finalI))));
        return jsonMap;
    }
}
