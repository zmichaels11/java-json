package demo.json;

import java.nio.CharBuffer;
import java.util.Objects;

public final class Fixed extends Number {
    public final long value;
    public final long scale;
    public final boolean isInteger;

    public Fixed(final long value, final long scale) {
        this.value = value;
        this.scale = scale;
        this.isInteger = (scale == 1L);
    }

    public Fixed(final Double value) {
        this.scale = 1L << 11;
        this.value = (long) (value * this.scale);
        this.isInteger = false;
    }

    @Override
    public int intValue() {
        if (this.isInteger) {
            return (int) this.value;
        } else {
            return (int) this.doubleValue();
        }
    }

    @Override
    public long longValue() {
        if (this.isInteger) {
            return this.value;
        } else {
            return (long) this.doubleValue();
        }
    }

    @Override
    public float floatValue() {
        return (float) this.doubleValue();
    }

    @Override
    public double doubleValue() {
        return ((double) this.value) / (double) (this.scale);
    }

    @Override
    public String toString() {
        return String.format("%d/%d", this.value, this.scale);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Fixed) {
            final Fixed o = (Fixed) other;

            if (this.scale == o.scale) {
                return this.value == o.value;
            } else {
                return Double.compare(this.doubleValue(), o.doubleValue()) == 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, scale);
    }

    private static double decodeDouble(final CharBuffer data) {
        int offset = 0;
        data.mark();

        while (data.hasRemaining()) {
            switch (data.get()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                case '.':
                case 'e':
                case 'E':
                    offset++;
                    break;
                default:
                    data.reset();

                    final CharBuffer subData = data.slice();

                    data.position(data.position() + offset);
                    subData.limit(offset);

                    return Double.parseDouble(subData.toString());
            }
        }

        throw new NumberFormatException("Malformed Double!");
    }

    public static Fixed decodeFixed(final CharBuffer strval) {
        boolean isNegative = false;
        long intPart = 0L;
        long value = 0L;
        int digits = 0;
        boolean hasDecimal = false;
        boolean isFixed = false;
        boolean isDone = false;

        strval.mark();

        while (!isDone && strval.hasRemaining()) {
            final char lookup = strval.get();

            switch (lookup) {
                case '-':
                    isNegative = true;
                    break;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    value = value * 10L + (lookup - '0');
                    digits ++;
                    break;
                case '/':
                    intPart = value;
                    value = 0L;
                    isFixed = true;
                    break;
                case '.':
                    intPart = value;
                    value = 0L;
                    digits = 0;
                    hasDecimal = true;
                    break;
                case 'e':
                case 'E':
                    strval.reset();
                    return new Fixed(decodeDouble(strval));
                default:
                    strval.position(strval.position() - 1);
                    isDone = true;
                    break;
            }
        }

        if (isFixed) {
            return (isNegative) ? new Fixed(-intPart, value) : new Fixed(intPart, value);
        } else if (hasDecimal) {
            long scale = 1L;

            for (int i = 0; i < digits; i++) {
                scale *= 10L;
            }

            return (isNegative) ? new Fixed(-intPart * scale - value, scale) : new Fixed(intPart * scale + value, scale);
        } else {
            return (isNegative) ? new Fixed(-value, 1) : new Fixed(value, 1);
        }
    }

    public static Fixed parseFixed(final String strval) {
        final char[] carr = strval.toCharArray();
        boolean isNegative = false;
        long intPart = 0L;
        long value = 0L;
        int digits = 0;
        boolean hasDecimal = false;
        boolean isFixed = false;

        for (int i = 0; i < carr.length; i++) {
            switch (carr[i]) {
                case '-':
                    isNegative = true;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    value = value * 10L + (carr[i] - '0');
                    digits++;
                    break;
                case '/':
                    intPart = value;
                    value = 0L;
                    isFixed = true;
                    break;
                case '.':
                    intPart = value;
                    value = 0L;
                    digits = 0;
                    hasDecimal = true;
                    break;
                case 'e':
                case 'E':
                    return new Fixed(Double.parseDouble(strval));
                default:
                    throw new NumberFormatException("Invalid character: " + carr[i]);
            }
        }

        if (isFixed) {
            return (isNegative) ? new Fixed(-intPart, value) : new Fixed(intPart, value);
        } else if (hasDecimal) {
            long scale = 1L;

            for (int i = 0; i < digits; i++) {
                scale *= 10L;
            }

            return (isNegative) ? new Fixed(-intPart * scale - value, scale) : new Fixed(intPart * scale + value, scale);
        } else {
            return (isNegative) ? new Fixed(-value, 1L) : new Fixed(value, 1L);
        }
    }
}
