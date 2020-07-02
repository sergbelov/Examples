package ru.examples;

/*
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*/
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerExample
{
//    log4j
/*
    static final Logger rootLogger = LogManager.getRootLogger();
    static final Logger userLogger = LogManager.getLogger();
*/
//    static final Logger userLogger = LogManager.getLogger(ru.examples.LoggerExample.class);

//    slf4j
    static final Logger rootLogger = LoggerFactory.getLogger(LoggerExample.class);
    static final Logger userLogger = LoggerFactory.getLogger(LoggerExample.class);
    static final Logger LOG = LoggerFactory.getLogger(LoggerExample.class);

    public static void main( String[] args )
    {
//        Configurator.setLevel(userLogger.getName(), Level.DEBUG);

//        App.class.getResource("../resources/log4j2.xml");
//        System.setProperties("log4j.configurationFile", "../resource/log4j2.xml");

//        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.INFO);
//        root.debug("hello world");

        for (int i = 0; i < 10; i++) {
            LOG.debug("slf4j: {}", i);
            LOG.info("slf4j: {}", i);
        }

        Integer x,y,z;
        String function = null;

        for (int i = 0; i < 100; i++){

            x = (int) (Math.random() * 1000);
            y = (int) (Math.random() * 2);

            try {
                switch ((int)(Math.random() * 4)){
                    case 3:
                        function = x +"/"+ y +" = ";
                        z = x / y;
                        break;
                    case 2:
                        function = x +"*"+ y +" = ";
                        z = x * y;
                        break;
                    case 1:
                        function = x +"-"+ y +" = ";
                        z = x - y;
                        break;
                    default:
                        function = x +"+"+ y + " = ";
                        z = x + y;
                        break;
                }

                userLogger.info(i + " " + function + z);
//                userLogger.warn(i + " " + function + z);

            } catch (Exception e) {
//                e.printStackTrace();
                userLogger.error(i + " " + function + e.getMessage());

                rootLogger.error(i + " " + function + e.getMessage());
//                rootLogger.fatal(function + e.getMessage());
            }
        }



        Configurator.setLevel(userLogger.getName(), Level.INFO);
        userLogger.info("Информирование 1");
        userLogger.warn("Предупреждение 1");
        userLogger.error("Ошибка 1");

        Configurator.setLevel(userLogger.getName(), Level.WARN);
        userLogger.info("Информирование 2");
        userLogger.warn("Предупреждение 2");
        userLogger.error("Ошибка 2");

//        Configurator.setLevel(System.getProperty(userLogger.getClass().getName()), Level.INFO);
        Configurator.setLevel(userLogger.getName(), Level.ERROR);
        userLogger.info("Информирование 3");
        userLogger.warn("Предупреждение 3");
        userLogger.error("Ошибка 3");



        userLogger.error("Сообщение параметр1 = {}, параметр2 = {}", 11, 22);

        test(10, 0);


    }

    private static void test(int a, int b){
        try {
            System.out.println(a / b);
        } catch (Exception e) {
            e.printStackTrace();
            userLogger.error("Exception", e);

/*
            userLogger.error("Exception: "
                    + e
                    + Arrays.asList(e.getStackTrace())
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining("\n"))
            );
*/

        }
    }
}


/*
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        LoggerConfig specificConfig = loggerConfig;

        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
*/
