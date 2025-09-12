package net.sf.openrocket.rw;

import java.io.Serializable;

public class Paraminfo extends BasicParam implements Serializable {
   private Long paramId;
   private String name;
   private String value;
   private String iotype;
   private String sign;
   private String type;
   private String unit = "";
   private Double lowerLimit = 0.0D;
   private Double upperLimit = 0.0D;
   private String columnNames = "";
   private String rowNames = "";
   private String enumCons = "";
   private Integer rows = 0;
   private Integer columns = 0;
   private String remark = "";
   private Boolean result = true;

   public Paraminfo() {
   }

   public Paraminfo(Long paramId) {
      this.paramId = paramId;
   }

   public Paraminfo(String name, String value, String sign, String type, String iotype, String initiotype, String unit, String url) {
      this.name = name;
      this.value = value;
      this.sign = sign;
      this.type = type;
      this.iotype = iotype;
      this.initiotype = initiotype;
      this.unit = unit;
      this.url = url;
   }

   public Paraminfo(String name, String value, String sign, String type) {
      this.name = name;
      this.value = value;
      this.sign = sign;
      this.type = type;
   }

   public Paraminfo(String name, String value, String sign, String type, String unit) {
      this.name = name;
      this.value = value;
      this.sign = sign;
      this.type = type;
      this.unit = unit;
   }

   public Paraminfo(String name, String value, String sign, String type, String unit, String url, String columnNames) {
      this.name = name;
      this.value = value;
      this.sign = sign;
      this.type = type;
      this.unit = unit;
      this.url = url;
      this.columnNames = columnNames;
   }

   public Paraminfo(String name, String value, String sign, String type, String unit, String url) {
      this.name = name;
      this.value = value;
      this.sign = sign;
      this.type = type;
      this.unit = unit;
      this.url = url;
   }

   public Paraminfo(Long paramId, String name, String value, String iotype, String sign, String type, String unit, String columnNames, String rowNames, Integer rows, Integer columns, String enumCons, String remark) {
      this.paramId = paramId;
      this.name = name;
      this.value = value;
      this.iotype = iotype;
      this.initiotype = iotype;
      this.sign = sign;
      this.type = type;
      this.unit = unit;
      this.columnNames = columnNames;
      this.rowNames = rowNames;
      this.rows = rows;
      this.columns = columns;
      this.enumCons = enumCons;
      this.remark = remark;
   }

   public Long getParamId() {
      return this.paramId;
   }

   public void setParamId(Long paramId) {
      this.paramId = paramId;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
      this.result = true;
   }

   public String getIotype() {
      return this.iotype;
   }

   public void setIotype(String iotype) {
      this.iotype = iotype;
   }

   public String getSign() {
      return this.sign;
   }

   public void setSign(String sign) {
      this.sign = sign;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getUnit() {
      return this.unit;
   }

   public void setUnit(String unit) {
      this.unit = unit;
   }

   public Double getLowerLimit() {
      return this.lowerLimit;
   }

   public void setLowerLimit(Double lowerLimit) {
      this.lowerLimit = lowerLimit;
   }

   public Double getUpperLimit() {
      return this.upperLimit;
   }

   public void setUpperLimit(Double upperLimit) {
      this.upperLimit = upperLimit;
   }

   public String getColumnNames() {
      return this.columnNames;
   }

   public void setColumnNames(String columnNames) {
      this.columnNames = columnNames;
   }

   public String getRowNames() {
      return this.rowNames;
   }

   public void setRowNames(String rowNames) {
      this.rowNames = rowNames;
   }

   public String getEnumCons() {
      return this.enumCons;
   }

   public void setEnumCons(String enumCons) {
      this.enumCons = enumCons;
   }

   public Integer getRows() {
      return this.rows;
   }

   public void setRows(Integer rows) {
      this.rows = rows;
   }

   public Integer getColumns() {
      return this.columns;
   }

   public void setColumns(Integer columns) {
      this.columns = columns;
   }

   public String getRemark() {
      return this.remark;
   }

   public void setRemark(String remark) {
      this.remark = remark;
   }

   public String getFullName() {
      return (this.docName == null ? "" : this.docName + ".") + this.name;
   }

   @Override
   public String toString() {
      return "Paraminfo{" +
              "paramId=" + paramId +
              ", name='" + name + '\'' +
              ", value='" + value + '\'' +
              ", iotype='" + iotype + '\'' +
              ", sign='" + sign + '\'' +
              ", type='" + type + '\'' +
              ", unit='" + unit + '\'' +
              ", lowerLimit=" + lowerLimit +
              ", upperLimit=" + upperLimit +
              ", columnNames='" + columnNames + '\'' +
              ", rowNames='" + rowNames + '\'' +
              ", enumCons='" + enumCons + '\'' +
              ", rows=" + rows +
              ", columns=" + columns +
              ", remark='" + remark + '\'' +
              ", result=" + result +
              '}';
   }

   public boolean isResult() {
      return this.result;
   }

   public void setResult(boolean result) {
      this.result = result;
   }
}
