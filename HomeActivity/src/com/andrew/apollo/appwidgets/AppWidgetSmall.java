/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.appwidgets;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.andrew.apollo.MusicPlaybackService;
import com.android.music.R;
import com.andrew.apollo.ui.activities.AudioPlayerActivity;
import com.andrew.apollo.ui.activities.HomeActivity;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.customwidget.MakeWidgetUtils;
import com.andrew.apollo.utils.customwidget.Setup;
import com.andrew.apollo.utils.customwidget.Utils;
import com.andrew.apollo.utils.customwidget.WData;

/**
 * 2x2 Widget
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 * 
 *         modify to allow using custom image
 * @author jmkl (jimikill3d@gmail.com)
 */
@SuppressLint("NewApi")
public class AppWidgetSmall extends AppWidgetProvider {

	public static final String CMDAPPWIDGETUPDATE = "app_widget_small_update";
	private MakeWidgetUtils customWidget;
	SharedPreferences mpref;
	private static AppWidgetSmall mInstance;

	public static synchronized AppWidgetSmall getInstance() {
		if (mInstance == null) {
			mInstance = new AppWidgetSmall();
		}
		return mInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {


		defaultAppWidget(context, appWidgetIds);
		final Intent updateIntent = new Intent(MusicPlaybackService.SERVICECMD);
		updateIntent.putExtra(MusicPlaybackService.CMDNAME,
				AppWidgetSmall.CMDAPPWIDGETUPDATE);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		context.sendBroadcast(updateIntent);
	}

	/**
	 * Initialize given widgets to default state, where we launch Music on
	 * default click and hide actions if service not running.
	 */
	private void defaultAppWidget(final Context context,
			final int[] appWidgetIds) {
		final RemoteViews appWidgetViews = new RemoteViews(
				context.getPackageName(), R.layout.app_widget_small);
		linkButtons(context, appWidgetViews, false);
		pushUpdate(context, appWidgetIds, appWidgetViews);
	}

	private void pushUpdate(final Context context, final int[] appWidgetIds,
			final RemoteViews views) {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		if (appWidgetIds != null) {
			appWidgetManager.updateAppWidget(appWidgetIds, views);
		} else {
			appWidgetManager.updateAppWidget(new ComponentName(context,
					getClass()), views);
		}
	}

	/**
	 * Check against {@link AppWidgetManager} if there are any instances of this
	 * widget.
	 */
	private boolean hasInstances(final Context context) {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		final int[] mAppWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(context, getClass()));
		return mAppWidgetIds.length > 0;
	}

	/**
	 * Handle a change notification coming over from
	 * {@link MusicPlaybackService}
	 */
	public void notifyChange(final MusicPlaybackService service,
			final String what) {
		if (hasInstances(service)) {
			if (MusicPlaybackService.META_CHANGED.equals(what)
					|| MusicPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
				performUpdate(service, null);
			}
		}
	}

