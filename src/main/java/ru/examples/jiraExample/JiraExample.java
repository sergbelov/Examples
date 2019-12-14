package ru.examples.jiraExample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.examples.jiraExample.utils.jira.api.json.Credential;
import ru.examples.jiraExample.utils.jira.api.json.FullUser;
import ru.examples.jiraExample.utils.jira.api.json.SessionData;
import ru.examples.jiraExample.utils.jira.api.Command;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class JiraExample
{
    private static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");
    private static String URL_BASE = "http://jira.org.ru";
    private static int MIN_PROC = 80;
    private static String TEAM = "496";
    private static String TEAM_NAME;

    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static DateFormat dateFormatTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static DateFormat dateFormatYMD = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat dateFormatMY = new SimpleDateFormat("MMyy");

    private static String dateFromYMD;
    private static String dateToYMD;
    private static String dateFromDMY;
    private static String dateToDMY;


    public static void main( String[] args )
    {

        String userName = "userName";
        String userPass = "userPassword";

        try {
            ObjectMapper mapper = new ObjectMapper();
            RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(new Credential(userName, userPass)));

            Request request = new Request.Builder()
                    .url(URL_BASE + Command.SESSION.getPath())
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                SessionData data = mapper.readValue(response.body().string(), SessionData.class);

                HttpUrl url = HttpUrl.parse(URL_BASE + Command.USER.getPath()).newBuilder()
                        .addQueryParameter("username", userName)
//                        .addQueryParameter("username", "Simashev-AV")
//                        .addQueryParameter("username", "Kaydash-IV")
                        .build();


                Request requestUser = new Request.Builder()
                        .url(url)
                        .header("Cookie", data.getSession().getName() + '=' + data.getSession().getValue())
                        .build();

                System.out.println(requestUser);

                Response responseUser = client.newCall(requestUser).execute();
                if (responseUser.isSuccessful()) {
                    System.out.println(responseUser.body().toString());
                    FullUser fullUser = mapper.readValue(responseUser.body().string(), FullUser.class);

                    System.out.println(fullUser.getDisplayName());
                    System.out.println(fullUser);

                    TEAM = "496";
                    analysisTeam(data, client);

                } else {
                    System.out.println(ImmutableMap.<String, Object>builder());
                }

                //mav = createModel("desktop", ImmutableMap.<String, Object>builder().put("jira", data.toString()).build());
            } else {
                String errorMessage = response.code() == 401 ? "Неверное имя пользователя/пароль." : "Отказано в доступе. Код = " + response.code();
                System.out.println(Optional.of(errorMessage));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }


    /*
    анализируем списания по команде (перебор сотрудников)
     */
    private static void analysisTeam(
            SessionData data,
            OkHttpClient client) {

        Calendar periodStartDate = Calendar.getInstance();
        periodStartDate.add(Calendar.DAY_OF_MONTH, -7); // анализируем предыдущую неделю

        HttpUrl url = HttpUrl.parse(URL_BASE + Command.TIMESHEET_APPROVAL.getPath()).newBuilder()
                .addQueryParameter("teamId", TEAM)
                .addQueryParameter("periodStartDate", dateFormatYMD.format(periodStartDate.getTime()).toString())
                .build();

System.out.println(url);

        Request requestTimeSheet = new Request.Builder()
                .url(url)
                .header("Cookie", data.getSession().getName() + '=' + data.getSession().getValue())
                .build();

        Response responseTimeSheet = null;
        try {
            responseTimeSheet = client.newCall(requestTimeSheet).execute();

            if (responseTimeSheet.isSuccessful()) {

                String responseString = responseTimeSheet.body().string();
                JSONObject jsonTeam = new JSONObject(responseString);

                TEAM_NAME = jsonTeam.getJSONObject("team").getString("name");
                dateFromYMD = jsonTeam.getJSONObject("period").getString("dateFrom");
                dateToYMD = jsonTeam.getJSONObject("period").getString("dateTo");

                System.out.println(TEAM_NAME+" "+
                dateFromYMD+" "+
                dateToYMD);

                try {
                    Date dateFromDMY = dateFormatYMD.parse(dateFromYMD);
                    Date dateTODMY = dateFormatYMD.parse(dateToYMD);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONArray jsonTeamArray = jsonTeam.getJSONArray("approvals");

// перебираем сотрудников
                for (int i = 0; i < jsonTeamArray.length(); i++) {
                    analysisUser(jsonTeamArray.getJSONObject(i));
                }
            }
            else{
                String errorMessage = "teamId="
                        .concat(TEAM)
                        .concat(responseTimeSheet.body().string());

                System.out.println(errorMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /*
    анализируем списания по сотруднику
    */
    private static void analysisUser(JSONObject jsonUser) {

        try {
            String userName = jsonUser.getJSONObject("user").getString("name");
            String userDisplayName = jsonUser.getJSONObject("user").getString("displayName");
            double requiredHours = jsonUser.getDouble("requiredSeconds") / 3600;
            double workedHours = jsonUser.getDouble("workedSeconds") / 3600;
            double proc = workedHours * 100 / requiredHours;

            if (proc < MIN_PROC) {
                System.out.println(
                        userName + "\t" +
                                userDisplayName + "\t" +
                                requiredHours + "\t" +
                                workedHours + "\t" +
                                proc
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
