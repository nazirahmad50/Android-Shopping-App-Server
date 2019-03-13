package com.nazir.shoppingserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.ViewHolder.OrderDetailAdapter;

public class OrderDetailActivity extends AppCompatActivity {

    TextView order_id,order_phone,address_order,total_order,comments_order;

    String order_id_value = "";

    RecyclerView lstHookahs;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id = findViewById(R.id.order_id);
        order_phone = findViewById(R.id.order_phone);
        address_order = findViewById(R.id.order_address);
        total_order = findViewById(R.id.order_total);
        comments_order = findViewById(R.id.order_comment);

        lstHookahs = findViewById(R.id.lstHookahs);
        lstHookahs.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        lstHookahs.setLayoutManager(layoutManager);

        if (getIntent() != null){

            order_id_value = getIntent().getStringExtra("hookahid");
        }

        order_id.setText(order_id_value);
        order_phone.setText(Common.cuurentRequest.getPhone());
        address_order.setText(Common.cuurentRequest.getAddress());
        total_order.setText(Common.cuurentRequest.getTotal());
        comments_order.setText(Common.cuurentRequest.getComment());

        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.cuurentRequest.getHookahs());

        adapter.notifyDataSetChanged();
        lstHookahs.setAdapter(adapter);


    }
}
