package com.nazir.shoppingserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.Interface.RecycleViewItemClickListener;
import com.nazir.shoppingserver.R;

public class HookahViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {


    public TextView hookahName;
    public ImageView hookahImage;

    private RecycleViewItemClickListener itemClickListener;

    public void setItemClickListener(RecycleViewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public HookahViewHolder(View itemView) {
        super(itemView);

        hookahName = itemView.findViewById(R.id.hookah_item);
        hookahImage = itemView.findViewById(R.id.hookah_image);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }



    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view, getAdapterPosition(), false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select the Action");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}
