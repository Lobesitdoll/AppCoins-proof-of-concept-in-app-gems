/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 01/08/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.fragment.implementations.AppViewFragment;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.VideoDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;

/**
 * Created by jdandrade on 8/10/16.
 */
public class VideoWidget extends Widget<VideoDisplayable> {

	private TextView title;
	private TextView subtitle;
	private ImageView image;
	private TextView videoTitle;
	private ImageView thumbnail;
	private View url;
	private Button getAppButton;
	private ImageView play_button;
	private FrameLayout media_layout;

	public VideoWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		title = (TextView)itemView.findViewById(R.id.card_title);
		subtitle = (TextView)itemView.findViewById(R.id.card_subtitle);
		image = (ImageView) itemView.findViewById(R.id.card_image);
		play_button = (ImageView) itemView.findViewById(R.id.play_button);
		media_layout = (FrameLayout)itemView.findViewById(R.id.media_layout);
		videoTitle = (TextView) itemView.findViewById(R.id.partial_social_timeline_thumbnail_title);
		thumbnail = (ImageView) itemView.findViewById(R.id.partial_social_timeline_thumbnail_image);
		url = itemView.findViewById(R.id.partial_social_timeline_thumbnail);
		getAppButton = (Button) itemView.findViewById(R.id.partial_social_timeline_thumbnail_get_app_button);
	}

	@Override
	public void bindView(VideoDisplayable displayable) {
		title.setText(displayable.getTitle());
		subtitle.setText(displayable.getTimeSinceLastUpdate(getContext()));
		videoTitle.setText(displayable.getVideoTitle());
		ImageLoader.loadWithShadowCircleTransform(displayable.getAvatarUrl(), image);
		ImageLoader.load(displayable.getThumbnailUrl(), thumbnail);
		play_button.setVisibility(View.VISIBLE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			media_layout.setForeground(
					getContext().getResources().getDrawable(R.color.overlay_black, getContext()
							.getTheme())
			);
		}else {
			media_layout.setForeground(
					getContext().getResources().getDrawable(R.color.overlay_black)
			);
		}

		if (getAppButton.getVisibility() != View.GONE && displayable.isGetApp()) {
			getAppButton.setVisibility(View.VISIBLE);
			getAppButton.setText(displayable.getAppText(getContext()));
			getAppButton.setOnClickListener(view -> ((FragmentShower) getContext())
					.pushFragmentV4(AppViewFragment.newInstance(displayable.getAppId())));
		}

		media_layout.setOnClickListener(v ->{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(displayable.getUrl()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
		});
	}

	@Override
	public void onViewAttached() {

	}

	@Override
	public void onViewDetached() {
		url.setOnClickListener(null);
		getAppButton.setOnClickListener(null);
	}
}