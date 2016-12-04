package net.dankito.appdownloader.app.apkverifier;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 22/11/16.
 */

public class AndroidObservatoryAkpSignatureVerifierTest {

  protected static final String VALID_SIGNATURE_KNOWN_TO_ANDROID_OBSERVATORY = "889ee6c8ec3c7dc460a5cec01620fa8b4a3f445e"; // NoRoot Firewall
  protected static final String APP_TITLE_OF_APP_KNOWN_TO_ANDROID_OBSERVATORY = "NoRoot Firewall";
  protected static final String PACKAGE_NAME_OF_APP_KNOWN_TO_ANDROID_OBSERVATORY = "app.greyshirts.firewall";

  protected static final String VALID_SIGNATURE_NOT_KNOWN_TO_ANDROID_OBSERVATORY = "79a7b1a36ab88550403b1fa34d47473713a81031"; // AutoStart
  protected static final String APP_TITLE_OF_APP_NOT_KNOWN_TO_ANDROID_OBSERVATORY = "AutoStart - No root";
  protected static final String PACKAGE_NAME_OF_APP_NOT_KNOWN_TO_ANDROID_OBSERVATORY = "com.autostart";


  protected AndroidObservatoryAkpSignatureVerifier underTest;


  @Before
  public void setUp() {
    underTest = new AndroidObservatoryAkpSignatureVerifier(new OkHttpWebClient());
  }


  @Test
  public void validSignatureKnownToAndroidObservatory_Succeeds() {
    AppInfo appInfo = new AppInfo(PACKAGE_NAME_OF_APP_KNOWN_TO_ANDROID_OBSERVATORY);
    appInfo.setTitle(APP_TITLE_OF_APP_KNOWN_TO_ANDROID_OBSERVATORY);
    DownloadedApkInfo downloadedApkInfo = new DownloadedApkInfo(appInfo, null, PACKAGE_NAME_OF_APP_KNOWN_TO_ANDROID_OBSERVATORY);
    List<String> signatureDigests = new ArrayList<>();
    signatureDigests.add(VALID_SIGNATURE_KNOWN_TO_ANDROID_OBSERVATORY);
    downloadedApkInfo.setSignatureDigests(signatureDigests);

    final List<VerifyApkFileResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyApkFile(downloadedApkInfo, new VerifyApkFileCallback() {
      @Override
      public void completed(VerifyApkFileResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    VerifyApkFileResult result = resultList.get(0);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertTrue(result.knowsApkSignature());
    Assert.assertTrue(result.couldVerifyApkSignature());
  }

  @Test
  public void validSignatureNotKnownToAndroidObservatory_Fails() {
    AppInfo appInfo = new AppInfo(PACKAGE_NAME_OF_APP_NOT_KNOWN_TO_ANDROID_OBSERVATORY);
    appInfo.setTitle(APP_TITLE_OF_APP_NOT_KNOWN_TO_ANDROID_OBSERVATORY);
    DownloadedApkInfo downloadedApkInfo = new DownloadedApkInfo(appInfo, null, PACKAGE_NAME_OF_APP_NOT_KNOWN_TO_ANDROID_OBSERVATORY);
    List<String> signatureDigests = new ArrayList<>();
    signatureDigests.add(VALID_SIGNATURE_NOT_KNOWN_TO_ANDROID_OBSERVATORY);
    downloadedApkInfo.setSignatureDigests(signatureDigests);

    final List<VerifyApkFileResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyApkFile(downloadedApkInfo, new VerifyApkFileCallback() {
      @Override
      public void completed(VerifyApkFileResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    VerifyApkFileResult result = resultList.get(0);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertFalse(result.knowsApkSignature());
    Assert.assertFalse(result.couldVerifyApkSignature());
  }

}
