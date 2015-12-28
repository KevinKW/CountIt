package com.hxchd.countit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_NAME = "CountIt";
    private CountData cd;
    private CountItViewPager vp;
    private FragmentPagerAdapter fpa;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    private ArrayList<Zt> zts = new ArrayList<>();
    private ArrayList<Zt> game_zts = new ArrayList<>();
    private ImageButton btn_history;
    private ImageButton btn_round;
    private ImageButton btn_zts;
    private int round_id = 0;
    private int touch_x = -1;
    private int touch_y = -1;
    private GridView gv_round;
    private Zt src_zt;
    private Zt dst_zt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCD();
        initView();
        bindButtonBar();
        // Enable Zts
        vp.setCurrentItem(1);
    }

    public void initCD() {
        cd = new CountData(this);
        cd.loadZts(zts);
        cd.loadGame(zts, game_zts);
        round_id = cd.loadRound();
    }

    public void resetBottomBtn() {
        ((ImageButton) findViewById(R.id.btn_tab_bottom_round))
                .setImageResource(R.drawable.ic_round_normal);
        ((ImageButton) findViewById(R.id.btn_tab_bottom_history))
                .setImageResource(R.drawable.ic_history_normal);
        ((ImageButton) findViewById(R.id.btn_tab_bottom_zts))
                .setImageResource(R.drawable.ic_zts_normal);
    }

    public void enableRound() {
        vp.setCurrentItem(0);
    }

    public void enableCountIt() {
        vp.setCurrentItem(1);
    }

    public void enableHistory() {
        vp.setCurrentItem(2);
    }

    public void bindButtonBar() {
        btn_round = (ImageButton) findViewById(R.id.btn_tab_bottom_round);
        btn_round.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp.setCurrentItem(0);
            }
        });

        btn_zts = (ImageButton) findViewById(R.id.btn_tab_bottom_zts);
        btn_zts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp.setCurrentItem(1);
            }
        });

        btn_history = (ImageButton) findViewById(R.id.btn_tab_bottom_history);
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp.setCurrentItem(2);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start) {
            startGame();
            return true;
        } else if (id == R.id.action_add_zt) {
            addZt();
            return true;
        } else if (id == R.id.action_del_zt) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("许来不许走！")
                    .show();
            return true;
        } else if (id == R.id.action_clean_db) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("调试专用 清除数据库内容")
                    .show();
            cd.cleanRound();
            cd.cleanGame();
            cd.cleanZt();
            return true;
        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_info)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        vp = (CountItViewPager) findViewById(R.id.viewpager);

        Round r = new Round();
        fragmentList.add(r);
        Countit c = new Countit();
        fragmentList.add(c);
        History h = new History();
        fragmentList.add(h);

        fpa = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int i) {
                return fragmentList.get(i);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };

        vp.setAdapter(fpa);

        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int currentIndex;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        refreshRoundView();
                        break;
                    case 1:
                        refreshZtsView();
                        break;
                    case 2:
                        refreshHistoryView();
                        break;
                    default:
                        break;
                }

                currentIndex = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void chooseGameZts() {
        AlertDialog.Builder dialog;
        final String[] optional_zts = new String[zts.size()];
        final boolean[] is_chosen = new boolean[zts.size()];
        for (int i = 0; i < zts.size(); i++) {
            optional_zts[i] = zts.get(i).name;
            is_chosen[i] = false;
        }
        dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setIcon(R.drawable.ic_round_pressed);
        dialog.setTitle(R.string.start_game);
        dialog.setMultiChoiceItems(optional_zts, is_chosen, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                for (int i = 0; i < is_chosen.length; i++) {
                    if (is_chosen[i]) {
                        Log.d(LOG_NAME, "Chosen ZT: " + optional_zts[i]);
                        for (Zt zt : zts) {
                            if (zt.name.equals(optional_zts[i])) {
                                game_zts.add(zt);
                                break;
                            }
                        }
                    }
                }
                if (game_zts.size() > 1) {
                    startGameInternal();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.need_select_zt)
                            .show();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.create().show();
    }

    public void startGameInternal() {
        round_id = 0;
        cd.startGame(game_zts);
        refreshZtsView();
        refreshHistoryView();
        refreshRoundView();
    }

    public void refreshZtsView() {
        resetBottomBtn();
        btn_zts.setImageResource(R.drawable.ic_zts_pressed);

        TextView tv = (TextView) findViewById(R.id.zts_nogame);
        Log.d(LOG_NAME, String.valueOf(tv == null));
        if (game_zts.size() > 0) {
            if (tv != null) {
                tv.setVisibility(View.GONE);
            }
            List<Map<String, Object>> zt_items = new ArrayList<Map<String, Object>>();
            for (Zt zt : game_zts) {
                Map<String, Object> zt_item = new HashMap<String, Object>();
                zt_item.put("image", R.mipmap.ic_launcher);
                zt_item.put("name", zt.name);
                zt_item.put("result", zt.result);
                zt_item.put("win", zt.win);
                zt_item.put("loss", zt.loss);
                zt_item.put("draw", zt.draw);
                zt_items.add(zt_item);
            }
            SimpleAdapter sad = new SimpleAdapter(this, zt_items, R.layout.zt_status,
                    new String[]{"image", "name", "win", "loss", "draw", "result"},
                    new int[]{R.id.image, R.id.name, R.id.win, R.id.loss, R.id.draw, R.id.result});
            ListView lv = (ListView) findViewById(R.id.zt_list);
            lv.setAdapter(sad);
        } else {
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    public Zt getZtAtPosistion(int position) {
        HashMap<String, Object> item = (HashMap<String, Object>) gv_round.getItemAtPosition(position);
        return (Zt) item.get("zt");
    }

    public void refreshRoundView() {

        resetBottomBtn();
        btn_round.setImageResource(R.drawable.ic_round_pressed);

        final TextView tv = (TextView) findViewById(R.id.round_nogame);
        Log.d(LOG_NAME, String.valueOf(tv == null));
        if (game_zts.size() > 0) {
            if (tv != null) {
                tv.setVisibility(View.GONE);
            }
            TextView tv_checksum = (TextView) findViewById(R.id.tv_checksum);
            int checksum = 0;
            List<Map<String, Object>> zt_items = new ArrayList<Map<String, Object>>();
            for (Zt zt : game_zts) {
                Map<String, Object> zt_item = new HashMap<String, Object>();
                zt_item.put("zt", zt);
                zt_item.put("image", R.mipmap.ic_launcher);
                zt_item.put("name", zt.name);
                zt_item.put("delta", zt.delta);
                zt_items.add(zt_item);
                checksum = checksum + zt.delta;
            }
            tv_checksum.setText(String.valueOf(checksum));
            SimpleAdapter sad = new SimpleAdapter(this, zt_items, R.layout.zt_round,
                    new String[]{"image", "name", "delta"},
                    new int[]{R.id.image, R.id.name, R.id.delta});
            gv_round = (GridView) findViewById(R.id.gv_round);
            gv_round.setAdapter(sad);
            gv_round.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(LOG_NAME, "Item click" + view);
//                    HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
//                    Zt zt = (Zt) item.get("zt");
//                    setDelta(getZtAtPosistion(position));
                }
            });
            gv_round.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int tx;
                    int ty;
                    int position;
                    Log.d(LOG_NAME, "I'm touch" + event);
                    Log.d(LOG_NAME, "I'm touch" + v);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            tx = (int) event.getX();
                            ty = (int) event.getY();
                            position = gv_round.pointToPosition(tx, ty);
                            Log.d(LOG_NAME, "Get position: " + position);
                            if (position == GridView.INVALID_POSITION) {
                                vp.setTouchIntercept(true);
                                return false;
                            }
                            src_zt = getZtAtPosistion(position);
                            Log.d(LOG_NAME, "Get src_zt " + src_zt);
                            vp.setTouchIntercept(false);
                            break;
                        case MotionEvent.ACTION_UP:
                            tx = (int) event.getX();
                            ty = (int) event.getY();
                            position = gv_round.pointToPosition(tx, ty);
                            Log.d(LOG_NAME, "Get position: " + position);
                            if (position == GridView.INVALID_POSITION) {
                                src_zt = null;
                                vp.setTouchIntercept(true);
                                return false;
                            }
                            dst_zt = getZtAtPosistion(position);
                            if (src_zt.id != dst_zt.id) {
                                setDelta(src_zt, dst_zt);
                            } else {
                                Log.d(LOG_NAME, "Same zt: " + src_zt);
                                src_zt = null;
                            }
                            vp.setTouchIntercept(true);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            Log.d(LOG_NAME, "Move is cancel");
                            src_zt = null;
                            vp.setTouchIntercept(true);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                    }
                    return true;
                }
            });

            Button btn_ok = (Button) findViewById (R.id.btn_round_done);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int checksum = 0;
                    boolean doRound = false;
                    Log.d(LOG_NAME, "Click OK");
                    for (Zt zt : game_zts) {
                        checksum = checksum + zt.delta;
                        if (zt.delta != 0) {
                            doRound = true;
                        }
                    }
                    if ((checksum != 0) || (!doRound)) {
                        Log.d(LOG_NAME, "Not need to do");
                        return;
                    }
                    cd.begin();
                    // 这里我没有用try，如果出错的话，程序就直接崩溃，
                    // 由于没有commit transaction所以没关系
                    round_id = round_id + 1;
                    for (Zt zt : game_zts) {
                        cd.addRound(round_id, zt.id, zt.delta);
                        if (zt.delta == 0) {
                            zt.draw += 1;
                        } else if (zt.delta > 0) {
                            zt.win += 1;
                        } else {
                            zt.loss += 1;
                        }
                        zt.result += zt.delta;
                        zt.delta = 0;
                        cd.updateGame(zt);
                    }
                    cd.setTransOK();
                    cd.commit();

                    enableCountIt();
                }
            });

            Button btn_reset = (Button) findViewById(R.id.btn_round_reset);
            btn_reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Zt zt:game_zts) {
                        zt.delta = 0;
                    }
                    refreshRoundView();
                }
            });
        } else {
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    public void refreshHistoryView() {
        resetBottomBtn();
        btn_history.setImageResource(R.drawable.ic_history_pressed);
        TextView tv = (TextView) findViewById(R.id.his_nogame);
        Log.d(LOG_NAME, String.valueOf(tv == null));
        if (round_id > 0) {
            if (tv != null) {
                tv.setVisibility(View.GONE);
            }
        } else {
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startGame() {
        AlertDialog.Builder dialog;
        if (zts.size() == 0) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.no_zts)
                    .show();
            return;
        }
        if (game_zts.size() > 0) {
            dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setIcon(R.drawable.ic_round_pressed);
            dialog.setTitle(R.string.start_game);
            dialog.setMessage(R.string.check_start_game);

            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    game_zts.clear();
                    chooseGameZts();
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.create().show();
        } else {
            chooseGameZts();
        }
    }

    private void addZt() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setIcon(R.drawable.ic_round_pressed);
//        dialog.setTitle(R.string.add_zt);
        final View addZtView = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_zt_dialog, null);
        dialog.setView(addZtView);

        final EditText et = (EditText) addZtView.findViewById(R.id.et_zt_name);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                String text = et.getText().toString().trim();
                if (hasFocus) {
                    if (text.equals(getResources().getString(R.string.default_zt_name))) {
                        et.setText("");
                    }
                    et.setTextColor(Color.BLACK);
                } else {
                    if ((text.length() == 0)
                            || (text.equals(getResources().getString(R.string.default_zt_name)))) {
                        et.setTextColor(Color.GRAY);
                        et.setText(getResources().getString(R.string.default_zt_name));
                    }
                }
            }
        });
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = et.getText().toString().trim();
                Log.d(LOG_NAME, "zt name: " + name);
                if ((name.length() > 0)
                        && (!name.equals(getResources().getString(R.string.default_zt_name)))) {
                    zts.add(cd.addZt(name));
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.create().show();
    }

    public void setDelta(final Zt src_zt, final Zt dst_zt) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setIcon(R.drawable.ic_round_pressed);
        dialog.setTitle(R.string.input_delta);
        final View addRoundView = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_round_dialog, null);
        EditText et_delta = (EditText) addRoundView.findViewById(R.id.et_delta);
