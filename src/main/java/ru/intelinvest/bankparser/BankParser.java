/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.intelinvest.bankparser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author b1ack
 */
public class BankParser {

    private static String file = "banks.csv";//"F:\\yandex_disk\\java\\projects\\TEMP\\banks.csv";

    public static void main(String[] args) {
        List<String> bankNames = new ArrayList<>();
        List<Bank> sAndP = new ArrayList<>(); // 0
        List<Bank> moody = new ArrayList<>(); // 1
        List<Bank> fitch = new ArrayList<>(); // 2

        parseByType(sAndP, 0, 3);
        parseByType(moody, 5, 4);
        parseByType(fitch, 11, 4);
        for (int i = 1; i < 15; i++) {
            parse(bankNames, i);
        }
        System.out.println("S&P SIZE: " + sAndP.size());
        System.out.println("moody SIZE: " + moody.size());
        System.out.println("fitch SIZE: " + fitch.size());

        // совмещаем списки назнваний банков и данных
        for (int i = 0; i < sAndP.size(); i++) {
            sAndP.get(i).setName(bankNames.get(i));
            moody.get(i).setName(bankNames.get(i));
            fitch.get(i).setName(bankNames.get(i));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < sAndP.size(); i++) {
                writer.append(bankNames.get(i))
                        .append(";")
                        .append(sAndP.get(i).getPrognosis())
                        .append(";")
                        .append(sAndP.get(i).getRating())
                        .append(";")
                        .append(sAndP.get(i).getDate())
                        .append(";")
                        .append(moody.get(i).getPrognosis())
                        .append(";")
                        .append(moody.get(i).getRating())
                        .append(";")
                        .append(moody.get(i).getDate())
                        .append(";")
                        .append(fitch.get(i).getPrognosis())
                        .append(";")
                        .append(fitch.get(i).getRating())
                        .append(";")
                        .append(fitch.get(i).getDate())
                        .append(";")
                        .append("\r\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(BankParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void write(List<Bank> banks, String file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Bank b : banks) {
                writer.append(b.getName())
                        .append(";")
                        .append(b.getDate())
                        .append(";")
                        .append(b.getRating())
                        .append(";")
                        .append(b.getPrognosis())
                        .append(";")
                        .append("\r\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(BankParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void parseByType(List<Bank> banks, int type, int dateShift) {
        for (int i = 1; i < 15; i++) {
            parseBanks(banks, i, type, dateShift);
        }
    }

    private static void parse(List<String> bankNames, int page) {
        String url = "http://www.banki.ru/banks/ratings/agency/?PAGEN_2={0}&mode=2";
        try {
            Document doc = Jsoup.connect(MessageFormat.format(url, page)).get();
            Elements el = doc.select("#b-table-nameslist > table > tbody > tr > td > div");
            for (Element element : el) {
                bankNames.add(element.select("a").html());
            }
        } catch (IOException ex) {
            Logger.getLogger(BankParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void parseBanks(List<Bank> banks, int page, int type, int dateShift) {
        String url = "http://www.banki.ru/banks/ratings/agency/?PAGEN_2={0}&mode=2";
        try {
            Document doc = Jsoup.connect(MessageFormat.format(url, page)).get();
            Elements table = doc.select("#b-table-ratings > table > tbody");
            Elements rows = table.select("tr");
            for (int i = 0; i < rows.size(); i++) { //first row is the col names so skip it.
                Element row = rows.get(i);
                Elements cols = row.select("td");
                Bank b = new Bank();
                if (cols.size() > 0) {
                    if (cols.get(type).select("div").size() > 2) {
                        //System.out.println(cols.get(type).select("div").get(1).select("div").html());
                        //System.out.println(cols.get(type).select("div").get(3).select("div").html());
                        //System.out.println("DATE: " + cols.get(type + dateShift).select("div").html());
                        String rating = cols.get(type).select("div").get(1).select("div").html();
                        String prognosis = cols.get(type).select("div").get(3).select("div").html();
                        String date = cols.get(type + dateShift).select("div").html();
                        if (!rating.isEmpty()) {
                            b.setRating(rating);
                            b.setDate(date);
                            b.setPrognosis(prognosis);
                        }
                    }
                }
                banks.add(b);
            }
        } catch (IOException ex) {
            Logger.getLogger(BankParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class Bank {

        String name;
        String rating;
        String prognosis;
        String date;

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getPrognosis() {
            return prognosis;
        }

        public void setPrognosis(String prognosis) {
            this.prognosis = prognosis;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Bank{" + "name=" + name + ", rating=" + rating + ", prognosis=" + prognosis + ", date=" + date + '}';
        }
    }
}
