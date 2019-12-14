package ru.examples;

/**
 * Created by Сергей on 20.03.2018.
 */
public class TranslitExample {

    private String ru2en(char ch){
        switch (ch){
            case 'А': return "A";
            case 'Б': return "B";
            case 'В': return "V";
            case 'Г': return "G";
            case 'Д': return "D";
            case 'Е': return "E";
            case 'Ё': return "Yo";
            case 'Ж': return "Zh";
            case 'З': return "Z";
            case 'И': return "I";
            case 'Й': return "I";
            case 'К': return "K";
            case 'Л': return "L";
            case 'М': return "M";
            case 'Н': return "N";
            case 'О': return "O";
            case 'П': return "P";
            case 'Р': return "R";
            case 'С': return "S";
            case 'Т': return "T";
            case 'У': return "U";
            case 'Ф': return "F";
            case 'Х': return "Kh";
            case 'Ц': return "Tc";
            case 'Ч': return "Ch";
            case 'Ш': return "Sh";
            case 'Щ': return "Shch";
            case 'Ъ': return "";
            case 'Ы': return "Y";
            case 'Ь': return "'";
            case 'Э': return "Eh";
            case 'Ю': return "Iu";
            case 'Я': return "Ia";

            case 'а': return "a";
            case 'б': return "b";
            case 'в': return "v";
            case 'г': return "g";
            case 'д': return "d";
            case 'е': return "e";
            case 'ё': return "yo";
            case 'ж': return "zh";
            case 'з': return "z";
            case 'и': return "i";
            case 'й': return "i";
            case 'к': return "k";
            case 'л': return "l";
            case 'м': return "m";
            case 'н': return "n";
            case 'о': return "o";
            case 'п': return "p";
            case 'р': return "r";
            case 'с': return "s";
            case 'т': return "t";
            case 'у': return "u";
            case 'ф': return "f";
            case 'х': return "kh";
            case 'ц': return "tc";
            case 'ч': return "ch";
            case 'ш': return "sh";
            case 'щ': return "shch";
            case 'ъ': return "";
            case 'ы': return "y";
            case 'ь': return "'";
            case 'э': return "eh";
            case 'ю': return "iu";
            case 'я': return "ia";

            default: return String.valueOf(ch);
        }
    }

    public String ru2en(String s){
        StringBuilder sb = new StringBuilder(s.length()*2);
        for(char ch: s.toCharArray()){
            sb.append(ru2en(ch));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        TranslitExample translitExample = new TranslitExample();
        System.out.println(translitExample.ru2en("Привет, как дела?"));
    }
}

