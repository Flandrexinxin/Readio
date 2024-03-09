package cn.ruc.readio.userPageActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.ruc.readio.R;
import cn.ruc.readio.entity.EditWorkPicture;

public class EditPhotoAdapter extends RecyclerView.Adapter<EditPhotoAdapter.ViewHolder> {
    private List<EditWorkPicture> photoList;

    private editWorkActivity activity = null;

    public EditPhotoAdapter(editWorkActivity activity, List<EditWorkPicture> list){
        this.photoList = list;
        this.activity = activity;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView picTv;
        private  ImageView picDel;
        public ViewHolder(View view)
        {
            super(view);
            Log.d("RECD","ViewHolder");
            picTv = view.findViewById(R.id.editPic);
            picDel = view.findViewById(R.id.delPic);
        }

    }

    @NonNull
    @Override
    public EditPhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("RECD","onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R .layout.item_pic, parent,false);
        EditPhotoAdapter.ViewHolder viewHolder = new EditPhotoAdapter.ViewHolder(view);


        viewHolder.picTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                viewHolder.picDel.setVisibility(View.VISIBLE);
                photoList.get(viewHolder.getAbsoluteAdapterPosition()).setShowDel(true);
                activity.recyclerView.getAdapter().notifyDataSetChanged();
                return false;
            }
        });

        viewHolder.picDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoList.remove(viewHolder.getAbsoluteAdapterPosition());
                activity.recyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        activity.editBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int size = photoList.size();
                for(int i = 0;i < size;++i){
                    photoList.get(i).setShowDel(false);
                }
                activity.recyclerView.getAdapter().notifyDataSetChanged();
                return false;
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EditPhotoAdapter.ViewHolder holder, int position) {
        Log.d("RECD","onBindViewHolder");
        Bitmap bitmap = photoList.get(position).getPicture();
        holder.picTv.setImageBitmap(bitmap);
        if(photoList.get(position).getShowDel()){
            holder.picDel.setVisibility(View.VISIBLE);
        }else{
            holder.picDel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("RECD","getItemCount");
        return photoList.size();
    }
}
