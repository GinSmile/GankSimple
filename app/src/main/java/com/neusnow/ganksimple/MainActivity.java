package com.neusnow.ganksimple;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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

import com.neusnow.ganksimple.bean.Girl;
import com.neusnow.ganksimple.bean.GirlPageBean;
import com.neusnow.ganksimple.bean.SpacesItemDecoration;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    @Bind(R.id.swipe_refresh_widget)  SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.my_list)  RecyclerView mRecyclerView;
    @Bind(R.id.fab) FloatingActionButton fab;
    boolean isLoading;
    private int index = 1;
    private List<Girl> data = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private Handler handler = new Handler();
    private int[] lastPositions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //PicassoFaceDetector.initialize(this);

        ButterKnife.bind(this);

        //初次进入，显示加载动画，加载初始的数据
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        initData();


        //监听下拉动作
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //下拉刷新
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        data.clear();
                        index = 1;
                        getData(index);
                    }
                }, 2000);
            }
        });

        //添加适配器，点击事件
        adapter = new RecyclerViewAdapter(this, data);
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("test", "item position = " + position);

                Girl girl  = data.get(position);

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("date",girl.getPublishedAt());
                intent.putExtra("url",girl.getUrl());
                startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        //设置mRecyclerView
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
                Log.d("test", "onScrolled," + dx + " ,"+ dy);

                if(dy > 10) fab.hide();
                if(dy < -10) fab.show();


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
                                getData(index);
                                Log.d("test", "load more completed");
                                isLoading = false;
                            }
                        }, 1000);
                    }
                }
            }
        });



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.smoothScrollToPosition(0);
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                             }
                });
                initData();
            }
        });

    }

    //初始化数据
    public void initData(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    getData(1);
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
    private void getData(int index)  {

        //1.创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        //2.创建访问请求
        ApiService service = retrofit.create(ApiService.class);


        /*

        //-----------------------------不用rxjava---------------------------//

        // Call<GirlPageBean> call = service.getData(index);

         //3.发送请求
        call.enqueue(new Callback<GirlPageBean>() {
            @Override
            public void onResponse(Call<GirlPageBean> call, Response<GirlPageBean> response) {
                //4.处理结果
                if (response.isSuccessful()){
                    GirlPageBean result = response.body();
                    data.addAll(analysisResult(result));
                }

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

            @Override
            public void onFailure(Call<GirlPageBean> call, Throwable t) {

            }
        });

        */


        //-----------------------------用rxjava---------------------------//

        service.getData(index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<GirlPageBean>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(GirlPageBean girlPageBean) {
                                data.addAll(analysisResult(girlPageBean));
                                adapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                                adapter.notifyItemRemoved(adapter.getItemCount());
                                Toast.makeText(MainActivity.this, "下载完成," + adapter.getItemCount(), Toast.LENGTH_SHORT).show();

                            }
                        }
                );






    }


    /**
     * 从GirlPageBean中提取出Girl的集合
     */
    private Collection<Girl> analysisResult(GirlPageBean result){
        Collection<Girl> girls = new ArrayList<Girl>();
        List<GirlPageBean.ResultsBean> results = result.getResults();
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
