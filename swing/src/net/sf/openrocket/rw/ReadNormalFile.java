package net.sf.openrocket.rw;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadNormalFile {
   public static String[] PARAMINFO_SIGNS;
   public static String PARAMINFO_END;

   static {
      PARAMINFO_SIGNS = Constants.PARAMINFO_SIGNS;
      PARAMINFO_END = "%end%";
   }

   public static List<Paraminfo> readParams(File file) {
      return readParams(file, PARAMINFO_SIGNS, PARAMINFO_END);
   }

   public static List<Paraminfo> readParams(File file, String[] starts, String end) {
      List<Paraminfo> params = new ArrayList();
      if (file.exists()) {
         boolean flag = false;
         String line = "";
         StringBuilder value = new StringBuilder();
         int rows = 0;
         int columns = 0;
         Paraminfo para = new Paraminfo();
         FileLock lock = null;
         FileChannel fileChannel = null;
         FileInputStream fis = null;
         InputStreamReader isr = null;
         BufferedReader br = null;
         int var15 = 0;

         try {
            isr = new InputStreamReader(fis = new FileInputStream(file), "GBK");
            br = new BufferedReader(isr);
            fileChannel = fis.getChannel();

            do {
               try {
                  lock = fileChannel.tryLock(0L, Long.MAX_VALUE, true);
               } catch (Exception var63) {
               }

               if (lock != null) {
                  while(true) {
                     while((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!flag) {
                           String[] cells = line.split("\\s+");
                           String[] var20 = starts;
                           int var19 = starts.length;

                           for(int var68 = 0; var68 < var19; ++var68) {
                              String start = var20[var68];
                              if (line.toLowerCase().startsWith(start.toLowerCase()) && cells.length >= 3) {
                                 String name = cells[1];
                                 int sindex = cells[1].lastIndexOf("{");
                                 int eindex = cells[1].lastIndexOf("}");
                                 if (sindex != -1 && eindex != -1 && sindex < eindex) {
                                    String unit = name.substring(sindex + 1, eindex);
                                    para.setUnit(unit);
                                    name = name.substring(0, sindex);
                                 }

                                 int rsin = name.lastIndexOf("(");
                                 int rein = name.lastIndexOf(")");
                                 if (rsin != -1 && rein != -1 && rsin < rein) {
                                    String remark = name.substring(rsin + 1, rein);
                                    para.setRemark(remark);
                                    name = name.substring(0, rsin);
                                 }

                                 int sin = name.lastIndexOf("[");
                                 int ein = name.lastIndexOf("]");
                                 if (sin != -1 && ein != -1 && sin < ein) {
                                    String namesStr = name.substring(sin + 1, ein);
                                    String[] names = namesStr.split("&");
                                    int len = names.length;
                                    if (len == 2) {
                                       para.setColumnNames(names[0]);
                                       para.setRowNames(names[1]);
                                    } else if (len == 1) {
                                       para.setColumnNames(names[0]);
                                    }

                                    name = name.substring(0, sin);
                                 }

                                 int lin = name.lastIndexOf("<");
                                 int uin = name.lastIndexOf(">");
                                 if (lin != -1 && uin != -1 && lin < uin) {
                                    String range = name.substring(lin + 1, uin);
                                    String[] rgs = range.split(";");
                                    if (rgs.length == 2) {
                                       if (NumberUtils.isNumber(rgs[0])) {
                                          para.setLowerLimit(Double.valueOf(rgs[0]));
                                       }

                                       if (NumberUtils.isNumber(rgs[1])) {
                                          para.setUpperLimit(Double.valueOf(rgs[1]));
                                       }
                                    }

                                    name = name.substring(0, lin);
                                 }

                                 para.setSign(start);
                                 para.setName(name);
                                 para.setType(cells[2]);
                                 para.setUrl(file.getPath());
                                 flag = true;
                                 break;
                              }
                           }

                           cells = null;
                        } else if (flag && line.startsWith(end)) {
                           if (para.getSign().equals("%enum")) {
                              int sindex = value.lastIndexOf("{");
                              int eindex = value.lastIndexOf("}");
                              if (sindex != -1 && eindex != -1 && sindex < eindex) {
                                 String enumCons = value.substring(sindex + 1, eindex);
                                 para.setEnumCons(enumCons);
                                 para.setEnumTypes(enumCons.trim().split("\\s+"));
                                 value = new StringBuilder(value.substring(0, sindex));
                              } else {
                                 value = new StringBuilder(value.toString().trim());
                              }
                           }

                           if (para.getSign().equals("%ComArray")) {
                              --rows;
                           }

                           if (para.getSign().equals("%array1")) {
                              para.setValue(value.toString().trim().replace(" ", ","));
                           }

                           if (para.getSign().equals("%single") && (para.getType().equalsIgnoreCase("INT") || para.getType().equalsIgnoreCase("LNG") || para.getType().equalsIgnoreCase("FLT") || para.getType().equalsIgnoreCase("DBL"))) {
                              para.setValue(value.toString().trim().replace(",", ""));
                           } else {
                              para.setValue(value.toString().trim());
                           }

                           para.setRows(rows);
                           para.setColumns(columns);
                           params.add(para);
                           para = new Paraminfo();
                           value = new StringBuilder();
                           rows = 0;
                           columns = 0;
                           flag = false;
                        } else if (flag) {
                           if (rows == 0) {
                              columns = line.split("\\s+").length;
                              if (para.getSign().equals("%ComArray")) {
                                 para.setColumnNames(line);
                              } else {
                                 value = value.append(line).append("\r\n");
                              }

                              ++rows;
                           } else {
                              value = value.append(line).append("\r\n");
                              ++rows;
                           }
                        }
                     }

                     return params;
                  }
               }

               Thread.sleep(500L);
            } while(var15++ <= 9);

            System.out.println("\"" + file.getAbsolutePath() + "\"�ļ���ռ�ã�δ��ȡ�ļ������޷����ļ���");
            ArrayList var33 = (ArrayList) params;
            return var33;
         } catch (Exception var64) {
            var64.printStackTrace();
            System.out.println("��ȡ" + file.getName() + ":" + var64.getLocalizedMessage());
         } finally {
            if (lock != null) {
               try {
                  lock.release();
                  lock = null;
               } catch (IOException var62) {
                  var62.printStackTrace();
               }
            }

            if (fileChannel != null) {
               try {
                  fileChannel.close();
                  fileChannel = null;
               } catch (IOException var61) {
                  var61.printStackTrace();
               }
            }

            if (br != null) {
               try {
                  br.close();
                  br = null;
               } catch (IOException var60) {
                  var60.printStackTrace();
               }
            }

            if (isr != null) {
               try {
                  isr.close();
                  isr = null;
               } catch (IOException var59) {
                  var59.printStackTrace();
               }
            }

            if (fis != null) {
               try {
                  fis.close();
                  fis = null;
               } catch (IOException var58) {
                  var58.printStackTrace();
               }
            }

         }
      } else {
         System.out.println("ָ���ļ������ڣ�" + file.getAbsolutePath());
      }

      return params;
   }

   public static List<Paraminfo> readParams(File file, BufferedReader br) {
      List<Paraminfo> params = new ArrayList();
      String[] starts = PARAMINFO_SIGNS;
      String end = PARAMINFO_END;
      boolean flag = false;
      String line = "";
      StringBuilder value = new StringBuilder();
      int rows = 0;
      int columns = 0;
      Paraminfo para = new Paraminfo();
      InputStreamReader isr = null;

      try {
         isr = new InputStreamReader(new FileInputStream(file), "GBK");
         br = new BufferedReader(isr);

         while(true) {
            while((line = br.readLine()) != null) {
               line = line.trim();
               if (!flag) {
                  String[] cells = line.split("\\s+");
                  String[] var16 = starts;
                  int var15 = starts.length;

                  for(int var40 = 0; var40 < var15; ++var40) {
                     String start = var16[var40];
                     if (line.toLowerCase().startsWith(start.toLowerCase()) && cells.length >= 3) {
                        String name = cells[1];
                        int sindex = cells[1].lastIndexOf("{");
                        int eindex = cells[1].lastIndexOf("}");
                        if (sindex != -1 && eindex != -1 && sindex < eindex) {
                           String unit = name.substring(sindex + 1, eindex);
                           para.setUnit(unit);
                           name = name.substring(0, sindex);
                        }

                        int rsin = name.lastIndexOf("(");
                        int rein = name.lastIndexOf(")");
                        if (rsin != -1 && rein != -1 && rsin < rein) {
                           String remark = name.substring(rsin + 1, rein);
                           para.setRemark(remark);
                           name = name.substring(0, rsin);
                        }

                        int sin = name.lastIndexOf("[");
                        int ein = name.lastIndexOf("]");
                        if (sin != -1 && ein != -1 && sin < ein) {
                           String namesStr = name.substring(sin + 1, ein);
                           String[] names = namesStr.split("&");
                           int len = names.length;
                           if (len == 2) {
                              para.setColumnNames(names[0]);
                              para.setRowNames(names[1]);
                           } else if (len == 1) {
                              para.setColumnNames(names[0]);
                           }

                           name = name.substring(0, sin);
                        }

                        int lin = name.lastIndexOf("<");
                        int uin = name.lastIndexOf(">");
                        if (lin != -1 && uin != -1 && lin < uin) {
                           String range = name.substring(lin + 1, uin);
                           String[] rgs = range.split(";");
                           if (rgs.length == 2) {
                              if (NumberUtils.isNumber(rgs[0])) {
                                 para.setLowerLimit(Double.valueOf(rgs[0]));
                              }

                              if (NumberUtils.isNumber(rgs[1])) {
                                 para.setUpperLimit(Double.valueOf(rgs[1]));
                              }
                           }

                           name = name.substring(0, lin);
                        }

                        para.setSign(start);
                        para.setName(name);
                        para.setType(cells[2]);
                        para.setUrl(file.getPath());
                        flag = true;
                        break;
                     }
                  }

                  cells = null;
               } else if (flag && line.startsWith(end)) {
                  if (para.getSign().equals("%enum")) {
                     int sindex = value.lastIndexOf("{");
                     int eindex = value.lastIndexOf("}");
                     if (sindex != -1 && eindex != -1 && sindex < eindex) {
                        String enumCons = value.substring(sindex + 1, eindex);
                        para.setEnumCons(enumCons);
                        para.setEnumTypes(enumCons.trim().split("\\s+"));
                        value = new StringBuilder(value.substring(0, sindex));
                     } else {
                        value = new StringBuilder(value.toString().trim());
                     }
                  }

                  if (para.getSign().equals("%ComArray")) {
                     --rows;
                  }

                  if (!para.getSign().equals("%single") || !para.getType().equalsIgnoreCase("INT") && !para.getType().equalsIgnoreCase("LNG") && !para.getType().equalsIgnoreCase("FLT") && !para.getType().equalsIgnoreCase("DBL")) {
                     para.setValue(value.toString().trim());
                  } else {
                     para.setValue(value.toString().trim().replace(",", ""));
                  }

                  para.setRows(rows);
                  para.setColumns(columns);
                  params.add(para);
                  para = new Paraminfo();
                  value = new StringBuilder();
                  rows = 0;
                  columns = 0;
                  flag = false;
               } else if (flag) {
                  if (rows == 0) {
                     columns = line.split("\\s+").length;
                     if (para.getSign().equals("%ComArray")) {
                        para.setColumnNames(line);
                     } else {
                        value = value.append(line).append("\r\n");
                     }

                     ++rows;
                  } else {
                     value = value.append(line).append("\r\n");
                     ++rows;
                  }
               }
            }

            return params;
         }
      } catch (Exception var36) {
         var36.printStackTrace();
         System.out.println("��ȡ" + file.getName() + ":" + var36.getLocalizedMessage());
      } finally {
         try {
            if (br != null) {
               br.close();
            }

            if (isr != null) {
               isr.close();
            }
         } catch (Exception var35) {
            var35.printStackTrace();
         }

      }

      return params;
   }

   public static List<String> getColumnFromLists(List<List<String>> lists, int column) {
      List<String> clist = new ArrayList();
      Iterator var4 = lists.iterator();

      while(var4.hasNext()) {
         List<String> list = (List)var4.next();
         clist.add(((String)list.get(column)).trim());
      }

      return clist;
   }

   public static List<String> getColumn(List<Paraminfo> params, int column) {
      List<String> lists = new ArrayList();
      Iterator var4 = params.iterator();

      while(var4.hasNext()) {
         Paraminfo param = (Paraminfo)var4.next();
         String[] lines = param.getValue().split("\n");
         String[] var9 = lines;
         int var8 = lines.length;

         for(int var7 = 0; var7 < var8; ++var7) {
            String line = var9[var7];
            String[] cells = line.trim().split("\\s+");
            lists.add(cells[column].trim());
         }
      }

      lists.remove(0);
      return lists;
   }

   public static List<List<String>> getLists(String paramValue) {
      List<List<String>> lists = new ArrayList();
      String[] lines = paramValue.split("\n");
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

   public static String getParamValue(List<Paraminfo> params, String paramName) {
      String paramValue = "";
      Iterator var4 = params.iterator();

      while(var4.hasNext()) {
         Paraminfo param = (Paraminfo)var4.next();
         if (param.getName().equals(paramName.trim())) {
            paramValue = param.getValue();
            break;
         }
      }

      return paramValue;
   }

   public static Paraminfo getParaminfo(List<Paraminfo> Paraminfos, String paramName) {
      Paraminfo Paraminfo = null;
      Iterator var4 = Paraminfos.iterator();

      while(var4.hasNext()) {
         Paraminfo param = (Paraminfo)var4.next();
         if (param.getName().equals(paramName.trim())) {
            Paraminfo = param;
            break;
         }
      }

      return Paraminfo;
   }

   public static Paraminfo getParaminfo(List<Paraminfo> Paraminfos, Paraminfo Paraminfo) {
      Iterator var3 = Paraminfos.iterator();

      while(var3.hasNext()) {
         Paraminfo param = (Paraminfo)var3.next();
         if (param.getName().equals(Paraminfo.getName())) {
            Paraminfo.setValue(param.getValue());
            break;
         }
      }

      return Paraminfo;
   }

   public static Paraminfo getParaminfo(File file, String paramName) {
      List<Paraminfo> paramss = readParams(file);
      Paraminfo Paraminfo = null;
      Iterator var5 = paramss.iterator();

      while(var5.hasNext()) {
         Paraminfo param = (Paraminfo)var5.next();
         if (param.getName().equals(paramName.trim())) {
            Paraminfo = param;
            break;
         }
      }

      return Paraminfo;
   }

   public static Paraminfo getParaminfo(Paraminfo para) {
      ParamChild paramChild;
      if (para instanceof ParamChild) {
         paramChild = (ParamChild)para;
         Paraminfo param = paramChild.getParaminfo();
         List<Paraminfo> params = readParams(new File(param.getUrl().trim()));
         param = getParaminfo(params, param);
         paramChild.setParaminfo(param);
         String[][] arrays;
         if (paramChild.getColumn().equals(-1)) {
            arrays = StringUtils.toArraysFromString(param.getValue());
            String[] array = arrays[paramChild.getRow()];
            paramChild.setValue(StringUtils.toString(array));
         } else {
            arrays = StringUtils.toArraysFromString(param.getValue());
            paramChild.setValue(arrays[paramChild.getRow()][paramChild.getColumn()]);
         }

         params.clear();
         params = null;
      } else {
         List<Paraminfo> paramss = readParams(new File(para.getUrl().trim()));
         para = getParaminfo(paramss, para);
         paramss.clear();
         paramChild = null;
      }

      return para;
   }

   public static boolean isSingleNumber(Paraminfo param) {
      String type = param.getType();
      return param.getSign().equalsIgnoreCase("%single") && (type.equalsIgnoreCase("INT") || type.equalsIgnoreCase("LNG") || type.equalsIgnoreCase("FLT") || type.equalsIgnoreCase("DBL"));
   }
}
