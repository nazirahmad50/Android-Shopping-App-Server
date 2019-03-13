package com.nazir.shoppingserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.Interface.RecycleViewItemClickListener;
import com.nazir.shoppingserver.Model.Category;
import com.nazir.shoppingserver.Model.Hookah;
import com.nazir.shoppingserver.ViewHolder.HookahViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.Inflater;

public class HookahListActivity extends AppCompatActivity {


    //RecyclerView
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference hookahListRef;
    FirebaseRecyclerAdapter<Hookah, HookahViewHolder> adapter;

    FirebaseStorage storage;
    StorageReference storageReference;


    //Widgets for Alert Dialog
    MaterialEditText edtName, edtDescription, edtPrice, edtDiscount;
    ProcessButton btnSelect, btnUpload;
    ImageView gallery_image;

    FloatingActionButton fab;


    RelativeLayout root_layout;


    String categoryId = "";

    Hookah newHookahItem;

    Uri saveUri;

    //Search Functionality
    FirebaseRecyclerAdapter<Hookah, HookahViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hookah_list);

        //FireBase/Storage
        firebaseDatabase =  FirebaseDatabase.getInstance();
        hookahListRef = firebaseDatabase.getReference("hookahs");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        root_layout = findViewById(R.id.root_layout);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddHookahDialog();
            }
        });

        //RecycleView/LayoutManager
        recyclerView = findViewById(R.id.recycler_hookahs);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getHookahIntent();

        //Search Bar
        searchBar = findViewById(R.id.searchBar);
    }

    //********************************** Add new Hookah item *****************************************

    private void showAddHookahDialog() {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(HookahListActivity.this);
        alertDialog.setTitle("Add Category");
        alertDialog.setMessage("Please fill in the Information");


        LayoutInflater inflater = this.getLayoutInflater();
        View add_hookah_item_layout = inflater.inflate(R.layout.add_new_hookah_item_layout, null);

        edtName = add_hookah_item_layout.findViewById(R.id.edtName);
        edtDescription = add_hookah_item_layout.findViewById(R.id.edtDescription);
        edtPrice = add_hookah_item_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_hookah_item_layout.findViewById(R.id.edtDiscount);

        btnSelect = add_hookah_item_layout.findViewById(R.id.btnSelect);
        btnUpload = add_hookah_item_layout.findViewById(R.id.btnUpload);

        gallery_image = add_hookah_item_layout.findViewById(R.id.gallery_image);


        alertDialog.setView(add_hookah_item_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

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
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


                //Create new Hookah Item
                if (newHookahItem != null) {
                    hookahListRef.push().setValue(newHookahItem);
                    Snackbar.make(root_layout, "New Category" + newHookahItem.getName() + " was added", Snackbar.LENGTH_SHORT).show();

                }

            }
        });


        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                loadListHookah(categoryId);

            }
        });
        alertDialog.show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            saveUri = data.getData();
            btnSelect.setText("Image Selected");

            gallery_image.setImageURI(saveUri);
        }
    }

    private void chooseImag() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {

        if (saveUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploaded...");
            mDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("hookahImages/" + imgName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(HookahListActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();

                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set the valuees for new Category to firebase after we got downloaded link
                                    newHookahItem = new Hookah();
                                    newHookahItem.setName(edtName.getText().toString());
                                    newHookahItem.setDescription(edtDescription.getText().toString());
                                    newHookahItem.setPrice(edtPrice.getText().toString());
                                    newHookahItem.setDiscount(edtDiscount.getText().toString());
                                    newHookahItem.setMenuid(categoryId);
                                    newHookahItem.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(HookahListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show()    ;
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



    //********************************** Update/Delete Hookah Items *****************************************

    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if (item.getTitle().equals(Common.DELETE)){

            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }


    private void showUpdateDialog(final String key, final Hookah item) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(HookahListActivity.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill in the Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_hookah_item_layout = inflater.inflate(R.layout.add_new_hookah_item_layout, null);

        edtName = add_hookah_item_layout.findViewById(R.id.edtName);
        edtDescription = add_hookah_item_layout.findViewById(R.id.edtDescription);
        edtPrice = add_hookah_item_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_hookah_item_layout.findViewById(R.id.edtDiscount);

        btnSelect = add_hookah_item_layout.findViewById(R.id.btnSelect);
        btnUpload = add_hookah_item_layout.findViewById(R.id.btnUpload);

        gallery_image = add_hookah_item_layout.findViewById(R.id.gallery_image);


        //Set default names
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());

        Picasso.with(this)
                .load(item.getImage())
                .into(gallery_image);


        alertDialog.setView(add_hookah_item_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

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
                item.setName(edtName.getText().toString());
                item.setDescription(edtDescription.getText().toString());
                item.setPrice(edtPrice.getText().toString());
                item.setDiscount(edtDiscount.getText().toString());

                hookahListRef.child(key).setValue(item);

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

    private void changeImage(final Hookah item) {

        if (saveUri != null){

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imgName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(HookahListActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(HookahListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show()    ;
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

    private void deleteCategory(String key) {

        hookahListRef.child(key).removeValue();

    }



    //********************************** Loading Hookah List Items *****************************************

    private void getHookahIntent() {

        if (getIntent() != null){

            categoryId = getIntent().getStringExtra("CategoryKey");

            if (categoryId != null){

                loadListHookah(categoryId);
            }

        }
    }

    private void loadListHookah(String categoryId) {

        Query listHookahByCategoryId = hookahListRef.orderByChild("menuid").equalTo(categoryId);

        FirebaseRecyclerOptions<Hookah> options = new FirebaseRecyclerOptions.Builder<Hookah>()
                .setQuery(listHookahByCategoryId, Hookah.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Hookah, HookahViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HookahViewHolder viewHolder, int position, @NonNull Hookah model) {

                viewHolder.hookahName.setText(model.getName());

                Picasso.with(getApplicationContext())
                        .load(model.getImage())
                        .into(viewHolder.hookahImage);


                viewHolder.setItemClickListener(new RecycleViewItemClickListener() {
                    @Override
                    public void onClick(View view, int postion, boolean isLongClick) {

//                        Intent intent = new Intent(HookahListActivity.this, HookahDetailActivity.class);
//                        intent.putExtra("hookahId", adapter.getRef(position).getKey());
//                        startActivity(intent);

                    }
                });
            }

            @NonNull
            @Override
            public HookahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.hookah_list_item,parent,false);
                return new HookahViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }



}


