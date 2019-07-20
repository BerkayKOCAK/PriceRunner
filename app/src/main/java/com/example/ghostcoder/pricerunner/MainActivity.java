package com.example.ghostcoder.pricerunner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements HTTPAsyncTask.AsyncResponse
{
    Button scan;
    Button productListButton;
    Button offlineSearch;
    TextView scannedCode;

    String productName;
    String barcodeResult;
    String jobID;
    String modeHolder;
    String queryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobID=null;
        modeHolder = "";
        queryResult = null;
        barcodeResult = null;
        productName = null;

        scan = findViewById(R.id.scanNew);
        productListButton =  findViewById(R.id.productListButton);
        offlineSearch = findViewById(R.id.offlineSearch);
        scannedCode = findViewById(R.id.scannedCode);


        //Camera permission ask
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 666);
        }

        scan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View viewHolder)
            {
                ProductClass.setProductArray(null);
                ProductClass.setUrlArray(null);
                startScanner();
            }
        });

        productListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View viewHolder)
            {
                if(checkConnection())
                {
                    if (ProductClass.getProductArray() != null && barcodeResult != null)//!
                    {
                        //ProductClass.setProductArray(productAnalysis(queryResult));
                        //ProductClass.setUrlArray(urlAnalysis(queryResult));

                        Toast.makeText(getApplicationContext(), "SHOWING RESULTS", Toast.LENGTH_LONG).show();
                        Intent listIntent = new Intent(MainActivity.this, listActivity.class);
                        listIntent.putStringArrayListExtra("productArray", ProductClass.getProductArray());
                        listIntent.putStringArrayListExtra("urlArray", ProductClass.getUrlArray());
                        startActivity(listIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Still loading try again", Toast.LENGTH_LONG).show();
                        queryResult = startRequest("info", null);
                    }
                }
                else
                    {
                        Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                    }
            }
        });


        offlineSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View viewHolder)
            {
                if((barcodeResult != null) && (!productName.contains("TRY AGAIN")))//!
                {
                    BufferedReader reader;

                    try{
                        final InputStream file = getAssets().open("marketbarkodlistesi.txt");
                        reader = new BufferedReader(new InputStreamReader(file));
                        String temp ="";
                        String line = reader.readLine();
                        while(line != null){

                            line = reader.readLine();
                            temp = line;
                            if(temp.contains(barcodeResult))
                            {
                                temp = temp.replace(barcodeResult,"");

                                temp = new String(temp.getBytes("ISO-8859-1"), "UTF-8");
                                productName = temp.trim();
                                Toast.makeText(getApplicationContext(), "Found it on local file!\nName = "+productName, Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                    } catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Scan Again !", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public boolean checkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }
    //this override the implemented method from AsyncResponse
    @Override
    public void processFinish(String output){
        String result = output;

        if(result.contains("<?xml"))
        { //Toast.makeText(getApplicationContext(), "RESULT ON POST  =  " + output, Toast.LENGTH_LONG).show();
            ProductClass.setProductArray(productAnalysis(output));
            ProductClass.setUrlArray(urlAnalysis(output));
        }
    }

    private ArrayList<String> productAnalysis(String result)
    {
        ArrayList<String> productArrayTEMP = new  ArrayList<String>();


        XmlPullParserFactory factory = null;
        XmlPullParser xpp = null;
        Document parsedXML = null;
        String str = "";
        String temp = "";

        try {

            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory2.newDocumentBuilder();
            parsedXML = builder.parse(new InputSource(new StringReader(result)));
            //parsedXML.getElementsByTagName("").item(0).getTextContent();
            NodeList nList = parsedXML.getElementsByTagName("search-result");
            int counter = 0;
            //
            for (int i = 0; i < nList.getLength(); i++)
            {
                str ="";
                temp = "";
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;
                //Element cElement =  (Element) eElement.getElementsByTagName("search-result").item(0);
                str += "Name = " + eElement.getElementsByTagName("name").item(0).getTextContent();
                if ( eElement.getElementsByTagName("price").getLength() >0)
                {str += "\nPrice = " + eElement.getElementsByTagName("price").item(0).getTextContent()+" TL ";}
                if ( eElement.getElementsByTagName("shop-name").getLength() >0)
                {str += "\nShop name = " + eElement.getElementsByTagName("shop-name").item(0).getTextContent();}
                if ( eElement.getElementsByTagName("shop-url").getLength() >0)
                {

                    //temp += eElement.getElementsByTagName("shop-url").item(0).getTextContent();
                    productArrayTEMP.add(str);
                    
                }
                counter++;

            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return productArrayTEMP;
    }

    private ArrayList<String> urlAnalysis(String result)
    {
        ArrayList<String> urlArrayTEMP =  new  ArrayList<String>();


        XmlPullParserFactory factory = null;
        XmlPullParser xpp = null;
        Document parsedXML = null;
        String str = "";
        String temp = "";

        try {

            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory2.newDocumentBuilder();
            parsedXML = builder.parse(new InputSource(new StringReader(result)));
            //parsedXML.getElementsByTagName("").item(0).getTextContent();
            NodeList nList = parsedXML.getElementsByTagName("search-result");
            int counter = 0;
            //
            for (int i = 0; i < nList.getLength(); i++)
            {
                str ="";
                temp = "";
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;

                if ( eElement.getElementsByTagName("shop-url").getLength() >0)
                {

                    temp += eElement.getElementsByTagName("shop-url").item(0).getTextContent();
                    urlArrayTEMP.add(temp);

                }
                counter++;

            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return urlArrayTEMP;
    }

    private void startScanner()
    {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a barcode");
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    private String startRequest(String mode,String holder) {
        String result = null;


        if(mode == "name")
        {
            try {
                result = new HTTPAsyncTask(this)
                        .execute("http://axata3dapi.azurewebsites.net/api/Urun/getUrun?barcode=" + barcodeResult,null,"get")
                        .get();
                JSONObject reader = new JSONObject(result);
                JSONObject data = reader.getJSONObject("data");
                result = data.getString("urun_desc");
                //modeHolder = "post";
            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }
        }
        else if (mode == "post")
        {
            try {
                result = new HTTPAsyncTask(this)
                        .execute("https://api.priceapi.com/v2/jobs/",productName,"post").get();
                JSONObject reader = new JSONObject(result);
                result = reader.getString("job_id");

            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }

        }

        else if(mode == "info")
        {
            try {
                result = new HTTPAsyncTask(this)
                         .execute("https://api.priceapi.com/v2/jobs/"+jobID+"/download.xml?token=ZXXKYCLGGOMGTEIQXGFBRDHOHIAUJHRPWXUJAPISMLYKPJQWRLAJVSHNVSXZRZFV","info","get").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String holder = null;
        if(result != null) {
            if(result.getContents() == null) {

                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else
                {

                    //Toast.makeText(getApplicationContext(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    barcodeResult = result.getContents();
                    scannedCode.setBackgroundColor(Color.WHITE);
                    scannedCode.setText("SCAN RESULT = "+barcodeResult);
                    scannedCode.setTextColor(Color.RED);
                    if(checkConnection())
                        {
                            productName = startRequest("name",null);
                            Toast.makeText(getApplicationContext(), "product name = "+productName, Toast.LENGTH_LONG).show();
                            jobID = startRequest("post",null);
                            //Toast.makeText(getApplicationContext(), "JOB_ID =  " + jobID, Toast.LENGTH_LONG).show();
                        }
                }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
