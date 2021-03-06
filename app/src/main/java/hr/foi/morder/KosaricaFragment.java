package hr.foi.morder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hr.foi.morder.adapters.KosaricaAdapter;
import hr.foi.morder.model.Narudzba;
import hr.foi.morder.model.Racun;
import hr.foi.morder.model.StavkaNarudzbe;
import hr.foi.morder.model.Stol;

public class KosaricaFragment extends Fragment {
    private View view;
    public KosaricaAdapter kosaricaAdapter;
    public List<StavkaNarudzbe> stavkaNarudzbeList;
    public Integer brojNarudzbe = 0;
    public Double ukupnaCijena = 0.0;
    private RecyclerView recyclerView;
    private Button naruci;
    private FirebaseFirestore database;
    private String idNarudzbe = "", racunDokument, stolDokument;
    private Integer stolId = 0;
    private Integer racunId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_kosarica,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stavkaNarudzbeList = new ArrayList<>();
        buildRecyclerView();
        database = FirebaseFirestore.getInstance();
        //naruci = view.findViewById(R.id.buttonNaruci);
        dohvatiNarudzbu();

        //naruci.setOnClickListener(new View.OnClickListener() {
         //   @Override
           // public void onClick(View v) {
                //stanjeStola();
          //  }
        //});
    }

    public void stanjeStola() {
        if (stolId == 0) {
            dovrsiNarudzbu();
        } else {
            nastaviNarudzbu();
        }
    }

    public void addRacun(Integer id, Integer stol) {
        Map<String, Object> idRacuna = new Racun(id, stol).toMap();
        database.collection("Racun")
                .add(idRacuna)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                    }
                });
    }

    //Dohvat zadnjeg id narudzbe, pod tim id se spremaju artikli koji su u kosarici
    private void dohvatiNarudzbu() {
        database.collection("Narudzba").orderBy("id", Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                idNarudzbe = documentSnapshot.getId();
                                Narudzba narudzba = documentSnapshot.toObject(Narudzba.class);
                                brojNarudzbe = narudzba.getId();
                                racunId = narudzba.getRacun_id();
                                dohvatiStolID();
                                dohvatiStavkeKosarice();
                            }
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    //Dohvaćanje svih stavki koje je korisnik odabrao, spremaju se prvo na firestore te se onda dobavljaju i prikazuju, nisu vezani za narudzbu
    private void dohvatiStavkeKosarice() {
        database.collection("Stavka narudzbe").whereEqualTo("narudzba_id", brojNarudzbe).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<StavkaNarudzbe> stavkaNarudzbeList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                StavkaNarudzbe stavkaNarudzbe = documentSnapshot.toObject(StavkaNarudzbe.class);
                                stavkaNarudzbe.getArtikl_id();
                                stavkaNarudzbe.getCijena();
                                stavkaNarudzbe.getKolicina();
                                stavkaNarudzbeList.add(stavkaNarudzbe);
                                ukupnaCijena = ukupnaCijena + stavkaNarudzbe.getCijena();
                            }
                            kosaricaAdapter = new KosaricaAdapter(getContext(), stavkaNarudzbeList, database);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((getContext()));
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(kosaricaAdapter);
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    private void dohvatiStolID() {
        database.collection("Racun").whereEqualTo("id", racunId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Racun racun = documentSnapshot.toObject(Racun.class);
                                stolId = racun.getStol_id();
                            }
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    //Narudžba koja će sadržavati elemente košarice
    public void dovrsiNarudzbu() {
        database.collection("Stol").whereEqualTo("stanje", "slobodan").limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Stol stol = documentSnapshot.toObject(Stol.class);
                                stolDokument = documentSnapshot.getId();
                                stolId = stol.getId();
                                stol.getStanje();
                            }
                            database.collection("Stol").document(stolDokument).update("narudzba_id", brojNarudzbe);
                            database.collection("Stol").document(stolDokument).update("stanje", "zauzet");

                            database.collection("Racun").orderBy("id", Query.Direction.DESCENDING).limit(1)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                    Racun racun = documentSnapshot.toObject(Racun.class);
                                                    racunId = racun.getId();
                                                    racunDokument = documentSnapshot.getId();
                                                }
                                                addRacun(racunId + 1, stolId);
                                                database.collection("Narudzba").document(idNarudzbe).update("racun_id", racunId + 1);
                                            } else {
                                                Log.d("Error", "Error getting data");
                                            }
                                            Intent intent = new Intent(getContext(), NarucivanjeActivity.class);
                                            intent.putExtra("racunID", racunId + 1);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }
                });
    }

    public void nastaviNarudzbu() {
        database.collection("Racun").orderBy("id", Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Racun racun = documentSnapshot.toObject(Racun.class);
                                racunId = racun.getId();
                            }
                            database.collection("Narudzba").document(idNarudzbe).update("racun_id", racunId);
                            Intent intent = new Intent(getContext(), NarucivanjeActivity.class);
                            intent.putExtra("racunID", racunId + 1);
                            startActivity(intent);
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    public void dovrsiNarudzbuDostave() {
        database.collection("Racun").orderBy("id", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Racun racun = documentSnapshot.toObject(Racun.class);
                                racunId = racun.getId();
                            }
                            Long generiraniKod = (long) new Random().nextInt(999999)+100000;
                            addRacunDostave(racunId + 1, "dostava", generiraniKod);
                            database.collection("Narudzba").document(idNarudzbe).update("racun_id", racunId + 1);

                        } else {
                            Log.d("Error", "Error getting data");
                        }
                        Intent intent = new Intent(getContext(), NarucivanjeDostavaActivity.class);
                        intent.putExtra("racunID", racunId + 1);
                        startActivity(intent);
                    }
                });
    }

    public void addRacunDostave(Integer id, String status, Long kod) {
        Map<String, Object> idRacuna = new Racun(id, status, kod).toMap();
        database.collection("Racun")
                .add(idRacuna)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                    }
                });
    }

    //Recycler view za prikaz elemenata košarice
    public void buildRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerViewKosarica);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(kosaricaAdapter);
    }
}
