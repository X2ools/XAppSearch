
package org.x2ools.xappsearchlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;
import org.x2ools.xappsearchlib.tools.JsonParser;
import org.x2ools.xappsearchlib.tools.ToPinYinUtils;

import java.io.File;
import java.util.List;

public class T9AppsView extends FrameLayout implements T9ViewDelegate, RecognizerListener {

    public static final boolean DEBUG = true;
    private static final String TAG = "T9AppsView";
    private static final String FLYTEK_APPID = "5af836f8";

    private T9ViewModel mViewModel;

    private EditText mFilterView;
    private GridView mAppsGridView;

    private StringBuilder mFilterText = new StringBuilder();
    private AppsAdapter mAdapter;
    private SpeechRecognizer mSR;

    private boolean enMode = false;

    public T9AppsView(Context context) {
        this(context, null);
    }

    public T9AppsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public T9AppsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.t9_apps_content, this);

        if (!isInEditMode()) {
            mViewModel = new T9ViewModel(context);
            mViewModel.setDelegate(this);
            initSubViews();
            mViewModel.filter("");
            SpeechUtility.createUtility(context, "appid=" + FLYTEK_APPID);

            mSR = SpeechRecognizer.createRecognizer(context,
                    code -> Log.d(TAG, "SpeechRecognizer init() code = " + code));
            mSR.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mSR.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            mSR.setParameter(SpeechConstant.ASR_AUDIO_PATH, new File(context.getFilesDir(), "audio.wav").getAbsolutePath());
            mSR.setParameter(SpeechConstant.ASR_SOURCE_PATH, new File(context.getFilesDir(), "audio.wav").getAbsolutePath());
        }
    }

    @Override
    public void onVolumeChanged(int volume, byte[] bytes) {
//        Log.d(TAG, "onVolumeChanged " + volume);
    }

    @Override
    public void onBeginOfSpeech() {
        Log.d(TAG, "onBeginOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean isLast) {
        Log.d(TAG, "onResult");
        if (recognizerResult == null) return;
        Log.d(TAG, recognizerResult.getResultString());
        String text = JsonParser.parseGrammarResult(recognizerResult.getResultString());
        Log.d(TAG, text);
        List<SearchItem> all = T9Search.getInstance().getAllSubject().getValue();
        if (all == null || all.size() == 0) return;
        SearchItem recognized = null;
        for (SearchItem item : all) {
            if (ToPinYinUtils.getPinYin(text, true).contains(item.getFullpinyin())) {
                recognized = item;
                break;
            }
        }
        if (recognized != null) {
            startItemApp(recognized);
        } else {
            mSR.cancel();
            if (enMode) {
                enMode = false;
                mSR.setParameter(SpeechConstant.LANGUAGE, "zh_CN");
                mSR.setParameter(SpeechConstant.AUDIO_SOURCE, "1");
            } else {
                enMode = true;
                mSR.setParameter(SpeechConstant.LANGUAGE, "en_US");
                mSR.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
            }
            mSR.startListening(this);
        }
    }

    @Override
    public void onError(SpeechError speechError) {
        Log.d(TAG, "onError " + speechError);
        mSR.startListening(this);
    }

    @Override
    public void onEvent(int eventType, int i1, int i2, Bundle bundle) {
        Log.d(TAG, "onEvent " + eventType);
    }

    private void initSubViews() {
        mAppsGridView = findViewById(R.id.appsList);
        mFilterView = findViewById(R.id.numFilter);

        mAppsGridView.setOnItemClickListener((adapterView, view, i, l) -> {
            SearchItem item = (SearchItem) mAdapter.getItem(i);
            startItemApp(item);
        });

        mAppsGridView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            SearchItem item = (SearchItem) mAdapter.getItem(i);
            if (item instanceof ContactItem) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                        item.getId() + "");
                intent.setData(uri);
                getContext().startActivity(intent);
            } else if (item instanceof AppItem) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.parse("package:" + ((AppItem)item).getComponentName()));
                getContext().startActivity(intent);
                hideView();
            }
            return true;
        });

        mFilterView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.filter(s.toString());
            }
        });
    }

    private void startItemApp(SearchItem item) {
        Intent intent = mViewModel.getLaunchIntent(item);
        if (intent != null) {
            try {
                getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mViewModel.recordUsage(item);
        hideView();
    }

    public void clearFilter() {
        mFilterText = new StringBuilder();
        onTextChanged();
    }

    private void onTextChanged() {
        mFilterView.setText(mFilterText);
    }

    public void hideView() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
        }
    }

    public void onPause() {
        mSR.stopListening();
    }

    public void onResume() {
        mSR.startListening(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mSR.cancel();
        mSR.destroy();
    }

    @Override
    public void showItems(List<SearchItem> searchItems) {
        mAdapter = new AppsAdapter(getContext(), searchItems);
        mAppsGridView.setAdapter(mAdapter);
    }
}
