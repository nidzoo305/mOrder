package hr.foi.morder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import hr.foi.morder.adapters.ListAdapter;
import hr.foi.morder.model.Artikl;
import hr.foi.morder.model.Racun;

public class DetaljiNarudzbeActivity extends AppCompatActivity
{
    ArrayList<Artikl> listaArtikala = new ArrayList<>();
    private FirebaseFirestore database;
    Button btnPlaceOrder;
    private ListView listView;
    private ListAdapter listAdapter;
    int stolID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalji_narudzbe2);
        Intent intent = getIntent();
        stolID = Integer.parseInt(intent.getStringExtra("stolID"));
        getArticles();

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    public void getArticles(){
        database = FirebaseFirestore.getInstance();
        database.collection("Racun")
                .whereEqualTo("stol_id", stolID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Racun racun = documentSnapshot.toObject(Racun.class);
                              //  Artikl artikl = racun.getStol().getNarudzba().getArtikl();
                               // listaArtikala.add(artikl);
                            }
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                        listView = findViewById(R.id.customListView);
                        listAdapter = new ListAdapter(getApplicationContext(), listaArtikala);
                        listView.setAdapter(listAdapter);
                    }
                });
    }
}