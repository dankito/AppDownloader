package net.dankito.appdownloader.downloader.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ganymed on 20/11/16.
 */
public class ApkLeecherDownloadLinkGeneratorTest {

  ApkLeecherDownloadLinkGenerator underTest;


  @Before
  public void setUp() throws Exception {
    underTest = new ApkLeecherDownloadLinkGenerator();
  }

  @Test
  public void generateDownloadUrl_AppNamesContainingDash() throws Exception {
    testDownloadUrlGeneration("NetGuard - no-root firewall 2.56", "2016-11-09", "http://apkleecher.com/apps/2016/11/09/NetGuard_no_root_firewall%202.56_[www.apkleecher.com].apk");

//    testDownloadUrlGeneration("AutoStart - No root 2.2", "2016-06-23", "http://apkleecher.com/apps/2016/06/23/AutoStart_No_root%202.2_[www.Apkleecher.com].apk");
    testDownloadUrlGeneration("AutoStart - No root 2.2", "2016-06-23", "http://apkleecher.com/apps/2016/06/23/AutoStart_No_root%202.2_[www.apkleecher.com].apk");

    testDownloadUrlGeneration("CyberGhost - Free VPN & Proxy 5.5.1.7", "2016-01-18", "http://apkleecher.com/apps/2016/01/18/CyberGhost_Free_VPN_Proxy%205.5.1.7_[www.apkleecher.com].apk");
  }

  @Test
  public void generateDownloadUrl_AppNamesContainingBraces() throws Exception {
//    testDownloadUrlGeneration("ADB Wireless (no root) 1.2", "2016-06-13", "http://apkleecher.com/apps/2016/06/13/ADB_Wireless_no_root_%201.2_[www.Apkleecher.com].apk");
    testDownloadUrlGeneration("ADB Wireless (no root) 1.2", "2016-06-13", "http://apkleecher.com/apps/2016/06/13/ADB_Wireless_no_root_%201.2_[www.apkleecher.com].apk");

//    testDownloadUrlGeneration("BusyBox Install (No Root) 3.66.0.41", "2016-06-16", "http://apkleecher.com/apps/2016/06/16/BusyBox_Install_No_Root_%203.66.0.41_[www.Apkleecher.com].apk");
    testDownloadUrlGeneration("BusyBox Install (No Root) 3.66.0.41", "2016-06-16", "http://apkleecher.com/apps/2016/06/16/BusyBox_Install_No_Root_%203.66.0.41_[www.apkleecher.com].apk");

    testDownloadUrlGeneration("VPN Master(Free unblock proxy) 5.2.1", "2016-11-20", "http://apkleecher.com/apps/2016/11/20/VPN_Master_Free_unblock_proxy%205.2.1_[www.apkleecher.com].apk");

    testDownloadUrlGeneration("VPN Master(Free unblock proxy) 5.2.1", "2016-11-20", "http://apkleecher.com/apps/2016/11/20/VPN_Master_Free_unblock_proxy%205.2.1_[www.apkleecher.com].apk");
  }

  @Test
  public void generateDownloadUrl_AppNamesContainingOtherNonAlphanumericalCharacters() throws Exception {
//    testDownloadUrlGeneration("VideoShowLite: Video editor 7.1.7 lite", "2016-11-20", "http://apkleecher.com/apps/2016/11/20/VideoShowLite_Video_editor%207.1.7%20lite_[www.Apkleecher.com].apk");
    testDownloadUrlGeneration("VideoShowLite: Video editor 7.1.7 lite", "2016-11-20", "http://apkleecher.com/apps/2016/11/20/VideoShowLite_Video_editor%207.1.7%20lite_[www.apkleecher.com].apk");

    testDownloadUrlGeneration("VivaVideo: Free Video Editor 5.3.0", "2016-11-20", "http://apkleecher.com/apps/2016/11/20/VivaVideo_Free_Video_Editor%205.3.0_[www.apkleecher.com].apk");
  }


  protected void testDownloadUrlGeneration(String appName, String lastUpdatedOn, String correctDownloadLink) {
    underTest.setAppNameAndVersion(appName);
    underTest.setLastUpdatedOn(lastUpdatedOn);

    Assert.assertEquals(correctDownloadLink, underTest.generateDownloadUrl());
  }

}