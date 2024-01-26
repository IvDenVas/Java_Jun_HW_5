package ru.geekbrains.junior.chat.server;

public class Main {
    public static void main(String[] args) {
        String x = "Denis: Olga-->5";
        String[] x1 = x.split(": ");
        String x2 = x1[1];
        if (x2.split("-->").length == 2) {
            String[] x3 = x1[1].split("-->");
            System.out.println(x3[1]);
        } else System.out.println("-");
    }
}
