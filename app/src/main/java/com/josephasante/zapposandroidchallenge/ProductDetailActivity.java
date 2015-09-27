package com.josephasante.zapposandroidchallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProductDetailActivity extends ActionBarActivity {

    private static final String QUERY_URL = "https://zappos.amazon.com/mobileapi/v1/product/asin/";

    /* Product Details */
    private String asin = "";
    private String price = "";
    private String origPrice = "";

    private ProgressDialog progressDialog;

    /* Sharing */
    ShareActionProvider mShareActionProvider;

    /* Views */
    private TextView nameTextView;
    private TextView origPriceTextView;
    private TextView priceTextView;
    private WebView descriptionWebView;
    private TextView colorTextView;
    private ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if ("com.josephasante.zapposandroidchallenge.action.OPEN_VIEW".equals(action)) {
            Log.i(getClass().getSimpleName(), "EXTRA: "+intent.getExtras().getString("myextra"));
        }

        // Get asin
        //Intent intent = getIntent();
        asin = intent.getStringExtra("asin");
        price = intent.getStringExtra("price");
        origPrice = intent.getStringExtra("originalPrice");

        // Initialize text views
        nameTextView = (TextView) findViewById(R.id.productDetail_name);

        origPriceTextView = (TextView) findViewById(R.id.productDetail_origPrice);
        origPriceTextView.setPaintFlags(origPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        priceTextView = (TextView) findViewById(R.id.productDetail_price);
        descriptionWebView = (WebView) findViewById(R.id.productDetail_description);
        colorTextView = (TextView) findViewById(R.id.productDetail_color);

        productImage = (ImageView) findViewById(R.id.productDetail_image);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting product details...");
        progressDialog.setCancelable(false);

        setUpView();

        getProductDetails();
    }

    private void setUpView() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);

        MenuItem shareDetails = menu.findItem(R.id.menu_item_share);
        if (shareDetails != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareDetails);
        }

        // Create an Intent to share your content
        setShareIntent();

        return true;
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Product Details");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareIntent.toUri(Intent.URI_INTENT_SCHEME));

            mShareActionProvider.setShareIntent(shareIntent);

            Log.d("uggh", shareIntent.toUri(Intent.URI_INTENT_SCHEME));
        }

    }

    private void getProductDetails() {

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Show ProgressDialog to inform user that a task in the background is occurring
        progressDialog.show();

        client.get(QUERY_URL + asin,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        // Dismiss ProgressDialog
                        progressDialog.dismiss();

                        setShareIntent();

                        // Success!
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                        // update the data in your custom method.
                        if (jsonObject.has("productName"))
                            nameTextView.setText(jsonObject.optString("productName"));

                        if (jsonObject.has("description")) {
                            String stringToSearch = jsonObject.optString("description");

                            descriptionWebView.loadData(
                                    "<html><body style=\"background-color:" +
                                            "#B3E5FC" +
                                            "\">" + jsonObject.optString("description") +
                                            "</body></html>", "text/html", null);
                        }

                        // See if there is an image url in the Object
                        if (jsonObject.has("defaultImageUrl")) {

                            // If so, grab the imageUrl out from the object
                            String imageURL = jsonObject.optString("defaultImageUrl");

                            // Use Picasso to load the image
                            Picasso.with(getApplicationContext()).load(imageURL).into(productImage);
                        }

                        priceTextView.setText(price);
                        if (!origPrice.equals("null") && origPrice != null)
                            origPriceTextView.setText(origPrice);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        // Dismiss ProgressDialog
                        progressDialog.dismiss();

                        // Failure :(
                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                        // Log error message
                        Log.e(getComponentName().getShortClassName(), statusCode + " " + throwable.getMessage());
                    }
                });
    }
}
