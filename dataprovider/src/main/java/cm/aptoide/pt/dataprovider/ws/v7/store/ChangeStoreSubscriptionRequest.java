package cm.aptoide.pt.dataprovider.ws.v7.store;

import cm.aptoide.pt.dataprovider.ws.BaseBodyDecorator;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.ChangeStoreSubscriptionResponse;
import cm.aptoide.pt.dataprovider.ws.v7.V7;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import rx.Observable;

/**
 * Created by trinkes on 03/03/2017.
 */

public class ChangeStoreSubscriptionRequest
    extends V7<ChangeStoreSubscriptionResponse, ChangeStoreSubscriptionRequest.Body> {
  private static final String BASE_HOST = "https://ws75-primary.aptoide.com/api/7/";

  protected ChangeStoreSubscriptionRequest(Body body, String baseHost) {
    super(body, baseHost);
  }

  public static ChangeStoreSubscriptionRequest of(String storeName,
      ChangeStoreSubscriptionResponse.StoreSubscriptionState storeSubscription,
      String aptoideClientUUID, String accessToken) {
    Body body = (Body) new BaseBodyDecorator(aptoideClientUUID).decorate(
        new Body(storeName, storeSubscription), accessToken);
    return new ChangeStoreSubscriptionRequest(body, BASE_HOST);
  }

  public static ChangeStoreSubscriptionRequest of(String storeName,
      ChangeStoreSubscriptionResponse.StoreSubscriptionState storeSubscription,
      String aptoideClientUUID, String accessToken, String storeUser, String sha1PassWord) {
    Body body = (Body) new BaseBodyDecorator(aptoideClientUUID).decorate(
        new Body(storeName, storeSubscription, storeUser, sha1PassWord), accessToken);
    return new ChangeStoreSubscriptionRequest(body, BASE_HOST);
  }

  @Override
  protected Observable<ChangeStoreSubscriptionResponse> loadDataFromNetwork(Interfaces interfaces,
      boolean bypassCache) {
    return interfaces.changeStoreSubscription(bypassCache, body);
  }

  @EqualsAndHashCode(callSuper = true) public static class Body extends BaseBody {
    @Getter private final String storeName;
    @Getter private final ChangeStoreSubscriptionResponse.StoreSubscriptionState status;
    @Getter private String storePassSha1;
    @Getter private String storeUser;

    public Body(String storeName, ChangeStoreSubscriptionResponse.StoreSubscriptionState status) {
      this.storeName = storeName;
      this.status = status;
    }

    public Body(String storeName, ChangeStoreSubscriptionResponse.StoreSubscriptionState status,
        String storeUser, String storePassSha1) {
      this.storeName = storeName;
      this.storePassSha1 = storePassSha1;
      this.status = status;
      this.storeUser = storeUser;
    }
  }
}