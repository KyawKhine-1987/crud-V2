package net.freelance.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import net.freelance.android.pets.data.PetContract.PetEntry;

/**
 * Created by Kyaw Khine on 01/19/2017.
 */

public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();/** Tag for the log messages */

    private static final int PETS = 100;/** URI matcher code for the content URI for the pets table */

    private static final int PET_ID = 101;/** URI matcher code for the content URI for a single pet in the pets table */

    /*** UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.*/
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDBHelper mPetDBHelper;//Database helper object.

    /*** Initialize the provider and the database helper object.*/
    @Override
    public boolean onCreate() {
        Log.i(LOG_TAG, "TEST : onCreate() called...");
        mPetDBHelper = new PetDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(LOG_TAG, "TEST : query() called...");
        SQLiteDatabase db = mPetDBHelper.getReadableDatabase();// Get readable database

        Cursor c;// This cursor will hold the result of the query

        int match = sUriMatcher.match(uri);// Figure out if the URI matcher can match the URI to a specific code

        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                c = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://net.freelance.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry.COLUMN_PET_ID + "=?";
                selectionArgs = new String[]{ContentUris.parseId(uri) + ""};
                //selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                c = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can't query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c; // Return the cursor
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.i(LOG_TAG, "TEST : getType() called...");

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.i(LOG_TAG, "TEST : insert() called...");

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion isn't supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues contentValues) {
        // Check that the name is not null
        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name!");
        }

        // Check that the gender is valid
        Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender!");
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight!");
        }

        SQLiteDatabase db = mPetDBHelper.getWritableDatabase();

        long id = db.insert(PetEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "TEST : Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);// uri: content://net.freelance.android.pets/pets

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "TEST : delete() called...");

        //Get writeable database
        SQLiteDatabase db = mPetDBHelper.getWritableDatabase();

        int rowsDeleted;// Track the number of rows that were deleted

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                //return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry.COLUMN_PET_ID + "=?";
                selectionArgs = new String[]{ContentUris.parseId(uri) + ""};
                //return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion isn't supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;// Return the number of rows deleted
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "TEST : update() called...");

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry.COLUMN_PET_ID + "=?";
                selectionArgs = new String[]{ContentUris.parseId(uri) + ""};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update isn't supported for " + uri);
        }
    }

    /**** Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.*/
    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present, check that the name value is not null.
        if (contentValues.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name!");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present, check that the gender value is valid.
        if (contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender!");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present, check that the gender value is valid.
        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            // Check that the weight is greater than or equal to 0 kg
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight!");
            }
        }

        // No need to check the breed, any value is valid (including null).
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase db = mPetDBHelper.getWritableDatabase();
        // Returns the number of database rows affected by the update statement
        //return db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        int rowsUpdated = db.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;// Return the number of rows updated
    }
}
