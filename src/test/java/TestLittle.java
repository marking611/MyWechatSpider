import org.junit.Test;

/**
 * Created by makai on 2017/8/24.
 */
public class TestLittle {
    @Test
    public void testRandom(){
        for (int j = 0;j<10;j++){
            int i = (int) (Math.random()*5);
            System.out.println(i);
        }

    }
}
