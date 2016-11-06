package net.dankito.appdownloader.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ganymed on 03/11/16.
 */

public class ThreadPool implements IThreadPool {

  protected ExecutorService executorService = Executors.newCachedThreadPool();


  public void runAsync(Runnable task) {
    executorService.submit(task);
  }

}
