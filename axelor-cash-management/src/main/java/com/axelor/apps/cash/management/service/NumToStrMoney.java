package com.axelor.apps.cash.management.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class NumToStrMoney {

  /** Amount of money */
  private BigDecimal amount;

  /** Constructor from Long */
  public NumToStrMoney(long l) {
    String s = String.valueOf(l);
    if (!s.contains(".")) {
      s += ".0";
    }
    this.amount = new BigDecimal(s);
  }

  /** Constructor from Double */
  public NumToStrMoney(double l) {
    String s = String.valueOf(l);
    if (!s.contains(".")) {
      s += ".0";
    }
    this.amount = new BigDecimal(s);
  }

  /** Constructor from String */
  public NumToStrMoney(String s) {
    if (!s.contains(".")) {
      s += ".0";
    }
    this.amount = new BigDecimal(s);
  }
  /** Return the amount as a string */
  public String asString() {
    return amount.toString();
  }

  /** Return the amount in words, to the nearest kopecks */
  public String num2str() {
    return num2str(false);
  }

  /**
   * Output the amount in words
   *
   * @param stripkop boolean flag - show pennies or not
   * @return String Amount in words
   */
  public String num2str(boolean stripkop) {
    String[][] sex = {
      {"", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
      {"", "одна", "две", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
    };
    String[] str100 = {
      "",
      "сто",
      "двести",
      "триста",
      "четыреста",
      "пятьсот",
      "шестьсот",
      "семьсот",
      "восемьсот",
      "девятьсот"
    };
    String[] str11 = {
      "",
      "десять",
      "одиннадцать",
      "двенадцать",
      "тринадцать",
      "четырнадцать",
      "пятнадцать",
      "шестнадцать",
      "семнадцать",
      "восемнадцать",
      "девятнадцать",
      "двадцать"
    };
    String[] str10 = {
      "",
      "десять",
      "двадцать",
      "тридцать",
      "сорок",
      "пятьдесят",
      "шестьдесят",
      "семьдесят",
      "восемьдесят",
      "девяносто"
    };
    String[][] forms = {
      {"копейка", "копейки", "копеек", "1"},
      {"рубль", "рубля", "рублей", "0"},
      {"тысяча", "тысячи", "тысяч", "1"},
      {"миллион", "миллиона", "миллионов", "0"},
      {"миллиард", "миллиарда", "миллиардов", "0"},
      {"триллион", "триллиона", "триллионов", "0"}, // можно добавлсть дальше секстиллионы и т.д.
    };
    // we receive rubles and kopecks separately
    long rub = amount.longValue();
    String[] moi = amount.toString().split("\\.");
    long kop = Long.valueOf(moi[1]);
    if (!moi[1].substring(0, 1).equals("0")) { // does not start with zeros
      if (kop < 10) {
        kop *= 10;
      }
    }
    String kops = String.valueOf(kop);
    if (kops.length() == 1) {
      kops = "0" + kops;
    }
    long rub_tmp = rub;
    // The amount divider into segments of 3 digits from the end
    ArrayList segments = new ArrayList();
    while (rub_tmp > 999) {
      long seg = rub_tmp / 1000;
      segments.add(rub_tmp - (seg * 1000));
      rub_tmp = seg;
    }
    segments.add(rub_tmp);
    Collections.reverse(segments);
    // analyzing segments
    String o = "";
    if (rub == 0) { // если соль
      o = "ноль " + morph(0, forms[1][0], forms[1][1], forms[1][2]);
      if (stripkop) {
        return o;
      } else {
        return o + " " + kop + " " + morph(kop, forms[0][0], forms[0][1], forms[0][2]);
      }
    }
    // Greater than zero
    int lev = segments.size();
    for (int i = 0; i < segments.size(); i++) { // iterating through the segments
      int sexi = (int) Integer.valueOf(forms[lev][3].toString()); // defining the genders
      int ri = (int) Integer.valueOf(segments.get(i).toString()); // current segment
      if (ri == 0 && lev > 1) { // if segment ==0 And not the last level (there Units)
        lev--;
        continue;
      }
      String rs = String.valueOf(ri); // number per line
      // normalization
      if (rs.length() == 1) {
        rs = "00" + rs; // two zeroes in the prefix?
      }
      if (rs.length() == 2) {
        rs = "0" + rs; // or is one better?
      } // we get the numbers of the dls analysis
      int r1 = (int) Integer.valueOf(rs.substring(0, 1)); // first digit
      int r2 = (int) Integer.valueOf(rs.substring(1, 2)); // second
      int r3 = (int) Integer.valueOf(rs.substring(2, 3)); // third
      int r22 = (int) Integer.valueOf(rs.substring(1, 3)); // second and third
      // Super-nano-Digital Analyzer
      if (ri > 99) {
        o += str100[r1] + " "; // Hundreds
      }
      if (r22 > 20) { // >20
        o += str10[r2] + " ";
        o += sex[sexi][r3] + " ";
      } else { // <=20
        if (r22 > 9) {
          o += str11[r22 - 9] + " "; // 10-20
        } else {
          o += sex[sexi][r3] + " "; // 0-9
        }
      }
      // Units of measurement (rubles...)
      o += morph(ri, forms[lev][0], forms[lev][1], forms[lev][2]) + " ";
      lev--;
    }
    // Kopecks in digital form
    if (stripkop) {
      o = o.replaceAll(" {2,}", " ");
    } else {
      o = o + "" + kops + " " + morph(kop, forms[0][0], forms[0][1], forms[0][2]);
      o = o.replaceAll(" {2,}", " ");
    }
    return o;
  }

  /**
   * * Incline the word form
   *
   * @param n Long number of objects
   * @param f1 String variant of the dls word form of one object
   * @param f2 String variant of the dls word form of two objects
   * @param f5 String a variant of the word form of dls for objects
   * @return String is the correct variant of the word form for the specified number of objects
   */
  public static String morph(long n, String f1, String f2, String f5) {
    n = Math.abs(n) % 100;
    long n1 = n % 10;
    if (n > 10 && n < 20) {
      return f5;
    }
    if (n1 > 1 && n1 < 5) {
      return f2;
    }
    if (n1 == 1) {
      return f1;
    }
    return f5;
  }

  public static void main(String[] args) {
    NumToStrMoney ntsm = new NumToStrMoney(152105.41);
    /*  System.out.println(ntsm.asString());*/
    /*   System.out.println(ntsm.num2str(true));*/
    System.out.println(ntsm.num2str());
  }
}
