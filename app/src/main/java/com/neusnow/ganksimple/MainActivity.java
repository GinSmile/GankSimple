package com.neusnow.ganksimple;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.neusnow.ganksimple.bean.Girl;
import com.neusnow.ganksimple.bean.GirlPageBean;
import com.neusnow.ganksimple.bean.SpacesItemDecoration;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    boolean isLoading;
    private int index = 1;
    private List<Girl> data = new ArrayList<Girl>();
    private RecyclerViewAdapter adapter;
    private Handler handler = new Handler();
    public OkHttpClient mOkHttpClient;

    private int[] lastPositions;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //创建okHttpClient对象
        mOkHttpClient = new OkHttpClient();

        PicassoFaceDetector.initialize(this);

        initData();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_list);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //下拉刷新
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        data.clear();
                        getData();
                        index = 1;
                    }
                }, 2000);
            }
        });

        adapter = new RecyclerViewAdapter(this, data);
        //添加点击事件
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("test", "item position = " + position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        //final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        final StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setAdapter(adapter);

        SpacesItemDecoration decoration=new SpacesItemDecoration(16);
        mRecyclerView.addItemDecoration(decoration);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("test", "StateChanged = " + newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("test", "onScrolled");


                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                int lastVisibleItemPosition = findMax(lastPositions);

                if (lastVisibleItemPosition + 1 == adapter.getItemCount()) {
                    Log.d("test", "loading executed");

                    boolean isRefreshing = mSwipeRefreshLayout.isRefreshing();
                    if (isRefreshing) {
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        return;
                    }
                    if (!isLoading) {
                        isLoading = true;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                index++;
                                getMoreData(index);
                                Log.d("test", "load more completed");
                                isLoading = false;
                            }
                        }, 1000);
                    }
                }
            }
        });

    }

    public void initData(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    getData();
            }
        }, 100);

    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * 获取测试数据
     */
    private void getData()  {
        Request request = new Request.Builder()
                .url("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/6/1")
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String res = response.body().string();
                Log.v("te::","download complete...1");


                Collection<Girl> girls = analysisJson(res);
                data.addAll(girls);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        Toast.makeText(MainActivity.this, "下载完成," + adapter.getItemCount(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



        Log.v("te::","download complete...2");

    }

    private void getMoreData(int index)  {
        Request request = new Request.Builder()
                .url("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/" + index)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String res = response.body().string();
                Log.v("te::","download complete...1");


                Collection<Girl> girls = analysisJson(res);
                data.addAll(girls);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        Toast.makeText(MainActivity.this, "下载完成," + adapter.getItemCount(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



        Log.v("te::","download complete...2");

    }

    /**
     * 利用GSON解析JSON
     */
    private Collection<Girl> analysisJson(String jsonData){
        Collection<Girl> girls = new ArrayList<Girl>();

        Gson gson = new Gson();
        GirlPageBean girlPageBean = gson.fromJson(jsonData, GirlPageBean.class);
        List<GirlPageBean.ResultsBean> results = girlPageBean.getResults();
        for(GirlPageBean.ResultsBean res : results){
            Girl girl = new Girl();
            girl.setDesc(res.getDesc());
            girl.setUrl(res.getUrl());
            girl.setPublishedAt(res.getPublishedAt());
            girl.setWho(res.getWho());

            girls.add(girl);
        }
        return girls;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        PicassoFaceDetector.releaseDetector();
    }
}
