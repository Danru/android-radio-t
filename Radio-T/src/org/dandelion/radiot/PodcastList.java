package org.dandelion.radiot;

import java.util.List;

public class PodcastList {
	private static Factory factory;

	public interface IModel {
		List<PodcastItem> retrievePodcasts() throws Exception;
	}

	public interface IView {
		void updatePodcasts(List<PodcastItem> podcasts);
		void showProgress();
		void closeProgress();
		void showErrorMessage(String errorMessage);
		void close();
	}

	public interface IPresenter {
		void refreshData();
		void cancelLoading();
	}

	public static IPresenter getPresenter(IView view, String feedUrl) {
		Factory f = getFactory();
		IModel model = f.createModel(feedUrl);
		IPresenter presenter = f.createPresenter(model, view);
		return presenter;
	}

	private static Factory getFactory() {
		if (null == factory) {
			factory = new Factory();
		}
		return factory;
	}

	public static void setFactory(Factory newInstance) {
		factory = newInstance;
	}

	public static void resetFactory() {
		setFactory(null);
	}

	public static class Factory {
		public IModel createModel(String url) {
			return new RssFeedModel(url);
		}

		public IPresenter createPresenter(IModel model, IView view) {
			return new AsyncPresenter(model, view);
		}
	}
}
