package com.baijiahulian.live.ui.videoplayer;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baijiahulian.avsdk.liveplayer.ViESurfaceViewRenderer;
import com.baijiahulian.live.ui.R;
import com.baijiahulian.live.ui.base.BaseFragment;
import com.baijiahulian.live.ui.utils.RxUtils;
import com.baijiahulian.livecore.utils.LPErrorPrintSubscriber;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Shubo on 2017/3/4.
 */

public class VideoPlayerFragment extends BaseFragment implements VideoPlayerContract.View {

    private VideoPlayerContract.Presenter presenter;
    private GestureDetector gestureDetector;
    private FrameLayout flContainer;
    private TextView tvName;
    private Subscription subscriptionOfClickable;

    @Override
    public void setPresenter(VideoPlayerContract.Presenter presenter) {
        super.setBasePresenter(presenter);
        this.presenter = presenter;
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    protected View getContentView() {
        flContainer = new FrameLayout(getActivity());
        ViewGroup.LayoutParams flLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flContainer.setLayoutParams(flLp);
        //视频
        View videoView = ViESurfaceViewRenderer.CreateRenderer(getActivity(), true);
        ((SurfaceView) videoView).setZOrderMediaOverlay(true);
        flContainer.addView(videoView);
        //名字
        tvName = new TextView(getActivity());
        FrameLayout.LayoutParams tvLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLp.gravity = Gravity.BOTTOM;
        tvName.setGravity(Gravity.CENTER);
        tvName.setTextColor(getResources().getColor(R.color.live_white));
        tvName.setPadding(0, 5, 0, 5);
        tvName.setLines(1);
        tvName.setBackgroundResource(R.drawable.shape_remote_name_bg);
        tvName.setLayoutParams(tvLp);
        flContainer.addView(tvName);

        return flContainer;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        presenter.getPlayer().setVideoView((GLSurfaceView) flContainer.getChildAt(0));
        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public void showCurrentVideoUserName(String name) {
        tvName.setText(name);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            presenter.switchWithMaximum();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            presenter.popUpRemoteVideoDialog();
            view.setEnabled(false);
            subscriptionOfClickable = Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<Long>() {
                        @Override
                        public void call(Long aLong) {
                            view.setEnabled(true);
                        }
                    });
            return super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unSubscribe(subscriptionOfClickable);
    }
}