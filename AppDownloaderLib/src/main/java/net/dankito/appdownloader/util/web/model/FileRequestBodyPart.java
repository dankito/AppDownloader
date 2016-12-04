package net.dankito.appdownloader.util.web.model;

import java.io.File;

/**
 * Created by ganymed on 04/12/16.
 */

public class FileRequestBodyPart extends RequestBodyPart {

  protected File file;

  protected String fileMimeType;

  protected String filename;


  public FileRequestBodyPart(String name, File file, String fileMimeType) {
    super(name);
    this.fileMimeType = fileMimeType;
    this.file = file;
  }

  public FileRequestBodyPart(String name, File file, String fileMimeType, String filename) {
    this(name, file, fileMimeType);
    this.filename = filename;
  }


  public File getFile() {
    return file;
  }

  public String getFileMimeType() {
    return fileMimeType;
  }

  public String getFilename() {
    return filename;
  }


  @Override
  public String toString() {
    return getFile().getName();
  }

}
