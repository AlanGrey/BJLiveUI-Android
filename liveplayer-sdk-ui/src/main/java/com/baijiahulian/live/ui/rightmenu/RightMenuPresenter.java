package com.baijiahulian.live.ui.rightmenu;

import android.util.Log;

import com.baijiahulian.live.ui.activity.LiveRoomRouterListener;
import com.baijiahulian.live.ui.utils.RxUtils;
import com.baijiahulian.livecore.context.LPConstants;
import com.baijiahulian.livecore.listener.OnSpeakApplyCountDownListener;
import com.baijiahulian.livecore.models.LPSpeakInviteModel;
import com.baijiahulian.livecore.models.imodels.IMediaControlModel;
import com.baijiahulian.livecore.models.imodels.IMediaModel;
import com.baijiahulian.livecore.utils.LPErrorPrintSubscriber;
import com.baijiahulian.livecore.wrapper.LPPlayer;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

import static com.baijiahulian.live.ui.utils.Precondition.checkNotNull;

/**
 * Created by Shubo on 2017/2/15.
 */

public class RightMenuPresenter implements RightMenuContract.Presenter {

    private LiveRoomRouterListener liveRoomRouterListener;
    private RightMenuContract.View view;
    private LPConstants.LPUserType currentUserType;
    private Subscription subscriptionOfMediaControl, subscriptionOfMediaPublishDeny, subscriptionOfSpeakApplyDeny, subscriptionOfClassEnd, subscriptionOfSpeakApplyResponse,
            subscriptionOfSpeakInvite, subscriptionOfClassStart, subscriptionOfUserOut;
    private int speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
    private boolean isDrawing = false;

    public RightMenuPresenter(RightMenuContract.View view) {
        this.view = view;
    }

    @Override
    public void visitOnlineUser() {
        liveRoomRouterListener.navigateToUserList();
    }

    @Override
    public void changeDrawing() {
        if (!isDrawing && !liveRoomRouterListener.canStudentDraw()) {
            view.showCantDraw();
            return;
        }
        if (!liveRoomRouterListener.isTeacherOrAssistant() && !liveRoomRouterListener.getLiveRoom().isClassStarted()) {
            view.showCantDrawCauseClassNotStart();
            return;
        }
        liveRoomRouterListener.navigateToPPTDrawing();
        isDrawing = !isDrawing;
        view.showDrawingStatus(isDrawing);
    }

    @Override
    public void managePPT() {
        if (currentUserType == LPConstants.LPUserType.Teacher
                || currentUserType == LPConstants.LPUserType.Assistant) {
            liveRoomRouterListener.navigateToPPTWareHouse();
        }
    }

    public int getSpeakApplyStatus() {
        return speakApplyStatus;
    }

