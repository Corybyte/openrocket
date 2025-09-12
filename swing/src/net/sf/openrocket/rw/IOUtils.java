package net.sf.openrocket.rw;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IOUtils {
   public static final String CHARACTER_GBK = "GBK";
   public static final String CHARACTER_UTF8 = "UTF-8";

   public static String toString(File file) {
      StringBuilder sb = new StringBuilder();
      BufferedReader br = null;
      FileInputStream fis = null;

      try {
         int rows = 0;
         fis = new FileInputStream(file.getAbsolutePath());

         String line;
         for(br = new BufferedReader(new InputStreamReader(fis, "GBK")); (line = br.readLine()) != null; sb.append(line)) {
            if (rows == 0) {
               ++rows;
            } else {
               sb.append("\r\n");
            }
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

         if (fis != null) {
            try {
               fis.close();
            } catch (IOException var16) {
               var16.printStackTrace();
            }
         }

      }

      return sb.toString();
   }

   public static StringBuilder toString(File file, String encode) {
      StringBuilder sb = new StringBuilder();
      BufferedReader br = null;
      FileInputStream fis = null;

      try {
         int rows = 0;
         fis = new FileInputStream(file.getAbsolutePath());

         String line;
         for(br = new BufferedReader(new InputStreamReader(fis, encode)); (line = br.readLine()) != null; sb.append(line)) {
            if (rows == 0) {
               ++rows;
            } else {
               sb.append("\r\n");
            }
         }
      } catch (Exception var19) {
         var19.printStackTrace();
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var18) {
               var18.printStackTrace();
            }
         }

         if (fis != null) {
            try {
               fis.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

      }

      return sb;
   }

   public static List<String> toList(File file) {
      List<String> list = new ArrayList();
      BufferedReader br = null;
      FileInputStream fis = null;

      try {
         String line = null;
         fis = new FileInputStream(file.getAbsolutePath());
         br = new BufferedReader(new InputStreamReader(fis, "GBK"));

         while((line = br.readLine()) != null) {
            list.add(line);
         }
      } catch (Exception var17) {
         var17.printStackTrace();
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var16) {
               var16.printStackTrace();
            }
         }

         if (fis != null) {
            try {
               fis.close();
            } catch (IOException var15) {
               var15.printStackTrace();
            }
         }

      }

      return list;
   }

   public static List<List<String>> toLists(File file) {
      List<List<String>> lists = new ArrayList();
      BufferedReader br = null;
      FileInputStream fis = null;

      try {
         fis = new FileInputStream(file.getAbsolutePath());
         br = new BufferedReader(new InputStreamReader(fis, "GBK"));

         while(true) {
            String[] infos;
            ArrayList list;
            do {
               String line;
               if ((line = br.readLine()) == null) {
                  return lists;
               }

               list = new ArrayList();
               infos = line.trim().split("\\s+");
            } while(infos.length <= 0);

            String[] var10 = infos;
            int var9 = infos.length;

            for(int var8 = 0; var8 < var9; ++var8) {
               String info = var10[var8];
               list.add(info);
            }

            lists.add(list);
         }
      } catch (Exception var23) {
         var23.printStackTrace();
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var22) {
               var22.printStackTrace();
            }
         }

         if (fis != null) {
            try {
               fis.close();
            } catch (IOException var21) {
               var21.printStackTrace();
            }
         }

      }

      return lists;
   }

   public static boolean stringToFile(String value, File file) {
      boolean flag = true;
      BufferedWriter bw = null;
      FileOutputStream fos = null;

      try {
         fos = new FileOutputStream(file.getAbsolutePath());
         bw = new BufferedWriter(new OutputStreamWriter(fos, "GBK"));
         bw.write(value);
         bw.flush();
      } catch (Exception var18) {
         var18.printStackTrace();
         flag = false;
      } finally {
         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

         if (fos != null) {
            try {
               fos.close();
            } catch (IOException var16) {
               var16.printStackTrace();
            }
         }

      }

      return flag;
   }

   public static boolean stringToFile(String value, File file, String encode) {
      boolean flag = true;
      BufferedWriter bw = null;
      FileOutputStream fos = null;

      try {
         fos = new FileOutputStream(file.getAbsolutePath());
         bw = new BufferedWriter(new OutputStreamWriter(fos, encode));
         bw.write(value);
         bw.flush();
      } catch (Exception var19) {
         var19.printStackTrace();
         flag = false;
      } finally {
         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var18) {
               var18.printStackTrace();
            }
         }

         if (fos != null) {
            try {
               fos.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

      }

      return flag;
   }

   public static boolean appendStringToFile(String sz, File file) {
      boolean flag = true;
      FileWriter fw = null;
      BufferedWriter bw = null;

      try {
         fw = new FileWriter(file, true);
         bw = new BufferedWriter(fw);
         bw.write(sz);
         bw.newLine();
         bw.flush();
      } catch (Exception var18) {
         var18.printStackTrace();
         flag = false;
      } finally {
         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

         if (fw != null) {
            try {
               fw.close();
            } catch (IOException var16) {
               var16.printStackTrace();
            }
         }

      }

      return flag;
   }

   public static boolean listToFile(List<String> list, File file) throws IOException {
      boolean flag = true;
      FileWriter fw = null;
      BufferedWriter bw = null;

      try {
         fw = new FileWriter(file);
         bw = new BufferedWriter(fw);

         for(int i = 0; i < list.size(); ++i) {
            bw.write((String)list.get(i));
            bw.newLine();
         }

         bw.flush();
         return flag;
      } finally {
         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var14) {
               var14.printStackTrace();
            }
         }

         if (fw != null) {
            try {
               fw.close();
            } catch (IOException var13) {
               var13.printStackTrace();
            }
         }

      }
   }

   public static boolean listsToFile(List<List<String>> lists, File file) {
      boolean flag = true;
      FileWriter fw = null;
      BufferedWriter bw = null;

      try {
         fw = new FileWriter(file);
         bw = new BufferedWriter(fw);

         for(int i = 0; i < lists.size(); ++i) {
            List<String> list = (List)lists.get(i);
            StringBuilder line = new StringBuilder();

            for(int j = 0; j < list.size(); ++j) {
               line.append((String)list.get(j)).append("    ");
            }

            bw.write(line.toString().trim());
            bw.newLine();
         }

         bw.flush();
         bw.close();
      } catch (Exception var21) {
         var21.printStackTrace();
         flag = false;
      } finally {
         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var20) {
               var20.printStackTrace();
            }
         }

         if (fw != null) {
            try {
               fw.close();
            } catch (IOException var19) {
               var19.printStackTrace();
            }
         }

      }

      return flag;
   }

   public static List<List<String>> toLists(String value) {
      List<List<String>> lists = new ArrayList();
      String[] lines = value.split("\n");
      String[] var6 = lines;
      int var5 = lines.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         String line = var6[var4];
         String[] cells = line.split("\\s+");
         List<String> list = new ArrayList(cells.length);
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

   public static String toString(List<List<String>> lists) {
      int row = 0;
      new StringBuilder();
      StringBuilder value = new StringBuilder();
      Iterator var5 = lists.iterator();

      while(var5.hasNext()) {
         List<String> list = (List)var5.next();
         if (row == 0) {
            ++row;
         } else {
            value.append("\r\n");
         }

         StringBuilder line = new StringBuilder();
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            String cell = (String)var7.next();
            line.append(cell).append("    ");
         }

         value.append(line);
      }

      return value.toString();
   }
}
