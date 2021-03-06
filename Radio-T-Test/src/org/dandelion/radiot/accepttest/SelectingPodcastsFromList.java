package org.dandelion.radiot.accepttest;


import android.os.Environment;
import org.dandelion.radiot.accepttest.drivers.HomeScreenDriver;
import org.dandelion.radiot.accepttest.drivers.PodcastListDriver;
import org.dandelion.radiot.accepttest.testables.*;
import org.dandelion.radiot.helpers.*;
import org.dandelion.radiot.podcasts.PodcastsApp;
import org.dandelion.radiot.podcasts.core.PodcastItem;
import org.dandelion.radiot.podcasts.download.FakeDownloaderActivity;

import java.io.File;

public class SelectingPodcastsFromList extends PodcastListAcceptanceTestCase {
    public static final String SAMPLE_URL = "http://example.com/podcast_file.mp3";
    private static final String TITLE = "Радио-Т 001";

    private FakePodcastPlayer player;
    private FakeDownloadManager downloadManager;
    private TestingPodcastsApp application;
    private HomeScreenDriver appDriver;
    private FakeMediaScanner mediaScanner;
    private FakeNotificationManager notificationManager;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
        setupEnvironment();
        appDriver = createDriver();
    }

    public void testPlayPodcastFromInternet() throws Exception {
        PodcastListDriver driver = gotoPodcastListPage();
        PodcastItem item = driver.selectItemForPlaying(0);
		player.assertIsPlaying(item.getAudioUri());
	}

    public void testDownloadPodcastFileLocally() throws Exception {
        PodcastListDriver driver = gotoPodcastListPage();
        driver.makeSamplePodcastWithUrl(TITLE, SAMPLE_URL);
        File localPath = new File(getDownloadFolder(), "podcast_file.mp3");

        driver.selectItemForDownloading(0);
        downloadManager.assertSubmittedRequest(SAMPLE_URL, localPath);
        downloadManager.downloadComplete();

        mediaScanner.assertScannedFile(localPath);
        notificationManager.assertShowsSuccess(TITLE, localPath);
    }

    public void testDownloadFinishedWithError() throws Exception {
        PodcastListDriver driver = gotoPodcastListPage();
        driver.makeSamplePodcastWithUrl(TITLE, SAMPLE_URL);
        File localPath = new File(getDownloadFolder(), "podcast_file.mp3");
        final int errorCode = 1000;

        driver.selectItemForDownloading(0);
        downloadManager.assertSubmittedRequest(SAMPLE_URL, localPath);
        downloadManager.downloadAborted(errorCode);

        mediaScanner.assertNoInteractions();
        notificationManager.assertShowsError(TITLE, errorCode);
    }

    public void testMissingPodcastUrl() throws Exception {
        PodcastListDriver driver = gotoPodcastListPage();
        driver.makeSamplePodcastWithUrl(TITLE, null);
        
        driver.selectItemForDownloading(0);
        assertTrue(driver.waitForText("Неверная ссылка на аудио-файл подкаста"));
    }

    public void testCancelDownloadInProgress() throws Exception {
        PodcastListDriver driver = gotoPodcastListPage();
        driver.makeSamplePodcastWithUrl(TITLE, SAMPLE_URL);
        File localPath = new File(getDownloadFolder(), "podcast_file.mp3");

        driver.selectItemForDownloading(0);
        downloadManager.assertSubmittedRequest(SAMPLE_URL, localPath);
        downloadManager.cancelDownload();
        mediaScanner.assertNoInteractions();
    }

    public void testInformsUserOnUnsupportedPlatforms() throws Exception {
        application.setDownloadSupported(false);
        PodcastListDriver driver = gotoPodcastListPage();

        driver.selectItemForDownloading(0);

        appDriver.assertCurrentActivity(
                "Should inform user of unsupported platform",
                FakeDownloaderActivity.class);

    }

    private PodcastListDriver gotoPodcastListPage() throws InterruptedException {
        PodcastListDriver driver = appDriver.visitMainShowPage2();
        mainShowPresenter().assertPodcastListIsUpdated();
        return driver;
    }

    private void setupEnvironment() {
        player = new FakePodcastPlayer();
        downloadManager = new FakeDownloadManager(getInstrumentation().getTargetContext());
        mediaScanner = new FakeMediaScanner();
        notificationManager = new FakeNotificationManager();
        application = new TestingPodcastsApp(getInstrumentation().getTargetContext(),
                player, downloadManager, mediaScanner, notificationManager);
        application.setDownloadFolder(getDownloadFolder());
        PodcastsApp.setTestingInstance(application);
    }

    private static File getDownloadFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
}

