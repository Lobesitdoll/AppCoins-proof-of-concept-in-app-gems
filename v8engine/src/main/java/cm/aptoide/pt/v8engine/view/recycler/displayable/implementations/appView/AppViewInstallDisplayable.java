/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 10/08/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.appView;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.dataprovider.model.MinimalAd;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.model.v3.GetApkInfoJson;
import cm.aptoide.pt.model.v7.GetApp;
import cm.aptoide.pt.model.v7.GetAppMeta;
import cm.aptoide.pt.model.v7.Type;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.install.InstallManager;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.interfaces.Payments;
import io.realm.Realm;
import lombok.Cleanup;
import lombok.Getter;
import rx.Observable;

/**
 * Created by sithengineer on 06/05/16.
 */
public class AppViewInstallDisplayable extends AppViewDisplayable {

	private static final String TAG = AppViewInstallDisplayable.class.getName();

	@Getter private boolean shouldInstall;
	@Getter private GetApkInfoJson.Payment payment;
	@Getter private MinimalAd minimalAd;

	private InstallManager installManager;

	private long appId;
	private String packageName;

	public AppViewInstallDisplayable() {
	}

	public AppViewInstallDisplayable(InstallManager installManager, GetApp getApp, MinimalAd minimalAd) {
		this(installManager, getApp, minimalAd, false);
	}

	public AppViewInstallDisplayable(InstallManager installManager, GetApp getApp, MinimalAd minimalAd, boolean
			shouldInstall) {
		super(getApp, false);
		this.installManager = installManager;
		this.appId = getApp.getNodes().getMeta().getData().getId();
		this.packageName = getApp.getNodes().getMeta().getData().getPackageName();
		this.payment = getApp.getNodes().getMeta().getData().getPayment();
		this.minimalAd = minimalAd;
		this.shouldInstall = shouldInstall;
	}

	public Observable<Void> buyApp(Context context, GetAppMeta.App app) {
		return Observable.create(aVoid -> {
			// process payment, save info offline, send info to server and when server confirms the app purchase delete offline data
			Fragment fragment = ((FragmentShower) context).getLastV4();
			if (Payments.class.isAssignableFrom(fragment.getClass())) {
				((Payments) fragment).buyApp(app);
			}
		});
	}

	private Observable<Void> installOrUpdate(Context context, GetAppMeta.App app, Rollback.Action action) {
		return installManager.install(context, (PermissionRequest) context, appId).doOnNext(aVoid -> {
			AptoideUtils.ThreadU.runOnIoThread(() -> {
				@Cleanup
				Realm realm = Database.get();
				Database.RollbackQ.addRollbackWithAction(realm, app, action);
			});
		});
	}

	public Observable<Void> update(Context context, GetAppMeta.App app) {
		return installOrUpdate(context, app, Rollback.Action.UPDATE);
	}

	public Observable<Void> install(Context context, GetAppMeta.App app) {
		return installOrUpdate(context, app, Rollback.Action.INSTALL);
	}

	public Observable<Void> uninstall(Context context, GetAppMeta.App app) {
		return installManager.uninstall(context, packageName).doOnNext(aVoid -> {
			AptoideUtils.ThreadU.runOnIoThread(() -> {
				@Cleanup
				Realm realm = Database.get();
				Database.RollbackQ.addRollbackWithAction(realm, app, Rollback.Action.UNINSTALL);
			});
		});
	}

	public Observable<Void> downgrade(Context context, GetAppMeta.App app) {
		return installManager.uninstall(context, packageName)
				.concatWith(installManager.install(context, (PermissionRequest) context, appId))
				.doOnNext(aVoid -> {
					AptoideUtils.ThreadU.runOnIoThread(() -> {
						@Cleanup
						Realm realm = Database.get();
						Database.RollbackQ.addRollbackWithAction(realm, app, Rollback.Action.DOWNGRADE);
					});
				});
	}

	@Override
	public Type getType() {
		return Type.APP_VIEW_INSTALL;
	}

	@Override
	public int getViewLayout() {
		return R.layout.displayable_app_view_install;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.i(TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Logger.i(TAG, "onPause");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.i(TAG, "onSaveInstanceState");
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		Logger.i(TAG, "onViewStateRestored");
	}
}
