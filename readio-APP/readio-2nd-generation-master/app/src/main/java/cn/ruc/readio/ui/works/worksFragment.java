package cn.ruc.readio.ui.works;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ViewUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.ruc.readio.MainActivity;
import cn.ruc.readio.R;
import cn.ruc.readio.databinding.FragmentWorksBinding;
import cn.ruc.readio.ui.userpage.User;
import cn.ruc.readio.util.HttpUtil;
import cn.ruc.readio.util.Tools;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class worksFragment extends Fragment {

    static public Fragment workFrag;
    public int refreshTimes = -1;
    private RecyclerView recyclerView = null;
    User user = new User("zyy", "123456", "123456");
    ArrayList<Works> works = new ArrayList<Works>();
    private FragmentWorksBinding binding;
    private ReadWriteLock refreshTimesReadWriteLock = new ReentrantReadWriteLock();
    private ReadWriteLock worksListReadWriteLock = new ReentrantReadWriteLock();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWorksBinding.inflate(inflater, container, false);
        workFrag = this;
        ImageView searchButton = binding.searchButton;
        View root = binding.getRoot();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView = binding.worksColumn;
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        WorkAdapter workAdapter = new WorkAdapter(getContext(), works);
//        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(workAdapter);
//        alphaInAnimationAdapter.setDuration(500);
        recyclerView.setAdapter(new ScaleInAnimationAdapter(workAdapter));

        // 设置动画
//        Animation animation = AnimationUtils.loadAnimation(MainActivity.mainAct, R.anim.works_fragment_recyclerview_item_anime);
//        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);
//        layoutAnimationController.setOrder(LayoutAnimationController.ORDER_NORMAL);
//        layoutAnimationController.setDelay(0.2f);
//        recyclerView.setLayoutAnimation(layoutAnimationController);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int lastVisibleItemPosition = 0;
            private Boolean isSlidingToLast = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                manager.invalidateSpanAssignments(); //防止第一行到顶部有空白

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //停止滑动
                    int visibleItemCount = manager.getChildCount();
                    int totItemCount = manager.getItemCount();
                    if (isSlidingToLast && visibleItemCount > 0 && lastVisibleItemPosition >= totItemCount - 1) {
                        Log.d("WorkFragmentRefreshData", "从onScrollStateChanged调用refreshData");
                        refreshData();
                    }
                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int[] lastPositions = null;
                    lastPositions = new int[manager.getSpanCount()];
                    manager.findLastVisibleItemPositions(lastPositions);
                    lastVisibleItemPosition = findMax(lastPositions);
                    isSlidingToLast = true;
                } else {
                    isSlidingToLast = false;
                }
            }
        });
        binding.edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if (binding.edittext.getText().length() == 0) {
//                    worksListReadWriteLock.writeLock().lock();
//                    works.clear();
//                    worksListReadWriteLock.writeLock().unlock();
//                    Log.d("WorkFragmentRefreshData", "从afterTextChanged调用refreshData");
//                    refreshData();
//                }
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                worksListReadWriteLock.writeLock().lock();
                works.clear();
                worksListReadWriteLock.writeLock().unlock();
                refreshSearchData();
            }
        });
        return root;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {

        super.onResume();

        //新开一个线程来刷新数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                //为refreshTimes上写锁
                try {
                    refreshTimesReadWriteLock.writeLock().lock();
                    refreshTimes = -1;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    refreshTimesReadWriteLock.writeLock().unlock();
                }
                try{
                    worksListReadWriteLock.writeLock().lock();
                    works.clear();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    worksListReadWriteLock.writeLock().unlock();
                }

                Log.d("WorkFragmentRefreshData", "从onResume调用refreshData");

                refreshData();
            }
        }).start();

    }

    public void refreshData() {
        //每次加载recyclerView时先不让recyclerView滚动
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setNestedScrollingEnabled(false);
                }
            });
        }

        refreshTimesReadWriteLock.writeLock().lock();
        refreshTimes++;
        refreshTimesReadWriteLock.writeLock().unlock();

        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("mode", "recommend"));

        refreshTimesReadWriteLock.readLock().lock();
        queryParam.add(Pair.create("queryTimes", toString().valueOf(refreshTimes)));
        Log.d("WorkFragmentRefreshData", "refreshTimes = " + refreshTimes);
        refreshTimesReadWriteLock.readLock().unlock();

        HttpUtil.getRequestWithTokenAsyn(getActivity(), "/works/getPiecesBrief", queryParam, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    Tools.my_toast(getActivity(), "请求异常，加载不出来");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray data = jsonObject.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject datai = data.getJSONObject(i);
                        JSONObject useri = datai.getJSONObject("user");
                        Works work = new Works();
                        work.setPieceTitle(datai.getString("title"));
                        work.setContent(datai.getString("content"));
                        work.setLikesNum(datai.getInt("likes"));
                        work.setWorkID(datai.getInt("piecesId"));
                        if (datai.getInt("isLiked") == 1) {
                            work.changeMyLike();
                        }
                        User user = new User(useri.getString("userName"), useri.getString("email"), useri.getString("phoneNumber"));
                        user.setAvaID(useri.getString("avator"));
                        JSONArray picArray = datai.getJSONArray("picArray");
                        if (picArray.length() > 0) {
                            String coverId = picArray.getString(0);
                            work.setCoverId(coverId);
                        }
                        tags tagi = new tags();
                        tags tagj = new tags();
                        if (datai.has("tag")) {
                            JSONArray tagList = datai.getJSONArray("tag");
                            work.setTagNum(tagList.length());
                            if (tagList.length() == 1) {
                                JSONObject tagObj = (JSONObject) tagList.get(0);
                                String tagConent = tagObj.getString("content");
                                int tagID = tagObj.getInt("tagId");
                                int tagLinkedTimes = tagObj.getInt("linkedTimes");
                                tagi.setContent(tagConent);
                                tagi.setLinkedTimes(tagLinkedTimes);
                                tagi.setTagId(tagID);
                                work.setTag(tagi);
                            }
                            if (tagList.length() >= 2) {
                                JSONObject tagObj = (JSONObject) tagList.get(0);
                                String tagConent = tagObj.getString("content");
                                int tagID = tagObj.getInt("tagId");
                                int tagLinkedTimes = tagObj.getInt("linkedTimes");
                                tagi.setContent(tagConent);
                                tagi.setLinkedTimes(tagLinkedTimes);
                                tagi.setTagId(tagID);
                                work.setTag(tagi);

                                tagObj = (JSONObject) tagList.get(1);
                                String tagConent1 = tagObj.getString("content");
                                int tagID1 = tagObj.getInt("tagId");
                                int tagLinkedTimes1 = tagObj.getInt("linkedTimes");
                                tagj.setContent(tagConent1);
                                tagj.setLinkedTimes(tagLinkedTimes1);
                                tagj.setTagId(tagID1);
                                work.setTag2(tagj);
                            }
                        }
                        work.setUser(user);
                        worksListReadWriteLock.writeLock().lock();
//                        works.add(work);
                        insertWorkData(work);
                        worksListReadWriteLock.writeLock().unlock();
                    }

                    worksListReadWriteLock.writeLock().lock();
                    for (int i = 0; i < works.size(); ++i) {
//                                Bitmap pic = HttpUtil.getAvaSyn(works.get(i).getUser().getAvaID());
                        try {
                            if (getActivity() != null) {
                                String avaId = works.get(i).getUser().getAvaID();
                                if (avaId != null) {
                                    Bitmap pic = Tools.getImageBitmapSyn(getActivity(), works.get(i).getUser().getAvaID());
                                    pic = Tools.compressImage(pic);
//                                            works.get(i).getUser().setAvator(pic);
                                    setWorksListAvaBitmap(i,pic);
                                }
                                String coverId = works.get(i).getCoverId();
                                if (coverId != null) {
                                    Bitmap workCover = Tools.getImageBitmapSyn(getActivity(), coverId);
                                    workCover = Tools.compressImage(workCover);
//                                            works.get(i).setCoverBitmap(workCover);
                                    setWorksListCoverBitmap(i,workCover);
                                }
                            }
                        } catch (IOException | JSONException | ParseException e) {
                            if (getActivity() != null) {
                                Tools.my_toast(getActivity(), "图片加载出错啦！");
                            }
                        }
//                                Log.d("workadpter", "需要更新");
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            if (binding != null) {
//                                                binding.worksColumn.getAdapter().notifyDataSetChanged();
//                                            }
//                                            if(recyclerView != null){
//                                                recyclerView.getAdapter().notifyItemRangeChanged();
//                                            }
                                        }
                                    });
                                }
                    }
                    worksListReadWriteLock.writeLock().unlock();

                    if(activity != null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setNestedScrollingEnabled(true);
                            }
                        });
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.works_column);
                                    if (recyclerView != null) {
                                        recyclerView.getAdapter().notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void refreshSearchData() {

        String mode = "search";
        String keyword = binding.edittext.getText().toString();
        ArrayList<Pair<String, String>> queryParam = new ArrayList<>();
        queryParam.add(Pair.create("mode", mode));
        queryParam.add(Pair.create("keyword", keyword));
        HttpUtil.getRequestAsyn("/works/getPiecesBrief", queryParam, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    Tools.my_toast(getActivity(), "api启用失败");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    JSONArray data = jsonObject1.getJSONArray("data");
                    worksListReadWriteLock.writeLock().lock();
                    works.clear();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject datai = data.getJSONObject(i);
                        JSONObject useri = datai.getJSONObject("user");
                        Works work = new Works();
                        work.setPieceTitle(datai.getString("title"));
                        work.setContent(datai.getString("content"));
                        work.setLikesNum(datai.getInt("likes"));
                        work.setWorkID(datai.getInt("piecesId"));
                        if (datai.getInt("isLiked") == 1) {
                            work.changeMyLike();
                        }
                        User user = new User(useri.getString("userName"), useri.getString("email"), useri.getString("phoneNumber"));
                        user.setAvaID(useri.getString("avator"));
                        tags tagi = new tags();
                        tags tagj = new tags();
                        if (datai.has("tag")) {
                            JSONArray tagList = datai.getJSONArray("tag");
                            work.setTagNum(tagList.length());
                            if (tagList.length() == 1) {
                                JSONObject tagObj = (JSONObject) tagList.get(0);
                                String tagConent = tagObj.getString("content");
                                int tagID = tagObj.getInt("tagId");
                                int tagLinkedTimes = tagObj.getInt("linkedTimes");
                                tagi.setContent(tagConent);
                                tagi.setLinkedTimes(tagLinkedTimes);
                                tagi.setTagId(tagID);
                                work.setTag(tagi);
                            }
                            if (tagList.length() >= 2) {
                                JSONObject tagObj = (JSONObject) tagList.get(0);
                                String tagConent = tagObj.getString("content");
                                int tagID = tagObj.getInt("tagId");
                                int tagLinkedTimes = tagObj.getInt("linkedTimes");
                                tagi.setContent(tagConent);
                                tagi.setLinkedTimes(tagLinkedTimes);
                                tagi.setTagId(tagID);
                                work.setTag(tagi);

                                tagObj = (JSONObject) tagList.get(1);
                                String tagConent1 = tagObj.getString("content");
                                int tagID1 = tagObj.getInt("tagId");
                                int tagLinkedTimes1 = tagObj.getInt("linkedTimes");
                                tagj.setContent(tagConent1);
                                tagj.setLinkedTimes(tagLinkedTimes1);
                                tagj.setTagId(tagID1);
                                work.setTag2(tagj);
                            }
                        }
                        work.setUser(user);
                        works.add(work);
                    }
                    worksListReadWriteLock.writeLock().unlock();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            worksListReadWriteLock.writeLock().lock();
                            for (int i = 0; i < works.size(); ++i) {
//                                Bitmap pic = HttpUtil.getAvaSyn(works.get(i).getUser().getAvaID());
                                Bitmap pic = null;
                                try {
                                    if (getActivity() != null) {
                                        String avaId = works.get(i).getUser().getAvaID();
                                        if (avaId != null) {
                                            pic = Tools.getImageBitmapSyn(getActivity(), works.get(i).getUser().getAvaID());
                                            pic = Tools.compressImage(pic);
//                                            works.get(i).getUser().setAvator(pic);
                                            setWorksListAvaBitmap(i,pic);
                                        }
                                    }
                                } catch (IOException | JSONException | ParseException e) {
                                    if (getActivity() != null) {
                                        Tools.my_toast(getActivity(), "图片加载出错啦！");
                                    }
                                }
//                                Log.d("workadpter", "需要更新");
//                                if (getActivity() != null) {
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (binding != null) {
//                                                binding.worksColumn.getAdapter().notifyDataSetChanged();
//                                            }
//                                        }
//                                    });
//                                }
                            }
                            worksListReadWriteLock.writeLock().unlock();
                        }
                    }).start();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.works_column);
                                    if (recyclerView != null) {
                                        recyclerView.getAdapter().notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    if (getActivity() != null) {
                        Tools.my_toast(getActivity(), "搜索失败，请检查网络");
                    }

                }
            }
        });
    }

    private void setWorksListAvaBitmap(int idx ,Bitmap pic){
        works.get(idx).getUser().setAvator(pic);
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyItemChanged(idx);
                }
            });
        }
    }

    private void setWorksListCoverBitmap(int idx ,Bitmap pic){
        works.get(idx).setCoverBitmap(pic);
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyItemChanged(idx);
                }
            });
        }
    }

    private void clearWorksList(){
        int prevSize = works.size();
        works.clear();
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyItemRangeRemoved(0,prevSize);
                }
            });
        }
    }

    private void insertWorkData(Works work){
        works.add(work);
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyItemInserted(works.size() - 1);
                }
            });
        }
    }

}