package ru.examples.httpExample;

import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
//import org.apache.commons.codec.binary.Base64;

public class HttpExample {

    public static void main(String[] args) throws UnsupportedEncodingException {

        String user = "user";
        String pass = "pass";

        String encoding = Base64.getEncoder().encodeToString((user+":"+pass).getBytes("UTF-8"));
        System.out.println(encoding);
        System.out.println();

        Map<String, String> mapHeaders = new LinkedHashMap<>();
        mapHeaders.put("header1", "value1");
        mapHeaders.put("header3", "value3");
        mapHeaders.put("header2", "value2");


        Headers headers = Headers.of(mapHeaders);

        Headers headers2 = new Headers.Builder()
                .add("header1", mapHeaders.get("header1"))
                .add("header2", mapHeaders.get("header2"))
                .build();

        System.out.println(headers);
        System.out.println();
        System.out.println(headers2);


        Map<String, String> mapFormBody = new LinkedHashMap<String, String>(){{
            put("userName", "userName");
            put("password", "passwrord");
        }};
        System.out.println("userName: " + mapFormBody.get("userName"));

        mapFormBody.put("userName", "userNameNew");
        System.out.println("userName: " + mapFormBody.get("userName"));

        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : mapFormBody.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = builder.build();



/*
        try {
            URL url = new URL ("http://ip:port/login");
            String encoding = Base64.getEncoder().encodeToString(("test1:test1").getBytes(‌"UTF‌​-8"​));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = connection.getInputStream();
            BufferedReader in = new BufferedReader (new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }


        String urlBase = "http://localhost";

        String outFilePath = "C:/TEMP/";
        try {
            // URL url = new URL ("http://ip:port/download_url");
            URL url = new URL(urlBase);
            String authStr = user + ":" + pass;
            String authEncoded = Base64.getEncoder().encode(authStr.getBytes());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

            File file = new File(outFilePath);
            InputStream in = (InputStream) connection.getInputStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            for (int b; (b = in.read()) != -1;) {
                out.write(b);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
*/
/*
        try {
            DefaultHttpClient Client = new DefaultHttpClient();
            Client.getCredentialsProvider().setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("user1", "123456789"));

            HttpGet httpGet = new HttpGet("http://10.10.151.90/default.aspx");
            HttpResponse response = Client.execute(httpGet);

            System.out.println("response = " + response);

            BufferedReader breader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseString = new StringBuilder();
            String line = "";
            while ((line = breader.readLine()) != null) {
                System.out.println(line);
                responseString.append(line);
            }

            breader.close();
            String responseStr = responseString.toString();
            System.out.println("responseStr = " + responseStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }
}


/*
    public boolean connect(
            String urlBase,
            String referer,
            String userName,
            String userPass,
            String userNameField,
            String userPassField) {

//        LOG.trace("{} {}", userName, userPass);

        boolean r = false;
        try {
            String credential = Credentials.basic(userName, userPass);

            Headers headers = new Headers.Builder()
                    .add("Referer", referer)
                    .add("Authorization", credential)
                    .build();

            RequestBody requestBody = new FormBody.Builder()
                    .add(userNameField, userName)
                    .add(userPassField, userPass)
                    .build();

            Request request = new Request.Builder()
                    .url(urlBase )//+ Command.LOGIN.getPath())
                    .headers(headers)
                    .addHeader("content-type", "application/json")
                    .post(requestBody)
                    .build();

//            LOG.trace("########## Авторизация...");
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.code() == 200) {
//                cookie = response.headers().get("Set-Cookie");
//                LOG.debug("Авторизация - Ок: {}", cookie);

//                r = getTokenAndSession(); // получим токкен и ID сессии
            } else {
//                LOG.error("Ошибка при подключении к {}: {}; {}", URL_BASE, response.code(), response.message());
            }
            response.close();
        } catch (IOException e) {
//            LOG.error(e);
        }

        return r;
    }
*/
