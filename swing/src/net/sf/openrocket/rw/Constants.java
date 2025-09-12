package net.sf.openrocket.rw;

public class Constants {
   public static final String SYMBOL_RETURN = "\r";
   public static final String SYMBOL_SIMPLE_WRAP = "\n";
   public static final String SYMBOL_WRAP = "\r\n";
   public static final String SYMBOL_SPACE = " ";
   public static final String SYMBOL_SPACE_FOUR = "    ";
   public static final String REG_SPACE = "\\s+";
   public static final String SINGLE = "%single";
   public static final String ARRAY1 = "%array1";
   public static final String ARRAY2 = "%array2";
   public static final String COMARRAY = "%ComArray";
   public static final String AERO = "%aero";
   public static final String ENUM = "%enum";
   public static final String STRUCT = "%struct";
   public static final String[] PARAMINFO_SIGNS = new String[]{"%single", "%array1", "%array2", "%ComArray", "%aero", "%enum"};
   public static final String PARAMINFO_END = "%end%";
   public static final String STR = "STR";
   public static final String INT = "INT";
   public static final String LNG = "LNG";
   public static final String FLT = "FLT";
   public static final String DBL = "DBL";
   public static final String FILE = "FILE";
   public static final String[] DATA_TYPES = new String[]{"FLT", "INT", "STR", "FILE"};
}
