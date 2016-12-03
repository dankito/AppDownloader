package net.dankito.appdownloader.app.apkverifier.virustotal;

import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
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
public class VirusTotalApkFileVerifierTest {

  protected static final String AUTO_START_SHA_256 = "be805cbbfacf5ab11313b287104a51db28dada359042799e2dd03cc7898be88c";

  protected static final String NO_ROOT_FIREWALL_SHA_256 = "b8c7e4fd106c3be8fa0cf02d4d1ff805b9e858f8328fa0f88ca1b91581323bc5";


  protected VirusTotalApkFileVerifier underTest;


  @Before
  public void setUp() {
    underTest = new VirusTotalApkFileVerifier(new OkHttpWebClient());
  }


  @Test
  public void checkKnownFile_Succeeds() throws Exception {
    final List<VerifyApkFileResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    DownloadedApkInfo downloadedApkInfo = new DownloadedApkInfo(new AppInfo(""), null, "");
    downloadedApkInfo.setSha256CheckSum(NO_ROOT_FIREWALL_SHA_256);

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
    Assert.assertTrue(result.knowsFileChecksum());
    Assert.assertTrue(result.couldVerifyFileChecksum());
    Assert.assertTrue(result.isFileChecksumFromIndependentSource());
  }

//  @Test
//  public void fileNotKnownToVirusTotal_Fails() throws Exception {
//    boolean result = underTest.verifyApkFile(new AppInfo(""), "", "", "a" + NO_ROOT_FIREWALL_SHA_256.substring(1)); // fake a SHA256
//
//    Assert.assertFalse(result);
//  }

}