package net.sf.openrocket.rw;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringUtils {
    public static List<List<String>> toListsByString(String value) {
        List<List<String>> lists = new ArrayList();
        String[] lines = value.split("\n");
        String[] var6 = lines;
        int var5 = lines.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            String line = var6[var4];
            List<String> list = new ArrayList();
            String[] cells = line.trim().split("\\s+");
            String[] var12 = cells;
            int var11 = cells.length;

            for (int var10 = 0; var10 < var11; ++var10) {
                String cell = var12[var10];
                list.add(cell);
            }

            lists.add(list);
        }

        return lists;
    }

    public static List<List<Double>> toDoubleListsByString(String value) {
        List<List<Double>> lists = new ArrayList();
        if (value.trim().equals("")) {
            return lists;
        } else {
            String[] lines = value.split("\n");
            String[] var7 = lines;
            int var6 = lines.length;

            ArrayList list;
            for (int var5 = 0; var5 < var6; ++var5) {
                String line = var7[var5];
                list = new ArrayList();
                String[] cells = line.trim().split("\\s+");
                String[] var12 = cells;
                int var11 = cells.length;

                for (int var10 = 0; var10 < var11; ++var10) {
                    String cell = var12[var10];
                    list.add(Double.valueOf(cell));
                }

                lists.add(list);
            }
            return lists;
        }
    }


    public static String toString(List<List<String>> lists) {
        StringBuilder value = new StringBuilder();
        Iterator var3 = lists.iterator();

        while (var3.hasNext()) {
            List<String> list = (List) var3.next();
            StringBuilder line = new StringBuilder();
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
                String cell = (String) var6.next();
                line.append(cell).append("    ");
            }

            value.append(line).append("\r\n");
        }

        return value.toString();
    }

    public static String ToDString(List<List<Double>> lists) {
        StringBuilder value = new StringBuilder();
        Iterator var3 = lists.iterator();

        while (var3.hasNext()) {
            List<Double> list = (List) var3.next();
            StringBuilder line = new StringBuilder();
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
                Double cell = (Double) var6.next();
                line.append(cell).append("    ");
            }

            value.append(line).append("\r\n");
        }

        return value.toString();
    }

    public static String toStringByDoubleLists(List<List<Double>> lists) {
        StringBuilder value = new StringBuilder();
        Iterator var4 = lists.iterator();

        while (var4.hasNext()) {
            List<Double> list = (List) var4.next();
            StringBuilder line = new StringBuilder();
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
                Object cell = var6.next();
                line.append(cell.toString()).append("  ");
            }

            value.append(line).append("\r\n");
        }

        return value.toString().trim();
    }

    public static String toStringByDoublesList(List<double[]> lists) {
        StringBuilder value = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.000000");
        Iterator var5 = lists.iterator();

        while (var5.hasNext()) {
            double[] array = (double[]) var5.next();
            StringBuilder line = new StringBuilder();
            double[] var9 = array;
            int var8 = array.length;

            for (int var7 = 0; var7 < var8; ++var7) {
                Double cell = var9[var7];
                line.append(df.format(cell)).append("  ");
            }

            value.append(line).append("\r\n");
        }

        return value.toString().trim();
    }

    public static String[][] toArraysFromString(String value) {
        String[] lines = value.split("\n");
        int length = lines.length;
        String[][] cells = new String[length][];

        for (int i = 0; i < length; ++i) {
            cells[i] = lines[i].trim().split("\\s+");
        }

        lines = null;
        return cells;
    }

    public static String[] toArrayFromString(String value) {
        return value.split("\\s+");
    }

    public static double[] toDoubleArrayFromString(String value) {
        String[] cells = value.split("\\s+");
        double[] das = new double[cells.length];

        for (int i = 0; i < cells.length; ++i) {
            das[i] = Double.valueOf(cells[i]);
        }

        return das;
    }

    public static String toString(String[][] arrays) {
        String value = "";
        String[][] var5 = arrays;
        int var4 = arrays.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            String[] cells = var5[var3];
            String line = "";
            String[] var10 = cells;
            int var9 = cells.length;

            for (int var8 = 0; var8 < var9; ++var8) {
                String cell = var10[var8];
                line = line + cell + "   ";
            }

            value = value + line.trim() + "\r\n";
        }

        return value.trim();
    }

    public static String toString(String[] array) {
        String value = "";
        String[] var5 = array;
        int var4 = array.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            String cell = var5[var3];
            value = value + cell.trim() + "\t";
        }

        return value.trim();
    }

    public static String toString(double[] array) {
        String value = "";
        double[] var5 = array;
        int var4 = array.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            Double cell = var5[var3];
            value = value + cell.toString().trim() + "\t";
        }

        return value.trim();
    }

    public static String toArrayString(String name, int x, int y) {
        String xStr = x == -1 ? " " : String.valueOf(x);
        String yStr = y == -1 ? " " : String.valueOf(y);
        return name + '[' + xStr + ']' + '[' + yStr + ']';
    }

    public static String toArrayString(String name, int x) {
        return name + '[' + x + ']';
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
}
