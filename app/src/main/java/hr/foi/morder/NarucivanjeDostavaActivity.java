package hr.foi.morder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.foi.morder.adapters.ArticleRecyclerAdapter;
import hr.foi.morder.adapters.ExpendableListAdapter;
import hr.foi.morder.model.Artikl;
import hr.foi.morder.model.Kategorija;
import hr.foi.morder.model.Narudzba;

/**
 * The type Narucivanje dostava activity.
 */
public class NarucivanjeDostavaActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigation;
    private TextView textViewNovoUPonudi;
    private RecyclerView recyclerView;
    private FirebaseFirestore database;
    private ArticleRecyclerAdapter adapter;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> listHeader;
    private HashMap<String, List<String>> listChild;
    private HashMap<String, List<String>> listChildEx;
    private Long childId;
    private Integer idNarudzba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        drawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        expandableListView = findViewById(R.id.navigationmenu);
        navigation = findViewById(R.id.nv);
        setupDrawerContent(navigation);

        textViewNovoUPonudi = findViewById(R.id.naslovNovoUPonudi);

        recyclerView = findViewById(R.id.article_recycler);
        database = FirebaseFirestore.getInstance();

        loadLastArticles();
        dohvatiKategorije();
        dohvatiIdNarudzbe();
    }

    /*
     * Sets the page title about new articles.
     */
    private void setHomePageHeaderText() {
        textViewNovoUPonudi.setText(R.string.novo_u_ponudi);
    }

    /*
     * Removes title about new articles.
     */
    private void removeHomePageHeaderText() {
        textViewNovoUPonudi.setText("");
    }

    /*
     * Returns order ID from last order as a query result.
     */
    private void dohvatiIdNarudzbe() {
        database.collection("Narudzba").orderBy("id", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<Narudzba> narudzbaList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Narudzba narudzba = documentSnapshot.toObject(Narudzba.class);
                                narudzba.getId();
                                narudzbaList.add(narudzba);
                            }

                            for (Narudzba n : narudzbaList) {
                                idNarudzba = n.getId();
                            }
                            addIdNarudzba(idNarudzba + 1, 0.00, "dostava");


                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }


    /**
     * Add id narudzba.
     *
     * @param id     the id
     * @param cijena the cijena
     * @param status  the racun
     */

    public void addIdNarudzba(Integer id, Double cijena, String status) {
        Map<String, Object> idNarudzbe = new Narudzba(id, cijena, status).toMap();
        database.collection("Narudzba")
                .add(idNarudzbe)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                    }
                });
    }
    /*
     * Get categories from database.
     */
    private void dohvatiKategorije() {
        listChildEx = new HashMap<>();
        database.collection("Kategorija")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> kategorijaList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Kategorija kategorija = documentSnapshot.toObject(Kategorija.class);
                                kategorijaList.add(kategorija.getNaziv());
                            }
                            listChildEx.put("Jelovnik", kategorijaList);
                            listChild = listChildEx;
                            listHeader = new ArrayList<>(listChild.keySet());
                            expandableListAdapter = new ExpendableListAdapter(getApplicationContext(), listHeader, listChild);
                            expandableListView.setAdapter(expandableListAdapter);
                            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                @Override
                                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                    childId = parent.getExpandableListAdapter().getChildId(groupPosition, childPosition) + 1;
                                    v.setSelected(true);
                                    loadArticleList(childId);
                                    drawer.closeDrawers();
                                    return false;
                                }
                            });
                            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                                                                           @Override
                                                                           public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                                                                               return false;
                                                                           }
                                                                       }
                            );
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    /*
     * Loads list of articles grouped by category.
     */
    private void loadArticleList(long idKategorije) {
        database.collection("Artikl")
                .whereEqualTo("kategorija_id", idKategorije)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Artikl> articlesList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Artikl artikl = documentSnapshot.toObject(Artikl.class);
                                articlesList.add(artikl);
                            }
                            adapter = new ArticleRecyclerAdapter(articlesList, getApplicationContext(), database);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                        removeHomePageHeaderText();
                    }
                });
    }

    /*
     * Loads the last article as query result.
     */
    private void loadLastArticles() {
        database.collection("Artikl").orderBy("id", Query.Direction.DESCENDING).limit(3)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Artikl> articlesList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Artikl artikl = documentSnapshot.toObject(Artikl.class);
                                articlesList.add(artikl);
                            }
                            setHomePageHeaderText();
                            adapter = new ArticleRecyclerAdapter(articlesList, getApplicationContext(), database);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    private void setupDrawerContent(NavigationView nv) {
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerView(item);
                return true;
            }
        });
    }

    private void selectDrawerView(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.kosarica:
                Intent intent = new Intent(this, KosaricaDostavaActivity.class);
                startActivity(intent);
                break;

            case R.id.pocetna:
                Intent intent1 = new Intent(this, MainActivity.class);
                startActivity(intent1);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}