
package org.x2ools.xappsearchlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.util.List;

public class T9AppsView extends FrameLayout implements T9ViewDelegate {

    public static final boolean DEBUG = true;
    private static final String TAG = "T9AppsView";

    private T9ViewModel mViewModel;

    private TextView mFilterView;
    private GridView mAppsGridView;
    private View mDialpad;

    private StringBuilder mFilterText = new StringBuilder();
    private AppsAdapter mAdapter;

    private boolean qwerty;

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
        }
    }

    public boolean isQwerty() {
        return qwerty;
    }

    public void setQwerty(boolean qwerty, boolean anim) {
        this.qwerty = qwerty;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (qwerty) {
            if (anim) {
                ViewCompat.animate(mDialpad)
                        .translationY(mDialpad.getHeight())
                        .setDuration(250)
                        .withEndAction(() -> mDialpad.setVisibility(GONE))
                        .start();
            } else {
                mDialpad.setVisibility(GONE);
            }
            mFilterView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mFilterView.setFocusable(true);
            mFilterView.setFocusableInTouchMode(true);
            mFilterView.requestFocus();
            imm.showSoftInput(mFilterView, InputMethodManager.SHOW_IMPLICIT);
        } else {
            if (anim) {
                mDialpad.setVisibility(VISIBLE);
                mDialpad.setTranslationY(mDialpad.getHeight());
                ViewCompat.animate(mDialpad)
                        .translationY(0)
                        .setDuration(250)
                        .start();
            } else {
                mDialpad.setVisibility(VISIBLE);
            }
            mFilterView.clearFocus();
            mFilterView.setInputType(InputType.TYPE_NULL);
            mFilterView.setFocusable(false);
            imm.hideSoftInputFromWindow(mFilterView.getWindowToken(), 0);
        }
        mFilterText = new StringBuilder();
        mFilterView.setText(mFilterText);
    }

    private void initSubViews() {
        int[] buttons = new int[]{
                R.id.button1, R.id.button2, R.id.button3,

                R.id.button4, R.id.button5, R.id.button6,

                R.id.button7, R.id.button8, R.id.button9,

                R.id.buttonStar, R.id.button0, R.id.buttonDelete
        };

        for (int id : buttons) {
            findViewById(id).setOnClickListener(mOnClickListener);
            findViewById(id).setOnLongClickListener(mOnLongClickListener);
        }
        setOnClickListener(mOnClickListener);
        mAppsGridView = findViewById(R.id.appsList);
        mFilterView = findViewById(R.id.numFilter);
        mDialpad = findViewById(R.id.dialpad);

        mFilterView.setOnClickListener(v -> {
            if (T9Search.getInstance().isCallPhoneEnable()) {
                try {
                    Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"
                            + mFilterView.getText().toString()));
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mAppsGridView.setOnItemClickListener((adapterView, view, i, l) -> {
            SearchItem item = (SearchItem) mAdapter.getItem(i);
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
                intent.setData(Uri.parse("package:" + ((AppItem)item).getPackageName()));
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
                String text = s.toString();
                if ("0000".equals(text)) {
                    setQwerty(!qwerty, true);
                }
                mViewModel.filter(s.toString());
            }
        });
    }

    OnLongClickListener mOnLongClickListener = v -> {
        int id = v.getId();
        if (id == R.id.buttonDelete) {
            clearFilter();
        }
        return false;
    };

    OnClickListener mOnClickListener = view -> {
        int id = view.getId();
        if (id == R.id.button0 || id == R.id.button1
                || id == R.id.button2 || id == R.id.button3
                || id == R.id.button4 || id == R.id.button5
                || id == R.id.button6 || id == R.id.button7
                || id == R.id.button8 || id == R.id.button9) {
            int number = getNumberById(view.getId());
            mFilterText.append(number);
            onTextChanged();
        } else if (id == R.id.buttonStar) {
            if (mViewModel.isAllMode()) {
                mFilterText = new StringBuilder("*");
                switchModeAnimate(false);
            } else {
                mFilterText.delete(0, mFilterText.length());
                switchModeAnimate(true);
            }
            mFilterView.setText(mFilterText);
        } else if (id == R.id.buttonDelete) {
            if (TextUtils.isEmpty(mFilterText))
                return;
            mFilterText.deleteCharAt(mFilterText.length() - 1);
            onTextChanged();
        }
    };

    private void switchModeAnimate(final boolean allMode) {
        final View buttonNumber = findViewById(R.id.buttonNumber);

        ViewCompat.animate(buttonNumber)
                .translationY(allMode ? buttonNumber.getHeight() * 4 / 3 : 0)
                .setDuration(250)
                .withStartAction(() -> {
                    if (!allMode) buttonNumber.setVisibility(VISIBLE);
                })
                .withEndAction(() -> {
                    if (allMode) buttonNumber.setVisibility(GONE);
                })
                .start();
    }

    public void clearFilter() {
        mFilterText = new StringBuilder();
        onTextChanged();
    }

    private void onTextChanged() {
        mFilterView.setText(mFilterText);
    }

    private int getNumberById(int id) {
        if (id == R.id.button0) {
            return 0;
        } else if (id == R.id.button1) {
            return 1;
        } else if (id == R.id.button2) {
            return 2;
        } else if (id == R.id.button3) {
            return 3;
        } else if (id == R.id.button4) {
            return 4;
        } else if (id == R.id.button5) {
            return 5;
        } else if (id == R.id.button6) {
            return 6;
        } else if (id == R.id.button7) {
            return 7;
        } else if (id == R.id.button8) {
            return 8;
        } else if (id == R.id.button9) {
            return 9;
        } else {
            throw new RuntimeException("wrong number");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent : " + KeyEvent.keyCodeToString(event.getKeyCode()));
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            hideView();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void hideView() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
        }
    }

    @Override
    public void showItems(List<SearchItem> searchItems) {
        mAdapter = new AppsAdapter(getContext(), searchItems);
        mAppsGridView.setAdapter(mAdapter);
    }
}
