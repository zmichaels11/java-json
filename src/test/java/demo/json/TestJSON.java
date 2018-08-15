package demo.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJSON {
    @Test
    public void testEncode() throws IOException {
        final Map<String, Object> obj = new HashMap<>();

        obj.put("pi", 3.14);
        obj.put("array", Arrays.asList(1, 2, 3, 4));
        obj.put("greeting", "Hello World!");

        System.out.println(Encoder.encode(obj));
    }

    @Test
    public void testDecode() throws IOException {
        final Map<String, Object> obj = Decoder.decode("{\"array\":[1,2,3,4],\"greeting\":\"Hello World!\",\"pi\":3.14}\n");

        Assert.assertEquals(3.14, ((Number) obj.get("pi")).doubleValue(), 1E-9);

        final List<Number> array = (List<Number>) obj.get("array");
        final List<Number> expected = Arrays.asList(1L, 2L, 3L, 4L);

        for (int i = 0; i < array.size(); i++) {
            Assert.assertEquals(expected.get(i), ((Number) array.get(i)).longValue());
        }

        Assert.assertEquals("Hello World!", obj.get("greeting"));
    }
}
