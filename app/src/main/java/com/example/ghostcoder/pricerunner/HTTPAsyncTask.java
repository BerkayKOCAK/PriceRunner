package com.example.ghostcoder.pricerunner;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPAsyncTask extends AsyncTask<String , String , String>

{

    @Override
    protected  String doInBackground(String[] urls){

        try {
            if(urls[2]=="get")
            {return HttpGet(urls[0],urls[1]);}
            else
            {
                return HttpPost(urls[0],urls[1]);
            }

        } catch (IOException e) {

            //
            return "Unable to retrieve data for this barcode\nbarcode might not be in our database \nscanner might read wrong codes \n TRY AGAIN";
        } catch (JSONException e) {

            e.printStackTrace();
            return "JSON REQUEST IS BAD";
        }
    }



    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public HTTPAsyncTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }


    //Gets data from url , Connects to given url
    private  String HttpGet(String myUrl,String tempMode) throws IOException {
        InputStream inputStream = null;
        String result = "";

        URL url = new URL(myUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        inputStream = conn.getInputStream();
        result = convertInputStreamToString(inputStream);
        conn.disconnect();
        //Toast.makeText(this, "RESULT = "+result, Toast.LENGTH_LONG).show();

        return result;
    }


    public  String HttpPost(String url, String productNameHolder) throws IOException, JSONException {
        String response;
        String result = "";
        InputStream inputStream = null;


        URL urlPost = new URL(url);
        HttpURLConnection client = (HttpURLConnection) urlPost.openConnection();
        client.setRequestMethod("POST");
        client.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        client.setRequestProperty("Accept", "*/*");
        client.setDoOutput(true);
        client.setDoInput(true);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("token", "ZXXKYCLGGOMGTEIQXGFBRDHOHIAUJHRPWXUJAPISMLYKPJQWRLAJVSHNVSXZRZFV");
        jsonParam.put("country", "tr");
        jsonParam.put("source","google_shopping");
        jsonParam.put("topic", "search_results");
        jsonParam.put("key", "term");
        jsonParam.put("values", productNameHolder);

        Log.i("JSON", jsonParam.toString());
        client.connect();
        OutputStreamWriter os = new OutputStreamWriter (client.getOutputStream());
        os.write(jsonParam.toString());

        os.flush();

        int respCode =  client.getResponseCode();
        if(respCode != 200)
        {
            String respMSG = client.getResponseMessage();
            InputStream errStream =client.getErrorStream();
            result = convertInputStreamToString(errStream);
            result+= " MESSAGE = "+respMSG;
            client.disconnect();
        }
        else
        {
            inputStream = client.getInputStream();
            result = convertInputStreamToString(inputStream);
            client.disconnect();
        }

        return result;
    }


    //converts incoming input stream to string
    private static String convertInputStreamToString(InputStream inputStream)  throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null){
            result +=line;
        }

        inputStream.close();
        return result;
    }


}
