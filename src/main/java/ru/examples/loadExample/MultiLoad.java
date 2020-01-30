package ru.examples.loadExample;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import ru.utils.load.ScriptRun;
import ru.utils.load.utils.MultiRunService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MultiLoad implements ScriptRun {
    private static final Logger LOG = LogManager.getLogger(MultiLoad.class);

    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");

    private MultiRunService multiRunService = new MultiRunService();


    public void init() {
        multiRunService.init("MultiLoad_Template");
    }

    public void end() throws Exception {
        multiRunService.end();
    }

    public void action() throws Exception {

/*
// отладка графика VU
        testStartTime = 1579857384121L;
        vuList.add(new DateTimeValue(1579857384121L,10));
        vuList.add(new DateTimeValue(1579857389152L,20));
        vuList.add(new DateTimeValue(1579857394159L, 30));
        vuList.add(new DateTimeValue(1579857399190L, 40));
        vuList.add(new DateTimeValue(1579857404179L, 50));
        vuList.add(new DateTimeValue(1579857409188L, 60));
        vuList.add(new DateTimeValue(1579857414189L, 70));
        vuList.add(new DateTimeValue(1579857424213L, 90));
        vuList.add(new DateTimeValue(1579857429217L, 100));
        vuList.add(new DateTimeValue(1579857434216L, 110));
        vuList.add(new DateTimeValue(1579857439220L, 120));
        vuList.add(new DateTimeValue(1579857444221L, 130));
        vuList.add(new DateTimeValue(1579857449229L, 140));
        vuList.add(new DateTimeValue(1579857454233L, 150));
        vuList.add(new DateTimeValue(1579857459244L, 160));
        vuList.add(new DateTimeValue(1579857464247L, 170));
        vuList.add(new DateTimeValue(1579857469260L, 180));
        vuList.add(new DateTimeValue(1579857474263L, 190));
        vuList.add(new DateTimeValue(1579857479276L, 200));
        vuList.add(new DateTimeValue(1579857484308L, 210));
        vuList.add(new DateTimeValue(1579857489287L, 220));
        vuList.add(new DateTimeValue(1579857494303L, 230));
        vuList.add(new DateTimeValue(1579857499319L, 240));
        vuList.add(new DateTimeValue(1579857504334L, 250));
        vuList.add(new DateTimeValue(1579857509349L, 260));
        vuList.add(new DateTimeValue(1579857514350L, 270));
        vuList.add(new DateTimeValue(1579857519355L, 280));
        vuList.add(new DateTimeValue(1579857524357L, 290));
        vuList.add(new DateTimeValue(1579857529365L, 300));

        fileUtils2.saveResultToFile(graph.getGraphVU(testStartTime, vuList), "Reports/GraphVU_123.html");
        if (1==1) {
            return;
        }
*/

/*
// отладка графика TPC
        long testStartTime = 1579872790496L - 5000;
        List<DateTimeValue> tpcList = new ArrayList<>();
        tpcList.add(new DateTimeValue(1579872790496L, Arrays.asList(50, 11, 30)));
        tpcList.add(new DateTimeValue(1579872795490L, Arrays.asList(11, 12, 11)));
        tpcList.add(new DateTimeValue(1579872800505L, Arrays.asList(12, 13, 13)));
        tpcList.add(new DateTimeValue(1579872805520L, Arrays.asList(13, 14, 12)));
        tpcList.add(new DateTimeValue(1579872810529L, Arrays.asList(1000, 1, 2)));

        tpcList.add(new DateTimeValue(1579872815537L, 33));
        tpcList.add(new DateTimeValue(1579872820552L, 0.70));
        tpcList.add(new DateTimeValue(1579872825567L, 0.80));
        tpcList.add(new DateTimeValue(1579872830570L, 0.90));
        tpcList.add(new DateTimeValue(1579872835583L, 0.100));
        tpcList.add(new DateTimeValue(1579872840584L, 0.100));
        tpcList.add(new DateTimeValue(1579872846887L, 0.006));


        FileUtils fileUtils = new FileUtils();
        Graph graph = new Graph();

        fileUtils.writeFile("Reports/GraphTPC_123.html",
                graph.getSvgGraphLine(
                    "TPC",
                    new String[]{"TPC"},
                    testStartTime,
                    tpcList,
                    false,
                    true,
                    "#009f00"));

        if (1==1) {
            return;
        }

 */

        multiRunService.start(this);
    }


    public static void main(String[] args) throws Exception {
        Configurator.setRootLevel(Level.INFO);
        MultiLoad _multiLoad_ = new MultiLoad();

        _multiLoad_.init();
        _multiLoad_.action();
        _multiLoad_.end();
    }

    /**
     * Вызов API
     */
    public boolean start() {

// иммитация вызова API
        long delay = (long) ((Math.random() * 1900) + 100);
//        delay = 2000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

// имитация возникновения ошибки
        int rnd = (int) (Math.random() * 20);
        if (rnd == 11) {
            String text = "No resources to process message with messageId:\n" +
                    "ThreadPoolSizeConfig(methodConfiguration=MODULE, poolSize=";
            multiRunService.errorListAdd(System.currentTimeMillis(), text);
            if (multiRunService.isStopTestOnError() && multiRunService.getErrorCount() > multiRunService.getCountErrorForStopTest()){ //ToDo
                multiRunService.stop();
            }
            return false;
        }
        return true;
    }
}