    @Override
    public void speakApply() {
        checkNotNull(liveRoomRouterListener);

        if (!liveRoomRouterListener.getLiveRoom().isClassStarted()) {
            view.showHandUpError();
            return;
        }

        if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_NONE) {
            if (liveRoomRouterListener.getLiveRoom().getForbidRaiseHandStatus()) {
                view.showHandUpForbid();
                return;
            }

            if (liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().isSpeakersFull()){
                speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
                view.showSpeakClosedByServer();
                return;
            }

            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().requestSpeakApply(new OnSpeakApplyCountDownListener() {
                @Override
                public void onTimeOut() {
                    speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                    liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
                    view.showSpeakApplyCanceled();
                    view.showHandUpTimeout();
                }

                @Override
                public void onTimeCountDown(int counter, int timeOut) {
                    view.showSpeakApplyCountDown(timeOut - counter, timeOut);
                }
            });
            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_APPLYING;
            view.showWaitingTeacherAgree();
        } else if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_APPLYING) {
            // 取消发言请求
            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
            view.showSpeakApplyCanceled();
        } else if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING) {
            // 取消发言
            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
            liveRoomRouterListener.disableSpeakerMode();
            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                    .closeOtherSpeak(liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId());
            view.showSpeakApplyCanceled();
            if (isDrawing) {
                // 如果画笔打开 关闭画笔模式
                changeDrawing();
            }
        }
    }

    @Override
    public void changePPTDrawBtnStatus(boolean shouldShow) {
        if (shouldShow) {
            //老师或者助教或者已同意发言的学生可以使用ppt
            if (currentUserType == LPConstants.LPUserType.Teacher
                    || currentUserType == LPConstants.LPUserType.Assistant
                    || speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING) {
                view.showPPTDrawBtn();
            }
        } else {
            view.hidePPTDrawBtn();
        }
    }

    @Override
    public void onSpeakInvite(int confirm) {
        liveRoomRouterListener.getLiveRoom().sendSpeakInvite(confirm);
        if (confirm == 1) {
            //接受
            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING;
            liveRoomRouterListener.getLiveRoom().getRecorder().publish();
            if (!liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached())
                liveRoomRouterListener.attachLocalAudio();
            if (liveRoomRouterListener.getLiveRoom().getAutoOpenCameraStatus()) {
                Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
//                        .filter(new Func1<Long, Boolean>() {
//                            @Override
//                            public Boolean call(Long aLong) {
//                                return !liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached();
//                            }
//                        })
                        .subscribe(new LPErrorPrintSubscriber<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (!liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached())
                                    liveRoomRouterListener.attachLocalVideo();
                            }
                        });
            }
            view.showForceSpeak();
            liveRoomRouterListener.enableSpeakerMode();
        }
    }

    @Override
    public void setRouter(LiveRoomRouterListener liveRoomRouterListener) {
        this.liveRoomRouterListener = liveRoomRouterListener;
    }

    @Override
    public void subscribe() {
        checkNotNull(liveRoomRouterListener);
        currentUserType = liveRoomRouterListener.getLiveRoom().getCurrentUser().getType();

        if (liveRoomRouterListener.isTeacherOrAssistant()) {
            view.showTeacherRightMenu();
        } else {
            view.showStudentRightMenu();
            if (liveRoomRouterListener.getLiveRoom().getPartnerConfig().liveHideUserList == 1) {
                view.hideUserList();
            }
        }

        if (!liveRoomRouterListener.isTeacherOrAssistant()) {
            // 学生

            subscriptionOfMediaPublishDeny = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                    .getObservableOfMediaDeny()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaModel>() {
                        @Override
                        public void call(IMediaModel iMediaModel) {
                            if (!liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached() && !liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached())
                                view.showForceSpeakDenyByServer();
                            if(liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached()){
                                liveRoomRouterListener.getLiveRoom().getRecorder().detachAudio();
                            }
                            if(liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached()){
                                liveRoomRouterListener.getLiveRoom().getRecorder().detachVideo();
                                liveRoomRouterListener.detachLocalVideo();
                            }
//                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
//                            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();

                            if (liveRoomRouterListener.getLiveRoom().getRoomType() != LPConstants.LPRoomType.Multi)
                                view.showAutoSpeak();
                        }
                    });
            subscriptionOfSpeakApplyDeny = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().getObservableOfSpeakApplyDeny()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaModel>() {
                        @Override
                        public void call(IMediaModel iMediaModel) {
                            // 结束发言模式
                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
                            view.showSpeakClosedByServer();
                        }
                    });

            subscriptionOfMediaControl = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                    .getObservableOfMediaControl()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaControlModel>() {
                        @Override
                        public void call(final IMediaControlModel iMediaControlModel) {
                            if (iMediaControlModel.isApplyAgreed()) {
                                // 强制发言
                                if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING) {
                                    //已经在发言了
                                    if (iMediaControlModel.isAudioOn() && !liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached()) {
                                        liveRoomRouterListener.getLiveRoom().getRecorder().attachAudio();
                                    } else if (!iMediaControlModel.isAudioOn() && liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached()) {
                                        liveRoomRouterListener.getLiveRoom().getRecorder().detachAudio();
                                    }
                                    if (iMediaControlModel.isVideoOn() && !liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached()) {
                                        liveRoomRouterListener.attachLocalVideo();
                                    } else if (!iMediaControlModel.isVideoOn() && liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached()) {
                                        liveRoomRouterListener.detachLocalVideo();
                                    }

                                } else {
                                    speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING;
                                    liveRoomRouterListener.enableSpeakerMode();
                                    view.showForceSpeak();
                                    liveRoomRouterListener.showForceSpeakDlg(iMediaControlModel);
                                }
                            } else {
                                // 结束发言模式
                                speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                                liveRoomRouterListener.disableSpeakerMode();
                                if (isDrawing) {
                                    // 如果画笔打开 关闭画笔模式
                                    changeDrawing();
                                }
                                if (!iMediaControlModel.getSenderUserId().equals(liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId())) {
                                    // 不是自己结束发言的
                                    view.showSpeakClosedByTeacher();
                                }
                            }
                        }
                    });

            subscriptionOfSpeakApplyResponse = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().getObservableOfSpeakResponse()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaControlModel>() {
                        @Override
                        public void call(IMediaControlModel iMediaControlModel) {
                            if (!iMediaControlModel.getUser().getUserId()
                                    .equals(liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId()))
                                return;
                            // 请求发言的用户自己
                            if (iMediaControlModel.isApplyAgreed()) {
                                // 进入发言模式
                                liveRoomRouterListener.getLiveRoom().getRecorder().publish();
//                                liveRoomRouterListener.getLiveRoom().getRecorder().attachVideo();
                                liveRoomRouterListener.attachLocalAudio();
                                view.showSpeakApplyAgreed();
                                speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING;
                                liveRoomRouterListener.enableSpeakerMode();
                                if (liveRoomRouterListener.getLiveRoom().getAutoOpenCameraStatus()) {
                                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new LPErrorPrintSubscriber<Long>() {
                                                @Override
                                                public void call(Long aLong) {
                                                        liveRoomRouterListener.attachLocalVideo();
                                                }
                                            });
                                }
                            } else {
                                speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                                if (!iMediaControlModel.getSenderUserId().equals(liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId())) {
                                    // 不是自己结束发言的
                                    view.showSpeakApplyDisagreed();
                                }
                            }
                        }
                    });
        } else if (liveRoomRouterListener.getLiveRoom().getCurrentUser().getType() == LPConstants.LPUserType.Assistant) {
            // 助教
            subscriptionOfMediaPublishDeny = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                    .getObservableOfMediaDeny()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaModel>() {
                        @Override
                        public void call(IMediaModel iMediaModel) {
                            if (!liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached() && !liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached())
                                view.showForceSpeakDenyByServer();
                            if(liveRoomRouterListener.getLiveRoom().getRecorder().isAudioAttached()){
                                liveRoomRouterListener.getLiveRoom().getRecorder().detachAudio();
                            }
                            if(liveRoomRouterListener.getLiveRoom().getRecorder().isVideoAttached()){
                                liveRoomRouterListener.getLiveRoom().getRecorder().detachVideo();
                                liveRoomRouterListener.detachLocalVideo();
                            }

                            if (liveRoomRouterListener.getLiveRoom().getRoomType() != LPConstants.LPRoomType.Multi)
                                view.showAutoSpeak();
                        }
                    });
            subscriptionOfSpeakApplyDeny = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().getObservableOfSpeakApplyDeny()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaModel>() {
                        @Override
                        public void call(IMediaModel iMediaModel) {
                            // 结束发言模式
                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
                            view.showSpeakClosedByServer();
                        }
                    });

            subscriptionOfMediaControl = liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                    .getObservableOfMediaControl()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new LPErrorPrintSubscriber<IMediaControlModel>() {
                        @Override
                        public void call(IMediaControlModel iMediaControlModel) {
                            if (!iMediaControlModel.isApplyAgreed()) {
                                // 结束发言模式
                                liveRoomRouterListener.disableSpeakerMode();
                                if (isDrawing) changeDrawing();
                            }
                        }
                    });
        }

        subscriptionOfClassEnd = liveRoomRouterListener.getLiveRoom().getObservableOfClassEnd()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LPErrorPrintSubscriber<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_APPLYING) {
                            // 取消发言请求
                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM().cancelSpeakApply();
                            view.showSpeakApplyCanceled();
                        } else if (speakApplyStatus == RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING) {
                            // 取消发言
                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_NONE;
                            liveRoomRouterListener.disableSpeakerMode();
                            liveRoomRouterListener.getLiveRoom().getSpeakQueueVM()
                                    .closeOtherSpeak(liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId());
                            view.showSpeakApplyCanceled();
                            if (isDrawing) {
                                // 如果画笔打开 关闭画笔模式
                                changeDrawing();
                            }
                        }
                    }
                });

        subscriptionOfClassStart = liveRoomRouterListener.getLiveRoom().getObservableOfClassStart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LPErrorPrintSubscriber<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (liveRoomRouterListener.getLiveRoom().getCurrentUser().getType() == LPConstants.LPUserType.Student &&
                                liveRoomRouterListener.getLiveRoom().getRoomType() != LPConstants.LPRoomType.Multi) {
                            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING;
                        }
                    }
                });

        if (liveRoomRouterListener.getLiveRoom().getCurrentUser().getType() == LPConstants.LPUserType.Student &&
                liveRoomRouterListener.getLiveRoom().getRoomType() != LPConstants.LPRoomType.Multi) {
            view.showAutoSpeak();
            speakApplyStatus = RightMenuContract.STUDENT_SPEAK_APPLY_SPEAKING;
        }

        //邀请发言
        subscriptionOfSpeakInvite = liveRoomRouterListener.getLiveRoom().getObservableOfSpeakInvite()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LPErrorPrintSubscriber<LPSpeakInviteModel>() {
                    @Override
                    public void call(LPSpeakInviteModel lpSpeakInviteModel) {
                        if (liveRoomRouterListener.getLiveRoom().getCurrentUser().getUserId().equals(lpSpeakInviteModel.to)) {
                            liveRoomRouterListener.showSpeakInviteDlg(lpSpeakInviteModel.invite);
                        }
                    }
                });
    }

    @Override
    public void unSubscribe() {
        RxUtils.unSubscribe(subscriptionOfMediaControl);
        RxUtils.unSubscribe(subscriptionOfSpeakApplyResponse);
        RxUtils.unSubscribe(subscriptionOfClassEnd);
        RxUtils.unSubscribe(subscriptionOfSpeakInvite);
        RxUtils.unSubscribe(subscriptionOfClassStart);
        RxUtils.unSubscribe(subscriptionOfSpeakApplyDeny);
        RxUtils.unSubscribe(subscriptionOfMediaPublishDeny);
    }

    @Override
    public void destroy() {
        liveRoomRouterListener = null;
        view = null;
    }
}
