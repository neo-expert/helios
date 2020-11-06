package omegadrive;


public class Logger {
    private final String name;

    public Logger(String simpleName) {
        this.name=simpleName;
    }

    public void error(String s) {
        System.err.print("[");
        System.err.print(name);
        System.err.print("]");
        System.err.println(s);
    }

    public void info(String s) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.println(s);
    }

    public void error(String s, Throwable e) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.println(s);
    }
    public void error(String s, Object ... objects) {
        System.err.print("[");
        System.err.print(name);
        System.err.print("]");
        System.err.print(s);
        for(Object o:objects)
            System.err.print(o);
        System.err.println();
    }

    public void info(String s, Object ... objects) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.print(s);
        for(Object o:objects)
            System.out.print(o);
        System.out.println();
    }

    public void warn(String s, Object ... objects) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.print(s);
        for(Object o:objects)
            System.out.print(o);
        System.out.println();
    }


    public void debug(String s, Object ... objects) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.print(s);
        for(Object o:objects)
            System.out.print(o);
        System.out.println();
    }

    public void printf(int level, String s, Object ... objects) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        System.out.print(s);
        for(Object o:objects)
            System.out.print(o);
        System.out.println();
    }

    public void log(int level, Object ... objects) {
        System.out.print("[");
        System.out.print(name);
        System.out.print("]");
        for(Object o:objects)
            System.out.print(o);
        System.out.println();
    }

    public void error(Throwable e) {
        System.out.println(e);
    }

    public boolean isEnabled(int level) {
        return true;
    }
}
