package net.sf.openrocket.rw;

public class BasicParam {
   protected String initiotype;
   protected String url;
   protected String[] enumTypes;
   protected boolean initConfig;
   protected String initConfigUrl;
   protected boolean isInnerSendedValue = false;
   protected boolean isOuterSendedValue = false;
   protected boolean isOutputValue = false;
   protected String docName;

   public String getInitiotype() {
      return this.initiotype;
   }

   public void setInitiotype(String initiotype) {
      this.initiotype = initiotype;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String[] getEnumTypes() {
      return this.enumTypes;
   }

   public void setEnumTypes(String[] enumTypes) {
      this.enumTypes = enumTypes;
   }

   public boolean isInitConfig() {
      return this.initConfig;
   }

   public void setInitConfig(boolean initConfig) {
      this.initConfig = initConfig;
   }

   public String getInitConfigUrl() {
      return this.initConfigUrl;
   }

   public void setInitConfigUrl(String initConfigUrl) {
      this.initConfigUrl = initConfigUrl;
   }

   public boolean isInnerSendedValue() {
      return this.isInnerSendedValue;
   }

   public void setInnerSendedValue(boolean isInnerSendedValue) {
      this.isInnerSendedValue = isInnerSendedValue;
   }

   public boolean isOuterSendedValue() {
      return this.isOuterSendedValue;
   }

   public void setOuterSendedValue(boolean isOuterSendedValue) {
      this.isOuterSendedValue = isOuterSendedValue;
   }

   public boolean isOutputValue() {
      return this.isOutputValue;
   }

   public void setOutputValue(boolean isOutputValue) {
      this.isOutputValue = isOutputValue;
   }

   public String getDocName() {
      return this.docName;
   }

   public void setDocName(String docName) {
      this.docName = docName;
   }
}