//        et_delta.getShowSoftInputOnFocus();
        dialog.setView(addRoundView);
//        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                EditText et = (EditText) v;
//                String text = et.getText().toString().trim();
//                if (hasFocus) {
//                    if (text.equals(getResources().getString(R.string.default_zt_name))) {
//                        et.setText("");
//                    }
//                    et.setTextColor(Color.BLACK);
//                } else {
//                    if ((text.length() == 0)
//                            || (text.equals(getResources().getString(R.string.default_zt_name)))) {
//                        et.setTextColor(Color.GRAY);
//                        et.setText(getResources().getString(R.string.default_zt_name));
//                    }
//                }
//            }
//        });
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et = (EditText) addRoundView.findViewById(R.id.et_delta);
                String text = et.getText().toString().trim();
                Log.d(LOG_NAME, "zt delta: " + text);
                dst_zt.delta += Integer.parseInt(text);
                src_zt.delta -= Integer.parseInt(text);
                refreshRoundView();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.create().show();
        // 计划增加打开就弹出输入法的，结果下面的代码似乎不生效啊
//        Log.d(LOG_NAME, String.valueOf(et_delta.isFocusable())
//                + String.valueOf(et_delta.isFocusableInTouchMode())
//                + String.valueOf(et_delta.isFocused()));
//        InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(addRoundView, InputMethodManager.SHOW_FORCED);
//        et_delta.requestFocus();
//        Log.d(LOG_NAME, String.valueOf(et_delta.isFocusable())
//                + String.valueOf(et_delta.isFocusableInTouchMode())
//                + String.valueOf(et_delta.isFocused()));
    }
}
