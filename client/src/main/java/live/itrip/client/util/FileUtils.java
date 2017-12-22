package live.itrip.client.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Description: 文件操作
 *
 * @author JianF
 * Date:  2017/11/22
 * Time:  18:08
 * Modify:
 */
public class FileUtils {

    /**
     * write string buffer to file
     *
     * @param stringBuffer stringBuffer
     * @param filePath     filePath
     * @throws IOException IOException
     */
    public static void stringBufferWrite2File(StringBuffer stringBuffer, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        BufferedWriter bwr = new BufferedWriter(new FileWriter(file));
        //write contents of StringBuffer to a file
        bwr.write(stringBuffer.toString());

        //flush the stream
        bwr.flush();

        //close the stream
        bwr.close();
    }
}
