package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    DecimalFormat format = new DecimalFormat("#,##0.0000"); // Choose the number of decimal places to work with in case they are different than zero and zero value will be removed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "Loading data...", Toast.LENGTH_LONG).show();

        format.setRoundingMode(RoundingMode.UP); // Choose your Rounding Mode
        EditText inputValue = findViewById(R.id.input_value);
        Button btn = findViewById(R.id.convert_btn);
        ImageView changeDirection = findViewById(R.id.changeDirection);
        LinearLayout layout = findViewById(R.id.result_layout);
        Spinner source = findViewById(R.id.source);
        Spinner destination = findViewById(R.id.destination);
        TextView resultSourceLabel = findViewById(R.id.result_source_label);
        TextView resultDestinationLabel = findViewById(R.id.result_destination_label);
        TextView resultSourceValue = findViewById(R.id.result_source_value);
        TextView resultDestinationValue = findViewById(R.id.result_destination_value);
        TextView rateValue = findViewById(R.id.rate_value);

        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Double> valueList = new ArrayList<>();
        disableBtn(btn);


        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        RequestQueue queue = new RequestQueue(cache,network);
        queue.start();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://usd.fxexchangerate.com/rss.xml",
                response -> {
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();

                        Document document = builder.parse(new InputSource(new StringReader(response)));
                        document.getDocumentElement().normalize();

                        NodeList list = document.getElementsByTagName("item");
                        for(int i = 0; i <list.getLength(); i++){
                            Node node = list.item(i);
                            if(node.getNodeType() == Node.ELEMENT_NODE){
                                Element element = (Element) node;
                                String description = element.getElementsByTagName("description").item(0).getTextContent();
                                description = description.substring(description.indexOf(" = "));
                                String value = description.replaceAll("[^\\d.]+|\\.(?!\\d)", "");

                                String name = description.substring(description.indexOf(value) + value.length() + 1);
                                String code = element.getElementsByTagName("title").item(0).getTextContent();
                                code = code.substring(code.lastIndexOf('(') + 1, code.lastIndexOf(')'));

                                arrayList.add(code + " - " + name);
                                valueList.add(Double.parseDouble(value));

                                Log.v("description",value + " " + code + "-" + name);
                            }

                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
                        source.setAdapter(arrayAdapter);
                        destination.setAdapter(arrayAdapter);
                    }catch (Exception e){
                        Log.v("Line 96", e.toString());
                    }
                }, error -> {
                    Log.v("Volley Response", error.toString());
                    Toast.makeText(getApplicationContext(), "Failed to get Data, try again later", Toast.LENGTH_LONG).show();
                });
        queue.add(stringRequest);

        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(source.getSelectedItemPosition() == destination.getSelectedItemPosition()){
                    disableBtn(btn);
                }
                else
                    enableBtn(btn);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        destination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(source.getSelectedItemPosition() == destination.getSelectedItemPosition()){
                    disableBtn(btn);
                }
                else
                    enableBtn(btn);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        inputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputValue.getText().toString().length() > 3){
                    inputValue.removeTextChangedListener(this);
                    String raw = format.format(Double.parseDouble(inputValue.getText().toString().replaceAll("\\.","").replace(',', '.')));
                    raw = raw.replaceAll(",0000", "");
                    inputValue. setText(raw);
                    inputValue.setSelection(raw.length());
                    inputValue.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        btn.setOnClickListener(view -> {

            layout.setVisibility(LinearLayout.VISIBLE);
            resultSourceLabel.setText(arrayList.get(source.getSelectedItemPosition()).split(" - ")[0]);
            resultDestinationLabel.setText(arrayList.get(destination.getSelectedItemPosition()).split(" - ")[0]);
            String rawInput = inputValue.getText().toString().replaceAll("\\.", "").replaceAll(",", ".");
            Double value = rawInput.isEmpty() ? 1 : Double.parseDouble(rawInput);
            if(value <= 0){
                value = 1d;
            }
            resultSourceValue.setText(format.format(value).replaceAll(",0000", ""));
            Double rate = valueList.get(destination.getSelectedItemPosition())/valueList.get(source.getSelectedItemPosition());
            resultDestinationValue.setText(format.format(rate*value));
            rateValue.setText(format.format(rate));
        });
        changeDirection.setOnClickListener(view -> {
            int swap = source.getSelectedItemPosition();
            source.setSelection(destination.getSelectedItemPosition());
            destination.setSelection(swap);
        });

    }
    private void enableBtn(@NonNull Button btn){
        btn.setClickable(true);
        btn.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.color.purple_700));
    }
    private void disableBtn(@NonNull Button btn){
        btn.setClickable(false);
        btn.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.color.gray));
    }
}