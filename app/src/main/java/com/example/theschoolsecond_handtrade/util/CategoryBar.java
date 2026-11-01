package com.example.theschoolsecond_handtrade.util;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.GoodsBean;

/**
 * 分类标签栏工具类。
 *
 * 首页（"全部 / 书籍 / 数码 …"）和发布页（"书籍 / 数码 …"）都要用一排分类按钮，
 * 抽成一个方法，两边共用，样式保持一致。
 *
 * 为什么不用 RadioGroup：
 *   分类有 5 个，一行放不下需要换行；而 RadioGroup 只保证"直接子控件"互斥，
 *   分两行就会变成两个独立分组（可以各选一个）。这里自己维护"当前选中项"，
 *   通过重绘背景色实现单选，逻辑更直观。
 */
public final class CategoryBar {

    private CategoryBar() {
    }

    /**
     * 生成分类按钮。
     *
     * @param container     容器（LinearLayout，horizontal）
     * @param items         分类文案数组，如 {"全部","书籍",...}
     * @param selected      当前选中的分类
     * @param listener      点击回调（把被点的分类回传）
     */
    public static void build(LinearLayout container,
                             String[] items,
                             String selected,
                             OnCategorySelectedListener listener) {
        if (container == null || items == null) {
            return;
        }
        container.removeAllViews();
        Context context = container.getContext();

        for (String item : items) {
            TextView chip = new TextView(context);
            chip.setText(item);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(13);
            chip.setPadding(dp(context, 14), dp(context, 7), dp(context, 14), dp(context, 7));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(context, 8));
            chip.setLayoutParams(lp);

            boolean isSelected = item.equals(selected);
            applyChipStyle(chip, isSelected);

            chip.setOnClickListener(v -> {
                // 点完立刻把所有 chip 重绘一遍，保证只有一个高亮
                refreshSelection(container, item);
                if (listener != null) {
                    listener.onSelected(item);
                }
            });

            container.addView(chip);
        }
    }

    /** 把容器里除 selected 以外的 chip 全部设为未选中样式 */
    private static void refreshSelection(LinearLayout container, String selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            applyChipStyle(chip, selected.contentEquals(chip.getText()));
        }
    }

    /** 统一设置某一块 chip 的选中/未选中外观 */
    private static void applyChipStyle(TextView chip, boolean selected) {
        Context context = chip.getContext();
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_normal);
            chip.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    /** 首页：带"全部"选项 */
    public static String[] withAll() {
        String[] result = new String[GoodsBean.CATEGORIES.length + 1];
        result[0] = GoodsBean.CATEGORY_ALL;
        System.arraycopy(GoodsBean.CATEGORIES, 0, result, 1, GoodsBean.CATEGORIES.length);
        return result;
    }

    public interface OnCategorySelectedListener {
        void onSelected(String category);
    }
}
