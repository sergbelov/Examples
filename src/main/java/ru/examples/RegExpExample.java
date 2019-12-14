package ru.examples;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Сергей on 29.04.2018.
 */
public class RegExpExample {
    public static void main(String[] args) {

        Pattern pattern;
        Matcher matcher;
/*
https://ru.wikipedia.org/wiki/%D0%A0%D0%B5%D0%B3%D1%83%D0%BB%D1%8F%D1%80%D0%BD%D1%8B%D0%B5_%D0%B2%D1%8B%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F

Символ	Эквивалент	Соответствие
\d	[0-9]           Цифровой символ
\D	[^0-9]          Нецифровой символ
\s	[ \f\n\r\t\v]	Пробельный символ
\S	[^ \f\n\r\t\v]  Непробельный символ
\w	[[:word:]]      Буквенный или цифровой символ или знак подчёркивания
\W	[^[:word:]]     Любой символ, кроме буквенного или цифрового символа или знака подчёркивани

^	Начало текста (или строки при модификаторе ?m)	^a	    aaa aaa
$	Конец текста (или строки при модификаторе ?m)	a$	    aaa aaa
\b	Граница слова	                                a\b	    aaa aaa
                                                    \ba	    aaa aaa
\B	Не граница слова	                            \Ba\B	aaa aaa
\G	Предыдущий успешный поиск	                    \Ga	    aaa aaa (поиск остановился на 4-й позиции — там, где не нашлось a

|    или                 (black|wait)
()   маска               matcher.group(>0)
(?:) маска без захвата

?	Ноль или одно	{0,1}	colou?r	color, colour
*	Ноль или более	{0,}	colou*r	color, colour, colouur и т. д.
+	Одно или более	{1,}	colou+r	colour, colouur и т. д. (но не color)

{n}	    Ровно n раз	            colou{3}r	colouuur
{m,n}	От m до n включительно	colou{2,4}r	colouur, colouuur, colouuuur
{m,}	Не менее m	            colou{2,}r	colouur, colouuur, colouuuur и т. д.
{,n}	Не более n	            colou{,3}r	color, colour, colouur, colouuur

Жадный	Ленивый
*	    *?
+	    +?
{n,}	{n,}?

Жадный	Ревнивый
*	    *+
?	    ?+
+	    ++
{n,}	{n,}+

 */
        String regex =
                "(\\[.+?\\]\\s*?\\[.+?\\[.+?\\]\\])" +
                "|(\\[(сервер1|сервер2)\\])" +
                "|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})" +

//                "(^[\\d]{4}-[\\d]{2}-[\\d]{2} [\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3})"; // дата и время события;
//                "^[\\d :\\.-]{23} \\[([\\w ]+?)\\]"; // уровень события
//                "^[\\d :\\.-]{23} \\[[\\w ]+?\\] \\[(.+?)\\]"; // пакет
//                "^[\\d :\\.-]{23} \\[[\\w ]+?\\] \\[.+?\\] \\[(\\[.+?\\].+?)\\]"; // поток

                "|(^[\\d :\\.-]{23} \\[[\\w ]+?\\] \\[.+?\\] \\[\\[.+?\\].+?\\])";

//        "(^[\\d]{4}-[\\d]{2}-[\\d]{2} [\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3})" + // дата и время события;
//        "^[\\d :\\.-]{23} \\[([\\w ]+?)\\]" // уровень события
//        "^[\\d :\\.-]{23} \\[[\\w ]+?\\] \\[(.+?)\\]" // пакет
//        "^[\\d :\\.-]{23} \\[[\\w ]+?\\] \\[.+?\\] \\[(\\[.+?\\].+?)\\]" // поток

        String data = "[10] [20[30]]строка 10 [11] [21[31]] строка 11 [12]      [22[32]] строка 12 [сервер1] 192.168.1.1\n"+
                "строка 20 [123 1][456 1[789 1]] строка 21 [123 2][456 2[789 2]] строка 22 [сервер2] 192.168.1.2\n" +
                "2018-05-04 09:10:42.957 [DEBUG] [com.org.module1.api.impl.logging.LoggableAspect] [[module1]-server-rejected-thread-13] строка 3\n" +
                "2018-05-04 09:10:43.541 [DEBUG] [com.org.core.transport.rpc.impl.StatelessRpcInvokerImpl] [[module2]-server-rejected-thread-13] строка 4 ";

//        pattern = Pattern.compile(regex);
        pattern = Pattern.compile(regex, Pattern.MULTILINE); // если есть \n\r в строке
        matcher = pattern.matcher(data);

        System.out.println(data);
        System.out.println("=====================================");
        while (matcher.find()) {
            System.out.println("Full match: " + matcher.group(0));
//            data = data.replace(matcher.group(0), "");
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    System.out.println("Group " + i + ": " + matcher.group(i));
                    data = data.replace(matcher.group(i), "");
                }
            }
        }
        System.out.println("=====================================");
        System.out.println(data);
        System.exit(0);




//        final String uuidMask = "[0-9a-zA-Z+]{8}-[0-9a-zA-Z+]{4}-[0-9a-zA-Z+]{4}-[0-9a-zA-Z+]{4}-[0-9a-zA-Z+]{12}";
        final String uuidMask = "[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}";
        UUID uuid;
        StringBuilder uuidList = new StringBuilder();
        for (int i = 0; i < 3; i ++){
            uuid = UUID.randomUUID();
            if (uuidList.length() > 0){ uuidList.append(", "); }
            uuidList.append("id='")
                    .append(uuid)
                    .append("'");
        }
        System.out.println(uuidList.toString());
        pattern = Pattern.compile(uuidMask);
        matcher = pattern.matcher(uuidList.toString());
        while (matcher.find()){
            System.out.println(matcher.group());
        }

        regExp1();
    }

    private static void regExp1(){
        // Строка для сканирования, чтобы найти шаблон
        String str = "Крещение Руси произошло в 988 году! Не так ли?";
        String pattern = "(.*)(\\d+)(.*)";

        // Создание Pattern объекта
        Pattern p = Pattern.compile(pattern);

        // Создание matcher объекта
        Matcher m = p.matcher(str);
        if (m.find( )) {
            System.out.println("Найдено значение: " + m.group(0));
            System.out.println("Найдено значение: " + m.group(1));
            System.out.println("Найдено значение: " + m.group(2));
        }else {
            System.out.println("НЕ СОВПАДАЕТ");
        }
    }
}
