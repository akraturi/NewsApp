package com.example.amit.newsreaderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView mArticalListView;
    private ArrayList<String> mHeadlines;
    private ArrayList<String> mUrls;
    private ArrayList<String> mHtmlContents;
    private ArrayList<String> mImageUrls;
    private String sourceUrl="https://newsapi.org/v2/top-headlines?sources=the-times-of-india&apiKey=98612ca98cff450886ebdf795025707a";
    private String fetchedJson;
    private ArrayAdapter<String> mArrayAdapter;
    private int mCurrentArticle=0;
    private  RequestQueue requestQueue;
    private int htmlResponseCount=0;
    private SQLiteDatabase articalsDB;
    private  boolean mNetworkStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articalsDB=openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articalsDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY,headline VARCHAR,url VARCHAR,imageurl VARCHAR,html VARCHAR)");
        mHeadlines=new ArrayList<String>();
        mHtmlContents=new ArrayList<String>();
        mUrls=new ArrayList<String>();
        mImageUrls=new ArrayList<String>();
       // mUrls=new ArrayList<Integer>();
        requestQueue= Volley.newRequestQueue(MainActivity.this);
      /*  mHeadlines.add("Headline2");
        mHeadlines.add("Headline3");*/

        //mUrls.add(R.drawable.icon);
       // mUrls.add(R.drawable.icon);
       // mUrls.add(R.drawable.icon);

        mArticalListView=findViewById(R.id.articalslistview);
        mArrayAdapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1,mHeadlines);
        mNetworkStatus=getNetworkStatus();
        //CustomAdapter mArrayAdapter=new CustomAdapter(MainActivity.this,mImageUrls,mHeadlines);
        if(mNetworkStatus) {
            articalsDB.delete("articles",null,null);
            fetchJson();
            }
        else
        {
            Cursor c=articalsDB.rawQuery("SELECT * FROM articles",null);

            int headlineIndex=c.getColumnIndex("headline");
            Log.i("headLineIND:",Integer.toString(headlineIndex));
            int urlIndex=c.getColumnIndex("url");
            Log.i("urlIndex:",Integer.toString(urlIndex));
            int htmlIndex=c.getColumnIndex("html");
            Log.i("htmlIndex:",Integer.toString(htmlIndex));

            c.moveToFirst();

            while(c.moveToNext())
            {   Log.i("inside cursor:","I am in");
                mHeadlines.add(c.getString(headlineIndex));
                mUrls.add(c.getString(urlIndex));
                mHtmlContents.add(c.getString(htmlIndex));
            }
            Log.i("headlines:d",mHeadlines.toString());
            Log.i("urls:d",mUrls.toString());
            Log.i("html:d",mHtmlContents.toString());
            mArrayAdapter.notifyDataSetChanged();
            //c.close();
        }
        mArticalListView.setAdapter(mArrayAdapter);

        mArticalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentArticle=i;
                showArticle();
            }
        });

    }
    public void fetchJson()
    {
        StringRequest stringRequest=new StringRequest(Request.Method.GET, sourceUrl, new Response.Listener<String>() {
           @Override
           public void onResponse(String response) {
            Log.i("Fetched data:",response);
            fetchedJson=response;
            decodeJson();
           }
       }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
               error.printStackTrace();
           }
       });
       requestQueue.add(stringRequest);

    }
    public void  decodeJson()
    {
        try {
            JSONObject jsonObject=new JSONObject(fetchedJson);
            String articles=jsonObject.getString("articles");
            Log.i("articles:",articles);
            JSONArray jsonArray=new JSONArray(articles);
            Log.i("articlesarray:",jsonArray.toString());

            for(int i=0;i<jsonArray.length();i++)
            {
                String title=jsonArray.getJSONObject(i).getString("title");
                String url=jsonArray.getJSONObject(i).getString("url");
                String imageurl=jsonArray.getJSONObject(i).getString("urlToImage");
                url=url.replace("http","https");
                mHeadlines.add(title);
                mUrls.add(url);
                mImageUrls.add(imageurl);
               // updateDataBase(title,url,imageurl);
                Log.i("headline:",title);
                Log.i("url:",url);
            }
            for(int i=0;i<mUrls.size();i++)
            {
                getHtml(mUrls.get(i));
            }
            mArrayAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void showArticle()
    {   Intent intent=new Intent(MainActivity.this,ArticleActivity.class);
        intent.putExtra("html",mHtmlContents.get(mCurrentArticle));
        intent.putExtra("url",mUrls.get(mCurrentArticle));
        mNetworkStatus=getNetworkStatus();
        intent.putExtra("network",mNetworkStatus);
        Log.i("clicked html:",mUrls.get(mCurrentArticle));
        startActivity(intent);
    }
    public void getHtml(String url)
    {
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            Log.i("htmlResponse:",response);
            mHtmlContents.add(response);
            updateDataBase(mHeadlines.get(htmlResponseCount),mUrls.get(htmlResponseCount),mImageUrls.get(htmlResponseCount),response);
            htmlResponseCount++;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }
    public void updateDataBase(String headline,String url,String imageurl,String html)
    {
        String sql="INSERT INTO articles (headline,url,imageurl,html) VALUES (?,?,?,?)";
        SQLiteStatement sqLiteStatement=articalsDB.compileStatement(sql);
        sqLiteStatement.bindString(1,headline);
        sqLiteStatement.bindString(2,url);
        sqLiteStatement.bindString(3,imageurl);
        sqLiteStatement.bindString(4,html);
        sqLiteStatement.execute();

    }
    public boolean getNetworkStatus()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
