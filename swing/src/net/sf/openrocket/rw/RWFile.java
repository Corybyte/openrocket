package net.sf.openrocket.rw;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RWFile {
   public static List<List<String>> getLists(String filePath) {
      ArrayList lists = new ArrayList();

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = null;
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());
         br = new BufferedReader(new InputStreamReader(fis, "gb2312"));

         while(true) {
            ArrayList list;
            String[] infos;
            do {
               if ((str = br.readLine()) == null) {
                  br.close();
                  fis.close();
                  return lists;
               }

               list = new ArrayList();
               str = str.trim();
               infos = str.split("\\s+");
            } while(infos.length <= 0);

            String[] var11 = infos;
            int var10 = infos.length;

            for(int var9 = 0; var9 < var10; ++var9) {
               String info = var11[var9];
               list.add(info);
            }

            lists.add(list);
         }
      } catch (Exception var12) {
         var12.printStackTrace();
         return lists;
      }
   }

   public static List<List<String>> getLists(String filePath, int n) {
      ArrayList lists = new ArrayList();

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = null;
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());
         br = new BufferedReader(new InputStreamReader(fis, "gb2312"));

         while(true) {
            ArrayList list;
            String[] infos;
            do {
               if ((str = br.readLine()) == null) {
                  br.close();
                  fis.close();
                  return lists;
               }

               list = new ArrayList();
               str = str.trim();
               infos = str.split("\\s+");
            } while(infos.length != n);

            String[] var12 = infos;
            int var11 = infos.length;

            for(int var10 = 0; var10 < var11; ++var10) {
               String info = var12[var10];
               list.add(info);
            }

            lists.add(list);
         }
      } catch (Exception var13) {
         var13.printStackTrace();
         return lists;
      }
   }

   public static String readFile(String filePath) {
      String rStr = "";

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = "";
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());

         for(br = new BufferedReader(new InputStreamReader(fis, "gb2312")); (str = br.readLine()) != null; rStr = rStr + str + "\r\n") {
         }

         rStr = rStr.trim();
         br.close();
         fis.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return rStr;
   }

   public static StringBuffer readFileByBuffer(String filePath, String encode) {
      StringBuffer sb = new StringBuffer("");

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = "";
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());

         for(br = new BufferedReader(new InputStreamReader(fis, encode)); (str = br.readLine()) != null; sb = sb.append(str + "\r\n")) {
            str = str.trim();
         }

         sb.trimToSize();
         br.close();
         fis.close();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return sb;
   }

   public static StringBuffer readFileByBuffer(String filePath) {
      StringBuffer sb = new StringBuffer("");

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = "";
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());

         for(br = new BufferedReader(new InputStreamReader(fis, "gb2312")); (str = br.readLine()) != null; sb = sb.append(str + "\r\n")) {
            str = str.trim();
         }

         sb.trimToSize();
         br.close();
         fis.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return sb;
   }

   public static List<String> readFileByList(String filePath) {
      ArrayList list = new ArrayList();

      try {
         BufferedReader br = null;
         FileInputStream fis = null;
         String str = null;
         File file = new File(filePath);
         fis = new FileInputStream(file.getAbsolutePath());
         br = new BufferedReader(new InputStreamReader(fis, "gb2312"));

         while((str = br.readLine()) != null) {
            str = str.trim();
            list.add(str);
         }

         br.close();
         fis.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return list;
   }

   public static boolean writeFileByGBK(String sz, String filePath) {
      boolean flag = true;

      try {
         BufferedWriter bw = null;
         File file = new File(filePath);
         FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
         bw = new BufferedWriter(new OutputStreamWriter(fos, "GBK"));
         bw.write(sz);
         bw.flush();
         bw.close();
      } catch (Exception var6) {
         var6.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByUTF8(String sz, String filePath) {
      boolean flag = true;

      try {
         BufferedWriter bw = null;
         File file = new File(filePath);
         FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
         bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
         bw.write(sz);
         bw.flush();
         bw.close();
      } catch (Exception var6) {
         var6.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByEncode(String sz, String filePath, String encode) {
      boolean flag = true;

      try {
         BufferedWriter bw = null;
         File file = new File(filePath);
         FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
         bw = new BufferedWriter(new OutputStreamWriter(fos, encode));
         bw.write(sz);
         bw.flush();
         bw.close();
      } catch (Exception var7) {
         var7.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFile(String sz, String filePath) {
      boolean flag = true;

      try {
         FileWriter fw = null;
         BufferedWriter bw = null;
         fw = new FileWriter(new File(filePath));
         bw = new BufferedWriter(fw);
         bw.write(sz);
         bw.flush();
         fw.flush();
         bw.close();
         fw.close();
      } catch (Exception var5) {
         var5.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByAppend(String sz, String filePath) {
      boolean flag = true;

      try {
         FileWriter fw = null;
         BufferedWriter bw = null;
         fw = new FileWriter(new File(filePath), true);
         bw = new BufferedWriter(fw);
         bw.write(sz);
         bw.newLine();
         bw.flush();
         fw.flush();
         bw.close();
         fw.close();
      } catch (Exception var5) {
         var5.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByList(List<String> list, String PathfilePath) {
      boolean flag = true;

      try {
         FileWriter fw = new FileWriter(PathfilePath);
         BufferedWriter bw = new BufferedWriter(fw);

         for(int i = 0; i < list.size(); ++i) {
            bw.write(((String)list.get(i)).toString().trim());
            bw.newLine();
         }

         bw.flush();
         fw.flush();
         bw.close();
         fw.close();
      } catch (Exception var6) {
         var6.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByLists(List<List<String>> lists, String filePath) {
      boolean flag = true;

      try {
         FileWriter fw = new FileWriter(new File(filePath));
         BufferedWriter bw = new BufferedWriter(fw);

         for(int i = 0; i < lists.size(); ++i) {
            List<String> list = (List)lists.get(i);
            String line = "";

            for(int j = 0; j < list.size(); ++j) {
               line = line + "\t\t" + ((String)list.get(j)).trim();
            }

            bw.write(line.trim());
            bw.newLine();
         }

         bw.flush();
         fw.flush();
         bw.close();
         fw.close();
      } catch (Exception var9) {
         var9.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean writeFileByAppendList(List<String> list, String filePath) {
      boolean flag = true;

      try {
         FileWriter fw = new FileWriter(new File(filePath), true);
         BufferedWriter bw = new BufferedWriter(fw);

         for(int i = 0; i < list.size(); ++i) {
            bw.write(((String)list.get(i)).trim());
            bw.newLine();
         }

         bw.flush();
         fw.flush();
         bw.close();
         fw.close();
      } catch (Exception var6) {
         var6.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static List<List<String>> getListsByStr(String value) {
      List<List<String>> lists = new ArrayList();
      String[] lines = value.split("\n");
      String[] var6 = lines;
      int var5 = lines.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         String line = var6[var4];
         List<String> list = new ArrayList();
         String[] cells = line.split("\\s+");
         String[] var12 = cells;
         int var11 = cells.length;

         for(int var10 = 0; var10 < var11; ++var10) {
            String cell = var12[var10];
            list.add(cell);
         }

         lists.add(list);
      }

      return lists;
   }

   public static String getString(List<List<String>> lists) {
      String value = "";

      String line;
      for(Iterator var3 = lists.iterator(); var3.hasNext(); value = value + line + "\r\n") {
         List<String> list = (List)var3.next();
         line = "";

         String cell;
         for(Iterator var6 = list.iterator(); var6.hasNext(); line = line + cell + "   ") {
            cell = (String)var6.next();
         }
      }

      return value.trim();
   }
}
