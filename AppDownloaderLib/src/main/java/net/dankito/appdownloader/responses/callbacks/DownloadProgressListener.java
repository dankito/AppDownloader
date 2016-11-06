package net.dankito.appdownloader.responses.callbacks;

/**
 * Created by ganymed on 03/11/16.
 */

public interface DownloadProgressListener {

  void progress(float progress, byte[] downloadedChunk);

}
