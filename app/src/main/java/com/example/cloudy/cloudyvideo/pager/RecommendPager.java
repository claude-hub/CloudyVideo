package com.example.cloudy.cloudyvideo.pager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.cloudy.cloudyvideo.R;
import com.example.cloudy.cloudyvideo.adapter.NetVideoPagerAdapter;
import com.example.cloudy.cloudyvideo.base.BasePager;
import com.example.cloudy.cloudyvideo.domain.MediaItem;
import com.example.cloudy.cloudyvideo.utils.Constants;
import com.example.cloudy.cloudyvideo.utils.LogUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class RecommendPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView mListview;

    @ViewInject(R.id.tv_nonet)
    private TextView mTv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mProgressBar;

    private ArrayList<MediaItem> mediaItems;
    private boolean isLoadMore = false;
    private NetVideoPagerAdapter adapter;

    public RecommendPager(Context context) {
        super(context);
    }

    /**
     * 初始化当前页面的控件，由父类调用
     * @return
     */
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        //第一个参数是RecommendPager.this,第二个是布局
        x.view().inject(this,view);
        return view;
    }


    @Override
    public void initData() {
        super.initData();
        LogUtil.e("推荐的数据被初始化");
        //联网
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }
    private void processData(String json) {

        if(!isLoadMore){
            mediaItems = parseJson(json);
//            showData();
        }else{
            //加载更多
            //要把得到更多的数据，添加到原来的集合中
            ArrayList<MediaItem> moreDatas = parseJson(json);
            isLoadMore = false;
            mediaItems.addAll(parseJson(json));
            //刷新适配器
//            adapter.notifyDataSetChanged();
//            onLoad();
        }
    }
    private void showData() {
        //设置适配器
        if(mediaItems != null && mediaItems.size() >0){
            //有数据
            //设置适配器
            adapter = new NetVideoPagerAdapter(context,mediaItems);
            mListview.setAdapter(adapter);
//            onLoad();
            //把文本隐藏
            mTv_nonet.setVisibility(View.GONE);
        }else{
            //没有数据
            //文本显示
            mTv_nonet.setVisibility(View.VISIBLE);
        }


        //ProgressBar隐藏
        mProgressBar.setVisibility(View.GONE);
    }
    /**
     * 解决json数据：
     * 1.用系统接口解析json数据
     * 2.使用第三方解决工具（Gson,fastjson）
     * @param json
     * @return
     */
    private ArrayList<MediaItem> parseJson(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if(jsonArray!= null && jsonArray.length() >0){

                for (int i=0;i<jsonArray.length();i++){

                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);

                    if(jsonObjectItem != null){

                        MediaItem mediaItem = new MediaItem();

                        String movieName = jsonObjectItem.optString("movieName");//name
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");//desc
                        mediaItem.setDesc(videoTitle);

                        String imageUrl = jsonObjectItem.optString("coverImg");//imageUrl
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");//data
                        mediaItem.setData(hightUrl);

                        //把数据添加到集合
                        mediaItems.add(mediaItem);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaItems;
    }
}
