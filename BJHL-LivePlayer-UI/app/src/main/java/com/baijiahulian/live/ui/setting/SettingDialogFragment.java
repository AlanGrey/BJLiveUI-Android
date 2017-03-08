package com.baijiahulian.live.ui.setting;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.baijiahulian.live.ui.R;
import com.baijiahulian.live.ui.base.BaseDialogFragment;
import com.baijiahulian.live.ui.utils.Query;

/**
 * Created by Shubo on 2017/3/2.
 */

public class SettingDialogFragment extends BaseDialogFragment implements SettingContract.View {

    private Query $;
    private SettingContract.Presenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_setting;
    }

    @Override
    protected void init(Bundle savedInstanceState, Bundle arguments) {
        super.title(getString(R.string.live_setting)).editable(false);
        $ = Query.with(contentView);
        $.id(R.id.dialog_setting_mic).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changeMic();
            }
        });
        $.id(R.id.dialog_setting_camera).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changeCamera();
            }
        });
        $.id(R.id.dialog_setting_beauty_filter).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changeBeautyFilter();
            }
        });
        $.id(R.id.dialog_setting_radio_ppt_fs).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setPPTFullScreen();
            }
        });
        $.id(R.id.dialog_setting_radio_ppt_os).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setPPTOverspread();
            }
        });
        $.id(R.id.dialog_setting_radio_definition_low).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setDefinitionLow();
            }
        });
        $.id(R.id.dialog_setting_radio_definition_high).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setDefinitionHigh();
            }
        });
        $.id(R.id.dialog_setting_radio_link_up_1).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setUpLinkTCP();
            }
        });
        $.id(R.id.dialog_setting_radio_link_up_2).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setUpLinkUDP();
            }
        });
        $.id(R.id.dialog_setting_radio_link_down_1).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setDownLinkTCP();
            }
        });
        $.id(R.id.dialog_setting_radio_link_down_2).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setDownLinkUDP();
            }
        });
    }

    @Override
    public void showMicOpen() {
        $.id(R.id.dialog_setting_mic).enable(true);
    }

    @Override
    public void showMicClosed() {
        $.id(R.id.dialog_setting_mic).enable(false);
    }

    @Override
    public void showCameraOpen() {
        $.id(R.id.dialog_setting_camera).enable(true);
    }

    @Override
    public void showCameraClosed() {
        $.id(R.id.dialog_setting_camera).enable(false);
    }

    @Override
    public void showBeautyFilterEnable() {
        $.id(R.id.dialog_setting_beauty_filter).enable(true);
    }

    @Override
    public void showBeautyFilterDisable() {
        $.id(R.id.dialog_setting_beauty_filter).enable(false);
    }

    @Override
    public void showPPTFullScreen() {
        $.id(R.id.dialog_setting_radio_ppt_fs).enable(true);
        $.id(R.id.dialog_setting_radio_ppt_os).enable(false);
        ((Button) $.id(R.id.dialog_setting_radio_ppt_fs).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
        ((Button) $.id(R.id.dialog_setting_radio_ppt_os).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
    }

    @Override
    public void showPPTOverspread() {
        $.id(R.id.dialog_setting_radio_ppt_fs).enable(false);
        $.id(R.id.dialog_setting_radio_ppt_os).enable(true);
        ((Button) $.id(R.id.dialog_setting_radio_ppt_fs).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
        ((Button) $.id(R.id.dialog_setting_radio_ppt_os).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
    }

    @Override
    public void showDefinitionLow() {
        $.id(R.id.dialog_setting_radio_definition_low).enable(true);
        $.id(R.id.dialog_setting_radio_definition_high).enable(false);
        ((Button) $.id(R.id.dialog_setting_radio_definition_low).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
        ((Button) $.id(R.id.dialog_setting_radio_definition_high).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
    }

    @Override
    public void showDefinitionHigh() {
        $.id(R.id.dialog_setting_radio_definition_low).enable(false);
        $.id(R.id.dialog_setting_radio_definition_high).enable(true);
        ((Button) $.id(R.id.dialog_setting_radio_definition_low).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
        ((Button) $.id(R.id.dialog_setting_radio_definition_high).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
    }

    @Override
    public void showUpLinkTCP() {
        $.id(R.id.dialog_setting_radio_link_up_1).enable(true);
        $.id(R.id.dialog_setting_radio_link_up_2).enable(false);
        ((Button) $.id(R.id.dialog_setting_radio_link_up_1).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
        ((Button) $.id(R.id.dialog_setting_radio_link_up_2).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
    }

    @Override
    public void showUpLinkUDP() {
        $.id(R.id.dialog_setting_radio_link_up_1).enable(false);
        $.id(R.id.dialog_setting_radio_link_up_2).enable(true);
        ((Button) $.id(R.id.dialog_setting_radio_link_up_1).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
        ((Button) $.id(R.id.dialog_setting_radio_link_up_2).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
    }

    @Override
    public void showDownLinkTCP() {
        $.id(R.id.dialog_setting_radio_link_down_1).enable(true);
        $.id(R.id.dialog_setting_radio_link_down_2).enable(false);
        ((Button) $.id(R.id.dialog_setting_radio_link_down_1).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
        ((Button) $.id(R.id.dialog_setting_radio_link_down_2).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
    }

    @Override
    public void showDownLinkUDP() {
        $.id(R.id.dialog_setting_radio_link_down_1).enable(false);
        $.id(R.id.dialog_setting_radio_link_down_2).enable(true);
        ((Button) $.id(R.id.dialog_setting_radio_link_down_1).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_text_color));
        ((Button) $.id(R.id.dialog_setting_radio_link_down_2).view()).setTextColor(ContextCompat.getColor(getContext(), R.color.live_white));
    }

    @Override
    public void showVisitorFail() {
        showToast(getString(R.string.live_media_visitor_fail));
    }

    @Override
    public void showStudentFail() {
        showToast(getString(R.string.live_media_student_fail));
    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
        super.setBasePresenter(presenter);
        this.presenter = presenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter = null;
        $ = null;
    }
}
