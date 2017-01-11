package cm.aptoide.pt.dataprovider.ws.v7;

import cm.aptoide.pt.dataprovider.interfaces.EndlessControllerWithCache;
import cm.aptoide.pt.model.v7.BaseV7EndlessDatalistResponse;
import cm.aptoide.pt.model.v7.store.ListStores;
import cm.aptoide.pt.model.v7.store.Store;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by neuro on 03-01-2017.
 */

public class V7EndlessController<U> implements EndlessControllerWithCache<U> {

  private final V7<? extends BaseV7EndlessDatalistResponse<U>, ? extends Endless> v7request;
  private int total;
  private int offset;
  private boolean loading;
  private boolean stableData;
  private Observable<List<U>> observable;

  public V7EndlessController(
      V7<? extends BaseV7EndlessDatalistResponse<U>, ? extends Endless> v7request) {
    this.v7request = v7request;
  }

  public static List<Store> from(ListStores listStores) {
    return listStores.getDatalist().getList();
  }

  @Override public Observable<List<U>> get() {
    return v7request.observe().map(listStores1 -> listStores1.getDatalist().getList());
  }

  @Override public Observable<List<U>> loadMore() {
    return loadMore(false);
  }

  public Observable<List<U>> loadMore(boolean bypassCache) {

    if (!loading) {
      if (hasMoreElements()) {
        return observable = v7request.observe(bypassCache)
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .cache()
            .doOnSubscribe(() -> {
              loading = true;
            })
            .doOnError(error -> {
              //remove spinner if webservice respond with error
              loading = false;
            })
            .map(response -> {

              List<U> list;

              if (response.hasData()) {

                stableData = response.hasStableTotal();
                if (stableData) {
                  total = response.getTotal();
                  offset = response.getNextSize();
                } else {
                  total += response.getTotal();
                  offset += response.getNextSize();
                }
                v7request.getBody().setOffset(offset);
                list = response.getDatalist().getList();
              } else {
                list = Collections.emptyList();
              }

              loading = false;

              return list;
            });
      } else {
        return Observable.just(Collections.emptyList());
      }
    } else {
      return observable;
    }
  }

  private boolean hasMoreElements() {
    return (stableData) ? offset < total : offset <= total;
  }
}
