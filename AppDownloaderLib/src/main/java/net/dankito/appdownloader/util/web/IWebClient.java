package net.dankito.appdownloader.util.web;

/**
 * Created by ganymed on 03/11/16.
 */

public interface IWebClient {

  void getAsync(RequestParameters parameters, final RequestCallback callback);

  void postAsync(RequestParameters parameters, final RequestCallback callback);

  WebClientResponse head(RequestParameters parameters);

}
