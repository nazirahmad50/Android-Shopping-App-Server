package com.nazir.shoppingserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.dd.processbutton.ProcessButton;
import com.nazir.shoppingserver.Interface.RecycleViewItemClickListener;
import com.nazir.shoppingserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId, txtorderStatus, txtOrderPhone, txtOrderAddress;

    public ProcessButton btnEdit,btnDirection,btnDetail,btnRemove;

    private RecycleViewItemClickListener itemClickListener;

    public void setItemClickListener(RecycleViewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = itemView.findViewById(R.id.order_id);
        txtorderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnDirection = itemView.findViewById(R.id.btnDirection);
        btnDetail = itemView.findViewById(R.id.btnDetail);
        btnRemove = itemView.findViewById(R.id.btnRemove);



    }



}
