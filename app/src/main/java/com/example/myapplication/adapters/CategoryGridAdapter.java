package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.utils.Constants;

/**
 * CategoryGridAdapter is a custom BaseAdapter that populates the 6 category tiles
 * inside the GridView on NewComplaintFragment (PRD Section 3 & Section 5.2 U5).
 */
public class CategoryGridAdapter extends BaseAdapter {

    private final Context context;
    private final String[] categories = Constants.CATEGORIES;
    private final int[] subtitles = {
            R.string.cat_sub_hostel,
            R.string.cat_sub_transport,
            R.string.cat_sub_academic,
            R.string.cat_sub_fees,
            R.string.cat_sub_library,
            R.string.cat_sub_general
    };
    private final int[] icons = {
            R.drawable.ic_cat_hostel,
            R.drawable.ic_cat_transport,
            R.drawable.ic_cat_academic,
            R.drawable.ic_cat_fees,
            R.drawable.ic_cat_library,
            R.drawable.ic_cat_general
    };

    private int selectedPosition = 0;

    /**
     * Constructor that stores the hosting Context for inflating item_category_grid.xml.
     */
    public CategoryGridAdapter(Context context) {
        this.context = context;
    }

    /**
     * Updates the highlighted tile position and refreshes the GridView.
     */
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    /**
     * Returns the currently selected category name (e.g. "Hostel & Mess").
     */
    public String getSelectedCategory() {
        return categories[selectedPosition];
    }

    /**
     * Returns the total number of categories in the grid (6).
     */
    @Override
    public int getCount() {
        return categories.length;
    }

    /**
     * Returns the category name at the given grid position.
     */
    @Override
    public Object getItem(int position) {
        return categories[position];
    }

    /**
     * Returns the position as the row ID.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or reuses an item_category_grid.xml tile view and styles it as selected or unselected.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);
        }

        View layoutTile = view.findViewById(R.id.layoutCategoryTile);
        View frameIcon = view.findViewById(R.id.frameCategoryIcon);
        ImageView ivIcon = view.findViewById(R.id.ivCategoryIcon);
        View frameCheck = view.findViewById(R.id.frameCategoryCheck);
        TextView tvName = view.findViewById(R.id.tvCategoryName);
        TextView tvSub = view.findViewById(R.id.tvCategorySub);

        tvName.setText(categories[position]);
        tvSub.setText(subtitles[position]);
        ivIcon.setImageResource(icons[position]);

        boolean isSelected = (position == selectedPosition);
        if (isSelected) {
            layoutTile.setBackgroundResource(R.drawable.bg_category_selected);
            frameIcon.setBackgroundResource(R.drawable.bg_circle_crimson);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.white));
            frameCheck.setVisibility(View.VISIBLE);
            tvName.setTextColor(ContextCompat.getColor(context, R.color.crimson_deep));
            tvSub.setTextColor(ContextCompat.getColor(context, R.color.crimson_dark));
        } else {
            layoutTile.setBackgroundResource(R.drawable.bg_category_unselected);
            frameIcon.setBackgroundResource(R.drawable.bg_circle_white);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.midnight));
            frameCheck.setVisibility(View.GONE);
            tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            tvSub.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        return view;
    }
}
