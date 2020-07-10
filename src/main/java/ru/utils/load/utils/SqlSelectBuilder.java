package ru.utils.load.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.utils.db.DBService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SqlSelectBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SqlSelectBuilder.class);
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    private final String deletereasonsIgnored = "'process instance deleted'" +
            ",'terminate end event'" +
            ",'boundary event'" +
            ",'event subprocess'" +
            ",'event based gateway cancel'" +
            ",'transaction canceled'";

    public SqlSelectBuilder() {
    }


    /**
     * Общее количество записей в Jobs
     *
     * @return
     */
    public String getCountJobsAll() {
        return getCountJobsAll("");
    }

    /**
     * Общее количество записей в Jobs
     *
     * @param key
     * @return
     */
    public String getCountJobsAll(String key) {

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
     * BpmsJobEntityImpl
     * BpmsTimerJobEntityImpl
     * RetryPolicyJobEntityImpl
     *
     * @param table
     * @return
     */
    public String getCountJobs(String table) {
        return getCountJobs(table, "");
    }

    /**
     * Количество записей в таблице
     * BpmsJobEntityImpl
     * BpmsTimerJobEntityImpl
     * RetryPolicyJobEntityImpl
     *
     * @param table
     * @param key
     * @return
     */
    public String getCountJobs(String table, String key) {
        String lockowner;
        if (table.equalsIgnoreCase("BpmsJobEntityImpl")) {
            lockowner = "LOCKOWNER";
        } else {
            lockowner = "BPMSLOCKOWNER";
        }
        String sql = "select count(1) as cnt, " +
                "NVL(JobType, 'none') as JobType, " +
                "NVL(" + lockowner + ",'none') as host " +
                "from " + table + " j " +
                (key != null && !key.isEmpty() ?
                        "join  pdi on pdi.id = j.processdefinitionid and pdi.key = '" + key + "'" :
                        "") +
                " group by JobType, " + lockowner;

        LOG.debug("Количество записей в {}...\n{}", table, sql);
        return sql;
    }


    /**
     * Количество процессов с ошибками
     *
     * @param key
     * @return
     */
    public String getCountFailed(String key) {

        String sql = "select count(pi.id) as cnt\n" +
                "from  pi \n" +
                "join  pd on pd.id = pi.processdefinitionid \n" +
                "where pi.starttime > to_timestamp('{startTimeBegin}','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and pi.endtime between to_timestamp('{startTime}','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('{stopTime}','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and pi.processdefinitionkey = '" + key + "'\n" +
                "and pi.PROCESSSTATE = 'FAILED'";
//                "and pi.deleteReason is not null \n" +
//                "and NVL(pi.deletereason, ' ') not in (" + deletereasonsIgnored + ")";

        LOG.debug("Количество процессов с ошибками...\n{}", sql);
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

        String sql = "with process as (select\n" +
                "level as lvl,\n" +
                "connect_by_root (pi.processinstanceid) as root_process_id,\n" +
                "connect_by_root (pd.name) as root_process_name,\n" +
                "connect_by_root (pi.durationinmillis) as root_process_duration,\n" +
                "pd.name as process_name,\n" +
                "pi.durationinmillis as process_duration,\n" +
                "pi.processstate,\n" +
                "pi.processinstanceid\n" +
                "from  pi \n" +
                "join  pd on pd.id = pi.processdefinitionid \n" +
                "start with pi.superprocessinstanceid is null\n" +
                "and pi.starttime between to_timestamp('" + sdf1.format(startTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                "and to_timestamp('" + sdf1.format(stopTime) + "','DD/MM/YYYY HH24:MI:SS.FF')\n" +
                (key.isEmpty() ? "" : "and pi.processdefinitionkey = '" + key + "'\n") + // задан processdefinitionkey
                "connect by prior pi.id = pi.superprocessinstanceid\n" +
                ") \n" +
                "select distinct\n" +
                "p.processstate,\n" +
                "p.root_process_name,\n" +
                "min(p.root_process_duration) as root_process_min,\n" +
                "max(p.root_process_duration) as root_process_max,\n" +
                "round(avg(p.root_process_duration),3) as root_process_avg,\n" +
                "p.process_name,\n" +
                "NVL(pa.activityname, ' ') as activityname,\n" +
                "count(pa.ID) as count,\n" +
                "min(pa.durationinmillis) as min,\n" +
                "max(pa.durationinmillis) as max,\n" +
                "round(avg(pa.durationinmillis),3) as avg,\n" +
                "count(ret.id) as RetryCount \n" +
                "from process p\n" +
                "join pa on pa.processinstanceid = p.processinstanceid\n" +
                "left join ret on ret.EXECUTIONID = pa.EXECUTIONID\n" +
                "and ret.MMTTASKID = pa.ACTIVITYID\n" +
                "and ret.FACTSTARTDATE is not null\n" +
                "where pa.calledprocessinstanceid is null\n" +
                "group by p.processstate, p.root_process_name, p.process_name, pa.activityname\n" +
                "order by processstate, root_process_name, process_name, activityname";

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
     *
     * @param key
     * @param dbService
     * @return
     */
    public String getProcessDefinitionName(String key, DBService dbService) {
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
