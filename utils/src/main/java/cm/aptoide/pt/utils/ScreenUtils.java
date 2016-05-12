/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 12/05/2016.
 */

package cm.aptoide.pt.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import cm.aptoide.pt.logger.Logger;

/**
 * Created by neuro on 14-04-2016.
 */
public final class ScreenUtils {

	public static final float REFERENCE_WIDTH_DPI = 360;

	private static ScreenUtilsCache screenWidthInDipCache = new ScreenUtilsCache();

	public static int getCurrentOrientation(final Context context) {
		return context.getResources().getConfiguration().orientation;
	}

	public static float getScreenWidthInDip(final Context context) {
		if (getCurrentOrientation(context) != screenWidthInDipCache.orientation) {
			WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);
			screenWidthInDipCache.set(getCurrentOrientation(context), dm.widthPixels / dm.density);
		}

		return screenWidthInDipCache.value;
	}



	public static String screenshotToThumb(Context context, String imageUrl, String orientation) {

		String screen = null;

		try {

			if (imageUrl.contains("_screen")) {

				String sizeString = IconSizeUtils.generateSizeStringScreenshots(context, orientation);

				String[] splitUrl = imageUrl.split("\\.(?=[^\\.]+$)");
				screen = splitUrl[0] + "_" + sizeString + "." + splitUrl[1];

			} else {

				String[] splitString = imageUrl.split("/");
				StringBuilder db = new StringBuilder();
				for (int i = 0; i != splitString.length - 1; i++) {
					db.append(splitString[i]);
					db.append("/");
				}

				db.append("thumbs/mobile/");
				db.append(splitString[splitString.length - 1]);
				screen = db.toString();
			}

		} catch (Exception e) {
			Logger.printException(e);
			// FIXME uncomment the following lines
			//Crashlytics.setString("imageUrl", imageUrl);
			//Crashlytics.logException(e);
		}

		return screen;
	}

	private static class ScreenUtilsCache {

		private int orientation = -1;
		private float value;

		public void set(int currentOrientation, float value) {
			this.orientation = currentOrientation;
			this.value = value;
		}
	}
}

