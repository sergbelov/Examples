package ru.examples.loadExample;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import ru.utils.load.ScriptRun;
import ru.utils.load.gui.GuiControl;
import ru.utils.load.utils.MultiRun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


/**
 * Вызов API в несколько потоков
 * нагрузка регулируется параметрами
 * !!! не запускать из PC
 * -Dltf.logger.level=info
 *
 * @author Belov Sergey
 */
public class MultiLoad implements ScriptRun {
    private static final Logger LOG = LogManager.getLogger(MultiLoad.class);
    private MultiRun multiRun = new MultiRun();

    public void init() throws Exception {
//        initialize();
        multiRun.init("MultiLoad");
    }

    public void end() throws Exception {
        multiRun.end();
    }

    /**
     * Основное действие скрипта.
     */
    public void action() throws Exception {
        GuiControl guiControl = new GuiControl(multiRun);
        multiRun.start(this);
    }



    public static void main(String[] args) throws Exception {
        Configurator.setRootLevel(Level.INFO);
        MultiLoad multiLoad = new MultiLoad();
        multiLoad.init();
        multiLoad.action();
        multiLoad.end();
        System.exit(0);
    }

    /**
     * Запуск API по номеру
     * @param apiNum
     * @return
     */
    public void start(int apiNum) throws Exception{
        switch (apiNum){
            case 0:
                start0(apiNum);
                break;
            case 1:
                start1(apiNum);
                break;
            case 2:
                start2(apiNum);
                break;
            default:
                LOG.warn("Не задана API под номером {}", apiNum);
        }
    }

    /**
     * Вызов API 0
     */
    public void start0(int apiNum) throws Exception {
        String processName = multiRun.getMultiRunService(apiNum).getKeyBpm();

// иммитация вызова API
        long delay = (long) (Math.random() * (1000 + multiRun.getMultiRunService(apiNum).getVuCount()));
//        delay = 2000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

// имитация возникновения ошибки
        int rnd = (int) (Math.random() * (multiRun.getMultiRunService(apiNum).getVuCount() * 1000));
//        rnd = 1;
        if (rnd == 11) { // типо ошибка
//        if (rnd % 2 == 0) { // типо ошибка
            String text = "No resources to process message with messageId:\n" +
                    "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize=";
            throw new Exception(text);
        }

    }


    /**
     * Вызов API 1
     */
    public void start1(int apiNum) throws  Exception{
        String processName = multiRun.getMultiRunService(apiNum).getKeyBpm();

// иммитация вызова API
        long delay = (long) (Math.random() * (2000 + multiRun.getMultiRunService(apiNum).getVuCount()));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

// имитация возникновения ошибки
        int rnd = (int) (Math.random() * (multiRun.getMultiRunService(apiNum).getVuCount() * 20));
//        rnd = 1;
        if (rnd == 11) { // типо ошибка
//        if (rnd % 2 == 0) { // типо ошибка
            String text = "No resources to process message with messageId:\n" +
                    "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize=";
            throw new Exception(text);
        }
    }

    /**
     * Вызов API 2
     */
    public void start2(int apiNum) throws Exception{
        String processName = multiRun.getMultiRunService(apiNum).getKeyBpm();
        int rnd = (int) (Math.random() * (multiRun.getMultiRunService(apiNum).getVuCount() * 10));
        if (rnd == 5) { // типо ошибка
            String text = "No resources to process message with messageId:\n" +
                    "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize=";
            throw new Exception(text);
        }

    }

}
