package net.dankito.appdownloader.util.web.model;

/**
 * Created by ganymed on 04/12/16.
 */

public abstract class RequestBodyPart {

  protected String fieldName;


  public RequestBodyPart(String fieldName) {
    this.fieldName = fieldName;
  }


  public String getFieldName() {
    return fieldName;
  }


  @Override
  public String toString() {
    return getFieldName();
  }

}
