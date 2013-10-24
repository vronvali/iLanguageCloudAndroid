package ca.ilanguage.android.ilanguagecloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

public class JavascriptInterface {

	Context mContext;
	String mCloudTitle;
	String mCloudString;
	String mCloudFont;

	String[] saveFileLocation = new String[1];
	String[] saveMimeType = new String[1];

	/** Instantiate the interface and set the context */
	JavascriptInterface(Context c) {
		mContext = c;
	}

	@android.webkit.JavascriptInterface
	public String getCloudString() {
		return mCloudString;
	}

	@android.webkit.JavascriptInterface
	public String getCloudFont() {
		return mCloudFont;
	}

	public void setCloudParams(String mCloudTitle, String mCloudString,
			String mCloudFont) {
		this.mCloudTitle = mCloudTitle.replaceAll("\\W+", "");
		this.mCloudString = mCloudString;
		this.mCloudFont = mCloudFont;
	}

	@android.webkit.JavascriptInterface
	public void getLocalStorage(String keyValue, String fileType) {
		Long currentTime = System.currentTimeMillis();

		storeImage(keyValue, this.mCloudTitle + "_" + currentTime.toString()
				+ "." + fileType, fileType);

	}

	private void storeImage(String imageData, String filename, String fileType) {

		byte[] imageAsBytes = Base64.decode(imageData, 0);

		File filePath = new File(Environment.getExternalStorageDirectory()
				+ "/iLanguageCloud/");
		filePath.mkdirs();

		String fileString = filePath.toString() + "/" + filename;
		saveFileLocation[0] = fileString;
		saveMimeType[0] = (fileType.equalsIgnoreCase("png")) ? "image/png" : "image/svg+xml";

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;

		try {
			fos = new FileOutputStream(fileString);
			bos = new BufferedOutputStream(fos);
			bos.write(imageAsBytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MediaScannerConnection.scanFile(mContext, saveFileLocation,
				saveMimeType, new OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {
						Log.v("testing updater", "file " + path
								+ " was scanned successfully: " + uri);

						notifyUser(uri, saveMimeType[0]);

					}
				});
	}
	
	private void notifyUser(Uri uri, String imageMimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//		intent.setType(imageMimeType);

		PackageManager packageManager = mContext.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		boolean isIntentSafe = activities.size() > 0;

		if (!isIntentSafe) { return; }

		String titlePrompt = "Open image with";
		Intent chooser = Intent.createChooser(intent, titlePrompt);

		PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, chooser, 0);

		Notification n  = new NotificationCompat.Builder(mContext)
		        .setContentTitle("Image saved successfully.")
		        .setContentText("Only PNGs are viewable in the Gallery.")
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setTicker("Image saved successfully. Click to view.")
		        .setContentIntent(pIntent)
		        .setAutoCancel(true)
		        .build();

		NotificationManager notificationManager = 
				(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0, n);
	}

}
