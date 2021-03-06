package org.dandelion.radiot.podcasts.download;

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.File;

public class SystemMediaScanner implements MediaScanner {
    private Context context;

    public SystemMediaScanner(Context context) {
        this.context = context;
    }

    @Override
    public void scanAudioFile(File path) {
        MediaScannerConnection.scanFile(context, 
                new String[] {path.toString()},
                new String[] {"audio/mpeg"},
                null);
    }
}
