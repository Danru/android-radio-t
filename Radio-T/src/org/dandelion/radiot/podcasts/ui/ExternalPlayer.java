package org.dandelion.radiot.podcasts.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import org.dandelion.radiot.podcasts.core.PodcastItem;
import org.dandelion.radiot.podcasts.core.PodcastAction;

public class ExternalPlayer implements PodcastAction {
    public ExternalPlayer() {
    }

    @Override
    public void perform(Context context, PodcastItem podcast) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(podcast.getAudioUri()), "audio/mpeg");
		context.startActivity(Intent.createChooser(intent, null));
	}
}