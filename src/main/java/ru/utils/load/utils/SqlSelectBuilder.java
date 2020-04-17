package ru.utils.load.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.utils.db.DBService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SqlSelectBuilder {

    private static final Logger LOG = LogManager.getLogger();
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    public SqlSelectBuilder() {
    }


    /**
     * Общее количество записей в Jobs
     * @return
     */
    public String getCountJobEntityImplAll() {
        return getCountJobEntityImplAll("");
    }
    /**
     * Общее количество записей в Jobs
     *
     * @param key
     * @return
     */
    public String getCountJobEntityImplAll(String key) {
/*
        String sql = "select \n " +
                "count(j1.processdefinitionid) + count(j2.processdefinitionid) + count(j3.processdefinitionid) as cnt,\n " +
                "count(j1.processdefinitionid) as JobCount,\n " +
                "count(j2.processdefinitionid) as TimerJobCount,\n " +
                "count(j3.processdefinitionid) as RetryPolicyJobCount\n " +
                "from \n " +
                "left join \n " +
                "left join \n " +
                "left join \n " +
                (key != null && !key.isEmpty() ? "where pdi.key = '" + key + "'\n"  : "" );
*/

        String sql = "select \n" +
                "(select count(1) from ) as JobCount,\n" +
                "(select count(1) from ) as TimerJobCount,\n" +
                "(select count(1) from ) as RetryPolicyJobCount \n" +
                "from dual";
        LOG.debug("Общее количество записей в Jobs...\n{}", sql);
        return sql;
    }

    /**
     * Количество записей в таблице
     *      BpmsJobEntityImpl
     *      BpmsTimerJobEntityImpl
     *      RetryPolicyJobEntityImpl
     *
     * @param table
     * @return
     */
    public String getCountJobEntityImpl(String table) {
        return getCountJobEntityImpl(table, "");
    }
    /**
     * Количество записей в таблице
     *      BpmsJobEntityImpl
     *      BpmsTimerJobEntityImpl
     *      RetryPolicyJobEntityImpl
     *
     * @param table
     * @param key
     * @return
     */
    public String getCountJobEntityImpl(String table, String key) {
        String sql = "select count(1) as cnt " +
                "from BPMS." + table + " j " +
                (key != null && !key.isEmpty() ?
                        "join pdi on pdi.id = j.processdefinitionid " +
                        "and pdi.key = '" + key + "'" :
                        "");
        LOG.debug("Количество записей в {}...\n{}", table, sql);
        return sql;
    }

    /**
     * Запросы для очистки очередей
     *
     * @return
     */
    public String getClearRunningProcess() {
        String sql = "-- Очистка очереди (выполнять пока очереди не станут пустыми)\n" +
                "delete from ;\n" +
                "delete from ;\n" +
                "delete from ;\n" +
                "-- Не нужно чистить delete from ;\n" +
                "--update  set processstate = 'FAILED' where processstate = 'RUNNING'; \n" +
                "--commit;";
        LOG.debug("Запросы для очистки очереди...\n{}", sql);
        return sql;
    }

    /**
     * Процессы
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getProcesses(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select hpi.starttime, hpi.endtime, hpi.processstate " +
                "from  hpi " +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')";

        LOG.debug("Процессы...\n{}", sql);
        return sql;
    }

    /**
     * Статистика по статусам из БД
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getProcessesState(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select pdi.key, " +
                "hpi.processstate, " +
                "count(1) as cnt " +
                "from  hpi " +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "group by pdi.key, hpi.processstate";

        LOG.debug("Статистика по статусам {} - {}...\n{}",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }

    /**
     * Длительность выполнения процесса (информация из БД)
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getProcessesDuration(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select pdi.key, " +
                "count(1) as cnt, " +
                "min(hpi.DURATIONINMILLIS), " +
                "max(hpi.DURATIONINMILLIS), " +
                "avg(hpi.DURATIONINMILLIS) " +
                "from  hpi " +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "' " +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and hpi.processstate = 'COMPLETED' " +
                "group by pdi.key";

        LOG.debug("Длительность выполнения {} - {}...\n{}",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }

    /**
     * Время затраченное на переходы между задачами процесса
     *
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
                "from  pi " +
                "join  pa on pa.PROCESSINSTANCEID = pi.id \n" +
                "join  pd on pd.id = pi.processdefinitionid \n" +
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
                "    from  pi \n" +
                "    join  pd on pd.id = pi.processdefinitionid \n" +
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

        LOG.debug("Время затраченное на переходы между задачами процесса {} - {}...\n{}",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }


    /**
     * Длительность выполнения задач
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getTaskDuration(
            String key,
            long startTime,
            long stopTime
    ) {
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
                "join  pa on pa.PROCESSINSTANCEID = pi.id\n" +
                "join  pd on pd.id = pi.processdefinitionid\n" +
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
                "from  pi \n" +
                "join  pd on pd.id = pi.processdefinitionid \n" +
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
                "join  pa on pa.PROCESSINSTANCEID = p.processinstanceid\n" +
                "where pa.CALLEDPROCESSINSTANCEID is null\n" +
                "group by p.PROCESSSTATE, p.root_process_name, pa.ACTIVITYNAME\n" +
                "order by PROCESSSTATE, root_process_name, ACTIVITYNAME";

        LOG.debug("Длительность выполнения задач {} - {}...\n",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }


    public String getStepStopInSec(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select\n" +
                "hpi.PROCESSSTATE, " +
                "to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS') as sec, " +
                "count(hpa.id) as cnt\n" +
                "from  hpi\n" +
                "join  hpa on hpa.PROCESSINSTANCEID = hpi.id\n" +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "'\n" +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and hpi.PROCESSSTATE = 'COMPLETED'\n" +
                "group by to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS'), hpi.PROCESSSTATE\n" +
                "order by 2, 1";

        LOG.debug("Количество шагов завершенных в секунду {} - {}...\n{}",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }

    /**
     * Поиск дублей
     *
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String getDuplicateCheck(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select distinct " +
                "PROCESSDEFINITIONKEY, " +
                "PROCESSINSTANCEID, " +
                "ACTIVITYID, " +
                "min(STARTTIME) as STARTTIME, " +
                "count(1) as cnt\n" +
                "from  hai\n" +
                "where hai.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "group by PROCESSDEFINITIONKEY, processinstanceid, ACTIVITYID, EXECUTIONID, ACTIVITYNAME\n" +
                "having count(1) > 1\n" +
                "order by 4";

        LOG.debug("Поиск дублей {} - {}...\n{}",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }


    /**
     * @param key
     * @param startTime
     * @param stopTime
     * @return
     */
    public String get2(
            String key,
            long startTime,
            long stopTime
    ) {
        String sql = "select\n" +
                "hpi.PROCESSSTATE, " +
                "to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS') as sec, " +
                "count(hpa.id) as cnt\n" +
                "from  hpi\n" +
                "join  hpa on hpa.PROCESSINSTANCEID = hpi.id\n" +
                "join  pdi on pdi.id = hpi.processdefinitionid and pdi.key = '" + key + "'\n" +
                "where hpi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF') " +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
//                "and hpi.PROCESSSTATE = 'COMPLETED'\n" +
                "group by to_char(hpa.endtime,'DD-MM-YYYY HH24:MI:SS'), hpi.PROCESSSTATE\n" +
                "order by 2, 1";

        LOG.debug("Длительность выполнения задач {} - {}...\n",
                sdf1.format(startTime),
                sdf1.format(stopTime),
                sql);

        return sql;
    }

    /**
     * Имя процесса по id
     * @param key
     * @param dbService
     * @return
     */
    public String getProcessDefinitionName(String key, DBService dbService){
        String res = null;
        if (dbService != null) {
            String sql = "select name " +
                    "from  " +
                    "where key = '" + key + "' " +
                    "and rownum = 1 " +
                    "order by version desc";
            try {
                Connection connection = dbService.getConnection();
                Statement statement = dbService.createStatement(connection);
                ResultSet resultSet = dbService.executeQuery(statement, sql);
                if (resultSet.next()) {
                    res = resultSet.getString("name");
                }
                resultSet.close();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
