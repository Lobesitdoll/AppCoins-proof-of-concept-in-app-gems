package cm.aptoide.pt.presenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import cm.aptoide.pt.AptoideApplication;
import cm.aptoide.pt.AutoUpdate;
import cm.aptoide.pt.Install;
import cm.aptoide.pt.InstallManager;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.dataprovider.ws.v7.store.StoreContext;
import cm.aptoide.pt.install.InstallCompletedNotifier;
import cm.aptoide.pt.install.installer.RootInstallationRetryHandler;
import cm.aptoide.pt.notification.ContentPuller;
import cm.aptoide.pt.notification.NotificationSyncScheduler;
import cm.aptoide.pt.preferences.managed.ManagerPreferences;
import cm.aptoide.pt.util.ApkFy;
import cm.aptoide.pt.view.DeepLinkManager;
import cm.aptoide.pt.view.navigator.FragmentNavigator;
import cm.aptoide.pt.view.store.home.HomeFragment;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by marcelobenites on 18/01/17.
 */
public class MainPresenter implements Presenter {

  private static final String TAG = MainPresenter.class.getSimpleName();
  private final MainView view;
  private final ContentPuller contentPuller;
  private final InstallManager installManager;
  private final RootInstallationRetryHandler rootInstallationRetryHandler;
  private final CrashReport crashReport;
  private final SharedPreferences sharedPreferences;
  private final SharedPreferences securePreferences;
  private final FragmentNavigator fragmentNavigator;
  private final DeepLinkManager deepLinkManager;
  private final String defaultStore;
  private final String defaultTheme;
  private NotificationSyncScheduler notificationSyncScheduler;
  private InstallCompletedNotifier installCompletedNotifier;
  private ApkFy apkFy;
  private AutoUpdate autoUpdate;
  private boolean firstCreated;

  public MainPresenter(MainView view, InstallManager installManager,
      RootInstallationRetryHandler rootInstallationRetryHandler, CrashReport crashReport,
      ApkFy apkFy, AutoUpdate autoUpdate, ContentPuller contentPuller,
      NotificationSyncScheduler notificationSyncScheduler,
      InstallCompletedNotifier installCompletedNotifier, SharedPreferences sharedPreferences,
      SharedPreferences securePreferences, FragmentNavigator fragmentNavigator,
      DeepLinkManager deepLinkManager, String defaultStore, String defaultTheme) {
    this.view = view;
    this.defaultStore = defaultStore;
    this.installManager = installManager;
    this.rootInstallationRetryHandler = rootInstallationRetryHandler;
    this.crashReport = crashReport;
    this.apkFy = apkFy;
    this.autoUpdate = autoUpdate;
    this.contentPuller = contentPuller;
    this.notificationSyncScheduler = notificationSyncScheduler;
    this.installCompletedNotifier = installCompletedNotifier;
    this.fragmentNavigator = fragmentNavigator;
    this.deepLinkManager = deepLinkManager;
    this.firstCreated = true;
    this.sharedPreferences = sharedPreferences;
    this.securePreferences = securePreferences;
    this.defaultTheme = defaultTheme;
  }

  @Override public void present() {
    view.getLifecycle()
        .filter(event -> View.LifecycleEvent.CREATE.equals(event))
        .doOnNext(created -> apkFy.run())
        .filter(created -> firstCreated)
        .doOnNext(created -> notificationSyncScheduler.forceSync())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> contentPuller.start())
        .doOnNext(__ -> navigate())
        .subscribe(__ -> {
        }, throwable -> crashReport.log(throwable));

    setupInstallErrorsDisplay();
  }

  @Override public void saveState(Bundle state) {
  }

  @Override public void restoreState(Bundle state) {
    firstCreated = false;
  }

  private void setupInstallErrorsDisplay() {
    view.getLifecycle()
        .filter(event -> View.LifecycleEvent.RESUME.equals(event))
        .flatMap(lifecycleEvent -> rootInstallationRetryHandler.retries()
            .compose(view.bindUntilEvent(View.LifecycleEvent.PAUSE)))
        .distinctUntilChanged(installationProgresses -> installationProgresses.size())
        .filter(installationProgresses -> !installationProgresses.isEmpty())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(installationProgresses -> {
          watchInstalls(installationProgresses);
          view.showInstallationError(installationProgresses.size());
        }, throwable -> crashReport.log(throwable));

    view.getLifecycle()
        .filter(lifecycleEvent -> View.LifecycleEvent.RESUME.equals(lifecycleEvent))
        .flatMap(lifecycleEvent -> installManager.getTimedOutInstallations())
        .filter(installationProgresses -> !installationProgresses.isEmpty())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(noInstallErrors -> view.dismissInstallationError(),
            throwable -> crashReport.log(throwable));

    view.getLifecycle()
        .filter(lifecycleEvent -> View.LifecycleEvent.RESUME.equals(lifecycleEvent))
        .flatMap(event -> installCompletedNotifier.getWatcher())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(allInstallsCompleted -> view.showInstallationSuccessMessage());

    view.getLifecycle()
        .filter(lifecycleEvent -> View.LifecycleEvent.RESUME.equals(lifecycleEvent))
        .flatMap(lifecycleEvent -> view.getInstallErrorsDismiss())
        .flatMapCompletable(click -> installManager.cleanTimedOutInstalls())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(timeoutErrorsCleaned -> {
        }, throwable -> crashReport.log(throwable));
  }

  // FIXME we are showing home by default when we should decide were to go here and provide
  // proper up/back navigation to home if needed
  private void navigate() {
    showHome();
    if (ManagerPreferences.isCheckAutoUpdateEnable(sharedPreferences)
        && !AptoideApplication.isAutoUpdateWasCalled()) {
      // only call auto update when the app was not on the background
      autoUpdate.execute();
    }
  }

  private void showHome() {
    Fragment home = HomeFragment.newInstance(defaultStore, StoreContext.home, defaultTheme);
    fragmentNavigator.navigateToWithoutBackSave(home, true);
  }

  private void watchInstalls(List<Install> installs) {
    for (Install install : installs) {
      installCompletedNotifier.add(install.getPackageName(), install.getVersionCode(),
          install.getMd5());
    }
  }
}
