package gtanks.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static void log(String msg) {
        Logger.log(Type.INFO, msg);
    }

    public static void log(Type type, String msg) {
        Log tempLog = new Log(type, msg);
        System.out.println("[" + Logger.getCurrentTimeStamp() + "] " + String.valueOf(tempLog));
    }

    public static void debug(String msg) {
        Logger.debug(Type.INFO, msg);
    }

    public static void debug(Type type, String msg) {
        Log tempLog = new Log(type, msg);
        System.out.println("[" + Logger.getCurrentTimeStamp() + "] " + String.valueOf(tempLog));
    }

    private static String getCurrentTimeStamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").format(LocalDateTime.now());
    }
}
