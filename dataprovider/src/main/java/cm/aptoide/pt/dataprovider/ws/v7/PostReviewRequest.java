/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 29/07/2016.
 */

package cm.aptoide.pt.dataprovider.ws.v7;

import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.dataprovider.DataProvider;
import cm.aptoide.pt.dataprovider.repository.IdsRepository;
import cm.aptoide.pt.dataprovider.ws.Api;
import cm.aptoide.pt.model.v7.BaseV7Response;
import cm.aptoide.pt.networkclient.WebService;
import cm.aptoide.pt.networkclient.okhttp.OkHttpClientFactory;
import cm.aptoide.pt.preferences.secure.SecurePreferencesImplementation;
import cm.aptoide.pt.utils.AptoideUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import rx.Observable;

/**
 * Created by sithengineer on 20/07/16.
 */
public class PostReviewRequest extends V7<BaseV7Response,PostReviewRequest.Body> {

	private static final String BASE_HOST = "http://ws75-primary.aptoide.com/api/7/";

	protected PostReviewRequest(Body body, String baseHost) {
		super(body, OkHttpClientFactory.getSingletonClient(), WebService.getDefaultConverter(), baseHost);
	}

	public static PostReviewRequest of(String storeName, String packageName, String title, String textBody, Integer rating) {
		//
		//  http://ws75-primary.aptoide.com/api/7/setReview/package_name/cm.aptoide
		// .pt/store_name/apps/title/Best%20app%20store/rating/5/access_token/ca01ee1e05ab4d82d99ef143e2816e667333c6ef
		//
		IdsRepository idsRepository = new IdsRepository(SecurePreferencesImplementation.getInstance(), DataProvider.getContext());
		Body body = new Body(idsRepository.getAptoideClientUUID(), AptoideAccountManager.getAccessToken(), AptoideUtils.Core.getVerCode(), "pool", Api.LANG,
				Api
				.isMature(), Api.Q, storeName, packageName, title, textBody, rating);
		return new PostReviewRequest(body, BASE_HOST);
	}

	@Override
	protected Observable<BaseV7Response> loadDataFromNetwork(Interfaces interfaces, boolean bypassCache) {
		return interfaces.postReview(body, true);
	}

	@Data
	@Accessors(chain = false)
	@EqualsAndHashCode(callSuper = true)
	public static class Body extends BaseBody {

		private String storeName;
		private String packageName;
		private String title;
		private String body;
		private Integer rating;

		public Body(String aptoideId, String accessToken, int aptoideVersionCode, String cdn, String lang, boolean mature, String q, String storeName, String
				packageName, String title, String body, Integer rating) {
			super(aptoideId, accessToken, aptoideVersionCode, cdn, lang, mature, q);

			this.storeName = storeName;
			this.packageName = packageName;
			this.title = title;
			this.body = body;
			this.rating = rating;
		}
	}
}