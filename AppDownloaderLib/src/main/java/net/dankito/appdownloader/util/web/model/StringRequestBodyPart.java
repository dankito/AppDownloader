package net.dankito.appdownloader.util.web.model;

/**
 * Created by ganymed on 04/12/16.
 */

public class StringRequestBodyPart extends RequestBodyPart {

  protected String value;


  public StringRequestBodyPart(String name, String value) {
    super(name);
    this.value = value;
  }


  public String getValue() {
    return value;
  }


  @Override
  public String toString() {
    return getFieldName() + " = " + getValue();
  }

}
