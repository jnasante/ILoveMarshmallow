package com.josephasante.zapposandroidchallenge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String QUERY_URL = "https://zappos.amazon.com/mobileapi/v1/search?term=";

    private EditText searchEditText;
    private ListView productListView;
    private ProductListAdapter productListAdapter;

    private ProgressDialog progressDialog;

    private JSONArray resultsJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = (EditText) findViewById(R.id.searchEditText);
        productListView = (ListView) findViewById(R.id.productsListView);

        productListAdapter = new ProductListAdapter(this, getLayoutInflater());

        productListView.setAdapter(productListAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching for product...");
        progressDialog.setCancelable(false);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ProductDetailActivity.class);


                try {
                    if (((JSONObject) resultsJsonArray.get(position)).has("asin"))
                        intent.putExtra("asin", ((JSONObject) resultsJsonArray.get(position)).optString("asin"));


                    if (((JSONObject) resultsJsonArray.get(position)).has("price"))
                        intent.putExtra("price", ((JSONObject) resultsJsonArray.get(position)).optString("price"));

                    if (((JSONObject) resultsJsonArray.get(position)).has("originalPrice"))
                        intent.putExtra("originalPrice", ((JSONObject) resultsJsonArray.get(position)).optString("originalPrice"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void search(View view) {
        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        getProducts(searchEditText.getText().toString());
    }

    private void getProducts(String searchString) {

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Show ProgressDialog to inform user that a task in the background is occurring
        progressDialog.show();

        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        // Dismiss ProgressDialog
                        progressDialog.dismiss();

                        // Success!
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                        // update the data in your custom method.
                        resultsJsonArray = jsonObject.optJSONArray("results");
                        productListAdapter.updateResults(resultsJsonArray);

                        if (resultsJsonArray.length() == 0) {
                            findViewById(R.id.failureTextView).setVisibility(View.VISIBLE);
                        }
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
