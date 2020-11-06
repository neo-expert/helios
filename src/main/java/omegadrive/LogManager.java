package omegadrive;

public class LogManager {
    public static Logger getLogger(String simpleName) {
        return new Logger(simpleName);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }
}
