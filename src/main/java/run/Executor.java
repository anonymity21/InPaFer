package run;

import config.Constant;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Executor {
    private final static String __name__ = "@Executor ";

    public static List<String> execute(String[] command) {
        Process process = null;
        final List<String> message = new ArrayList<>();
        try {
            ProcessBuilder builder = getProcessBuilder(command);
            builder.redirectErrorStream(true);
            process = builder.start();
            final InputStream inputStream = process.getInputStream();
            Thread processReader = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        message.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            processReader.start();
            try {
                processReader.join();
                process.waitFor();
            } catch (InterruptedException e) {
                return new LinkedList<>();
            }
        } catch (IOException ignored) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        //log.info(message.toString());
        return message;
    }

    private static ProcessBuilder getProcessBuilder(String[] command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> evn = builder.environment();
        evn.put("JAVA_HOME", Constant.COMMAND_JAVA_HOME);
        evn.put("PATH", Constant.COMMAND_JAVA_HOME + "/bin:" + evn.get("PATH"));
        return builder;
    }

}
