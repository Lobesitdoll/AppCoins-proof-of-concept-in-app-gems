package cm.aptoide.pt.viewRateAndCommentReviews;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.annotation.Partners;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.database.accessors.AccessorFactory;
import cm.aptoide.pt.database.accessors.InstalledAccessor;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.dataprovider.DataProvider;
import cm.aptoide.pt.dataprovider.repository.IdsRepositoryImpl;
import cm.aptoide.pt.dataprovider.ws.v7.GetAppRequest;
import cm.aptoide.pt.dataprovider.ws.v7.ListReviewsRequest;
import cm.aptoide.pt.interfaces.AptoideClientUUID;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.model.v7.Comment;
import cm.aptoide.pt.model.v7.GetAppMeta;
import cm.aptoide.pt.model.v7.Review;
import cm.aptoide.pt.preferences.secure.SecurePreferencesImplementation;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.adapters.CommentsAdapter;
import cm.aptoide.pt.v8engine.fragment.AptoideBaseFragment;
import cm.aptoide.pt.v8engine.fragment.implementations.AppViewFragment;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.util.DialogUtils;
import cm.aptoide.pt.v8engine.util.StoreThemeEnum;
import cm.aptoide.pt.v8engine.util.ThemeUtils;
import cm.aptoide.pt.v8engine.view.recycler.displayable.Displayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.ProgressBarDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.CommentDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.listeners.EndlessRecyclerOnScrollListener;
import cm.aptoide.pt.viewRateAndCommentReviews.layout.RatingBarsLayout;
import cm.aptoide.pt.viewRateAndCommentReviews.layout.RatingTotalsLayout;
import com.trello.rxlifecycle.android.FragmentEvent;
import java.util.List;
import lombok.Getter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RateAndReviewsFragment extends AptoideBaseFragment<CommentsAdapter>
    implements ItemCommentAdderView<Review, CommentsAdapter> {

  private static final String TAG = RateAndReviewsFragment.class.getSimpleName();
  private final AptoideClientUUID aptoideClientUUID;
  private final DialogUtils dialogUtils;

  private long appId;
  @Getter private long reviewId;
  private String packageName;
  @Getter private String storeName;
  private String storeTheme;
  @Getter private String appName;
  private MenuItem installMenuItem;
  private RatingTotalsLayout ratingTotalsLayout;
  private RatingBarsLayout ratingBarsLayout;
  private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

  public RateAndReviewsFragment() {
    aptoideClientUUID = new IdsRepositoryImpl(SecurePreferencesImplementation.getInstance(),
        DataProvider.getContext());
    dialogUtils = new DialogUtils();
  }

  public static RateAndReviewsFragment newInstance(long appId, String appName, String storeName,
      String packageName, String storeTheme) {
    RateAndReviewsFragment fragment = new RateAndReviewsFragment();
    Bundle args = new Bundle();
    args.putLong(BundleCons.APP_ID, appId);
    args.putString(BundleCons.APP_NAME, appName);
    args.putString(BundleCons.STORE_NAME, storeName);
    args.putString(BundleCons.PACKAGE_NAME, packageName);
    args.putString(BundleCons.STORE_THEME, storeTheme);
    fragment.setArguments(args);
    return fragment;
  }

  public static RateAndReviewsFragment newInstance(long appId, String appName, String storeName,
      String packageName, long reviewId) {
    RateAndReviewsFragment fragment = new RateAndReviewsFragment();
    Bundle args = new Bundle();
    args.putLong(BundleCons.APP_ID, appId);
    args.putString(BundleCons.APP_NAME, appName);
    args.putString(BundleCons.STORE_NAME, storeName);
    args.putString(BundleCons.PACKAGE_NAME, packageName);
    args.putLong(BundleCons.REVIEW_ID, reviewId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override protected boolean displayHomeUpAsEnabled() {
    return true;
  }

  @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_install, menu);
    installMenuItem = menu.findItem(R.id.menu_install);

    InstalledAccessor accessor = AccessorFactory.getAccessorFor(Installed.class);
    accessor.get(packageName).subscribe(installed -> {
      if (installed != null) {
        // app installed... update text
        installMenuItem.setTitle(R.string.open);
      }
    }, err -> {
      CrashReport.getInstance().log(err);
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getActivity().onBackPressed();
      return true;
    }
    if (itemId == R.id.menu_install) {
      ((FragmentShower) getContext()).pushFragmentV4(V8Engine.getFragmentProvider()
          .newAppViewFragment(packageName, storeName, AppViewFragment.OpenType.OPEN_AND_INSTALL));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void loadExtras(Bundle args) {
    super.loadExtras(args);
    appId = args.getLong(BundleCons.APP_ID);
    reviewId = args.getLong(BundleCons.REVIEW_ID);
    packageName = args.getString(BundleCons.PACKAGE_NAME);
    storeName = args.getString(BundleCons.STORE_NAME);
    appName = args.getString(BundleCons.APP_NAME);
    storeTheme = args.getString(BundleCons.STORE_THEME);
  }

  @Override public int getContentViewId() {
    return R.layout.fragment_rate_and_reviews;
  }

  @Override public void bindViews(View view) {
    super.bindViews(view);
    final FloatingActionButton floatingActionButton =
        (FloatingActionButton) view.findViewById(R.id.fab);
    setHasOptionsMenu(true);

    ratingTotalsLayout = new RatingTotalsLayout(view);
    ratingBarsLayout = new RatingBarsLayout(view);

    floatingActionButton.setOnClickListener(v -> {
      dialogUtils.showRateDialog(getActivity(), appName, packageName, storeName,
          () -> invalidateReviews());
    });
  }

  private void invalidateReviews() {
    clearDisplayables();
    fetchReviews();
  }

  private void fetchReviews() {
    ListReviewsRequest reviewsRequest =
        ListReviewsRequest.of(storeName, packageName, AptoideAccountManager.getAccessToken(),
            aptoideClientUUID.getAptoideClientUUID());

    getRecyclerView().removeOnScrollListener(endlessRecyclerOnScrollListener);
    endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(this.getAdapter(), reviewsRequest,
            new ListFullReviewsSuccessRequestListener(this), Throwable::printStackTrace);
    getRecyclerView().addOnScrollListener(endlessRecyclerOnScrollListener);
    endlessRecyclerOnScrollListener.onLoadMore(false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (storeTheme != null) {
      ThemeUtils.setStatusBarThemeColor(getActivity(), StoreThemeEnum.get(storeTheme));
      ThemeUtils.setStoreTheme(getActivity(), storeTheme);
    }
  }

  @Override public void load(boolean create, boolean refresh, Bundle savedInstanceState) {
    super.load(create, refresh, savedInstanceState);
    Logger.d(TAG, "Other versions should refresh? " + create);
    fetchRating(refresh);
    fetchReviews();
  }

  private void fetchRating(boolean refresh) {
    GetAppRequest.of(appId, AptoideAccountManager.getAccessToken(),
        aptoideClientUUID.getAptoideClientUUID(), packageName)
        .observe(refresh)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
        .subscribe(getApp -> {
          if (getApp.isOk()) {
            GetAppMeta.App data = getApp.getNodes().getMeta().getData();
            setupTitle(data.getName());
            setupRating(data);
          }
          finishLoading();
        }, err -> {
          CrashReport.getInstance().log(err);
        });
  }

  public void setupTitle(String title) {
    if (hasToolbar()) {
      getToolbar().setTitle(title);
    }
  }

  private void setupRating(GetAppMeta.App data) {
    ratingTotalsLayout.setup(data);
    ratingBarsLayout.setup(data);
  }

  /*
  public void refreshReviews() {
    if(endlessRecyclerOnScrollListener!=null) {
      endlessRecyclerOnScrollListener.onLoadMore(false);
    }
  }
  */

  @NonNull @Override
  public CommentsReadMoreDisplayable createReadMoreDisplayable(final int itemPosition,
      Review review) {
    return new CommentsReadMoreDisplayable(review.getId(), true,
        review.getCommentList().getDatalist().getNext(),
        new SimpleReviewCommentAdder(itemPosition, this));
  }

  @Override protected CommentsAdapter createAdapter() {
    return new CommentsAdapter<>(RateAndReviewCommentDisplayable.class);
  }

  @Override
  public void createDisplayableComments(List<Comment> comments, List<Displayable> displayables) {
    for (final Comment comment : comments) {
      displayables.add(new CommentDisplayable(comment));
    }
  }

  void checkAndRemoveProgressBarDisplayable() {
    for (int i = 0; i < getAdapter().getItemCount(); i++) {
      Displayable displayable = getAdapter().getDisplayable(i);
      if (displayable instanceof ProgressBarDisplayable) {
        getAdapter().removeDisplayable(i);
        getAdapter().notifyItemRemoved(i);
      }
    }
  }

  /**
   * Bundle of constants
   */
  @Partners public static class BundleCons {
    public static final String APP_ID = "app_id";
    public static final String PACKAGE_NAME = "package_name";
    public static final String STORE_NAME = "store_name";
    public static final String APP_NAME = "app_name";
    public static final String REVIEW_ID = "review_id";
    public static final String STORE_THEME = "store_theme";
  }
}
