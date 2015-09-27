package com.josephasante.zapposandroidchallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by josephnasante on 9/21/15.
 */

public class ProductListAdapter extends BaseAdapter {

    private static final String IMAGE_URL_BASE = "http://ecx.images-amazon.com/images/I/31ql%2BZU57PL._AA160_.jpg";

    private Context context;
    private LayoutInflater inflater;
    private JSONArray jsonArray;

    public ProductListAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        this.jsonArray = new JSONArray();
    }

    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public Object getItem(int position) {
        return jsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public ImageView productIcon;
        public TextView nameTextView;
        public TextView priceTextView;
    }

    public void updateResults(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = inflater.inflate(R.layout.layout_product_result, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.productIcon = (ImageView) convertView.findViewById(R.id.searchResults_productIcon);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.searchResults_name);
            holder.priceTextView = (TextView) convertView.findViewById(R.id.searchResults_price);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current product's data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);

        // See if there is an image url in the Object
        if (jsonObject.has("imageUrl")) {

            // If so, grab the imageUrl out from the object
            String imageURL = jsonObject.optString("imageUrl");

            // Use Picasso to load the image
            Picasso.with(context).load(imageURL).placeholder(R.mipmap.ic_launcher).into(holder.productIcon);
        }

        String productName = "";
        String productPrice = "";

        if (jsonObject.has("productName")) {
            productName = jsonObject.optString("productName");
        }

        if (jsonObject.has("price")) {
            productPrice = jsonObject.optString("price");
        }

        // Send these Strings to the TextViews for display
        holder.nameTextView.setText(productName);
        holder.priceTextView.setText(productPrice);


        return convertView;
    }
}
