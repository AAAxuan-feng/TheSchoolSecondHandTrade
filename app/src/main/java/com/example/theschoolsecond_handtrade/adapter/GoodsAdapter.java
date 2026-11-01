package com.example.theschoolsecond_handtrade.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.GoodsBean;
import com.example.theschoolsecond_handtrade.util.Images;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页商品列表适配器。
 *
 * 布局：item_goods.xml
 * 控件：ivGoodsImg / tvGoodsName / tvGoodsDesc / tvPrice / tvStatus / tvSeller
 *
 * 知识点：
 *   1) ViewHolder 模式复用 ItemView，避免每次滚动都 findViewById
 *   2) extends RecyclerView.Adapter
 *   3) Glide 加载图片（统一走 util/Images，兼容演示图和相册 Uri）
 */
public class GoodsAdapter extends RecyclerView.Adapter<GoodsAdapter.GoodsViewHolder> {

    /** 点击回调，由 Activity 实现（跳详情页） */
    public interface OnItemClickListener {
        void onItemClick(GoodsBean goods);
    }

    private final Context context;
    private final List<GoodsBean> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public GoodsAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /** 整体替换数据并刷新列表 */
    public void setData(List<GoodsBean> goodsList) {
        data.clear();
        if (goodsList != null) {
            data.addAll(goodsList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_goods, parent, false);
        return new GoodsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsViewHolder holder, int position) {
        GoodsBean goods = data.get(position);

        holder.tvName.setText(goods.getName());
        holder.tvDesc.setText(goods.getDesc());
        holder.tvPrice.setText(goods.getPriceText());
        holder.tvCategory.setText(goods.getCategorySafe());

        // 卖家昵称可能为 null（LEFT JOIN 没匹配到时），兜底处理
        String seller = goods.getSellerName() == null ? "未知用户" : goods.getSellerName();
        holder.tvSeller.setText("卖家：" + seller);

        // 状态标签：在售绿色，已售出灰色
        holder.tvStatus.setText(goods.getStatusText());
        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_tag);
        holder.tvStatus.getBackground().mutate().setTint(
                ContextCompat.getColor(context,
                        goods.isOnSale() ? R.color.status_on_sale : R.color.status_sold));

        // 图片：Glide 统一加载
        Images.load(context, goods.getImgPath(), holder.ivImg);

        // 点击整张卡片 -> 详情页
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(goods);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class GoodsViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImg;
        final TextView tvName;
        final TextView tvDesc;
        final TextView tvPrice;
        final TextView tvCategory;
        final TextView tvStatus;
        final TextView tvSeller;

        GoodsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.ivGoodsImg);
            tvName = itemView.findViewById(R.id.tvGoodsName);
            tvDesc = itemView.findViewById(R.id.tvGoodsDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSeller = itemView.findViewById(R.id.tvSeller);
        }
    }
}
