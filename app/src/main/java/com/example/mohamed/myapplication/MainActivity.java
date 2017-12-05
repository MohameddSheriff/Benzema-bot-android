package com.example.mohamed.myapplication;

import android.app.Notification;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
TextView textView;
TextView typing;
ListView listView;
Button button;
Button quickButton;
EditText editText;
String uuid;
String enteredmsg;
ChatAdapter adapter;
boolean flag = false;
boolean quick = true;





// Add the request to the RequestQueue.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //textView = (TextView) findViewById(R.id.textView);
        listView = (ListView) findViewById(R.id.chat_list_view);
        //textView.setMovementMethod(LinkMovementMethod.getInstance());
        button = (Button) findViewById(R.id.button);
        quickButton = (Button) findViewById(R.id.quickButton);
        editText = (EditText) findViewById(R.id.editText2);
        typing = (TextView) findViewById(R.id.typing);
        adapter = new ChatAdapter(this,R.layout.single_message_layout);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(adapter.getCount()-1);
            }
        });
        new JSONtask2().execute("http://benzema-bot.herokuapp.com/welcome");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(editText.getText().toString().equals(""))) {
                    typing.setText("Typing..");

                    adapter.add(new DataProvider(flag, "You:\n" +editText.getText().toString()));
                    flag = !flag;
                    enteredmsg = editText.getText().toString();
                    editText.setText("");
                    new JSONtask().execute("http://benzema-bot.herokuapp.com/chat");
                }

            }
        });
        quickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quick) {
                    quick = false;
                    typing.setText("Typing..");
                    adapter.add(new DataProvider(flag, "You:\nSaturday"));
                    flag = !flag;
                    editText.setText("");
                    enteredmsg = "Saturday";
                    new JSONtask().execute("http://benzema-bot.herokuapp.com/chat");

                }
            }
        });


    }


    public class JSONtask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {

            try {
                    BufferedReader reader = null;
            String urlParameters  = "{\"message\":" + "\""+ enteredmsg + "\"}";
            byte[] postData       = urlParameters.getBytes("UTF-8");
            int    postDataLength = postData.length;

                URL url = new URL(strings[0]);
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod( "POST" );
                conn.setRequestProperty("AUTHORIZATION",uuid);
                conn.setRequestProperty( "Content-Type", "application/json");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                conn.setUseCaches( false );
                OutputStream os = conn.getOutputStream();
                os.write( postData );
              InputStream stream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String line = "";
                while((line = reader.readLine()) != null)
                {
                    buffer.append(line);
                }
                String finalJson = buffer.toString();
                JSONObject json = new JSONObject(finalJson);
                String message = json.getString("message");
                conn.connect();
                os.close();
                reader.close();
                stream.close();

                return "Benzema:\n"+message;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            catch (JSONException e) {
//                e.printStackTrace();
//            }
            return "no";
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            adapter.add(new DataProvider(flag,s));
            typing.setText("");
            quick = true;
            flag=!flag;


        }
    }

    public class JSONtask2 extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String line = "";
                while((line = reader.readLine()) != null)
                {
                    buffer.append(line);
                }
                String finalJson = buffer.toString();
                JSONObject json = new JSONObject(finalJson);
                String message = json.getString("message");
                uuid = json.getString("uuid");
                return "Benzema:\n"+ message;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return "no2";

        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            adapter.add(new DataProvider(flag,s));
            typing.setText("");
            flag=!flag;

        }
    }

}
