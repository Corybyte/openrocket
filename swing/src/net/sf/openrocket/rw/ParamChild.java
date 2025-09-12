package net.sf.openrocket.rw;

import java.io.Serializable;

public class ParamChild extends Paraminfo implements Serializable {
   private static final long serialVersionUID = 1L;
   private int row = 0;
   private int column = 0;
   private Paraminfo paraminfo;

   public ParamChild() {
   }

   public ParamChild(String name, String value, String sign, String type, String unit, int row, int column) {
      super(name, value, sign, type, unit, (String)null);
      this.row = row;
      this.column = column;
   }

   public ParamChild(String name, String value, String unit, String sign, String type, String iotype, String initiotype, int row, int column, String url) {
      super(name, value, sign, type, iotype, initiotype, unit, url);
      this.row = row;
      this.column = column;
   }

   public Integer getRow() {
      return this.row;
   }

   public void setRow(Integer row) {
      this.row = row;
   }

   public Integer getColumn() {
      return this.column;
   }

   public void setColumn(Integer column) {
      this.column = column;
   }

   public Paraminfo getParaminfo() {
      return this.paraminfo;
   }

   public void setParaminfo(Paraminfo paraminfo) {
      this.paraminfo = paraminfo;
   }
}
