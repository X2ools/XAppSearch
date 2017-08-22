
package org.x2ools.xappsearchlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
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

    private StringBuilder mFilterText = new StringBuilder();
    private AppsAdapter mAdapter;

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

        mFilterView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (T9Search.getInstance().isCallPhoneEnable()) {
                    try {
                        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"
                                + mFilterView.getText().toString()));
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mAppsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchItem item = (SearchItem) mAdapter.getItem(i);
                Intent intent = mViewModel.getLaunchIntent(item);
                if (intent != null) {
                    try {
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                hideView();
            }
        });

        mAppsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
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
            }
        });
    }

    OnLongClickListener mOnLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            if (id == R.id.buttonDelete) {
                clearFilter();
            }
            return false;
        }

    };

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
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
                mFilterText.delete(0, mFilterText.length());
                mFilterView.setText(mFilterText);

                if (mViewModel.isAllMode()) {
                    mViewModel.setApplicationsData();
                    switchModeAnimate(false);
                } else {
                    mViewModel.setAllApplicationsData();
                    switchModeAnimate(true);
                }
            } else if (id == R.id.buttonDelete) {
                if (TextUtils.isEmpty(mFilterText))
                    return;
                mFilterText.deleteCharAt(mFilterText.length() - 1);
                onTextChanged();
            }
        }

    };

    private void switchModeAnimate(final boolean allMode) {
        final View buttonNumber = findViewById(R.id.buttonNumber);

        Animation anim;
        if (allMode)
            anim = new TranslateAnimation(0, 0, 0, buttonNumber.getHeight() * 4 / 3);
        else {
            anim = new TranslateAnimation(0, 0, buttonNumber.getHeight() * 4 / 3, 0);
        }
        anim.setDuration(500);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if (!allMode)
                    buttonNumber.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (allMode)
                    buttonNumber.setVisibility(View.GONE);
            }
        });
        buttonNumber.startAnimation(anim);
    }

    public void clearFilter() {
        mFilterText = new StringBuilder();
        onTextChanged();
    }

    private void onTextChanged() {
        mViewModel.filter(mFilterText.toString());
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
