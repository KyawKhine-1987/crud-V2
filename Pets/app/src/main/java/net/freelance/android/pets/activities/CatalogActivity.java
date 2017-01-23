package net.freelance.android.pets.activities;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.freelance.android.pets.R;
import net.freelance.android.pets.adapter.PetCursorAdapter;
import net.freelance.android.pets.data.PetContract.PetEntry;

/*** Displays list of pets that were entered and stored in the app.*/
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PET_LOADER = 0;/** Identifier for the pet data loader */
    PetCursorAdapter mPetCursorAdapter;/** Adapter for the ListView */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST : onCreate() called...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent i = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(i);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.rl_empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mPetCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent i = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);//content://net.freelance.android.pets/pets/2
                i.setData(currentPetUri);// Set the URI on the data field of the intent
                startActivity(i);// Launch the {@link EditorActivity} to display the data for the current pet.
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOG_TAG, "TEST : onCreateOptionsMenu() called...");
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "TEST : onOptionsItemSelected() called...");
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:// Respond to a click on the "Insert dummy data" menu option
                insertPet();
                return true;// Do nothing for now

            case R.id.action_delete_all_entries:// Respond to a click on the "Delete all entries" menu option
                deleteAllPets();// Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //When u used to LoaderManager.LoaderCallbacks<Cursor>, u must do implement this beneath three methods.
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "TEST : onCreateLoader() called...");
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry.COLUMN_PET_ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,     // Parent activity context
                PetEntry.CONTENT_URI,     // Provider content URI to query
                projection,               // Columns to include in the resulting Cursor
                null,                     // No selection clause
                null,                     // No selection arguments
                null);                    // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "TEST : onLoadFinished() called...");
        mPetCursorAdapter.swapCursor(data);// Update {@link PetCursorAdapter} with this new cursor containing updated pet data
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "TEST : onLoaderReset() called...");
        mPetCursorAdapter.swapCursor(null);// Callback called when the data needs to be deleted
    }

    private void insertPet() {
        Log.i(LOG_TAG, "TEST : insertPet() called...");
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues contentValues = new ContentValues();
        contentValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        contentValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        contentValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, contentValues);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        Log.i(LOG_TAG, "TEST : deleteAllPets() called...");
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, " TEST : " + rowsDeleted + " rows deleted from pet database");
    }
}

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST : onCreate() called...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               *//* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
                Intent i = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(i);
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper and pass the context, which is the current activity.
//        mPetDBHelper = new PetDBHelper(this);

        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

//        View emptyView = findViewById(R.id.empty_view);
//        petListView.setEmptyView(emptyView);

        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mPetCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);//content://net.freelance.android.pets/pets/2
                i.setData(currentPetUri);
                startActivity(i);
            }
        });

        getLoaderManager().initLoader(PET_LOADER, null, this);
    }*/

    /*@Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }*/

    /*private void displayDatabaseInfo() {
        Log.i(LOG_TAG, "TEST : displayDatabaseInfo() called...");

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PetEntry.COLUMN_PET_ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        // Perform a query on the provider using the ContentProvider.
        // Use the {@link PetEntry#CONTENT_URI} to access the pet data.
        Cursor c = getContentResolver().query(
                PetEntry.CONTENT_URI,  //The content URI
                projection,            //The columns to return for each row
                null,                  //Selection Criteria
                null,                  //Selection Criteria
                null);                 //The sort order for returned row

        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        PetCursorAdapter petCursorAdapter = new PetCursorAdapter(this, c);

        petListView.setAdapter(petCursorAdapter);
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "TEST : onOptionsItemSelected() called...");
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:// Respond to a click on the "Insert dummy data" menu option
                insertPet();
               *//* displayDatabaseInfo();*//*
                return true;// Do nothing for now

            case R.id.action_delete_all_entries:// Respond to a click on the "Delete all entries" menu option
                return true;// Do nothing for now
        }
        return super.onOptionsItemSelected(item);
    }*/

 /**
 * Temporary helper method to display information in the onscreen TextView about the state of
 * the pets database.
 */
   /* private void displayDatabaseInfo() {
        Log.i(LOG_TAG, "TEST : displayDatabaseInfo() called...");
       /* Create and/or open a database to read from it
        SQLiteDatabase db = mPetDBHelper.getReadableDatabase();/*

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PetEntry.COLUMN_PET_ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        // Perform a query on the pets table
//        Cursor c = db.query(
//                PetEntry.TABLE_NAME,  // The table to query
//                projection,           // The columns to return
//                null,                 // The columns for the WHERE clause
//                null,                 // The values for the WHERE clause
//                null,                 // Don't group the rows
//                null,                 // Don't filter by row groups
//                null);                // The sort order

        Cursor c = getContentResolver().query(
                PetEntry.CONTENT_URI,  //The content URI
                projection,            //The columns to return for each row
                null,                  //Selection Criteria
                null,                  //Selection Criteria
                null);                 //The sort order for returned row

        TextView displayView = (TextView) findViewById(R.id.text_view_pet);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The pets table contains <number of rows in Cursor> pets.
            // _id - name - breed - gender - weight
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The pets table contains " + c.getCount() + " pets.\n\n");// Display on screen how many rows on there?
            displayView.append(PetEntry.COLUMN_PET_ID + " - " +
                    PetEntry.COLUMN_PET_NAME + " - " +
                    PetEntry.COLUMN_PET_NAME + " - " +
                    PetEntry.COLUMN_PET_BREED + " - " +
                    PetEntry.COLUMN_PET_GENDER + " - " +
                    PetEntry.COLUMN_PET_WEIGHT + "\n");// Display on screen by each columns.

            // Figure out the index of each column
            int idColumnIndex = c.getColumnIndex(PetEntry.COLUMN_PET_ID);
            int nameColumnIndex = c.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = c.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = c.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = c.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Iterate through all the returned rows in the cursor
            while (c.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = c.getInt(idColumnIndex);
                String currentName = c.getString(nameColumnIndex);
                String currentBreed = c.getString(breedColumnIndex);
                int currentGender = c.getInt(genderColumnIndex);
                int currentWeight = c.getInt(weightColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentBreed + " - " +
                        currentGender + " - " +
                        currentWeight));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            c.close();
        }
    }*/
   /* private void displayDatabaseInfo() {
        Log.i(LOG_TAG, "TEST : displayDatabaseInfo() called...");

        //Create and/or open a database to read from it.
        SQLiteDatabase db = mPetDBHelper.getReadableDatabase();

        //Perform this raw SQL Query "SELECT * FROM pets" to get a Cursor that contains all rows from the pets table.
        Cursor c = db.rawQuery("SELECT * FROM " + PetContract.PetEntry.TABLE_NAME + " NOLOCK;", null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table : " + c.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its resources and makes it invalid.
            c.close();
        }
    }*/
