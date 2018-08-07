package com.myscrap;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myscrap.application.AppController;
import com.myscrap.model.Exchange;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.lang.reflect.Type;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class LMEFragment extends Fragment {

    private Tracker mTracker;
    private View v;
    private SwipeRefreshLayout swipe;
    private TableLayout tableLayoutLME;
    private TableLayout tableLayoutCOMEX;
    private TableLayout tableLayoutSHANGHAI;

    private TextView mLMETime, mCOMEXTime, mSHANGHAITime;
    private View mLMEView, mCOMEXView, mSHANGHAIView;

    private String[] LME_COLUMN = { "LME", "Contract", "Last", "Change"};
    private String[] COMEX_COLUMN = { "COMEX", "Month", "Last", "Change"};
    private String[] SHANGHAI_COLUMN = { "SHANGHAI", "Month", "Last", "Change"};
    private Subscription lmeSubscription;

    public LMEFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_lme, null);
        mTracker = AppController.getInstance().getDefaultTracker();

        swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        tableLayoutLME = (TableLayout) v.findViewById(R.id.tableLayoutLME);
        tableLayoutLME.setStretchAllColumns(true);
        tableLayoutCOMEX = (TableLayout) v.findViewById(R.id.tableLayoutCOMEX);
        tableLayoutCOMEX.setStretchAllColumns(true);
        tableLayoutSHANGHAI = (TableLayout) v.findViewById(R.id.tableLayoutSHANGHAI);
        tableLayoutSHANGHAI.setStretchAllColumns(true);

        mLMETime = (TextView) v.findViewById(R.id.lme_time) ;
        mCOMEXTime = (TextView) v.findViewById(R.id.comex_time) ;
        mSHANGHAITime = (TextView) v.findViewById(R.id.shang_time) ;

        mLMEView = (View) v.findViewById(R.id.lme_time_view) ;
        mCOMEXView = (View) v.findViewById(R.id.comex_time_view) ;
        mSHANGHAIView = (View) v.findViewById(R.id.shang_time_view) ;

        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setOnRefreshListener(this::loadExchange);
        loadOfflineExchangeData();
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadExchange();
    }

    private void loadOfflineExchangeData() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (AppController.getInstance().getPrefManager().getUser() != null) {
            AppController.runOnUIThread(() -> {
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String OfflineExchangeData = UserUtils.getExchange(AppController.getInstance(), userId);
                if (OfflineExchangeData != null && !OfflineExchangeData.equalsIgnoreCase("")) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<Exchange>(){}.getType();
                    Exchange mExchange = gson.fromJson(OfflineExchangeData, type);
                    if (mExchange != null)
                        parseExchangeData(mExchange);
                    Log.d("GET Exchange", "DONE");
                }
            });
        }
    }

    private void createLMETableLayout(TableLayout mTableLayout, List<Exchange.LMEData> lmeData, String londonTime) {

        if (mTableLayout != null && getContext() != null)
            mTableLayout.post(() -> {
                if (lmeData != null) {
                    mTableLayout.removeAllViews();
                    TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
                    TableRow.LayoutParams tableRowParams;
                    mTableLayout.setBackgroundColor(Color.WHITE);

                    if (getContext() == null)
                        return;

                    int rowCountLME = lmeData.size() + 1;
                    int columnCountLME = LME_COLUMN.length;

                    for (int i = 0; i < rowCountLME; i++) {
                        TableRow tableRow = new TableRow(getContext());
                        if (i == 0) {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 10;
                            tableRowParams.rightMargin = 10;
                            tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_darker_gray));
                        } else {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 0;
                            tableRowParams.rightMargin = 0;
                            if ( i % 2 == 0){
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_two));
                            } else {
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_one));
                            }
                        }

                        for (int j = 0; j < columnCountLME; j++) {
                            TextView textView = new TextView(getContext());
                            textView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                            textView.setGravity(Gravity.CENTER);
                            if(i==0){
                                if (j == 0){
                                    textView.setGravity(Gravity.START);
                                    textView.setPadding(0,5,0,5);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }

                                if (j == 1) {
                                    textView.setGravity(Gravity.START);
                                    textView.setPadding(0,5,0,5);
                                }

                                if (j == 2){
                                    textView.setGravity(Gravity.END);
                                    textView.setPadding(0,5,0,5);
                                }

                                if (j == 3){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }
                                textView.setTypeface(Typeface.DEFAULT_BOLD);
                                textView.setText(LME_COLUMN[j]);
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_title_color));
                            }  else {
                                tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                tableRowParams.weight = 4;
                                tableRowParams.leftMargin = 0;
                                tableRowParams.rightMargin = 0;
                                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_text));
                                if (j == 0){
                                    textView.setGravity(Gravity.START);
                                    textView.setPadding(10,3,0,3);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if(lmeData.get(i-1).getTitle() != null)
                                        textView.setText(lmeData.get(i-1).getTitle());

                                    if (lmeData.get(i-1).getSymbol() != null && !lmeData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (lmeData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_up_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        } else {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_down_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        }
                                    }
                                }

                                if (j == 1) {
                                    textView.setPadding(10,3,0,3);
                                    textView.setGravity(Gravity.START);
                                    if(lmeData.get(i-1).getContract() != null)
                                        textView.setText(lmeData.get(i-1).getContract());
                                }

                                if (j == 2){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    if(lmeData.get(i-1).getLast() != null)
                                        textView.setText(lmeData.get(i-1).getLast());
                                }

                                if (j == 3){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if (lmeData.get(i-1).getSymbol() != null && !lmeData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (lmeData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            changesUp(textView,lmeData.get(i-1).getChange());
                                        } else {
                                            changesDown(textView,lmeData.get(i-1).getChange());
                                        }
                                    } else {
                                        changesEmpty(textView);
                                    }
                                }
                            }
                            tableRow.addView(textView, tableRowParams);
                        }
                        mTableLayout.addView(tableRow, tableLayoutParams);
                    }
                    mTableLayout.setVisibility(View.VISIBLE);

                    if (londonTime != null && !londonTime.equalsIgnoreCase("")){
                        mLMETime.setText("Last Updated London "+londonTime);
                        mLMETime.setVisibility(View.VISIBLE);
                        mLMEView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mLMETime.setVisibility(View.GONE);
                    mLMEView.setVisibility(View.GONE);
                    mTableLayout.setVisibility(View.GONE);
                }
            });
    }

    private void createCOMEXTableLayout(TableLayout mTableLayout, List<Exchange.ComexData> comexData, String newYorkTime) {
        if (mTableLayout != null && getContext() != null)
            mTableLayout.post(() -> {
                if (comexData != null) {
                    mTableLayout.removeAllViews();
                    TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
                    TableRow.LayoutParams tableRowParams;
                    mTableLayout.setBackgroundColor(Color.WHITE);

                    if (getContext() == null)
                        return;

                    int rowCountCOMEX = comexData.size() + 1;
                    int columnCountCOMEX = COMEX_COLUMN.length;

                    for (int i = 0; i < rowCountCOMEX; i++) {
                        TableRow tableRow = new TableRow(getContext());
                        if (i == 0) {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 10;
                            tableRowParams.rightMargin = 10;
                            tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_darker_gray));
                        } else {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 0;
                            tableRowParams.rightMargin = 0;
                            if ( i % 2 == 0){
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_two));
                            } else {
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_one));
                            }
                        }

                        for (int j = 0; j < columnCountCOMEX; j++) {
                            TextView textView = new TextView(getContext());
                            textView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                            textView.setGravity(Gravity.CENTER);
                            if(i==0){
                                if (j == 0){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.START);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }

                                if (j == 1) {
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.START);
                                }

                                if (j == 2){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.END);
                                }

                                if (j == 3){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }
                                textView.setTypeface(Typeface.DEFAULT_BOLD);
                                textView.setText(COMEX_COLUMN[j]);
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_title_color));
                            }  else {
                                tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                tableRowParams.weight = 4;
                                tableRowParams.leftMargin = 0;
                                tableRowParams.rightMargin = 0;
                                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_text));
                                if (j == 0){
                                    textView.setPadding(10,3,0,3);
                                    textView.setGravity(Gravity.START);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if(comexData.get(i-1).getTitle() != null)
                                        textView.setText(comexData.get(i-1).getTitle());

                                    if (comexData.get(i-1).getSymbol() != null && !comexData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (comexData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_up_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        } else {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_down_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        }
                                    }
                                }

                                if (j == 1) {
                                    textView.setPadding(10,3,0,3);
                                    textView.setGravity(Gravity.START);
                                    if(comexData.get(i-1).getMonth() != null)
                                        textView.setText(comexData.get(i-1).getMonth());
                                }

                                if (j == 2){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    if(comexData.get(i-1).getLast() != null)
                                        textView.setText(comexData.get(i-1).getLast());
                                }

                                if (j == 3){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if (comexData.get(i-1).getSymbol() != null && !comexData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (comexData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            changesUp(textView,comexData.get(i-1).getChange());
                                        } else {
                                            changesDown(textView,comexData.get(i-1).getChange());
                                        }
                                    } else {
                                        changesEmpty(textView);
                                    }
                                }
                            }

                            tableRow.addView(textView, tableRowParams);
                        }
                        mTableLayout.addView(tableRow, tableLayoutParams);
                    }
                    mTableLayout.setVisibility(View.VISIBLE);

                    if (newYorkTime != null && !newYorkTime.equalsIgnoreCase("")){
                        mCOMEXTime.setText("Last Updated NewYork "+newYorkTime);
                        mCOMEXTime.setVisibility(View.VISIBLE);
                        mCOMEXView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mCOMEXTime.setVisibility(View.GONE);
                    mCOMEXView.setVisibility(View.GONE);
                    mTableLayout.setVisibility(View.GONE);
                }
            });

    }

    private void createSHANGHAITableLayout(TableLayout mTableLayout, List<Exchange.ShanghaiData> shanghaiData, String shanghaiTime) {
        if (mTableLayout != null && getContext() != null)

            mTableLayout.post(() -> {
                if (shanghaiData != null) {
                    mTableLayout.removeAllViews();
                    TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
                    TableRow.LayoutParams tableRowParams;
                    mTableLayout.setBackgroundColor(Color.WHITE);

                    int rowCountSHANGHAI = shanghaiData.size() + 1;
                    int columnCountSHANGHAI = SHANGHAI_COLUMN.length;

                    if (getContext() == null)
                        return;

                    for (int i = 0; i < rowCountSHANGHAI; i++) {
                        TableRow tableRow = new TableRow(getContext());
                        if (i == 0) {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 10;
                            tableRowParams.rightMargin = 10;
                            tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_darker_gray));
                        } else {
                            tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            tableRowParams.weight = 4;
                            tableRowParams.leftMargin = 0;
                            tableRowParams.rightMargin = 0;
                            if ( i % 2 == 0){
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_two));
                            } else {
                                tableRow.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_one));
                            }
                        }

                        for (int j = 0; j < columnCountSHANGHAI; j++) {
                            TextView textView = new TextView(getContext());
                            textView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                            textView.setGravity(Gravity.CENTER);
                            if(i==0){
                                if (j == 0){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.START);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }

                                if (j == 1) {
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.START);
                                }

                                if (j == 2){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.END);
                                }

                                if (j == 3){
                                    textView.setPadding(0,5,0,5);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                }
                                textView.setTypeface(Typeface.DEFAULT_BOLD);
                                textView.setText(SHANGHAI_COLUMN[j]);
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_title_color));
                            }  else {
                                tableRowParams = new TableRow.LayoutParams( 0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                tableRowParams.weight = 4;
                                tableRowParams.leftMargin = 0;
                                tableRowParams.rightMargin = 0;
                                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.row_text));
                                if (j == 0){
                                    textView.setPadding(10,3,0,3);
                                    textView.setGravity(Gravity.START);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if(shanghaiData.get(i-1).getTitle() != null)
                                        textView.setText(shanghaiData.get(i-1).getTitle());

                                    if (shanghaiData.get(i-1).getSymbol() != null && !shanghaiData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (shanghaiData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_up_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        } else {
                                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.row_down_light));
                                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                        }
                                    }
                                }

                                if (j == 1) {
                                    textView.setPadding(10,3,0,3);
                                    textView.setGravity(Gravity.START);
                                    if(shanghaiData.get(i-1).getMonth() != null)
                                        textView.setText(shanghaiData.get(i-1).getMonth());
                                }

                                if (j == 2){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    if(shanghaiData.get(i-1).getLast() != null)
                                        textView.setText(shanghaiData.get(i-1).getLast());
                                }

                                if (j == 3){
                                    textView.setPadding(0,3,10,3);
                                    textView.setGravity(Gravity.END);
                                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(llp);
                                    if (shanghaiData.get(i-1).getSymbol() != null && !shanghaiData.get(i-1).getSymbol().equalsIgnoreCase("")) {
                                        if (shanghaiData.get(i-1).getSymbol().equalsIgnoreCase("+")) {
                                            changesUp(textView,shanghaiData.get(i-1).getChange());
                                        } else {
                                            changesDown(textView,shanghaiData.get(i-1).getChange());
                                        }
                                    } else {
                                        changesEmpty(textView);
                                    }
                                }
                            }

                            tableRow.addView(textView, tableRowParams);
                        }
                        mTableLayout.addView(tableRow, tableLayoutParams);
                    }
                    mTableLayout.setVisibility(View.VISIBLE);

                    if (shanghaiTime != null && !shanghaiTime.equalsIgnoreCase("")){
                        mSHANGHAITime.setText("Last Updated Shanghai "+shanghaiTime);
                        mSHANGHAITime.setVisibility(View.VISIBLE);
                        mSHANGHAIView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mSHANGHAITime.setVisibility(View.GONE);
                    mSHANGHAIView.setVisibility(View.GONE);
                    mTableLayout.setVisibility(View.GONE);
                }
            });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.exchange_refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                loadExchange();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("LME Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private void changesEmpty(TextView changesEmptyTextView){
        if(changesEmptyTextView != null){
            changesEmptyTextView.post(() -> {
                changesEmptyTextView.setTextColor(ContextCompat.getColor(AppController.getInstance(), R.color.row_text));
                changesEmptyTextView.setText("-");
            });

        }
    }

    private void lastEmpty(TextView lastEmptyTextView){
        if(lastEmptyTextView != null){
            lastEmptyTextView.post(() -> {
                lastEmptyTextView.setTextColor(ContextCompat.getColor(AppController.getInstance(), R.color.row_text));
                lastEmptyTextView.setText("-");
            });

        }
    }

    private void changesUp(TextView upTextView, String up){
        if(upTextView != null){
            upTextView.post(() -> {
                if( up != null){
                    upTextView.setText(up);
                    upTextView.setTextColor(ContextCompat.getColor(AppController.getInstance(), R.color.row_up));
                    upTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.feed_arrow_up, 0);
                } else {
                    changesEmpty(upTextView);
                }
            });

        }
    }

    private void changesDown(TextView downTextView, String down){
        if(downTextView != null){
            downTextView.post(() -> {
                if( down != null){
                    downTextView.setText("-"+down);
                    downTextView.setTextColor(ContextCompat.getColor(AppController.getInstance(), R.color.row_down));
                    downTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.feed_arrow_down, 0);
                } else {
                    changesEmpty(downTextView);
                }
            });

        }
    }

    private void loadExchange() {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            show();
            getExchangeData();
        } else {
            hide();
            if(HomeActivity.toolbar != null)
                SnackBarDialog.showNoInternetError(HomeActivity.toolbar);
        }
    }

    private void getExchangeData() {

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){

            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId;
            if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                userId = "3";
            } else {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                userId = AppController.getInstance().getPrefManager().getUser().getId();
            }

            String apiKey = UserUtils.getApiKey(getActivity());
            lmeSubscription = apiService.exchange(userId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .retry(3)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Exchange>() {
                        @Override
                        public void onCompleted() {
                            hide();
                        }

                        @Override
                        public void onError(Throwable e) {
                            hide();
                            Log.d("loadExchange", "onFailure");
                        }

                        @Override
                        public void onNext(Exchange exchange) {
                            if(exchange != null) {
                                parseExchangeData(exchange);
                                saveOfflineExchange(exchange);
                            }
                            Log.d("loadExchange", "onSuccess");
                        }
                    });
        } else {
            hide();
            if(v != null)
                SnackBarDialog.showNoInternetError(v);
        }
    }

    private void hide() {
        if(swipe != null && swipe.isRefreshing()) {
            new Thread(() -> swipe.post(() -> swipe.setRefreshing(false))).start();
        }
    }

    private void show() {
        if(swipe != null && !swipe.isRefreshing()){
            new Thread(() -> swipe.post(() -> swipe.setRefreshing(true))).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lmeSubscription != null && lmeSubscription.isUnsubscribed())
            lmeSubscription.unsubscribe();
    }

    private void parseExchangeData(Exchange exchange) {
        if (exchange != null)  {
            if (exchange.getLmeData() != null && !exchange.getLmeData().isEmpty())
                parseLMEData(exchange.getLmeData(), exchange.getLondonTime());
            if (exchange.getComexData() != null && !exchange.getComexData().isEmpty())
                parseCOMEXData(exchange.getComexData(), exchange.getNewYorkTime());
            if (exchange.getShanghaiData() != null && !exchange.getShanghaiData().isEmpty())
                parseSHANGHAIData(exchange.getShanghaiData(), exchange.getShanghaiTime());
        }
    }

    private void parseCOMEXData(List<Exchange.ComexData> comexData, String newYorkTime) {
        if (comexData != null && !comexData.isEmpty() && tableLayoutCOMEX != null){
            createCOMEXTableLayout(tableLayoutCOMEX, comexData, newYorkTime);
        }
    }

    private void parseLMEData(List<Exchange.LMEData> lmeData, String londonTime) {
        if (lmeData != null && !lmeData.isEmpty() && tableLayoutLME != null){
            createLMETableLayout(tableLayoutLME, lmeData, londonTime);
        }
    }

    private void parseSHANGHAIData(List<Exchange.ShanghaiData> shanghaiData, String shanghaiTime) {
        if (shanghaiData != null && !shanghaiData.isEmpty() && tableLayoutSHANGHAI != null){
            createSHANGHAITableLayout(tableLayoutSHANGHAI, shanghaiData, shanghaiTime);
        }
    }

    private void saveOfflineExchange(Exchange mExchange) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (mExchange != null){
            new Handler().post(() -> {
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                Gson gson = new Gson();
                String offlineExchangeData = gson.toJson(mExchange);
                UserUtils.saveExchange(AppController.getInstance(), offlineExchangeData, userId);
            });
            Log.d("saveExchange", "DONE");
        }
    }

}
