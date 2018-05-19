package vn.ithanh.udocter.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class Utils {

	/**
	 * Log debug
	 * 
	 * @return
	 */
	public static void log(String tag, String message) {
		if (Config.IS_DEBUG)
			Log.d(tag, message);
	}

	/**
	 * Convert InputStream to UTF8-String
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Validate empty string
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isNullOrEmpty(String input) {
		if (input == null)
			return true;
		if (input.trim().length() == 0)
			return true;
		return false;
	}

	public static Bitmap decodeFile(File f, int requiredSize) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = requiredSize;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * Copy InputStream to OutputStream
	 * 
	 * @param is
	 * @param os
	 */
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Get width pixel of screen
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getWidth(Context ctx) {
		Display display = ((WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		return display.getWidth();
	}

	/**
	 * Get height pixel of screen
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getHeight(Context ctx) {
		Display display = ((WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		return display.getHeight();
	}

	public static String getDistance(int distance) {
		if (distance > 1000){
			double kc = distance/1000;
			DecimalFormat format = new DecimalFormat("0.0");
			String formatted = format.format(kc);
			return kc + " KM";
		}
		return distance + " M";
	}
	public static String priceWithout(int price) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		return formatter.format(price) + " đ";
	}

	public static String getAvatarfacebook(String fbid){
		return "http://graph.facebook.com/"+ fbid +"/picture?type=normal";
	}

	public static boolean checkVNMobileNumber(String isdn){
		String[] listprefix = {"0120" , "84120" , "0121" , "84121" , "0122" , "84122" , "0123" , "84123" , "0124" , "84124" , "0125" , "84125" , "0126" , "84126" , "0127" , "84127" , "0128" , "84128" , "0129" , "84129" , "0162" , "84162" , "0163" , "84163" , "0164" , "84164" , "0165" , "84165" , "0166" , "84166" , "0167" , "84167" , "0168" , "84168" , "0169" , "84169" , "0186" , "84186" , "0188" , "84188" , "082" , "8482" , "086" , "8486" , "088" , "8488" , "089" , "8489" , "090" , "8490" , "091" , "8491" , "092" , "8492" , "093" , "8493" , "094" , "8494" , "096" , "8496" , "097" , "8497" , "098" , "8498" , "099" , "8499"};
		for (String prefix : listprefix) {
			if (isdn.startsWith(prefix)){
				isdn = isdn.replace(prefix, "");
				String regexStr = "^[0-9]{7}$";
				return isdn.matches(regexStr);
			}
		}

		return false;
	}
}
