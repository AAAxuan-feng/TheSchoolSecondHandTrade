package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.DBHelper;
import com.example.theschoolsecond_handtrade.db.GoodsBean;
import com.example.theschoolsecond_handtrade.util.CategoryBar;
import com.example.theschoolsecond_handtrade.util.Images;

/**
 * 发布商品页。
 *
 * 布局：activity_publish.xml
 * 控件：tvBack / layoutPickImage / ivGoodsImg / tvPickImage /
 *       etGoodsName / etGoodsPrice / etGoodsDesc / btnSubmit
 *
 * 知识点：
 *   1) ActivityResultContracts.OpenDocument 从相册选图（无需申请存储权限）
 *   2) 选完图后拿 Uri，直接存 Uri 字符串到 SQLite
 *   3) 表单校验（名称必填、价格必须是数字且 > 0）
 */
public class PublishActivity extends AppCompatActivity {

    private ImageView ivGoodsImg;
    private EditText etGoodsName;
    private EditText etGoodsPrice;
    private EditText etGoodsDesc;
    private Button btnSubmit;

    private DBHelper dbHelper;
    /** 选中的图片 Uri，没选则为 null */
    private Uri pickedImageUri;
    /** 选中的商品分类，默认"书籍" */
    private String selectedCategory = GoodsBean.CATEGORY_BOOK;

    /**
     * 相册选图。
     *
     * 用 ActivityResultContracts.GetContent：
     *   输入类型 I = String（一个 MIME 类型，如 "image/*"）
     *   输出类型 O = Uri（选中的图片）
     * 正好对应 ActivityResultLauncher<String>，不需要额外处理类型。
     *
     * ★ 这里不申请 READ_MEDIA_IMAGES 权限的原因：
     *   选图走的是系统文件选择器（SAF），用户选中后系统会把该 Uri 的临时读权限
     *   授予本 App，属于"按需授权"，不需要在 Manifest 里声明存储权限。
     *   缺点是重装/重启后这个 Uri 可能失效，所以发布时立刻把 Uri 存库并立即展示；
     *   课程作业场景足够。若要长期有效，可改用 OpenDocument + 持久化权限。
     */
    private ActivityResultLauncher<String> createPickImageLauncher() {
        ActivityResultContract<String, Uri> contract = new ActivityResultContracts.GetContent();
        ActivityResultCallback<Uri> callback = uri -> {
            if (uri == null) {
                return;   // 用户取消了选择
            }
            pickedImageUri = uri;
            Images.load(this, uri.toString(), ivGoodsImg);
        };
        return registerForActivityResult(contract, callback);
    }

    private final ActivityResultLauncher<String> pickImageLauncher = createPickImageLauncher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        dbHelper = DBHelper.getInstance(this);

        ivGoodsImg = findViewById(R.id.ivGoodsImg);
        etGoodsName = findViewById(R.id.etGoodsName);
        etGoodsPrice = findViewById(R.id.etGoodsPrice);
        etGoodsDesc = findViewById(R.id.etGoodsDesc);
        btnSubmit = findViewById(R.id.btnSubmit);

        // ========== 分类选择（默认第一项"书籍"）==========
        LinearLayout layoutCategoryPick = findViewById(R.id.layoutCategoryPick);
        selectedCategory = GoodsBean.CATEGORIES[0];
        CategoryBar.build(layoutCategoryPick, GoodsBean.CATEGORIES, selectedCategory,
                category -> selectedCategory = category);

        // 返回
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // 点图片区域选图
        findViewById(R.id.layoutPickImage).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 发布
        btnSubmit.setOnClickListener(v -> doPublish());
    }

    private void doPublish() {
        String name = etGoodsName.getText().toString().trim();
        String priceStr = etGoodsPrice.getText().toString().trim();
        String desc = etGoodsDesc.getText().toString().trim();

        // ---------- 表单校验 ----------
        if (TextUtils.isEmpty(name)) {
            toast("请填写商品名称");
            etGoodsName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            toast("请填写价格");
            etGoodsPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            toast("价格格式不正确");
            return;
        }
        if (price <= 0) {
            toast("价格必须大于 0");
            return;
        }
        if (pickedImageUri == null) {
            toast("请选择一张商品图片");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            desc = "卖家很懒，没有填写描述～";
        }

        int userId = LoginActivity.getUserId(this);
        if (userId <= 0) {
            toast("登录状态已失效，请重新登录");
            return;
        }

        // ---------- 入库 ----------
        btnSubmit.setEnabled(false);
        long goodsId = dbHelper.addGoods(name, price, desc,
                pickedImageUri.toString(), userId, selectedCategory);

        if (goodsId > 0) {
            toast("发布成功");
            startActivity(new Intent(this, MainActivity.class)); // 回首页看结果
            finish();
        } else {
            btnSubmit.setEnabled(true);
            toast("发布失败，请重试");
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
