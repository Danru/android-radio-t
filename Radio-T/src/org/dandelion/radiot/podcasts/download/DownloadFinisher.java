package org.dandelion.radiot.podcasts.download;

public class DownloadFinisher {
    private Downloader downloader;
    private MediaScanner mediaScanner;

    public DownloadFinisher(Downloader downloader, MediaScanner scanner) {
        this.downloader = downloader;
        this.mediaScanner = scanner;
    }

    public void finishDownload(long id) {
        DownloadTask completedTask = downloader.query(id);
        if (completedTask != null) {
            mediaScanner.scanAudioFile(completedTask.localPath);
        }
    }

}
