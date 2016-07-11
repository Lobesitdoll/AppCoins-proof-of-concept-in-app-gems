/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 04/07/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.appView;

import cm.aptoide.pt.model.v7.GetApp;
import cm.aptoide.pt.model.v7.Type;
import cm.aptoide.pt.v8engine.R;

/**
 * Created by sithengineer on 30/06/16.
 */
public class AppViewFlagThisDisplayable extends AppViewDisplayable {

	public AppViewFlagThisDisplayable() {
	}

	public AppViewFlagThisDisplayable(GetApp getApp) {
		super(getApp);
	}

	public AppViewFlagThisDisplayable(GetApp getApp, boolean fixedPerLineCount) {
		super(getApp, fixedPerLineCount);
	}

	@Override
	public Type getType() {
		return Type.APP_VIEW_FLAG_THIS;
	}

	@Override
	public int getViewLayout() {
		return R.layout.displayable_app_view_flag_this;
	}
}