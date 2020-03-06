package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.load.runnable.RunnableDbSelectTransitionsTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SqlSelectBuilder {

    private static final Logger LOG = LogManager.getLogger();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    public SqlSelectBuilder() {
    }

    /**
     * Время затраченное на переходы между задачами процесса
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getTransitionTime(
            String key,
            long startTime,
            long stopTime
    ) {

/*
        String sql = "select \n" +
                "pd.name as MAIN_PROCESS,\n" +
                "pi.ID as MAIN_ID,\n" +
                "pi.DURATIONINMILLIS as MAIN_DUR, \n" +
                "NVL(pa.ACTIVITYNAME,' ') as ACTIVITYNAME, \n" +
                "pa.DURATIONINMILLIS as DUR \n" +
                "from " +
                "join \n" +
                "join \n" +
                "where pi.processdefinitionkey = '" + key + "' " +
                "and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and pi.endtime < to_timestamp('" + sdf1.format(stopTime + 60000) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and pi.PROCESSSTATE = 'COMPLETED'\n" +
                "order by pd.name, pi.id, pa.ACTIVITYNAME";
*/

// все подзадачи процесса (рекурсия)
        String sql = "with process as (\n" +
                "    select\n" +
                "        level as lvl,\n" +
                "        CONNECT_BY_ROOT (pi.processinstanceid) as root_process_id,\n" +
                "        CONNECT_BY_ROOT (pd.name) as root_process_name,\n" +
                "        CONNECT_BY_ROOT (pi.DURATIONINMILLIS) as root_process_duration,\n" +
                "        pi.processinstanceid\n" +
//                "        pi.superprocessinstanceid,\n" +
//                "        pd.name,\n" +
//                "        pi.processstate,\n" +
//                "        pi.starttime as mainStartTime,\n" +
//                "        pi.endtime as mainEndTime,\n" +
//                "        pi.DURATIONINMILLIS as mainDuration\n" +
                "    from \n" +
                "    join \n" +
                "    \n" +
                "    start with pi.superprocessinstanceid is null\n" +
                "      and pi.PROCESSSTATE = 'COMPLETED'\n" +
                (key.isEmpty() ? "" : "      and pi.processdefinitionkey = '" + key + "' ") +
                "      and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "      and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "\n" +
                "    connect by prior pi.id = pi.superprocessinstanceid\n" +
                "    ) \n" +
                "select distinct\n" +
//                "    lvl,\n" +
                "    p.root_process_id,\n" +
                "    p.root_process_name,\n" +
                "    p.root_process_duration,\n" +
//                "    p.superprocessinstanceid,\n" +
//                "    p.processinstanceid,\n" +
//                "    p.name,\n" +
//                "    p.processstate,\n" +
//                "    p.mainStartTime,\n" +
//                "    p.mainEndTime,\n" +
//                "    p.mainDuration,\n" +
//                "    pa.activitytype,\n" +
//                "    NVL(pa.ACTIVITYNAME, ' ') as ACTIVITYNAME,\n" +
                "    pa.starttime as StartTime,\n" +
//                "    pa.endtime as EndTime,\n" +
                "    pa.DURATIONINMILLIS as duration,\n" +
                "    case when pa.CALLEDPROCESSINSTANCEID is null then 1 else 0 end as lastStepInLevel\n" +
                "from process p\n" +
                "join  pa on pa.PROCESSINSTANCEID = p.processinstanceid\n" +
                "order by root_process_id, StartTime";

        LOG.debug("Время затраченное на переходы между задачами процесса...\n{}", sql);
        return sql;
    }


    /**
     * Статистика по длительности выполнения задач...
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getProcessDuration(
            String key,
            long startTime,
            long stopTime
    ){
/*
        String sql = "select\n" +
                "pd.name as MAIN_PROCESS,\n" +
//                "count(pi.ID) as MAIN_COUNT,\n" +
                "min(pi.DURATIONINMILLIS) AS MAIN_MIN,\n" +
                "max(pi.DURATIONINMILLIS) AS MAIN_MAX,\n" +
                "ROUND(AVG(pi.DURATIONINMILLIS)) AS MAIN_AVG,\n" +
                "pi.PROCESSSTATE,\n" +
                "NVL(pa.ACTIVITYNAME,' ') as ACTIVITYNAME,\n" +
                "count(pa.ID) as COUNT,\n" +
                "min(pa.DURATIONINMILLIS) AS MIN,\n" +
                "max(pa.DURATIONINMILLIS) AS MAX,\n" +
                "ROUND(AVG(pa.DURATIONINMILLIS)) AS AVG\n" +
                "from  pi\n" +
                "join \n" +
                "join \n" +
                "where pi.processdefinitionkey = '" + key + "'\n" +
                "and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "group by pi.PROCESSSTATE, pd.name, pa.ACTIVITYNAME\n" +
                "order by PROCESSSTATE, MAIN_PROCESS, ACTIVITYNAME";
*/

        String sql = "with process as (\n" +
                "select\n" +
                "   level as lvl,\n" +
                "   CONNECT_BY_ROOT (pi.processinstanceid) as root_process_id,\n" +
                "   CONNECT_BY_ROOT (pd.name) as root_process_name,\n" +
                "   CONNECT_BY_ROOT (pi.durationinmillis) as root_process_duration,\n" +
                "   pi.processstate,\n" +
                "   pi.processinstanceid\n" +
                "from \n" +
                "join \n" +
                "\n" +
                "start with pi.superprocessinstanceid is null\n" +
                (key.isEmpty() ? "" : "   and pi.processdefinitionkey = '" + key + "'\n") +
                "   and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "   and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "\n" +
                "connect by prior pi.id = pi.superprocessinstanceid\n" +
                ") \n" +
                "select\n" +
                "   p.root_process_name as main_process,\n" +
                "   p.PROCESSSTATE,\n" +
                "   NVL(pa.ACTIVITYNAME, ' ') as activityname,\n" +
                "   count(pa.ID) as count,\n" +
                "   min(pa.DURATIONINMILLIS) as min,\n" +
                "   max(pa.DURATIONINMILLIS) as max,\n" +
                "   round(avg(pa.DURATIONINMILLIS),3) as avg\n" +
                "from process p\n" +
                "join \n" +
                "where pa.CALLEDPROCESSINSTANCEID is null\n" +
                "group by p.PROCESSSTATE, p.root_process_name, pa.ACTIVITYNAME\n" +
                "order by PROCESSSTATE, root_process_name, ACTIVITYNAME";

        LOG.debug("Статистика по длительности выполнения задач...\n", sql);
        return sql;
    }


    /**
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String get2(
            String key,
            long startTime,
            long stopTime
    ){
        String sql = "select\n" +
                "hpi.PROCESSSTATE, " +
                "to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS') as sec, " +
                "count(hpa.id) as cnt\n" +
                "from \n" +
                "join \n" +
                "join \n" +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and hpi.PROCESSSTATE = 'COMPLETED'\n" +
                "group by to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS'), hpi.PROCESSSTATE\n" +
                "order by 2, 1";

        LOG.debug("Статистика по длительности выполнения задач...\n", sql);
        return sql;
    }
}
