package com.nazir.shoppingserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nazir.shoppingserver.Model.Order;
import com.nazir.shoppingserver.R;

import java.util.List;

class OrderDetailViewHolder extends RecyclerView.ViewHolder{

    TextView name,quantity,price,discount;

    public OrderDetailViewHolder(View itemView){
        super(itemView);

        name =  itemView.findViewById(R.id.product_name);
        quantity =  itemView.findViewById(R.id.product_quantity);
        price =  itemView.findViewById(R.id.product_price);
        discount =  itemView.findViewById(R.id.product_discount);

    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailViewHolder> {

    List<Order> ordersList;

    public OrderDetailAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout,parent,false);

        return new OrderDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {

        Order order = ordersList.get(position);

        holder.name.setText(String.format("Name : %s", order.getProductName()));
        holder.quantity.setText(String.format("Quantity : %s", order.getQuantity()));
        holder.price.setText(String.format("Price : £%s", order.getPrice()));
        holder.discount.setText(String.format("Discount : %s", order.getDiscount()));


    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }
}
