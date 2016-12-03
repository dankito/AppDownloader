package net.dankito.appdownloader.app;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.HashAlgorithm;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by ganymed on 21/11/16.
 */

@RunWith(AndroidJUnit4.class)
public class AndroidAppPackageVerifierTest {

  protected static final int WAIT_FOR_RESULT_SECONDS = 20;


  protected static final String AUTO_START_APP_FILENAME = "AutoStart.apk";

  protected static final String AUTO_START_APP_TITLE = "com.autostart";

  protected static final String AUTO_START_APP_PACKAGE_NAME = "com.autostart";

  protected static final String AUTO_START_APP_VERSION = "2.2";

  protected static final String AUTO_START_APP_MD5_CHECKSUM = "dd19f8ad04467e1ac3ae562a2c49fd17";

  protected static final String AUTO_START_APP_SHA1_CHECKSUM = "1a1d512f8cd59bcfb0ae7b9be121c36d800eabb4";

  protected static final String AUTO_START_APP_APK_SIGNATURE = "79a7b1a36ab88550403b1fa34d47473713a81031";


  protected AndroidAppPackageVerifier underTest;


//  @Rule
//  public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
//      MainActivity.class);


  @BeforeClass
  public static void provideTestApks() throws Exception {
    copyFromResourcesToReadableDirectory(AUTO_START_APP_FILENAME);
  }

  @AfterClass
  public static void deleteTestApks() throws Exception {
    deleteApkFile(AUTO_START_APP_FILENAME);
  }


  @Before
  public void setUp() {
    underTest = new AndroidAppPackageVerifier(getTargetContext());
  }


  @Test
  public void apkHasWrongPackageName_ReturnsError() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo(AUTO_START_APP_PACKAGE_NAME + "_error");

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertFalse(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertFalse(result.isPackageNameCorrect());
    Assert.assertFalse(result.isVersionCorrect());
    Assert.assertFalse(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
  }

  @Test
  public void apkHasCorrectPackageName_Succeeds() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertTrue(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertTrue(result.isAppSignatureCorrect());
  }


  @Test
  public void apkHasWrongVersion_ReturnsError() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    downloadInfo.getAppInfo().setVersionString(AUTO_START_APP_VERSION + ".42");

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertFalse(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertFalse(result.isVersionCorrect());
    Assert.assertFalse(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
  }

  @Test
  public void apkHasCorrectVersion_Succeeds() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertTrue(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertTrue(result.isAppSignatureCorrect());
  }


  @Test
  public void apkHasWrongMD5Checksum_ReturnsError() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    downloadInfo.setFileHashAlgorithm(HashAlgorithm.MD5);
    downloadInfo.setFileChecksum("error");

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertFalse(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertFalse(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
  }

  @Test
  public void apkHasCorrectMD5Checksum_Succeeds() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertTrue(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertTrue(result.isAppSignatureCorrect());
  }


  @Test
  public void apkHasWrongSHA1Checksum_ReturnsError() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    downloadInfo.setFileHashAlgorithm(HashAlgorithm.SHA1);
    downloadInfo.setFileChecksum("error");

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertFalse(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertFalse(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
  }

  @Test
  public void apkHasCorrectSHA1Checksum_Succeeds() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    downloadInfo.setFileHashAlgorithm(HashAlgorithm.SHA1);
    downloadInfo.setFileChecksum(AUTO_START_APP_SHA1_CHECKSUM);

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertTrue(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertTrue(result.isAppSignatureCorrect());
  }


  @Test
  public void apkHasWrongSignature_ReturnsError() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    downloadInfo.setApkSignature("wrong");

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertFalse(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
  }

  @Test
  public void apkHasCorrectApkSignature_Succeeds() throws Exception {
    AppDownloadInfo downloadInfo = createTestDownloadInfo();

    final List<AppPackageVerificationResult> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
      @Override
      public void completed(AppPackageVerificationResult result) {
        resultList.add(result);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(WAIT_FOR_RESULT_SECONDS, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, resultList.size());

    AppPackageVerificationResult result = resultList.get(0);
    Assert.assertTrue(result.wasVerificationSuccessful());

    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertTrue(result.isPackageNameCorrect());
    Assert.assertTrue(result.isVersionCorrect());
    Assert.assertTrue(result.isFileChecksumCorrect());
    Assert.assertTrue(result.isAppSignatureCorrect());
  }


  protected AppDownloadInfo createTestDownloadInfo() {
    return createTestDownloadInfo(AUTO_START_APP_PACKAGE_NAME);
  }

  protected AppDownloadInfo createTestDownloadInfo(String packageName) {
    File apkFile = getApkFilePath(AUTO_START_APP_FILENAME);

    AppInfo testApp = new AppInfo(packageName);
    testApp.setTitle(AUTO_START_APP_TITLE);
    testApp.setVersionString(AUTO_START_APP_VERSION);

    AppDownloadInfo downloadInfo = new AppDownloadInfo(testApp, null);
    downloadInfo.setDownloadLocationPath(apkFile.getPath());
    downloadInfo.setFileHashAlgorithm(HashAlgorithm.MD5);
    downloadInfo.setFileChecksum(AUTO_START_APP_MD5_CHECKSUM);
    downloadInfo.setApkSignature(AUTO_START_APP_APK_SIGNATURE);
    testApp.addDownloadInfo(downloadInfo);

    AppDownloadInfo independentSourceDownloadInfoWithMD5Checksum = new AppDownloadInfo(testApp, null);
    independentSourceDownloadInfoWithMD5Checksum.setFileHashAlgorithm(HashAlgorithm.MD5);
    independentSourceDownloadInfoWithMD5Checksum.setFileChecksum(AUTO_START_APP_MD5_CHECKSUM);
    independentSourceDownloadInfoWithMD5Checksum.setApkSignature(AUTO_START_APP_APK_SIGNATURE);
    testApp.addDownloadInfo(independentSourceDownloadInfoWithMD5Checksum);

    AppDownloadInfo independentSourceDownloadInfoWithSHA1Checksum = new AppDownloadInfo(testApp, null);
    independentSourceDownloadInfoWithSHA1Checksum.setFileHashAlgorithm(HashAlgorithm.SHA1);
    independentSourceDownloadInfoWithSHA1Checksum.setFileChecksum(AUTO_START_APP_SHA1_CHECKSUM);
    independentSourceDownloadInfoWithSHA1Checksum.setApkSignature(AUTO_START_APP_APK_SIGNATURE);
    testApp.addDownloadInfo(independentSourceDownloadInfoWithSHA1Checksum);

    return downloadInfo;
  }


  protected static void copyFromResourcesToReadableDirectory(String resourceFilename) throws Exception {
    InputStream inputStream = AndroidAppPackageVerifierTest.class.getClassLoader().getResourceAsStream(resourceFilename);
    File destinationFile = getApkFilePath(resourceFilename);

    OutputStream fileOutputStream = new FileOutputStream(destinationFile);
    byte[] buffer = new byte[8 * 1024];
    int read = 0;

    while((read = inputStream.read(buffer, 0, buffer.length)) > 0) {
      fileOutputStream.write(buffer, 0, read);
    }

    fileOutputStream.flush();
    fileOutputStream.close();
    inputStream.close();
  }

  protected static void deleteApkFile(String filename) {
    try {
      File file = getApkFilePath(filename);
      if(file.exists()) {
        file.delete();
      }
    } catch(Exception e) {
    }
  }

  @NonNull
  protected static File getApkFilePath(String apkFilename) {
    File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    return new File(outputDir, apkFilename);
  }

}
