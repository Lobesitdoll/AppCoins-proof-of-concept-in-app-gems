/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 29/07/2016.
 */

package cm.aptoide.pt.dataprovider.ws.v7;

import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.dataprovider.DataProvider;
import cm.aptoide.pt.dataprovider.repository.IdsRepository;
import cm.aptoide.pt.dataprovider.ws.Api;
import cm.aptoide.pt.model.v7.ListComments;
import cm.aptoide.pt.networkclient.WebService;
import cm.aptoide.pt.networkclient.okhttp.OkHttpClientFactory;
import cm.aptoide.pt.preferences.secure.SecurePreferencesImplementation;
import cm.aptoide.pt.utils.AptoideUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import rx.Observable;

/**
 * Created by sithengineer on 20/07/16.
 */

/**
 * http://ws2.aptoide.com/api/7/listFullComments/info/1
 * <p>
 * http://ws2.aptoide.com/api/7/listComments/info/1
 */
public class ListCommentsRequest extends V7<ListComments,ListCommentsRequest.Body> {

	private static final String BASE_HOST = "http://ws2.aptoide.com/api/7/";

	private final boolean getAppInfo;

	protected ListCommentsRequest(Body body, String baseHost, boolean getAppInfo) {
		super(body, OkHttpClientFactory.getSingletonClient(), WebService.getDefaultConverter(), baseHost);
		this.getAppInfo = getAppInfo;
	}

	public static ListCommentsRequest of(long reviewId, int limit, boolean getAppInfo) {
		//
		//
		//
		IdsRepository idsRepository = new IdsRepository(SecurePreferencesImplementation.getInstance(), DataProvider.getContext());
		Body body = new Body(idsRepository.getAptoideClientUUID(), AptoideAccountManager.getAccessToken(), AptoideUtils.Core.getVerCode(), "pool", Api.LANG, Api
				.isMature(), Api.Q, limit, reviewId);
		return new ListCommentsRequest(body, BASE_HOST, getAppInfo);
	}

	@Override
	protected Observable<ListComments> loadDataFromNetwork(Interfaces interfaces, boolean bypassCache) {
		return getAppInfo ? interfaces.listFullComments(body, bypassCache) : interfaces.listComments(body, bypassCache);
	}

	@Data
	@Accessors(chain = false)
	@EqualsAndHashCode(callSuper = true)
	public static class Body extends BaseBody implements Endless {

		@Getter private Integer limit;
		@Getter @Setter private int offset;
		//private String lang;
		//private boolean mature;
		private String q = Api.Q;

		private long reviewId;

		public Body(String aptoideId, String accessToken, int aptoideVersionCode, String cdn, String lang, boolean mature, String q, int limit, long
				reviewId) {
			super(aptoideId, accessToken, aptoideVersionCode, cdn, lang, mature, q);

			this.limit = limit;
			this.reviewId = reviewId;
		}
	}
}