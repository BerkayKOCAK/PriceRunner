package com.example.ghostcoder.pricerunner;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class listActivity extends AppCompatActivity {

    ListView productList;
    public static ArrayList<String> productArray;
    public static ArrayList<String> urlArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        productArray = new ArrayList<String>();
        urlArray = new ArrayList<String>();
        Intent superClassIntent = getIntent();
        productList = findViewById(R.id.productList);
        productArray = superClassIntent.getStringArrayListExtra("productArray");
        urlArray = superClassIntent.getStringArrayListExtra("urlArray");
        //productArray = ProductClass.getProductArray();
        //urlArray = ProductClass.getUrlArray();
        if(productArray != null)
        {
            // give adapter to ListView UI element to render
            ArrayAdapter ad = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1, productArray);
            productList.setAdapter(ad);

            productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {


                    //Toast.makeText(getApplicationContext(),"selected = "+productArray.get(position)+"\nURL = "+urlArray.get(position),Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),"REDIRECTING = "+urlArray.get(position),Toast.LENGTH_LONG).show();
                    openWebURL(urlArray.get(position));


                }
            });
        }



    }


    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }
}
