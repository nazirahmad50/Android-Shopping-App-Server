package com.nazir.shoppingserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dd.processbutton.ProcessButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.Model.Banner;
import com.nazir.shoppingserver.Model.Hookah;
import com.nazir.shoppingserver.ViewHolder.BannerViewHolder;
import com.nazir.shoppingserver.ViewHolder.HookahViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class BannerActivity extends AppCompatActivity {


    //RecyclerView
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference bannersRef;
    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    //FirebaseStorage
    FirebaseStorage storage;
    StorageReference storageReference;

    FloatingActionButton addFab;


    //Widgets for Alert Dialog
    MaterialEditText edtItemName, edtHookahId;
    ProcessButton btnSelect, btnUpload;
    ImageView itemImage;

    RelativeLayout banner_activity_layout;


    Banner newBannerItem;

    Uri filePathUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //FireBase/Storage
        firebaseDatabase =  FirebaseDatabase.getInstance();
        bannersRef = firebaseDatabase.getReference("banner");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        //RecycleView/LayoutManager
        recyclerView = findViewById(R.id.recycler_banners);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        banner_activity_layout = findViewById(R.id.banner_activity_layout);


        addFab = findViewById(R.id.fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddBannerDialog();
            }
        });

        loadListBanner();

    }


    private void loadListBanner() {

        FirebaseRecyclerOptions<Banner> allBanners = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(bannersRef,Banner.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(allBanners) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder viewholder, int position, @NonNull Banner model) {

                viewholder.banner_name.setText(model.getName());

                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewholder.banner_image);

            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_item_layout,parent,false);

                return new BannerViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();

        recyclerView.setAdapter(adapter);

    }




    //********************************** Update/Delete Hookah Items *****************************************

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        if (item.getTitle().equals(Common.UPDATE)){

            updateBannerDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        } else if (item.getTitle().equals(Common.DELETE)){

            deleteBannerDialog(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);
    }

    private void updateBannerDialog(final String key, final Banner item) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Update Banner");
        alertDialog.setMessage("Please fill in the Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_hookah_item_layout = inflater.inflate(R.layout.add_new_banner_layout, null);

        edtItemName = add_hookah_item_layout.findViewById(R.id.edtItemName);
        edtHookahId = add_hookah_item_layout.findViewById(R.id.edtItemId);

        itemImage = add_hookah_item_layout.findViewById(R.id.gallery_image);


        btnSelect = add_hookah_item_layout.findViewById(R.id.btnSelect);
        btnUpload = add_hookah_item_layout.findViewById(R.id.btnUpload);



        //Set default names
        edtItemName.setText(item.getName());
        edtHookahId.setText(item.getId());

        Picasso.with(this)
                .load(item.getImage())
                .into(itemImage);


        alertDialog.setView(add_hookah_item_layout);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Events for buttons
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImag(); //Let user select image from gallery and save URI of image

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeImage(item);
            }
        });


        //Set Buttons
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Update information
                item.setName(edtItemName.getText().toString());
                item.setId(edtHookahId.getText().toString());


                bannersRef.child(key).setValue(item);

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

    private void changeImage(final Banner item) {

        if (filePathUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("bannerImages/" + imgName);

            imageFolder.putFile(filePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();

                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Update the Category image
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show()    ;
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());


                            for (int i =0 ;i<progress;i++){

                                mDialog.setMessage("Uploading " + i + "%");

                            }

                        }
                    });
        }
    }


    private void deleteBannerDialog(String key) {

        bannersRef.child(key).removeValue();
    }



    private void showAddBannerDialog() {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add New Banner");
        alertDialog.setMessage("Please fill in the Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_banner_layout = inflater.inflate(R.layout.add_new_banner_layout, null);

        edtItemName = add_banner_layout.findViewById(R.id.edtItemName);
        edtHookahId = add_banner_layout.findViewById(R.id.edtItemId);
        itemImage = add_banner_layout.findViewById(R.id.gallery_image);

        btnSelect = add_banner_layout.findViewById(R.id.btnSelect);
        btnUpload = add_banner_layout.findViewById(R.id.btnUpload);


        alertDialog.setView(add_banner_layout);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Events for buttons
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImag(); //Let user select image from gallery and save URI of image

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage();
            }
        });


        //Set Buttons
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if (newBannerItem != null){

                    bannersRef.push().setValue(newBannerItem);
                    Snackbar.make(banner_activity_layout, "New Banner" + newBannerItem.getName() + " was added", Snackbar.LENGTH_SHORT).show();

                }



            }
        });


        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                newBannerItem = null;

            }
        });
        alertDialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePathUri = data.getData();
            btnSelect.setText("Image Selected");

            itemImage.setImageURI(filePathUri);
        }
    }

    private void chooseImag() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {

        if (filePathUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploaded...");
            mDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("bannerImages/" + imgName);

            imageFolder.putFile(filePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();

                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set the values for new Banner to firebase after we got downloaded link
                                    newBannerItem = new Banner();
                                    newBannerItem.setName(edtItemName.getText().toString());
                                    newBannerItem.setId(edtHookahId.getText().toString());
                                    newBannerItem.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show()    ;
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                            for (int i =0 ;i<progress;i++){

                                mDialog.setMessage("Uploading " + i + "%");

                            }

                        }
                    });
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadListBanner();
    }
}
