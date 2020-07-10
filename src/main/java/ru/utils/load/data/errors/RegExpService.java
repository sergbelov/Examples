package ru.utils.load.data.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpService {
    private static final Logger LOG = LoggerFactory.getLogger(RegExpService.class);

    private final String maskClear1 = "" +
            "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})|" +
            "([0-9a-f]{8}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{12})|" +
            "([0-9a-f]{9}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{12})|" +
            "(null)";
//                "([0-9a-f]{33})|" +
//                "([0-9a-f]{32})";

//    private final String maskClear2 = "([0-9]+)";
    private final String maskClear2 = "([^\\D])";

    public RegExpService() {
    }

    /**
     * Обработать текст
     * @param text
     * @return
     */
    public String getText(String text){
        text = getText(text, maskClear1);
        text = getText(text, maskClear2);
        return text;
    }

    /**
     * Обработать текст
     * @param text
     * @param mask
     * @return
     */
    public String getText(String text, String mask){
        LOG.debug("Текст до преобразования:\n{}", text);
        Pattern pattern = Pattern.compile(mask, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            LOG.trace("Full match: {}", matcher.group(0));
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    LOG.trace("Group {}: {}", i, matcher.group(i));
                    text = text.replace(matcher.group(i), "");
                }
            }
        }
        LOG.debug("Текст после преобразования:\n{}", text);
        return text;
    }
}
