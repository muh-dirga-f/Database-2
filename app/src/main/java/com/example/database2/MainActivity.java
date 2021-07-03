package com.example.database2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ARTIST_ID = "artistid";
    public static final String ARTIST_NAME = "artistname";

    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerGenres;
    DatabaseReference databaseArtists;
    ListView listViewArtists;
    List<Artist> artists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseArtists = FirebaseDatabase.getInstance().getReference("artists_nya");
        editTextName = findViewById(R.id.editTextName);
        buttonAdd = findViewById(R.id.buttonAddArtist);
        spinnerGenres = findViewById(R.id.spinnerGenres);
        listViewArtists = findViewById(R.id.listViewArtists);
        artists = new ArrayList<>();

        buttonAdd.setOnClickListener(v -> addArtist());
        listViewArtists.setOnItemClickListener((adapterView, view, i, l) -> {
            Artist artist = artists.get(i);
            Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);
            intent.putExtra(ARTIST_ID, artist.getArtistId());
            intent.putExtra(ARTIST_NAME, artist.getArtistName());
            startActivity(intent);
        });

        listViewArtists.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Artist artist = artists.get(i);
            showUpdateDialog(artist.getArtistId(), artist.getArtistName());
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                artists.clear();
                for (DataSnapshot artistSnapshot : dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);
                    artists.add(artist);
                }
                ArtistList adapter = new ArtistList(MainActivity.this, artists);
                listViewArtists.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void showUpdateDialog(final String artistId, String artistName){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final Spinner spinnerGenres = dialogView.findViewById(R.id.spinnerGenres);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Update Artist " + artistName);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String genre = spinnerGenres.getSelectedItem().toString();

            if (TextUtils.isEmpty(name)){
                editTextName.setError("Name required");
                return;
            }
            updateArtist(artistId, name, genre);
            alertDialog.dismiss();
        });

        buttonDelete.setOnClickListener(v -> deleteArtist(artistId));
    }

    private void deleteArtist(String artistId){
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artists_nya").child(artistId);
        DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks_nya").child(artistId);
        drArtist.removeValue();
        drTracks.removeValue();
        Toast.makeText(this, "Artist is deleted", Toast.LENGTH_LONG).show();
    }

    private void updateArtist(String id, String name, String genre){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artists_nya").child(id);
        Artist artist = new Artist(id, name, genre);
        databaseReference.setValue(artist);
        Toast.makeText(this, "Artist Update Successfully", Toast.LENGTH_LONG).show();
    }

    private void addArtist() {
        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenres.getSelectedItem().toString();

        if(!TextUtils.isEmpty(name)){
            String id = databaseArtists.push().getKey();
            Artist artist = new Artist(id, name, genre);

            assert id != null;
            databaseArtists.child(id).setValue(artist);
            Toast.makeText(this,"Artist added", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"You should enter a name", Toast.LENGTH_LONG).show();
        }
    }
}