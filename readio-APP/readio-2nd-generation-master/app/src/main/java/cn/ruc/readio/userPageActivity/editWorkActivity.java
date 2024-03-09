package cn.ruc.readio.userPageActivity;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.ruc.readio.databinding.ActivityEditWorkBinding;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.ruc.readio.R;
import cn.ruc.readio.entity.EditWorkPicture;
import cn.ruc.readio.util.BitmapBase64;
import cn.ruc.readio.util.HttpUtil;
import cn.ruc.readio.util.Tools;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class editWorkActivity extends AppCompatActivity {
    public ActivityEditWorkBinding binding;
    static public editWorkActivity editworkActivity;
    public RecyclerView recyclerView = null;
    private ImageView temppic;
    private ArrayList<String> tagList = new ArrayList<>();
    private ArrayList<String> picNameList = new ArrayList<>();
    private ArrayList<String> picTypeList = new ArrayList<>();
    private ArrayList<String> picContentList = new ArrayList<>();
    private List<EditWorkPicture> BitmapList = new ArrayList<>();
    ArrayList<String> tagIdList = new ArrayList<>();
    private int withPhoto = 0;
    private Bitmap dataBitMap = null;
    public EditText editContent;
    public ScrollView editBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditWorkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_edit_work);
        editworkActivity = this;
        newWorksActivity.new_workActivity.finish();
        Intent intent = getIntent();

        editContent = (EditText) findViewById(R.id.editPiece);
        editBar = (ScrollView) findViewById(R.id.editBar);
        // 测试构造一些数据
//        @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.flower);
//        Bitmap mbitmap = BitmapFactory.decodeStream(is);
//        BitmapList.add(mbitmap);


        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView = binding.editPicBar;
        recyclerView = findViewById(R.id.editPicBar);
        recyclerView.setLayoutManager(layoutManager);
        EditPhotoAdapter adapter = new EditPhotoAdapter(editWorkActivity.this,BitmapList);
        recyclerView.setAdapter(adapter);

        String seriesName = intent.getStringExtra("seriesName");
        String seriesId = intent.getStringExtra("seriesId");
        String workName = intent.getStringExtra("workName");
        Log.d("hello", seriesName);
        TextView publish_button = (TextView) findViewById(R.id.publishButton);
        TextView exitEdit_button = (TextView) findViewById(R.id.editExitButton);
        TextView saveDraft_button = (TextView) findViewById(R.id.saveDraftButton);
        TextView addTag_button = (TextView) findViewById(R.id.addTagButton);
        LinearLayout uploadPic = findViewById(R.id.upload_photo);
        Activity thisact = this;
        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(editContent.getText()) || BitmapList.size() > 0) {
                    try {
                        publishPiece(workName, toString().valueOf(editContent.getText()), seriesId, seriesName, "1", picNameList, picTypeList,
                                picContentList);
                    } catch (JSONException e) {
                        Tools.my_toast(thisact, "出错了，发表失败");
                    }

                    Toast.makeText(editWorkActivity.this, "您编辑的内容已发布（⌯'▾'⌯）", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(editWorkActivity.this, "所以你写了什么？(´◔ ‸◔`)", Toast.LENGTH_SHORT).show();
                }
            }
        });
        exitEdit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editContent = (EditText) findViewById(R.id.editPiece);
                if (!TextUtils.isEmpty(editContent.getText())) {
                    Intent intent = new Intent(editWorkActivity.this, exitEditActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
        saveDraft_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                上传至服务器，更新数据库
                 */
                EditText editContent = (EditText) findViewById(R.id.editPiece);
                if (!TextUtils.isEmpty(editContent.getText()) || BitmapList.size() > 0) {
                    try {
                        publishPiece(workName, editContent.getText().toString(), seriesId, seriesName, "0", picNameList, picTypeList,
                                picContentList);
                    } catch (JSONException e) {
                        Tools.my_toast(thisact, "出错了，保存失败");
                    }

                    Toast.makeText(editWorkActivity.this, "您编辑的内容已保存至草稿箱（⌯'▾'⌯）", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(editWorkActivity.this, "所以你写了什么？(´◔ ‸◔`)", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        addTag_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(editWorkActivity.this, addTagActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = choosePicture();
                startActivityForResult(intent, 2);
            }
        });
    }

    public void publishPiece(String workName, String content, String seriesId, String seriesName, String status, ArrayList<String> picNameList, ArrayList<String> picTypeList, ArrayList<String> picContentList) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("piecesTitle", workName);
        jsonObject.put("content", content);
        jsonObject.put("seriesId", seriesId);
        jsonObject.put("seriesName", seriesName);
        jsonObject.put("status", status);
        jsonObject.put("tagNameList", new JSONArray(tagList));
        jsonObject.put("tagIdList", new JSONArray(tagIdList));
        JSONArray picArray = new JSONArray();
        for (int i = 0; i < picNameList.size(); i++) {
            JSONObject picDetail = new JSONObject();
            picDetail.put("fileName", picNameList.get(i));
            picDetail.put("fileType", picTypeList.get(i));
            picDetail.put("fileContent", picContentList.get(i));
            picArray.put(picDetail);
        }
        jsonObject.put("picList", picArray);
        String json = jsonObject.toString();

        HttpUtil.postRequestWithTokenJsonAsyn(this, "/works/addPieces", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(editWorkActivity.this, "发布失败，请重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 3) {
            String tagName = data.getStringExtra("tagName");
            String tagId = data.getStringExtra("tagId");
            Log.d("hello", tagName);
            if (tagList.contains(tagName)) {
            } else {
                tagList.add(tagName);
                tagIdList.add(tagId);
            }
        }
        if (requestCode == 2) {
            if (data != null) {
                Log.d(this.toString(), "成功从图库取到data");
                Uri uri = data.getData();
//                curAvatarImageView.setImageURI(uri);
                try {
                    dataBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String lastPathSegment = uri.getLastPathSegment();
                int pointIdx = lastPathSegment.lastIndexOf(".");
                String fileName = null;
                String fileType = null;
                if (pointIdx != -1) {
                    fileName = lastPathSegment.substring(0, pointIdx);
                    fileType = lastPathSegment.substring(pointIdx + 1, lastPathSegment.length());
                } else {
                    fileName = lastPathSegment;
                    fileType = "default";
                }
                picNameList.add(fileName);
                picTypeList.add(fileType);
                picContentList.add(BitmapBase64.bitmapToBase64(dataBitMap));
                BitmapList.add(new EditWorkPicture(dataBitMap));
//                temppic.setImageBitmap(dataBitMap);
//                Log.d("photophoto",toString().valueOf(dataBitMap));

                editWorkActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });


            }
        }
    }

    public Intent choosePicture() {
        if (Build.VERSION.SDK_INT >= 30) {// Android 11 (API level 30)
            return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            return Intent.createChooser(intent, null);
        }
    }
}
