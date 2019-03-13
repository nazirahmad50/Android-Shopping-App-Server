package com.nazir.shoppingserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.Interface.RecycleViewItemClickListener;
import com.nazir.shoppingserver.Model.Hookah;
import com.nazir.shoppingserver.Model.MyResponse;
import com.nazir.shoppingserver.Model.Notification;
import com.nazir.shoppingserver.Model.Request;
import com.nazir.shoppingserver.Model.Sender;
import com.nazir.shoppingserver.Model.Token;
import com.nazir.shoppingserver.Remote.APIService;
import com.nazir.shoppingserver.ViewHolder.HookahViewHolder;
import com.nazir.shoppingserver.ViewHolder.OrderViewHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatusActivity extends AppCompatActivity {

    //RecyclerView
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference requestRef;
    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    //Spinner for options(Placed,Shipped,On the Way)
    MaterialSpinner statusSpinner;

    //notification service
    APIService mAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        requestRef = firebaseDatabase.getReference("request");

        //RecyclerView
        recyclerView = findViewById(R.id.ordersList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //init service
        mAPIService = Common.getFCMService();

        LoadOrders();
    }

    private void LoadOrders() {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(requestRef,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtorderStatus.setText(Common.convertCodeToString(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                //Edit Button
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(), adapter.getItem(position));

                    }
                });

                //Remove Button
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DeleteOrder(adapter.getRef(position).getKey());

                    }
                });

                //Direction Button
                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(OrderStatusActivity.this, TrackingOrder.class);
                        Common.cuurentRequest = model;
                        startActivity(intent);
                    }
                });


                //Detail Button
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(OrderStatusActivity.this, OrderDetailActivity.class);
                        Common.cuurentRequest = model;
                        intent.putExtra("hookahid",adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_status_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showUpdateDialog(String key, final Request item) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatusActivity.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please Choose status");

        LayoutInflater inflator = this.getLayoutInflater();
        final View view = inflator.inflate(R.layout.update_order_layout, null);

        statusSpinner = view.findViewById(R.id.statusSpinner);
        statusSpinner.setItems("Placed", "On the Way", "Shipped");

        alertDialog.setView(view);

        final String localKeys = key;

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                item.setStatus(String.valueOf(statusSpinner.getSelectedIndex()));

                requestRef.child(localKeys).setValue(item);
                adapter.notifyDataSetChanged(); //To update item size

                sendOrderStatusToUser(localKeys,item);

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();
    }


    private void DeleteOrder(String key) {

        requestRef.child(key).removeValue();
        adapter.notifyDataSetChanged();//To update item size
    }


    //****************************************Notification*********************************
    private void sendOrderStatusToUser(final String key,final Request item) {



        DatabaseReference token = FirebaseDatabase.getInstance().getReference("tokens");
        token.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                            Token token = postSnapshot.getValue(Token.class);

                            Notification notification = new Notification("Shopping", "Your order "+key + " is " + Common.convertCodeToString(item.getStatus()));
                            Sender content = new Sender(token.getToken(),notification);

                            mAPIService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                                if (response.body().success == 1) {

                                                    Toast.makeText(OrderStatusActivity.this, "Order was updated", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(OrderStatusActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                }

                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }





}
