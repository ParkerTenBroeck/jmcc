import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

public class Script {
    public static void main(String... args) throws IOException {
        long entry = 0;
        long text_start = 0;
        long text_end = 0;

        for (var arg : args) {
            if (arg.contains("Entry")) {
                var pattern = Pattern.compile(".*0x([0-9A-Fa-f]+).*");
                var matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    entry = Long.parseLong(matcher.group(1), 16);
                }
            } else if (arg.contains(".text")) {
                var pattern = Pattern.compile("\\s*\\[.*]\\D+\\s+([0-9A-Fa-f]+)\\s+[0-9A-Fa-f]+\\s+([0-9A-Fa-f]+).*");
                var matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    text_start = Long.parseLong(matcher.group(1), 16);
                    text_end = text_start + Long.parseLong(matcher.group(2), 16);
                }
            }
        }

        try (FileOutputStream out = new FileOutputStream("out.prog");
             FileInputStream in = new FileInputStream("out.bin")) {

            ByteBuffer buffer = ByteBuffer.allocate(12);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt((int)entry);
            buffer.putInt((int)text_start);
            buffer.putInt((int)text_end);
            out.write(buffer.array());

            ;
            byte[] bytes = new byte[4096];
            int len;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

