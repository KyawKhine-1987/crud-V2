package net.freelance.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.freelance.android.pets.data.PetContract.PetEntry;

/**
 * Created by Kyaw Khine on 01/17/2017.
 */

public class PetDBHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDBHelper.class.getSimpleName();

    /*Name of the Database file.*/
    private static final String DATABASE_NAME = "shelter.db";

    /*Database Version. If you change the database schema, you must increment the database version.*/
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link PetDBHelper}.
     *
     * @param context of the app
     */
    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(LOG_TAG, "TEST : PetDBHelper() called...");
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "TEST : onCreate() called...");
        // Create a String that contains the SQL statement to create the pets table
        String sql_create_pet_table = "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
                PetEntry.COLUMN_PET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, " +
                PetEntry.COLUMN_PET_BREED + " TEXT, " +
                PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, " +
                PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(sql_create_pet_table);
    }

    /*** This is called when the database needs to be upgraded.*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LOG_TAG, "TEST : onUpgrade() called...");
        // The database is still at version 1, so there's nothing to do be done here.
        /*String sql_delete_pet_table =  "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;
        db.execSQL(sql_delete_pet_table);
        onCreate(db);*/
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LOG_TAG, "TEST : onDowngrade() called...");
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
