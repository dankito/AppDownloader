package net.dankito.appdownloader.app.apkverifier.virustotal;

import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.util.apps.TestAppDownloader;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 22/11/16.
 */
public class VirusTotalApkFileVerifierTest {

  protected static final String AUTO_START_APP_FILENAME = "AutoStart.apk";

  protected static final String AUTO_START_SHA_256 = "be805cbbfacf5ab11313b287104a51db28dada359042799e2dd03cc7898be88c";

  protected static final String NO_ROOT_FIREWALL_SHA_256 = "b8c7e4fd106c3be8fa0cf02d4d1ff805b9e858f8328fa0f88ca1b91581323bc5";


  protected static String testApkFilePath = null;


  protected VirusTotalApkFileVerifier underTest;



  @BeforeClass
  public static void provideTestApks() throws Exception {
    testApkFilePath = copyFromResourcesToReadableDirectory(AUTO_START_APP_FILENAME).getAbsolutePath();
  }

  @AfterClass
  public static void deleteTestApks() throws Exception {
    if(testApkFilePath != null) {
      deleteFile(testApkFilePath);
    }
  }


  @Before
  public void setUp() throws Exception {
    underTest = new VirusTotalApkFileVerifier(new OkHttpWebClient());
  }


  @Test
  public void checkKnownFile_Succeeds() throws Exception {
    DownloadedApkInfo downloadedApkInfo = createTestDownloadedApkInfo();

    final List<VerifyApkFileResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyApkFile(downloadedApkInfo, new VerifyApkFileCallback() {
      @Override
      public void completed(VerifyApkFileResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(30, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    VerifyApkFileResult result = resultList.get(0);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertTrue(result.knowsFileChecksum());
    Assert.assertTrue(result.couldVerifyFileChecksum());
    Assert.assertTrue(result.isFileChecksumFromIndependentSource());
  }

  @Test
  public void fileNotKnownToVirusTotal_ScansForVirusesOnline() throws Exception {
    DownloadedApkInfo downloadedApkInfo = createTestDownloadedApkInfo();
    downloadedApkInfo.setSha256CheckSum("a" + NO_ROOT_FIREWALL_SHA_256.substring(1)); // generate fake SHA256 checksum

    Assert.assertNotEquals(AppState.SCANNING_FOR_VIRUSES, downloadedApkInfo.getApp().getState());

    final List<VerifyApkFileResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyApkFile(downloadedApkInfo, new VerifyApkFileCallback() {
      @Override
      public void completed(VerifyApkFileResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(15, TimeUnit.MINUTES); } catch(Exception ignored) { }


    Assert.assertEquals(AppState.SCANNING_FOR_VIRUSES, downloadedApkInfo.getApp().getState());

    Assert.assertEquals(1, resultList.size());

    VerifyApkFileResult result = resultList.get(0);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertTrue(result.knowsFileChecksum());
    Assert.assertTrue(result.couldVerifyFileChecksum());
    Assert.assertTrue(result.isFileChecksumFromIndependentSource());
  }



  protected DownloadedApkInfo createTestDownloadedApkInfo() {
    AppInfo appInfo = new AppInfo("");

    AppDownloadInfo downloadInfo = new AppDownloadInfo(appInfo, new TestAppDownloader());
    downloadInfo.setDownloadLocationPath(testApkFilePath);

    DownloadedApkInfo downloadedApkInfo = new DownloadedApkInfo(appInfo, downloadInfo, "");
    downloadedApkInfo.setSha256CheckSum(NO_ROOT_FIREWALL_SHA_256);

    return downloadedApkInfo;
  }


  protected static File copyFromResourcesToReadableDirectory(String resourceFilename) throws Exception {
    InputStream inputStream = VirusTotalApkFileVerifierTest.class.getClassLoader().getResourceAsStream(resourceFilename);
    File destinationFile = File.createTempFile(resourceFilename, ".apk");

    OutputStream fileOutputStream = new FileOutputStream(destinationFile);
    byte[] buffer = new byte[8 * 1024];
    int read = 0;

    while((read = inputStream.read(buffer, 0, buffer.length)) > 0) {
      fileOutputStream.write(buffer, 0, read);
    }

    fileOutputStream.flush();
    fileOutputStream.close();
    inputStream.close();

    return destinationFile;
  }

  protected static void deleteFile(String filePath) {
    try {
      File file = new File(filePath);
      if(file.exists()) {
        file.delete();
      }
    } catch(Exception e) {
    }
  }

}