package org.dandelion.radiot.test;

import java.io.IOException;
import java.io.InputStream;

import org.dandelion.radiot.PodcastList;
import org.dandelion.radiot.PodcastList.IFeedSource;
import org.dandelion.radiot.PodcastList.IModel;
import org.dandelion.radiot.PodcastList.IPresenter;
import org.dandelion.radiot.PodcastList.IView;

import android.app.Instrumentation;
import android.content.res.AssetManager;
import android.net.Uri;

class LocalRssFeedFactory extends PodcastList.Factory {
	private AssetManager assets;

	public LocalRssFeedFactory(Instrumentation instrumentation) {
		assets = instrumentation.getContext().getAssets();
	}

	@Override
	public IPresenter createPresenter(final IModel model, final IView view) {
		return new IPresenter() {
			public void refreshData() {
				try {
					view.updatePodcasts(model.retrievePodcasts());
				} catch (Exception e) {
					view.showErrorMessage(e.getMessage());
				}
			}
			public void cancelLoading() {
			}
		};
	}

	@Override
	public IFeedSource createFeedSource(final String url) {
		return new IFeedSource() {
			@Override
			public InputStream openContentStream() throws IOException {
				return assets.open(getLocalFileName(url));
			}
		};
	}

	private String getLocalFileName(String url) {
		String filename;
		if (null == url) {
			filename = "radio-t";
		} else {
			filename = Uri.parse(url).getLastPathSegment();
		}
		return filename + ".xml";
	}
}
