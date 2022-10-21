package com.tingtingman.groceryapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.tingtingman.groceryapp.R;
import com.tingtingman.groceryapp.adapters.AdapterPromotionShop;
import com.tingtingman.groceryapp.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    private ImageButton backBtn, addPromoBtn, filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        addPromoBtn = findViewById(R.id.addPromoBtn);
        filteredTv = findViewById(R.id.filteredTv);
        filterBtn = findViewById(R.id.filterBtn);
        promoRv = findViewById(R.id.promoRv);

        //init firebase auth to get current user
        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromoCodes();

        //handle click, go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //handle click, open add promo code activity
        addPromoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PromotionCodesActivity.this, AddPromotionCodeActivity.class));
            }
        });

        //handle filter button click, show filter dialog
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialog();
            }
        });
    }

    private void filterDialog() {
        //options to display in dialog
        String[] options = {"All", "Expired", "Not Expired"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle item clicks
                        if (i==0){
                            //All clicked
                            filteredTv.setText("All Promotion Codes");
                            loadAllPromoCodes();
                        }
                        else  if (i==1){
                            //Expired clicked
                            filteredTv.setText("Expired Promotion Codes");
                            loadExpiredPromoCodes();
                        }
                        else if (i==2){
                            //Not Expired clicked
                            filteredTv.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromoCodes();
                        }
                    }
                })
                .show();
    }

    private void loadAllPromoCodes(){
        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadExpiredPromoCodes(){
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day =  calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day +"/"+ month +"/"+ year; //e.g. 29/06/2020

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            /*--------Check for expired-------*/
                            try {
                                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);
                                Date expireDate = sdformat.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0){
                                    //date 1 occurs after date 2
                                }
                                else if (expireDate.compareTo(currentDate) < 0){
                                    //date 1 occurs before date 2 (i.e. Expired)
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) == 0){
                                    //both date equals
                                }
                            }
                            catch (Exception e){

                            }


                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNotExpiredPromoCodes(){
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day =  calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate = day +"/"+ month +"/"+ year; //e.g. 29/06/2020

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            /*--------Check for expired-------*/
                            try {
                                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);
                                Date expireDate = sdformat.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0){
                                    //date 1 occurs after date 2
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) < 0){
                                    //date 1 occurs before date 2 (i.e. Expired)
                                }
                                else if (expireDate.compareTo(currentDate) == 0){
                                    //both date equals
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                            }
                            catch (Exception e){

                            }


                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}