package net.dankito.appdownloader.app;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;

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

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by ganymed on 21/11/16.
 */

@RunWith(AndroidJUnit4.class)
public class AndroidAppPackageVerifierTest {

  protected static final String AUTO_START_APP_FILENAME = "AutoStart.apk";

  protected static final String AUTO_START_APP_PACKAGE_NAME = "com.autostart";

  protected static final String AUTO_START_APP_VERSION = "2.2";


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
    File apkFile = getApkFilePath(AUTO_START_APP_FILENAME);
    AppInfo testApp = new AppInfo(AUTO_START_APP_PACKAGE_NAME + "_error");

    AppDownloadInfo downloadInfo = new AppDownloadInfo(testApp, null);
    downloadInfo.setDownloadLocationPath(apkFile.getPath());
    testApp.addDownloadInfo(downloadInfo);

    AppPackageVerificationResult result = underTest.verifyDownloadedApk(downloadInfo);

    Assert.assertFalse(result.wasVerificationSuccessful());
    Assert.assertTrue(result.isCompletelyDownloaded());
    Assert.assertFalse(result.isPackageNameCorrect());
    Assert.assertFalse(result.isVersionCorrect());
    Assert.assertFalse(result.isFileChecksumCorrect());
    Assert.assertFalse(result.isAppSignatureCorrect());
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
