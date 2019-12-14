package ru.examples.accountCheck;

public class AccountCheckExample {

    public static void main(String[] args) {
        AccountCheck ac = new AccountCheck();

        System.out.println(ac.chekRS("049205603", "40702810200000000001"));

        String bic = "049205603";
        String accountMask = "40702810xnnnnnnnnnnn";
        String account;

        for (int i = 0; i < 10; i++) {
            account = accountMask.replaceAll("nnnnnnnnnnn", String.format("%011d", i));

            for (int k = 0; k < 10; k++) {
                if (ac.chekRS(bic, account.replaceAll("x", String.valueOf(k)))) {
                    account = account.replaceAll("x", String.valueOf(k));
                    break;
                }
            }
            System.out.println(account);
        }
    }
}
