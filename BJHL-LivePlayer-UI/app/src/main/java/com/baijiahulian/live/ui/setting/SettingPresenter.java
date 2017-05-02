package com.baijiahulian.live.ui.setting;

import com.baijiahulian.live.ui.activity.LiveRoomRouterListener;
import com.baijiahulian.livecore.context.LPConstants;
import com.baijiahulian.livecore.wrapper.LPPlayer;
import com.baijiahulian.livecore.wrapper.LPRecorder;

import static com.baijiahulian.live.ui.utils.Precondition.checkNotNull;

/**
 * Created by Shubo on 2017/3/2.
 */

public class SettingPresenter implements SettingContract.Presenter {

    private SettingContract.View view;
    private LiveRoomRouterListener routerListener;
    private LPRecorder recorder;
    private LPPlayer player;

    public SettingPresenter(SettingContract.View view) {
        this.view = view;
    }

    @Override
    public void setRouter(LiveRoomRouterListener liveRoomRouterListener) {
        this.routerListener = liveRoomRouterListener;
        recorder = routerListener.getLiveRoom().getRecorder();
        player = routerListener.getLiveRoom().getPlayer();
    }

    @Override
    public void subscribe() {
        checkNotNull(routerListener);

        if (recorder.getLinkType() == LPConstants.LPLinkType.TCP)
            view.showUpLinkTCP();
        else
            view.showUpLinkUDP();

        if (player.getLinkType() == LPConstants.LPLinkType.TCP)
            view.showDownLinkTCP();
        else
            view.showDownLinkUDP();

        if (recorder.isAudioAttached())
            view.showMicOpen();
        else
            view.showMicClosed();

        if (recorder.isVideoAttached())
            view.showCameraOpen();
        else
            view.showCameraClosed();

        if (recorder.isBeautyFilterOn())
            view.showBeautyFilterEnable();
        else
            view.showBeautyFilterDisable();

        if (recorder.getVideoDefinition() == LPConstants.LPResolutionType.HIGH)
            view.showDefinitionHigh();
        else
            view.showDefinitionLow();

        if (routerListener.getPPTShowType() == LPConstants.LPPPTShowWay.SHOW_FULL_SCREEN)
            view.showPPTFullScreen();
        else
            view.showPPTOverspread();
    }

    @Override
    public void unSubscribe() {

    }

    @Override
    public void destroy() {
        routerListener = null;
        recorder = null;
        player = null;
        view = null;
    }

    @Override
    public void changeMic() {
        switch (routerListener.getLiveRoom().getCurrentUser().getType()) {
            case Teacher:
            case Assistant:
                if (!recorder.isPublishing()) {
                    recorder.publish();
                }
                if (recorder.isAudioAttached()) {
                    recorder.detachAudio();
                    view.showMicClosed();
                } else {
                    recorder.attachAudio();
                    view.showMicOpen();
                }
                break;
            case Student:
                if (!recorder.isPublishing()) {
                    view.showStudentFail();
                    return;
                }
                if (recorder.isAudioAttached()) {
                    recorder.detachAudio();
                    view.showMicClosed();
                } else {
                    recorder.attachAudio();
                    view.showMicOpen();
                }
                break;
            case Visitor:
                view.showVisitorFail();
                break;
        }

    }

    @Override
    public void changeCamera() {
        switch (routerListener.getLiveRoom().getCurrentUser().getType()) {
            case Teacher:
            case Assistant:
                if (!recorder.isPublishing()) {
                    recorder.publish();
                }
                if (recorder.isVideoAttached()) {
                    routerListener.detachVideo();
                    view.showCameraClosed();
                } else {
                    routerListener.attachVideo();
                    view.showCameraOpen();
                }
                break;
            case Student:
                if (!recorder.isPublishing()) {
                    view.showStudentFail();
                    return;
                }
                if (recorder.isVideoAttached()) {
                    routerListener.detachVideo();
                    view.showCameraClosed();
                } else {
                    routerListener.attachVideo();
                    view.showCameraOpen();
                }
                break;
            case Visitor:
                view.showVisitorFail();
                break;
        }
    }

    @Override
    public void changeBeautyFilter() {
        if (recorder.isBeautyFilterOn()) {
            recorder.closeBeautyFilter();
            view.showBeautyFilterDisable();
        } else {
            recorder.openBeautyFilter();
            view.showBeautyFilterEnable();
        }
    }

    @Override
    public void setPPTFullScreen() {
        routerListener.setPPTShowType(LPConstants.LPPPTShowWay.SHOW_FULL_SCREEN);
        view.showPPTFullScreen();
    }

    @Override
    public void setPPTOverspread() {
        routerListener.setPPTShowType(LPConstants.LPPPTShowWay.SHOW_COVERED);
        view.showPPTOverspread();
    }

    @Override
    public void setDefinitionLow() {
        recorder.setCaptureVideoDefinition(LPConstants.LPResolutionType.LOW);
        view.showDefinitionLow();
    }

    @Override
    public void setDefinitionHigh() {
        recorder.setCaptureVideoDefinition(LPConstants.LPResolutionType.HIGH);
        view.showDefinitionHigh();
    }

    @Override
    public void setUpLinkTCP() {
        recorder.setLinkType(LPConstants.LPLinkType.TCP);
        view.showUpLinkTCP();
    }

    @Override
    public void setUpLinkUDP() {
        recorder.setLinkType(LPConstants.LPLinkType.UDP);
        view.showUpLinkUDP();
    }

    @Override
    public void setDownLinkTCP() {
        player.setLinkType(LPConstants.LPLinkType.TCP);
        view.showDownLinkTCP();
    }

    @Override
    public void setDownLinkUDP() {
        player.setLinkType(LPConstants.LPLinkType.UDP);
        view.showDownLinkUDP();
    }
}
