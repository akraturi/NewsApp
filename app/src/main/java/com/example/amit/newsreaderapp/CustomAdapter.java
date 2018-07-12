package com.example.amit.newsreaderapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class CustomAdapter extends ArrayAdapter<String>{
    ArrayList<String> mUrls;
    ArrayList<String> mHeadLines;
    LayoutInflater inflater;

    public CustomAdapter(Context context, ArrayList<String> mUrls, ArrayList<String> mHeadLines) {
        super(context,R.layout.list_item);
        this.mUrls = mUrls;
        this.mHeadLines = mHeadLines;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.list_item, null, false);
            TextView textView = convertView.findViewById(R.id.titletextview);
            ImageView imageView = convertView.findViewById(R.id.headlineimageview);
            textView.setText(mHeadLines.get(position));
            Picasso.with(getContext()).load(mUrls.get(position)).into(imageView);
        return convertView;
    }

    @Override
    public int getCount() {
        return mHeadLines.size();
    }
}