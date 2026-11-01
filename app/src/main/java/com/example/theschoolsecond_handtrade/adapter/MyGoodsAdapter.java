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
 * "我的商品"列表适配器（我发布的 / 我买到的 / 我卖出的 三页共用）。
 *
 * 布局：item_my_goods.xml
 * 比首页多了：btnOffShelf（下架按钮）
 *
 * 三种页面的差异用构造参数控制：
 *   showOffShelf = true  -> 显示下架按钮（只有"我发布的"需要）
 *   showOffShelf = false -> 隐藏（"我买到的""我卖出的"不需要）
 */
public class MyGoodsAdapter extends RecyclerView.Adapter<MyGoodsAdapter.MyGoodsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(GoodsBean goods);
    }

    public interface OnOffShelfClickListener {
        void onOffShelf(GoodsBean goods);
    }

    private final Context context;
    private final List<GoodsBean> data = new ArrayList<>();
    private final boolean showOffShelf;
    private final OnItemClickListener itemListener;
    private final OnOffShelfClickListener offShelfListener;

    public MyGoodsAdapter(Context context,
                          boolean showOffShelf,
                          OnItemClickListener itemListener,
                          OnOffShelfClickListener offShelfListener) {
        this.context = context;
        this.showOffShelf = showOffShelf;
        this.itemListener = itemListener;
        this.offShelfListener = offShelfListener;
    }

    public void setData(List<GoodsBean> goodsList) {
        data.clear();
        if (goodsList != null) {
            data.addAll(goodsList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyGoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_my_goods, parent, false);
        return new MyGoodsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGoodsViewHolder holder, int position) {
        GoodsBean goods = data.get(position);

        holder.tvName.setText(goods.getName());
        holder.tvDesc.setText(goods.getDesc());
        holder.tvPrice.setText(goods.getPriceText());
        holder.tvCategory.setText(goods.getCategorySafe());

        String seller = goods.getSellerName() == null ? "未知用户" : goods.getSellerName();
        holder.tvSeller.setText("卖家：" + seller);

        holder.tvStatus.setText(goods.getStatusText());
        holder.tvStatus.getBackground().mutate().setTint(
                ContextCompat.getColor(context,
                        goods.isOnSale() ? R.color.status_on_sale : R.color.status_sold));

        Images.load(context, goods.getImgPath(), holder.ivImg);

        // 下架按钮：只在"我发布的"页显示，且只在售时才可点
        if (showOffShelf && goods.isOnSale()) {
            holder.btnOffShelf.setVisibility(View.VISIBLE);
            holder.btnOffShelf.setOnClickListener(v -> {
                if (offShelfListener != null) {
                    offShelfListener.onOffShelf(goods);
                }
            });
        } else {
            holder.btnOffShelf.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onItemClick(goods);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyGoodsViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImg;
        final TextView tvName;
        final TextView tvDesc;
        final TextView tvPrice;
        final TextView tvCategory;
        final TextView tvStatus;
        final TextView tvSeller;
        final TextView btnOffShelf;

        MyGoodsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.ivGoodsImg);
            tvName = itemView.findViewById(R.id.tvGoodsName);
            tvDesc = itemView.findViewById(R.id.tvGoodsDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSeller = itemView.findViewById(R.id.tvSeller);
            btnOffShelf = itemView.findViewById(R.id.btnOffShelf);
        }
    }
}
