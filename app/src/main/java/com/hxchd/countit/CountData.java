package com.hxchd.countit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by KevinKW on 2015-12-16.
 */
public class CountData extends SQLiteOpenHelper {
    private static final String LOG_NAME = "CountData";
    private static final int dbVersion = 1;
    private static final String dbName = "CountData.getWritableDatabase()";
    private static final String ztTableName = "ZT";
    private static final String gameTableName = "GAME";
    private static final String roundTableName = "ROUND";
//    private Context context;

    public CountData(Context context) {
        super(context, dbName, null, dbVersion);
//        this.context = context;
    }

    private void cleanData() {
        cleanRound();
        cleanGame();
    }

    public void createTables(SQLiteDatabase db) {
        String createZtTable = "CREATE TABLE " + ztTableName +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " name VARCHAR NOT NULL)";
        String createGameTable = "CREATE TABLE " + gameTableName +
                "(zt_id INTEGER NOT NULL," +
                " win INTEGER NOT NULL," +
                " loss INTEGER NOT NULL," +
                " draw INTEGER NOT NULL," +
                " result INTEGER NOT NULL)";
        String createRoundTable = "CREATE TABLE " + roundTableName +
                "(id INTEGER NOT NULL," +
                " zt_id INTEGER NOT NULL," +
                " delta INTEGER NOT NULL)";
        db.execSQL(createZtTable);
        db.execSQL(createGameTable);
        db.execSQL(createRoundTable);
    }

    public void dropTables() {
        String dropZtTable = "DROP TABLE " + ztTableName;
        String dropGameTable = "DROP TABLE " + gameTableName;
        String dropRoundTable = "DROP TABLE " + roundTableName;
        getWritableDatabase().execSQL(dropZtTable);
        getWritableDatabase().execSQL(dropGameTable);
        getWritableDatabase().execSQL(dropRoundTable);
    }

    public void cleanGame() {
        getWritableDatabase().delete(gameTableName, null, null);
    }

    public void cleanZt() {
        getWritableDatabase().delete(ztTableName, null, null);
    }

    public void cleanRound() {
        getWritableDatabase().delete(roundTableName, null, null);
    }

    public Zt addZt(String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        getWritableDatabase().insert(ztTableName, null, values);
        Cursor cursor = getWritableDatabase().rawQuery("SELECT LAST_INSERT_ROWID() FROM " + ztTableName, null);
        int lastId = 0;
        if ((cursor != null) && cursor.moveToFirst()) {
            lastId = cursor.getInt(0);
        }
        return new Zt(lastId, name);
    }

    public void delZt(Zt zt) {
        getWritableDatabase().delete(ztTableName, "id = ?", new String[]{String.valueOf(zt.id)});
    }

    public void addRound(int id, int zt_id, int delta) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("zt_id", zt_id);
        values.put("delta", delta);
        getWritableDatabase().insert(roundTableName, null, values);
    }

    public void begin() {
        getWritableDatabase().beginTransaction();
    }

    public void commit() {
        getWritableDatabase().endTransaction();
    }

    public void setTransOK() {
        getWritableDatabase().setTransactionSuccessful();
    }

    public void addGameZt(Zt zt) {
        ContentValues values = new ContentValues();
        values.put("zt_id", zt.id);
        values.put("win", 0);
        values.put("loss", 0);
        values.put("draw", 0);
        values.put("result", 0);
        getWritableDatabase().insert(gameTableName, null, values);
    }

    public void updateGame(Zt zt) {
        ContentValues values = new ContentValues();
        values.put("win", zt.win);
        values.put("loss", zt.loss);
        values.put("draw", zt.draw);
        values.put("result", zt.result);
        getWritableDatabase().update(gameTableName, values, "zt_id = ?", new String[]{String.valueOf(zt.id)});
    }

    public void startGame(ArrayList<Zt> zts) {
        cleanRound();
        cleanGame();
        for (Zt zt:zts) {
            addGameZt(zt);
        }
    }

    public void loadZts(ArrayList<Zt> zts) {
        String[] params = {"id", "name"};
        Cursor cursor = getWritableDatabase().query(ztTableName, params, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Zt zt = new Zt(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")));
            Log.d(LOG_NAME, "Load ZT: " + zt);

            zts.add(zt);
        }
    }

    public void loadGame(ArrayList<Zt> zts, ArrayList<Zt> game_zts) {
        String[] params = {"zt_id", "win", "loss", "draw", "result"};
        Cursor cursor = getWritableDatabase().query(gameTableName, params, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int zt_id = cursor.getInt(cursor.getColumnIndex("zt_id"));
            for (Zt zt: zts) {
                if (zt.id == zt_id) {
                    zt.win = cursor.getInt(cursor.getColumnIndex("win"));
                    zt.loss = cursor.getInt(cursor.getColumnIndex("loss"));
                    zt.draw = cursor.getInt(cursor.getColumnIndex("draw"));
                    zt.result = cursor.getInt(cursor.getColumnIndex("result"));
                    game_zts.add(zt);
                    break;
                }
            }
        }
    }

    public int loadRound() {
        String sql = "SELECT ID FROM " + roundTableName + " ORDER BY ID DESC LIMIT 1;";
        int round = 0;
        Cursor cursor = getWritableDatabase().rawQuery(sql + roundTableName, null);
        if ((cursor != null) && cursor.moveToFirst()) {
            round = cursor.getInt(0);
        }
        Log.d(LOG_NAME, "Load Round: " + round);
        return round;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}