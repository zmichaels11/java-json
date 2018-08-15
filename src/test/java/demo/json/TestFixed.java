package demo.json;


import org.junit.Assert;
import org.junit.Test;

public class TestFixed {
    @Test
    public void testParse() {
        Assert.assertEquals(3.14, Fixed.parseFixed("3.14").doubleValue(), 1E-9);
        Assert.assertEquals(1.0, Fixed.parseFixed("1").doubleValue(), 1E-9);
    }
}