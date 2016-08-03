package com.neusnow.ganksimple;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neusnow.ganksimple.bean.Girl;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by xujin on 16/07/20.
 */
public class RecyclerViewAdapter extends Adapter<ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private Context context;
    private List data;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return data.size() == 0 ? 0 : data.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public RecyclerViewAdapter(Context context, List data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_base, parent,
                    false);
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_foot, parent,
                    false);
            return new FootViewHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            Girl girl = (Girl)data.get(position);
            ((ItemViewHolder) holder).tv_date.setText(girl.getPublishedAt().substring(0,10));
            ((ItemViewHolder) holder).tv_desc.setText(girl.getDesc());


            Picasso.with(context).load(girl.getUrl())
                    .transform(new CropSquareTransformation())
                    //.transform(new CenterFaceCrop(300,300))//人脸识别的类库，大小方面有点问题，有待解决
                    .placeholder(R.color.colorWhite)
                    .error(R.mipmap.girl_image)
                    .into(((ItemViewHolder) holder).iv_girl);

            if (onItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemLongClick(holder.itemView, position);
                        return false;
                    }
                });
            }
        }
    }


    static class ItemViewHolder extends ViewHolder {

        @Bind(R.id.tv_desc) TextView tv_desc;
        @Bind(R.id.tv_date) TextView tv_date;
        @Bind(R.id.iv_girl) ImageView iv_girl;

        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class FootViewHolder extends ViewHolder {

        public FootViewHolder(View view) {
            super(view);
        }
    }
}


class CropSquareTransformation implements Transformation {

    //截取从宽度和高度最小作为bitmap的宽度和高度
    @Override
    public Bitmap transform(Bitmap source) {
        int size=Math.min(source.getWidth(),source.getHeight());
        int x=(source.getWidth()-size)/2;
        int y=(source.getHeight()-size)/2;
        Bitmap result=Bitmap.createBitmap(source,x,y,size,size);
        if (result!=source){
            source.recycle();//释放bitmap
        }
        return result;
    }

    @Override
    public String key() {
        return "square()";
    }
}