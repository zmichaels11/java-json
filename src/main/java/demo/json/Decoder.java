package demo.json;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Decoder {
    private Decoder() {}

    private static String decodeEscapeString(final CharBuffer data) throws IOException {
        final StringBuilder out = new StringBuilder(8 * 1024);

        while (data.hasRemaining()) {
            final char lookup = data.get();

            switch (lookup) {
                case '\\':
                    switch (data.get()) {
                        case '\"':
                            out.append('\"');
                            break;
                        case '\\':
                            out.append('\\');
                            break;
                        case '/':
                            out.append('/');
                            break;
                        case 'b':
                            out.append('\b');
                            break;
                        case 'f':
                            out.append('\f');
                            break;
                        case 'n':
                            out.append('\n');
                            break;
                        case 'r':
                            out.append('\r');
                            break;
                        case 't':
                            out.append('\t');
                            break;
                        case 'u': {
                            final char[] uHex = new char[4];

                            data.get(uHex);

                            final int uVal = Integer.parseInt(new String(uHex), 16);

                            out.append(Integer.toString(uVal));
                        } break;
                        default:
                            throw new IOException("Unexpected escaped character: " + data.get(data.position() - 1));
                    } break;
                case '\"':
                    return out.toString();
                default:
                    out.append(lookup);
                    break;
            }
        }

        throw new IOException("Unclosed String!");
    }

    private static String decodeString(final CharBuffer data) throws IOException {
        data.get();
        data.mark();

        int offset = 0;

        while (data.hasRemaining()) {
            switch (data.get()) {
                case '\\':
                    data.reset();
                    return decodeEscapeString(data);
                case '\"': {
                    data.reset();

                    final CharBuffer subBuffer = data.slice();

                    data.position(data.position() + offset + 1);
                    subBuffer.limit(offset);

                    return subBuffer.toString();
                }
                default:
                    offset++;
                    // nothing to do
                    break;
            }
        }

        throw new IOException("Unclosed String!");
    }

    private static boolean isWhitespace(final char lookup) {
        switch (lookup) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }

    private static boolean decodeFalse(final CharBuffer data) {
        final char f = data.get();
        final char a = data.get();
        final char l = data.get();
        final char s = data.get();
        final char e = data.get();

        assert (f == 'f' || f == 'F');
        assert (a == 'a' || a == 'A');
        assert (l == 'l' || l == 'L');
        assert (s == 's' || s == 'S');
        assert (e == 'e' || e == 'E');

        return false;
    }

    private static boolean decodeTrue(final CharBuffer data) {
        final char t = data.get();
        final char r = data.get();
        final char u = data.get();
        final char e = data.get();

        assert (t == 't' || t == 'T');
        assert (r == 'r' || r == 'R');
        assert (u == 'u' || u == 'U');
        assert (e == 'e' || e == 'E');

        return true;
    }

    private static void skipWhitespace(final CharBuffer data) {
        while (data.hasRemaining()) {
            if (!isWhitespace(data.get(data.position()))) {
                return;
            }

            //dispose of character
            data.get();
        }
    }

    private static List decodeArray(final CharBuffer data) throws IOException {
        data.get();

        final List<Object> out = new ArrayList<>();

        while (data.hasRemaining()) {
            skipWhitespace(data);

            data.mark();

            switch (data.get()) {
                case ']':
                    return out;
                case ',':
                    break;
                default:
                    data.reset();
                    out.add(decodeAny(data));
                    break;
            }
        }

        throw new IOException("Malformed Array!");
    }

    private static Map<String, Object> decodeObject(final CharBuffer data) throws IOException {
        data.get();

        final Map<String, Object> out = new HashMap<>();

        while (data.hasRemaining()) {
            skipWhitespace(data);

            if (data.get(data.position()) == '}') {
                return out;
            }

            skipWhitespace(data);

            final String key = decodeString(data);

            skipWhitespace(data);

            final char sep = data.get();

            assert(sep == ':');

            skipWhitespace(data);

            final Object value = decodeAny(data);

            out.put(key, value);

            skipWhitespace(data);

            switch (data.get()) {
                case '}':
                    return out;
                case ',':
                    break;
                default:
                    throw new IOException("Malformed Object!");
            }
        }

        throw new IOException("Malformed Object!");
    }

    private static Object decodeNull(final CharBuffer data) {
        final char[] value = new char[4];

        data.get(value);

        assert (value[0] == 'n' || value[0] == 'N');
        assert (value[1] == 'u' || value[1] == 'U');
        assert (value[2] == 'l' || value[2] == 'L');
        assert (value[3] == 'l' || value[3] == 'L');

        return null;
    }

    private static Object decodeAny(final CharBuffer data) throws IOException {
        final char lookup = data.get(data.position());

        switch (lookup) {
            case '\"':
                return decodeString(data);
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
                return Fixed.decodeFixed(data);
            case '[':
                return decodeArray(data);
            case '{':
                return decodeObject(data);
            case 't':
            case 'T':
                return decodeTrue(data);
            case 'f':
            case 'F':
                return decodeFalse(data);
            case 'n':
            case 'N':
                return decodeNull(data);
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                skipWhitespace(data);
                return decodeAny(data);
            default:
                throw new IOException("Malformed JSON!");
        }
    }

    public static Map<String, Object> decode(final CharBuffer data) throws IOException {
        final Object out = decodeAny(data);

        if (out instanceof Map) {
            return (Map<String, Object>) out;
        } else {
            throw new IOException("Malformed JSON!");
        }
    }

    public static Map<String, Object> decode(final String strval) throws IOException {
        return decode(CharBuffer.wrap(strval));
    }
}
