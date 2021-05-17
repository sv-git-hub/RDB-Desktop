package logging;

import researchApp.GlobalVariables;

        import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.util.logging.*;

public class LoggerWrapper {

    private static LoggerWrapper instance = new LoggerWrapper();
    public Logger myLogger;
    private LoggerWrapper(){
        createLogging();
    }
    public static LoggerWrapper getInstance(){
        return instance;
    }
    private void createLogging() {
        try {
            Path path = Paths.get(GlobalVariables.LOGGING);
            myLogger = Logger.getLogger(LoggerWrapper.class.getName());
            if (!Files.exists(path)) Files.createDirectories(path);
            FileHandler fileHandler = new FileHandler(path.toString() + "/session.log");
            fileHandler.setFormatter(new SimpleFormatter());
            myLogger.addHandler(fileHandler);
            myLogger.setLevel(Level.FINEST);

        } catch (IOException ex) {
            myLogger.log(Level.SEVERE, "FileHandler failure.");

        }
    }
}