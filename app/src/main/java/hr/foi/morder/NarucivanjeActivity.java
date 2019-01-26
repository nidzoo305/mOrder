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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hr.foi.morder.adapters.ArticleRecyclerAdapter;
import hr.foi.morder.adapters.ExpendableListAdapter;
import hr.foi.morder.model.Artikl;
import hr.foi.morder.model.Kategorija;


public class NarucivanjeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigation;
    private RecyclerView recyclerView;
    private FirebaseFirestore database;
    private ArticleRecyclerAdapter adapter;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> listHeader;
    private HashMap<String, List<String>> listChild;
    private HashMap<String, List<String>> listChildEx;
    private Long childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        drawer = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        expandableListView = findViewById(R.id.navigationmenu);
        navigation = findViewById(R.id.nv);
        setupDrawerContent(navigation);
        navigation.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.article_recycler);
        database = FirebaseFirestore.getInstance();
        dohvatiKategorije();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.kosarica:
                Intent intent = new Intent(this, KosaricaActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void dohvatiKategorije(){
        listChildEx = new HashMap<>();
        database.collection("Kategorija")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<String> kategorijaList = new ArrayList<>();
                            for(DocumentSnapshot documentSnapshot: task.getResult()){
                                Kategorija kategorija = documentSnapshot.toObject(Kategorija.class);
                                kategorijaList.add(kategorija.getNaziv());
                            }
                            listChildEx.put("Jelovnik", kategorijaList);
                            listChild = listChildEx;
                            listHeader = new ArrayList<String>(listChild.keySet());
                            expandableListAdapter = new ExpendableListAdapter(getApplicationContext(), listHeader, listChild);
                            expandableListView.setAdapter(expandableListAdapter);
                            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                @Override
                                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                    childId = parent.getExpandableListAdapter().getChildId(groupPosition, childPosition)+1;
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
                            });
                        }
                        else{
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    private void loadArticleList(long idKategorije) {
        database.collection("Artikl")
                .whereEqualTo("kategorija_id", idKategorije)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<Artikl> articlesList = new ArrayList<>();
                            for(DocumentSnapshot documentSnapshot: task.getResult()){
                                Artikl artikl = documentSnapshot.toObject(Artikl.class);
                                articlesList.add(artikl);
                            }

                            adapter = new ArticleRecyclerAdapter(articlesList, getApplicationContext(), database);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        }
                        else{
                            Log.d("Error", "Error getting data");
                        }
                    }
                });
    }

    private void setupDrawerContent(NavigationView nv){
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerView(item);
                return true;
            }
        });
    }

    private void selectDrawerView(MenuItem item){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
