package ru.utils.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Map.Entry.comparingByKey;

/**
 * Created by Сергей on 19.05.2018.
 */
public class PropertiesService {
    private static final Logger LOG = LogManager.getLogger();
    private String fileName;
    private boolean addKey;
    private Map<String, String> propertyMap;

    public PropertiesService() {
        this.addKey = true; // список параметров из файла
        this.propertyMap = new LinkedHashMap<String, String>();
    }

    public PropertiesService(Map<String, String> propertyMap) {
        this.addKey = false; // список параметров задан
        this.propertyMap = propertyMap;
    }

    public PropertiesService(String fileName) {
        this.addKey = true; // список параметров из файла
        this.propertyMap = new LinkedHashMap<String, String>();
        readProperties(fileName);
    }

    public PropertiesService(String fileName, Map<String, String> propertyMap) {
//        this.addKey = false; // список параметров задан
        this.addKey = true;
        this.propertyMap = propertyMap;
        readProperties(fileName);
    }


    public void readProperties(String fileName, Level level) {
        Configurator.setLevel(LOG.getName(), level);
        readProperties(fileName);
    }

    public void readProperties(String fileName) {
        this.fileName = fileName;
        StringBuilder report = new StringBuilder();
        report
                .append("Параметры из файла ")
                .append(fileName)
                .append(":");

        boolean fileExists = false;
        File file = new File(fileName);
        if (file.exists()) { // найден файл с параметрами
            StringBuilder reportTrace = new StringBuilder();
            reportTrace
                    .append("Параметры в файле ")
                    .append(fileName)
                    .append(":");

            Properties properties = new Properties();
            try (InputStream inputStream = new FileInputStream(file)) {
                properties.load(inputStream);

                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    reportTrace
                            .append("\r\n\t")
                            .append(entry.getKey().toString())
                            .append(": ")
                            .append(entry.getValue().toString());

                    if (addKey || propertyMap.get(entry.getKey()) != null) {
                        propertyMap.put(
                                entry.getKey().toString(),
                                entry.getValue().toString());
                    }
                }
                LOG.trace(reportTrace);
//                for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
//                    propertyMap.put(entry.getKey(), pr.getProperty(entry.getKey(), entry.getValue()));
//                }
                fileExists = true;
            } catch (IOException e) {
                LOG.error(e);
            }
        } else {
            report.append("\r\n\tФайл не найден, используем параметры по умолчанию:");
        }

        // параметры со значениями
        propertyMap
                .entrySet()
                .stream()
//                .sorted(comparingByKey())
                .forEach(x -> {
                    report
                            .append("\r\n\t")
                            .append(x.getKey())
                            .append(": ")
                            .append(x.getValue());
                });

        if (fileExists) {
            LOG.info(report);
        } else {
            LOG.warn(report);
        }
    }


    public boolean setProperty(String key, String value) {
        boolean r = false;
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream(fileName)) {
            properties.load(inputStream);
            r = true;
        } catch (IOException e) {
            LOG.error(e);
        }

        if (r) {
            try (OutputStream outputStream = new FileOutputStream(fileName)) {
                properties.setProperty(key, value);
                properties.store(outputStream, null);
            } catch (IOException e) {
                r = false;
                LOG.error(e);
            }
        }
        return r;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean containsKey(String key){
        return propertyMap.containsKey(key);
    }

    public String get(String key) {
        if (containsKey(key)) {
            return propertyMap.get(key);
        } else {
            LOG.warn("Параметр <{}> не найден", key);
            return null;
        }
    }

    public String getString(String key) {
        try {
            return get(key);
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return null;
        }
    }

    public String getStringDecode(String key) {
        try {
            return getStringDecrypt(get(key));
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return null;
        }
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            LOG.error("Не верный формат данных {}",
                    get(key),
                    e);
            return 0;
        }
    }

    public long getLong(String key) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException e) {
            LOG.error("Не верный формат данных {}",
                    get(key),
                    e);
            return 0L;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(get(key));
        } catch (NumberFormatException e) {
            LOG.error("Не верный формат данных {}",
                    get(key),
                    e);
            return 0;
        }
    }

    public float getFloat(String key) {
        try {
            return Float.parseFloat(get(key));
        } catch (NumberFormatException e) {
            LOG.error("Не верный формат данных {}",
                    get(key),
                    e);
            return 0;
        }
    }

    public boolean getBoolean(String key) {
        try {
            return Boolean.parseBoolean(get(key));
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return false;
        }
    }

    public Date getDate(String key) {
        return getDate(key, "dd/MM/yyyy");
    }

    public Date getDate(String key, String dateFormat) {
        Date date = null;
        try {
            DateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            date = simpleDateFormat.parse(get(key));
        } catch (ParseException e) {
            LOG.error("Не верный формат данных {}",
                    get(key),
                    e);
        }
        return date;
    }

    public Level getLevel(String key) {
        try {
            return Level.getLevel(get(key));
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return null;
        }
    }

    public String[] getStringList(String key) {
        return get(key).split(",");
    }

    public int[] getIntList(String key) {
        try {
            return Arrays
                    .stream(get(key).split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return null;
        }
    }

    public byte[] getByteArray(String key) {
        return getByteArray(key, 16);
    }

    public byte[] getByteArray(String key, int radix) {
        try {
            return new BigInteger(get(key), radix).toByteArray();
        } catch (Exception e) {
            LOG.error("Ошибка при получении параметра {}", e);
            return null;
        }
    }

    public JSONObject getJSONObject(String key) {
        JSONObject jsonObject = null;
        String value = get(key);
        if (value != null && value.startsWith("{")) {
            try {
                jsonObject = new JSONObject(value);
            } catch (JSONException e) {
                LOG.error(e);
            }
        }
        return jsonObject;
    }

    public JSONArray getJSONArray(String key) {
        JSONArray jsonArray = null;
        String value = get(key);
        if (value != null && value.startsWith("[")) {
            try {
                jsonArray = new JSONArray(value);
            } catch (JSONException e) {
                LOG.error(e);
            }
        }
        return jsonArray;
    }

    public <T> List<T> getJsonList(String key, TypeToken typeToken) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(get(key), typeToken.getType());
    }
/*
    public List<?> getJsonList(String key) {
        Gson gson = new GsonBuilder().create();
        String jsonString = get(key);
        return gson.fromJson(jsonString, new TypeToken<List<?>>(){}.getType());
    }
*/

    private String getStringEncrypt(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    private String getStringDecrypt(String data) {
        try {
            return new String((Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            return "";
        }
    }

}
