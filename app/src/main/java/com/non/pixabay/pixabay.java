package com.non.pixabay;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class pixabay extends AppCompatActivity {

    private ListView lvphoto;
    private EditText edittext;
    List<information> infolist;


    photoAdapter adapter ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixabay);
        lvphoto= (ListView) findViewById(R.id.lvphotos);
        infolist = new ArrayList<information>() ;


        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        // image cashing
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start


        adapter = new photoAdapter(getApplicationContext(), R.layout.custom_row, infolist);
        lvphoto.setAdapter(adapter);
        WebService_Connection searcheddata = new WebService_Connection("");
        searcheddata.execute();

        edittext = (EditText) findViewById(R.id.edit_txt);
        edittext.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    infolist.clear();
                    String charsearch = s.toString();
                    adapter = new photoAdapter(getApplicationContext(), R.layout.custom_row, infolist);
                    lvphoto.setAdapter(adapter);
                    WebService_Connection searcheddata = new WebService_Connection(charsearch);
                    searcheddata.execute();
                }
                else {
                    WebService_Connection searcheddata = new WebService_Connection("");
                    searcheddata.execute();
                }
            }
        });

    }



    public class WebService_Connection extends AsyncTask< String, String, List<information> > {

        public static final String REQUEST_TYPE="GET";
        public static final int READ_TIMEOUT =15000;
        public static final int CONNECTION_TIMEOUT =15000;
        private String q ;
        public  WebService_Connection(String found)
        {
          this.q=found;
        }

        @Override
       protected List<information> doInBackground(String... params)

        {


            Log.d("DoInBackground" , "called!");

            String KEY="6682976-9b2b85247f5f07d541cacaca2";
            String urlstr="https://pixabay.com/api/?key="+ KEY +"&q="+ q +"&image_type=photo&response_group=image_details";
            HttpURLConnection urlconnection=null;
            BufferedReader bufferreader =null;
            InputStreamReader streamReader=null;
            String line;
            String response ="";
            List<information> informationList = null ;

            try{
                URL url=new URL(urlstr);
                urlconnection=(HttpURLConnection) url.openConnection();
                urlconnection.setRequestMethod(REQUEST_TYPE);
                urlconnection.setReadTimeout(READ_TIMEOUT);
                urlconnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlconnection.connect();

                streamReader=new InputStreamReader(urlconnection.getInputStream());
                bufferreader =new BufferedReader(streamReader);

                StringBuilder stringBuilder = new StringBuilder();

                while((line = bufferreader.readLine()) !=null)
                {
                    stringBuilder.append(line);
                }

                response =stringBuilder.toString();
                JSONObject jsonobj = new JSONObject(response);
                JSONArray photos = jsonobj.getJSONArray("hits");

                StringBuffer finalbufferdata = new StringBuffer();
                informationList=new ArrayList<information>();

                for(int i=0; i<photos.length(); i++) {
                    JSONObject jsonobject = photos.getJSONObject(i);
                    information info=new information();
                    info.setImage(jsonobject.getString("webformatURL"));
                    informationList.add(info);
                }


            }catch(MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                urlconnection.disconnect();

                try {
                    if(streamReader !=null)
                        streamReader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

                try {
                    if(bufferreader !=null)
                        bufferreader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            return informationList;
        }


        protected void onPostExecute(List<information> result){
            super.onPostExecute(infolist);
            // TODO need to set data to the list

            if (result != null)
                infolist.addAll(result);
            adapter.notifyDataSetChanged();

        }

    }


    public class photoAdapter extends ArrayAdapter{

        private List<information> infolist;
        private int resource;
        private LayoutInflater inflater;

        public photoAdapter(Context context,  int resource, List<information> objects) {
            super(context, resource, objects);
            infolist=objects;
            this.resource= resource;
            inflater= (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        public View getView (int position, View convertview, ViewGroup parent){

            if(convertview == null){
                convertview=inflater.inflate(resource, null);
            }

            ImageView imageIcone;
            imageIcone=(ImageView) convertview.findViewById(R.id.list_icon);
            ImageLoader.getInstance().displayImage( infolist.get(position).getImage(),imageIcone);
            return convertview;
        }


    }



}

