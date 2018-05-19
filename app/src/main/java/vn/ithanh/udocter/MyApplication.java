package vn.ithanh.udocter;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import vn.ithanh.udocter.util.Utils;

public class MyApplication extends Application {

	private static int WithScreen;

	public static int getWithScreen() {
		return WithScreen;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressWarnings("unused")
	@Override
	public void onCreate() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		}
		super.onCreate();
		System.setProperty("java.net.preferIPv6Addresses", "false");
		System.setProperty("java.net.preferIPv4Addresses", "true");
		initImageLoader(getApplicationContext());
		
		WithScreen = Utils.getWidth(getApplicationContext());
	}

	public MyApplication() {
		super();
		mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
	}

	public static interface OnLowMemoryListener {
		/**
		 * Callback to be invoked when the system needs memory.
		 */
		public void onLowMemoryReceived();
	}

	private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;

	/**
	 * Add a new listener to registered {@link OnLowMemoryListener}.
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(
					listener));
		}
	}

	/**
	 * Remove a previously registered listener
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			int i = 0;
			while (i < mLowMemoryListeners.size()) {
				final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
				if (l == null || l == listener) {
					mLowMemoryListeners.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		int i = 0;
		while (i < mLowMemoryListeners.size()) {
			final OnLowMemoryListener listener = mLowMemoryListeners.get(i)
					.get();
			if (listener == null) {
				mLowMemoryListeners.remove(i);
			} else {
				listener.onLowMemoryReceived();
				i++;
			}
		}
	}

	public static MyApplication getInstance(Context ctx) {
		return (MyApplication) ctx.getApplicationContext();
	}

	public static ImageLoader uilImageLoader = ImageLoader.getInstance();

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(100 * 1024 * 1024)
				// 100 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
