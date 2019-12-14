package ru.examples.accountCheck;

/**
 * Проверка корректности счета
 */
public class AccountCheck {

    private int[] maskAccount = {7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1}; // весовые коэффициенты

    // корректность счета
    private boolean check(String value) {
        Integer summa = 0;
        String ch;
        for (int i = 0; i < 23; i++) {
            ch = String.valueOf(value.charAt(i));
            summa = summa + (Integer.parseInt(ch) * maskAccount[i]) % 10; // вычисление контрольной суммы
        }
        return (summa % 10 == 0);
    }

    // корсчет
    public boolean chekKS(String bik, String account) {
        return bik.length() == 9 &&
                bik.matches("[0-9]+") &&
                account.length() == 20 &&
                account.matches("[0-9]+") &&
                check("0" + bik.substring(5, 7) + account);
    }

    // расчетный счет
    public boolean chekRS(String bik, String account) {
        return bik.length() == 9 &&
                bik.matches("[0-9]+") &&
                account.length() == 20 &&
                account.matches("[0-9]+") &&
                check(bik.substring(6, 9) + account);
    }

}
