import org.junit.jupiter.api.*;

public class MainTest {


    @Test
    public void testPorts(){
        Assertions.assertTrue(Main.isActivePort("173.194.222.121", 80),
                "Открытый порт не определился как открытый");
    }
}