	/**
	 * Update all active widget instances by pushing changes
	 */
	public void performUpdate(final MusicPlaybackService service,
			final int[] appWidgetIds) {
		File f = new File(Utils.DEF_SKIN);
		if (!f.exists())
			new Setup().copy(service);
		
		final RemoteViews appWidgetView = new RemoteViews(
				service.getPackageName(), R.layout.app_widget_small);

		customWidget = new MakeWidgetUtils();
		mpref = PreferenceManager.getDefaultSharedPreferences(service);
		
		String curSkin = mpref.getString(Utils.W_SKIN, Utils.DEF_SKIN);
		
		WData data = Utils.getSkinInfo(curSkin);
		
		Bitmap cover = service.getAlbumArt();		
		
		
		

		final CharSequence trackName = service.getTrackName();
		final CharSequence artistName = service.getArtistName();
		
		Bitmap bitmap = customWidget.MakeCover(cover,curSkin,data.getUkuran());
		
	// Set the titles and artwork
		appWidgetView.setTextViewText(R.id.app_widget_small_line_one, trackName);
		appWidgetView.setTextViewText(R.id.app_widget_small_line_two,	artistName);
		
		appWidgetView.setTextColor(R.id.app_widget_small_line_one, data.getTitlecolor());
		appWidgetView.setTextColor(R.id.app_widget_small_line_two, data.getArtistcolor());
		
		
		appWidgetView.setImageViewBitmap(R.id.app_widget_small_image, bitmap);

		// Set correct drawable for pause state
		final boolean isPlaying = service.isPlaying();
		if (isPlaying) {
			appWidgetView.setImageViewUri(R.id.app_widget_small_play,Uri.fromFile(new File(curSkin+Utils.W_PAUSE)));
			if (ApolloUtils.hasJellyBean()) {
				appWidgetView.setContentDescription(R.id.app_widget_small_play,
						service.getString(R.string.accessibility_pause));
			}
		} else {
			appWidgetView.setImageViewUri(R.id.app_widget_small_play,Uri.fromFile(new File(curSkin+Utils.W_PLAY)));
			if (ApolloUtils.hasJellyBean()) {
				appWidgetView.setContentDescription(R.id.app_widget_small_play,
						service.getString(R.string.accessibility_play));
			}
		}
		appWidgetView.setImageViewUri(R.id.app_widget_small_previous,Uri.fromFile(new File(curSkin+Utils.W_PREV)));
		appWidgetView.setImageViewUri(R.id.app_widget_small_next,Uri.fromFile(new File(curSkin+Utils.W_NEXT)));
		appWidgetView.setImageViewUri(R.id.app_widget_small_button_container,Uri.fromFile(new File(curSkin+Utils.W_BG)));

		// Link actions buttons to intents
		linkButtons(service, appWidgetView, isPlaying);

		// Update the app-widget
		pushUpdate(service, appWidgetIds, appWidgetView);

		// Build the notification
		if (ApolloUtils.isApplicationSentToBackground(service)) {
			service.mBuildNotification = true;
		}
	}

	/**
	 * Link up various button actions using {@link PendingIntents}.
	 * 
	 * @param playerActive
	 *            True if player is active in background, which means widget
	 *            click will launch {@link AudioPlayerActivity}, otherwise we
	 *            launch {@link MusicBrowserActivity}.
	 */
	private void linkButtons(final Context context, final RemoteViews views,
			final boolean playerActive) {
		Intent action;
		PendingIntent pendingIntent;

		final ComponentName serviceName = new ComponentName(context,
				MusicPlaybackService.class);

		// Now playing
		if (playerActive) {
			action = new Intent(context, AudioPlayerActivity.class);
			pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
			views.setOnClickPendingIntent(R.id.app_widget_small_info_container,
					pendingIntent);
			views.setOnClickPendingIntent(R.id.app_widget_small_image,
					pendingIntent);
		} else {
			// Home
			action = new Intent(context, HomeActivity.class);
			pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
			views.setOnClickPendingIntent(R.id.app_widget_small_info_container,
					pendingIntent);
			views.setOnClickPendingIntent(R.id.app_widget_small_image,
					pendingIntent);
		}

		// Previous track
		action = new Intent(MusicPlaybackService.PREVIOUS_ACTION);
		action.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, action, 0);
		views.setOnClickPendingIntent(R.id.app_widget_small_previous,
				pendingIntent);

		// Play and pause
		action = new Intent(MusicPlaybackService.TOGGLEPAUSE_ACTION);
		action.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, action, 0);
		views.setOnClickPendingIntent(R.id.app_widget_small_play, pendingIntent);

		// Next track
		action = new Intent(MusicPlaybackService.NEXT_ACTION);
		action.setComponent(serviceName);
		pendingIntent = PendingIntent.getService(context, 0, action, 0);
		views.setOnClickPendingIntent(R.id.app_widget_small_next, pendingIntent);
	}

}
