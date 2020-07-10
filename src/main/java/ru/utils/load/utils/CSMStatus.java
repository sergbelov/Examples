package ru.utils.load.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CSMStatus {

    private static final Logger LOG = LoggerFactory.getLogger(CSMStatus.class);

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
//                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
                }
            }
    };

    public static void main(String[] args) {

        String urlBase = "https://";


        CSMStatus csmStatus = new CSMStatus();
        List<ModuleStatus> moduleStatusList = csmStatus.getInfo(urlBase);
        moduleStatusList.forEach(x -> {
            LOG.info("{} {} {} {}",
                    x.getHost(),
                    x.getModule(),
                    x.getVersion(),
                    x.getEnabled());
        });


    }


    /**
     * Получение информации из CSM
     * @param urlCsm
     * @return
     */
    public List<ModuleStatus> getInfo(String urlCsm) {
        List<ModuleStatus> moduleStatusList = new ArrayList<>();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient()
                    .newBuilder()
                    .sslSocketFactory(sslSocketFactory)

//                    .hostnameVerifier(new NoopHostnameVerifier())
                    .hostnameVerifier(new BrowserCompatHostnameVerifier())

                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();

            Request request = new Request.Builder()
                    .url(urlCsm)
                    .build();

            Response response = client.newCall(request).execute();
            JSONArray jsonArray = new JSONArray(response.body().string());
            for (int h = 0; h < jsonArray.length(); h++) {
                JSONObject jsonObjectHost = jsonArray.getJSONObject(h);
                LOG.trace("{}", jsonObjectHost.toString());

                String host = jsonObjectHost.getString("host");
                String module = jsonObjectHost.getJSONObject("module").getString("normalName");
                String version = jsonObjectHost.getJSONObject("module").getString("version");
                boolean enabled = jsonObjectHost.getJSONObject("module").getBoolean("enabled");
                LOG.debug("{} {} {} {}", host, module, version, enabled);

                if (!findModule(moduleStatusList, host, module, version, enabled)){ // исключаем дубли
                    moduleStatusList.add(new ModuleStatus(
                            host,
                            module,
                            version,
                            enabled));
                }
            }
        } catch (Exception e) {
            LOG.error("Ошибка при получении данных {}\n", urlCsm, e);
        }

        return moduleStatusList;
    }

    /**
     * Получение информации из CSM
     * @param urlCsm
     * @return
     */
    public List<ModuleStatus> getInfo1(String urlCsm){
        List<ModuleStatus> moduleStatusList = new ArrayList<>();
/*
        try {
            URL url = new URL(csmUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Encoding", "UTF-8");
            connection.setDoOutput(true);
            InputStream content = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line;
            StringBuilder data = new StringBuilder();
            while ((line = in.readLine()) != null) {
                data.append(line);
            }

            LOG.debug("CSM Response: {}", data.toString());
            JSONArray jsonArray = new JSONArray(data.toString());
            for (int h = 0; h < jsonArray.length(); h++) {
                JSONObject jsonObjectHost = jsonArray.getJSONObject(h);
                LOG.debug("CSM Response[{}]: {}", h, jsonObjectHost.toString());

                String host = jsonObjectHost.getString("host");
                String module = jsonObjectHost.getJSONObject("module").getString("normalName");
                String version = jsonObjectHost.getJSONObject("module").getString("version");
                boolean enabled = jsonObjectHost.getJSONObject("module").getBoolean("enabled");

                if (!findModule(moduleStatusList, host, module, version, enabled)){ // исключаем дубли
                    moduleStatusList.add(new ModuleStatus(
                            host,
                            module,
                            version,
                            enabled));
                }
            }

        } catch (Exception e) {
            LOG.error("Ошибка при получении данных {}\n", urlCsm, e);
        }

 */
        return moduleStatusList;
    }


    /**
     * Информация уже имеется
     * @param moduleStatusList
     * @param host
     * @param module
     * @param version
     * @param enabled
     * @return
     */
    private boolean findModule(
            List<ModuleStatus> moduleStatusList,
            String host,
            String module,
            String version,
            boolean enabled){

        for (ModuleStatus moduleStatus : moduleStatusList){
            if (moduleStatus.getHost().equalsIgnoreCase(host) &&
            moduleStatus.getModule().equalsIgnoreCase(module) &&
            moduleStatus.getVersion().equalsIgnoreCase(version) &&
            moduleStatus.getEnabled() == enabled){
                return true;
            }
        }
        return false;
    }

    /**
     * Информация по модулю
     */
    class ModuleStatus {
        String host;
        String module;
        String version;
        boolean enabled;

        public ModuleStatus(
                String host,
                String module,
                String version,
                boolean enabled) {
            this.host = host;
            this.module = module;
            this.version = version;
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public String getModule() {
            return module;
        }

        public String getVersion() {
            return version;
        }

        public boolean getEnabled() {
            return enabled;
        }
    }
}
