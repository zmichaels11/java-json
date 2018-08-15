package demo.json;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;

public final class Encoder {
    private Encoder() {}

    public static void encode(final CharBuffer output, final Map<String, Object> data) throws IOException {
        encodeObjectSafe(output, data);
    }

    public static String encode(final Map<String, Object> data, final int maxJSONSize) throws IOException {
        final CharBuffer output = CharBuffer.allocate(maxJSONSize);

        encode(output, data);
        output.flip();

        return output.toString();
    }

    private static final int DEFAULT_MAX_JSON_FILE_SIZE = 128 * 1024;

    public static String encode(final Map<String, Object> data) throws IOException {
        return encode(data, DEFAULT_MAX_JSON_FILE_SIZE);
    }

    private static void encodeObjectSafe(final CharBuffer output, final Map<String, Object> obj) throws IOException {
        output.put('{');

        int i = 0;

        for (Map.Entry<String, Object> pair : obj.entrySet()) {
            encodeStringSafe(output, pair.getKey());
            output.put(':');
            encodeAnySafe(output, pair.getValue());

            if (i < obj.size() - 1) {
                output.put(',');
            }

            i++;
        }

        output.put('}');
    }

    private static void encodeAnySafe(final CharBuffer output, final Object obj) throws IOException {
        if (obj == null) {
            encodeNullSafe(output);
        } else if (obj instanceof Map) {
            encodeObjectSafe(output, (Map<String, Object>) obj);
        } else if (obj instanceof List) {
            encodeArraySafe(output, (List) obj);
        } else if (obj instanceof String) {
            encodeStringSafe(output, (String) obj);
        } else if (obj instanceof Fixed) {
            encodeFixedSafe(output, (Fixed) obj);
        } else if (obj instanceof Double) {
            encodeDoubleSafe(output, (double) obj);
        } else if (obj instanceof Float) {
            encodeDoubleSafe(output, (float) obj);
        } else if (obj instanceof Long) {
            encodeLongSafe(output, (long) obj);
        } else if (obj instanceof Integer) {
            encodeLongSafe(output, (int) obj);
        } else if (obj instanceof Short) {
            encodeLongSafe(output, (short) obj);
        } else if (obj instanceof Byte) {
            encodeLongSafe(output, (byte) obj);
        } else if (obj instanceof Boolean) {
            encodeBooleanSafe(output, (boolean) obj);
        } else {
            throw new IOException("Unsupported Object type: " + obj.getClass().getSimpleName());
        }
    }

    private static void encodeStringSafe(final CharBuffer output, final String str) {
        output.put('\"');

        final char[] chars = str.toCharArray();

        for (char c : chars) {
            switch (c) {
                case '\\':
                    output.put("\\\\");
                    break;
                case '\"':
                    output.put("\\\"");
                    break;
                case '\n':
                    output.put("\\\n");
                    break;
                case '\t':
                    output.put("\\\t");
                    break;
                case '\r':
                    output.put("\\\r");
                    break;
                default:
                    output.put(c);
                    break;
            }
        }

        output.put('\"');
    }

    private static void encodeNullSafe(final CharBuffer output) {
        output.put("null");
    }

    private static void encodeArraySafe(final CharBuffer output, final List arr) throws IOException {
        output.put('[');

        if (!arr.isEmpty()) {
            for (int i = 0; i < arr.size() - 1; i++) {
                encodeAnySafe(output, arr.get(i));
                output.put(',');
            }

            encodeAnySafe(output, arr.get(arr.size() - 1));
        }

        output.put(']');
    }

    private static void encodeFixedSafe(final CharBuffer output, final Fixed value) {
        output.put(value.toString());
    }

    private static void encodeDoubleSafe(final CharBuffer output, final double value) {
        output.put(Double.toString(value));
    }

    private static void encodeLongSafe(final CharBuffer output, final long value) {
        output.put(Long.toString(value));
    }

    private static void encodeBooleanSafe(final CharBuffer output, final boolean value) {
        output.put(Boolean.toString(value));
    }
}
