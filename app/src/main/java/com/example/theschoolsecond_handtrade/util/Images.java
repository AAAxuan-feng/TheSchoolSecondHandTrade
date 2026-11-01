package com.example.theschoolsecond_handtrade.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.theschoolsecond_handtrade.R;

import java.io.File;

/**
 * 图片加载工具类。
 *
 * 数据库里 img_path 有两种可能的值：
 *   1) "res:demo_book"  —— 预置演示商品，指向项目内的 drawable 资源
 *   2) "content://..."  —— 用户从相册选的图（Android 10+ 就是这种 Uri）
 *   3) "/storage/..."   —— 早期版本可能存成绝对路径，这里兼容一下
 *
 * 统一用 Glide 加载，演示图走资源 id，真实图走 Uri。
 */
public final class Images {

    /** 演示图前缀 */
    public static final String RES_PREFIX = "res:";

    private Images() {
    }

    /**
     * 把数据库里的 img_path 字符串转换成 Glide 能接受的模型。
     *
     * @return drawable 资源 id（int）或 Uri 对象
     */
    public static Object toModel(Context context, String imgPath) {
        if (TextUtils.isEmpty(imgPath)) {
            return R.drawable.demo_book;   // 没图时给个占位，避免空白
        }
        if (imgPath.startsWith(RES_PREFIX)) {
            String name = imgPath.substring(RES_PREFIX.length());
            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            return resId != 0 ? resId : R.drawable.demo_book;
        }
        if (imgPath.startsWith("content://") || imgPath.startsWith("file://")
                || imgPath.startsWith("http")) {
            return Uri.parse(imgPath);
        }
        // 老数据：直接是文件绝对路径
        return Uri.fromFile(new File(imgPath));
    }

    /** 加载到 ImageView，带圆角占位图 */
    public static void load(Context context, String imgPath, ImageView target) {
        Glide.with(context)
                .load(toModel(context, imgPath))
                .placeholder(R.drawable.demo_book)
                .error(R.drawable.demo_book)
                .centerCrop()
                .into(target);
    }
}
