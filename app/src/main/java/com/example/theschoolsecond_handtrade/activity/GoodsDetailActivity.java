package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.DBHelper;
import com.example.theschoolsecond_handtrade.db.GoodsBean;
import com.example.theschoolsecond_handtrade.util.Images;

/**
 * 商品详情页。
 *
 * 布局：activity_goods_detail.xml
 * 控件：tvBack / ivGoodsImg / tvGoodsName / tvPrice / tvStatus /
 *       tvSeller / tvGoodsDesc / btnContact / btnBuy
 *
 * 知识点：
 *   1) Intent 传参（goods_id）取数据
 *   2) 按钮状态控制（自己的商品不能买、已售出不能买）
 *   3) 下单写库（订单表 + 商品状态，DBHelper 里用事务保证一致性）
 */
public class GoodsDetailActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private GoodsBean goods;
    private Button btnBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_detail);

        dbHelper = DBHelper.getInstance(this);

        final int goodsId = getIntent().getIntExtra("goods_id", -1);
        goods = dbHelper.getGoodsById(goodsId);

        // 商品不存在（比如已被删掉）：提示并退出
        if (goods == null) {
            Toast.makeText(this, "商品不存在或已下架", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
    }

    private void bindViews() {
        ImageView ivGoodsImg = findViewById(R.id.ivGoodsImg);
        TextView tvGoodsName = findViewById(R.id.tvGoodsName);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvSeller = findViewById(R.id.tvSeller);
        TextView tvGoodsDesc = findViewById(R.id.tvGoodsDesc);
        Button btnContact = findViewById(R.id.btnContact);
        btnBuy = findViewById(R.id.btnBuy);

        // ========== 填充数据 ==========
        Images.load(this, goods.getImgPath(), ivGoodsImg);

        tvGoodsName.setText(goods.getName());
        tvPrice.setText(goods.getPriceText());
        tvGoodsDesc.setText(goods.getDesc());
        tvCategory.setText(goods.getCategorySafe());

        String seller = goods.getSellerName() == null ? "未知用户" : goods.getSellerName();
        tvSeller.setText("卖家：" + seller);

        tvStatus.setText(goods.getStatusText());
        tvStatus.getBackground().mutate().setTint(
                ContextCompat.getColor(this,
                        goods.isOnSale() ? R.color.status_on_sale : R.color.status_sold));

        tvStatus.setVisibility(View.VISIBLE);

        // ========== 返回 ==========
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // ========== 联系卖家（模拟）==========
        btnContact.setOnClickListener(v -> {
            int currentUserId = LoginActivity.getUserId(this);
            if (currentUserId <= 0) {
                toast("请先登录");
                return;
            }
            if (currentUserId == goods.getUserId()) {
                toast("这是你自己发布的东西");
                return;
            }
            String phone = dbHelper.getContactOfSeller(goods.getUserId());
            new AlertDialog.Builder(this)
                    .setTitle("联系卖家")
                    .setMessage("卖家：" + seller + "\n联系方式：" + phone
                            + "\n\n（本项目为课程作业演示，联系方式为模拟数据）")
                    .setPositiveButton("知道了", null)
                    .show();
        });

        // ========== 立即购买 ==========
        btnBuy.setOnClickListener(v -> doBuy());

        // 自己的商品不能买、已售出不能买 —— 直接禁用按钮
        int currentUserId = LoginActivity.getUserId(this);
        if (!goods.isOnSale()) {
            btnBuy.setEnabled(false);
            btnBuy.setText("已售出");
        } else if (currentUserId == goods.getUserId()) {
            btnBuy.setEnabled(false);
            btnBuy.setText("这是你的商品");
        }
    }

    private void doBuy() {
        final int buyerId = LoginActivity.getUserId(this);
        if (buyerId <= 0) {
            toast("请先登录");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        if (buyerId == goods.getUserId()) {
            toast("不能购买自己发布的商品");
            return;
        }
        if (!goods.isOnSale()) {
            toast("这件商品已经被买走了");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("确认购买")
                .setMessage("确定要购买「" + goods.getName() + "」吗？\n价格："
                        + goods.getPriceText())
                .setNegativeButton("再想想", null)
                .setPositiveButton("确定购买", (dialog, which) -> executeBuy(buyerId))
                .show();
    }

    private void executeBuy(int buyerId) {
        long orderId = dbHelper.buyGoods(goods.getId(), buyerId, goods.getUserId());
        if (orderId <= 0) {
            toast("下单失败，请重试");
            return;
        }
        toast("购买成功！可在「个人中心 → 我买到的」查看");
        // 回到首页，列表会刷新（该商品不再显示在售）
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
