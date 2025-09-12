package net.sf.openrocket.rw;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WriteNormalFile {
   private static ArrayList<String> PARAMINFO_SIGNS_LIST = new ArrayList();
   private static String PARAMINFO_END;

   static {
      PARAMINFO_END = ReadNormalFile.PARAMINFO_END;
      String[] PARAMINFO_SIGNS = Constants.PARAMINFO_SIGNS;

      for(int i = 0; i < PARAMINFO_SIGNS.length; ++i) {
         PARAMINFO_SIGNS_LIST.add(PARAMINFO_SIGNS[i]);
      }

   }

   public static boolean writeParam(Paraminfo para, String fileUrl) {
      List<Paraminfo> paras = new ArrayList();
      paras.add(para);
      return writeParams(paras, fileUrl, false);
   }

   public static boolean writeParam(Paraminfo para) {
      List<Paraminfo> paras = new ArrayList();
      paras.add(para);
      return writeParams(paras, para.getUrl(), false);
   }

   public static boolean appendParams(List<Paraminfo> paras, String fileUrl) {
      return writeParams(paras, fileUrl, true);
   }

   public static boolean appendParam(Paraminfo para, String fileUrl) {
      List<Paraminfo> paras = new ArrayList();
      paras.add(para);
      return writeParams(paras, fileUrl, true);
   }

   public static boolean appendParam(Paraminfo para) {
      List<Paraminfo> paras = new ArrayList();
      paras.add(para);
      return writeParams(paras, para.getUrl(), true);
   }

   public static boolean writeParams(List<Paraminfo> paras, String fileUrl) {
      return writeParams(paras, fileUrl, false);
   }

   public static boolean writeParams(List<Paraminfo> paras, String fileUrl, boolean isAppend) {
      boolean flag = true;
      StringBuilder parVal = new StringBuilder();

      String sign;
      String names;
      String remark;
      String unit;
      for(int i = 0; i < paras.size(); ++i) {
         Paraminfo para = (Paraminfo)paras.get(i);
         if (para != null) {
            sign = para.getSign();
            if (!PARAMINFO_SIGNS_LIST.contains(sign)) {
               flag = false;
               break;
            }

            names = null;
            if ((para.getColumnNames() == null || para.getColumnNames().equals("")) && (para.getRowNames() == null || para.getRowNames().equals(""))) {
               names = "";
            } else if (para.getColumnNames() != null && !para.getColumnNames().equals("")) {
               if (para.getRowNames() != null && !para.getRowNames().equals("")) {
                  names = "[" + para.getColumnNames() + "&" + para.getRowNames() + "]";
               } else {
                  names = "[" + para.getColumnNames() + "]";
               }
            } else {
               names = "[&" + para.getRowNames() + "]";
            }

            remark = para.getRemark() == null ? "" : (para.getRemark().equals("") ? "" : "(" + para.getRemark() + ")");
            unit = para.getUnit() == null ? "" : (para.getUnit().equals("") ? "" : "{" + para.getUnit() + "}");
            if (!sign.equals("%single")) {
               String[] rows;
               if (sign.equals("%array1")) {
                  rows = para.getValue().split("\\s+");
                  parVal.append(para.getSign() + "  " + para.getName() + names + remark + unit + "  " + para.getType() + "  " + rows.length + "  %");
                  parVal.append("\r\n");
                  parVal.append(para.getValue());
               } else {
                  String[] columns;
                  if (sign.equals("%array2")) {
                     rows = para.getValue().split("\n");
                     columns = rows[0].trim().split("\\s+");
                     parVal.append(para.getSign() + "  " + para.getName() + names + remark + unit + "  " + para.getType() + "  " + rows.length + "  " + columns.length + "  %");
                     parVal.append("\r\n");
                     parVal.append(para.getValue());
                  } else if (sign.equals("%aero")) {
                     rows = para.getValue().split("\n");
                     columns = rows[1].trim().split("\\s+");
                     parVal.append(para.getSign() + "  " + para.getName() + names + remark + unit + "  " + para.getType() + "  " + rows.length + "  " + columns.length + "  %");
                     parVal.append("\r\n");
                     parVal.append(para.getValue());
                  } else if (sign.equals("%ComArray")) {
                     rows = para.getValue().split("\n");
                     columns = rows[0].trim().split("\\s+");
                     parVal.append(para.getSign() + "  " + para.getName() + remark + unit + "  " + para.getType() + "  " + rows.length + "  " + columns.length + "  %");
                     parVal.append("\r\n");
                     parVal.append(para.getColumnNames());
                     parVal.append("\r\n");
                     parVal.append(para.getValue());
                  } else if (sign.equals("%enum")) {
                     parVal.append(para.getSign() + "  " + para.getName() + remark + unit + "  " + para.getType() + "  %");
                     parVal.append("\r\n");
                     parVal.append(para.getValue()).append("{" + para.getEnumCons() + "}");
                  }
               }
            } else {
               String range = null;
               if (para.getLowerLimit() == 0.0D && para.getUpperLimit() == 0.0D) {
                  range = "";
               } else if (para.getLowerLimit() == null) {
                  range = "<NULL;" + para.getUpperLimit() + ">";
               } else if (para.getUpperLimit() == null) {
                  range = "<" + para.getLowerLimit() + ";NULL>";
               } else {
                  range = "<" + para.getLowerLimit() + ";" + para.getUpperLimit() + ">";
               }

               parVal.append(para.getSign() + "  " + para.getName() + range + remark + unit + "  " + para.getType() + "  %");
               parVal.append("\r\n");
               parVal.append(para.getValue());
            }

            parVal.append("\r\n");
            parVal.append(PARAMINFO_END);
            parVal.append("\r\n");
         }
      }

      if (!flag) {
         System.out.println("某参数对象数据类型不存在。");
         return flag;
      } else {
         FileOutputStream fos = null;
         OutputStreamWriter osw = null;
         BufferedWriter bw = null;
         FileLock lock = null;
         FileChannel fileChannel = null;
         File file = new File(fileUrl);
         int var52 = 0;

         try {
            osw = new OutputStreamWriter(fos = new FileOutputStream(file, isAppend), "GBK");
            bw = new BufferedWriter(osw);
            fileChannel = fos.getChannel();

            do {
               try {
                  lock = fileChannel.tryLock();
               } catch (Exception var44) {
               }

               if (lock != null) {
                  bw.write(parVal.toString());
                  bw.flush();
                  return flag;
               }

               Thread.sleep(500L);
            } while(var52++ <= 9);

            System.out.println("\"" + file.getAbsolutePath() + "\"文件被占用，未获取文件锁，无法写文件。");
            return false;
         } catch (Exception var45) {
            var45.printStackTrace();
            System.out.println("\"" + file.getAbsolutePath() + "\"文件被占用，未获取文件锁，无法写文件。");
            flag = false;
            return flag;
         } finally {
            if (lock != null) {
               try {
                  lock.release();
                  remark = null;
               } catch (IOException var43) {
                  var43.printStackTrace();
               }
            }

            if (fileChannel != null) {
               try {
                  fileChannel.close();
                  unit = null;
               } catch (IOException var42) {
                  var42.printStackTrace();
               }
            }

            if (bw != null) {
               try {
                  bw.close();
                  names = null;
               } catch (IOException var41) {
                  var41.printStackTrace();
               }
            }

            if (osw != null) {
               try {
                  osw.close();
                  sign = null;
               } catch (IOException var40) {
                  var40.printStackTrace();
               }
            }

            if (fos != null) {
               try {
                  fos.close();
                  fos = null;
               } catch (IOException var39) {
                  var39.printStackTrace();
               }
            }

         }
      }
   }

   public static boolean updateParam(String oldName, Paraminfo newParamInfo) {
      boolean flag = false;

      try {
         File file = new File(newParamInfo.getUrl());
         List<Paraminfo> paras = ReadNormalFile.readParams(file);
         List<Paraminfo> params = new ArrayList();
         Iterator var7 = paras.iterator();

         while(var7.hasNext()) {
            Paraminfo param = (Paraminfo)var7.next();
            if (oldName.trim().equals(param.getName())) {
               params.add(newParamInfo);
               flag = true;
            } else {
               params.add(param);
            }
         }

         if (flag) {
            flag = writeParams(params, file.getAbsolutePath());
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean updateParamIOType(Paraminfo oldParamInfo, String newUrl) {
      boolean flag = true;

      try {
         flag = deleteParam(oldParamInfo);
         if (flag) {
            oldParamInfo.setUrl(newUrl);
            flag = appendParam(oldParamInfo);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean deleteParam(Paraminfo para) {
      boolean flag = false;

      try {
         File file = new File(para.getUrl());
         List<Paraminfo> paras = ReadNormalFile.readParams(file);
         Iterator var5 = paras.iterator();

         while(var5.hasNext()) {
            Paraminfo removeParam = (Paraminfo)var5.next();
            if (para.getName().equals(removeParam.getName().trim())) {
               paras.remove(removeParam);
               flag = true;
               break;
            }
         }

         if (flag) {
            flag = writeParams(paras, para.getUrl());
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean updateParam(Paraminfo para) {
      boolean flag = true;

      try {
         ParamChild paramChild;
         Paraminfo param;
         String[][] arrays;
         if (para instanceof ParamChild) {
            paramChild = (ParamChild)para;
            param = paramChild.getParaminfo();
            if (paramChild.getColumn().equals(-1)) {
               arrays = StringUtils.toArraysFromString(param.getValue());
               String[] array = StringUtils.toArrayFromString(paramChild.getValue());

               for(int i = 0; i < arrays[0].length; ++i) {
                  arrays[paramChild.getRow()][i] = array[i];
               }

               param.setValue(StringUtils.toString(arrays));
               arrays = null;
            } else {
               arrays = StringUtils.toArraysFromString(param.getValue());
               arrays[paramChild.getRow()][paramChild.getColumn()] = paramChild.getValue();
               param.setValue(StringUtils.toString(arrays));
               arrays = null;
            }

            para = param;
         }

         File file = new File(para.getUrl());
         List<Paraminfo> paras = ReadNormalFile.readParams(file);
         List<Paraminfo> params = new ArrayList();
         Iterator var12 = paras.iterator();

         while(var12.hasNext()) {
            Paraminfo param1 = (Paraminfo)var12.next();
            if (para.getName().trim().equals(param1.getName())) {
               params.add(para);
               flag = true;
            } else {
               params.add(param1);
            }
         }

         if (flag) {
            flag = writeParams(params, file.getAbsolutePath());
         }

         paras.clear();
         param = null;
         params.clear();
         arrays = null;
         paramChild = null;
      } catch (Exception var7) {
         var7.printStackTrace();
         flag = false;
      }

      return flag;
   }

   public static boolean updateParamValue(String name, String value, String url) {
      try {
         File file = new File(url);
         List<Paraminfo> params = ReadNormalFile.readParams(file);
         Iterator var6 = params.iterator();

         while(var6.hasNext()) {
            Paraminfo param = (Paraminfo)var6.next();
            if (param.getName().trim().equals(name)) {
               param.setValue(value);
            }
         }

         writeParams(params, file.getAbsolutePath());
         return true;
      } catch (Exception var7) {
         var7.printStackTrace();
         return false;
      }
   }
}
