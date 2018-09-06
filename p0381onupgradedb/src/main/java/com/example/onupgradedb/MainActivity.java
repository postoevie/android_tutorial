package com.example.onupgradedb;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

   final String myLogs = "MY_LOGS";
    String[] people_name = { "Иван", "Марья", "Петр", "Антон", "Даша",
            "Борис", "Костя", "Игорь" };
    String[] people_positions = { "Программер", "Бухгалтер",
            "Программер", "Программер", "Бухгалтер", "Директор",
            "Программер", "Охранник" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DBHelper dbh  = new DBHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();
        writeStaff(db);
        Log.d(myLogs, String.valueOf(db.getVersion()));

    }

    class DBHelper extends SQLiteOpenHelper{

        DBHelper(Context context){
            super(context,"MyDB", null, 3);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(myLogs, " --- onCreate database --- ");

            String[] people_name = { "Иван", "Марья", "Петр", "Антон", "Даша",
                    "Борис", "Костя", "Игорь" };
            int[] people_posid = { 2, 3, 2, 2, 3, 1, 2, 4 };

            // данные для таблицы должностей
            int[] position_id = { 1, 2, 3, 4 };
            String[] position_name = { "Директор", "Программер", "Бухгалтер",
                    "Охранник" };
            int[] position_salary = { 15000, 13000, 10000, 8000 };

            ContentValues cv = new ContentValues();

            // создаем таблицу должностей
            db.execSQL("create table position (" + "id integer primary key,"
                    + "name text, salary integer" + ");");

            // заполняем ее
            for (int i = 0; i < position_id.length; i++) {
                cv.clear();
                cv.put("id", position_id[i]);
                cv.put("name", position_name[i]);
                cv.put("salary", position_salary[i]);
                db.insert("position", null, cv);
            }

            // создаем таблицу людей
            db.execSQL("create table people ("
                    + "id integer primary key autoincrement,"
                    + "name text, posid integer);");

            // заполняем ее
            for (int i = 0; i < people_name.length; i++) {
                cv.clear();
                cv.put("name", people_name[i]);
                cv.put("posid", people_posid[i]);
                db.insert("people", null, cv);
            }
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(myLogs, " --- onUpgrade database from " + oldVersion
                    + " to " + newVersion + " version --- ");



                ContentValues cv = new ContentValues();

                // данные для таблицы должностей
                int[] position_id = { 1, 2, 3, 4 };
                String[] position_name = { "Директор", "Программер",
                        "Бухгалтер", "Охранник" };
                int[] position_salary = { 15000, 13000, 10000, 8000 };

                db.beginTransaction();
                try {
                    // создаем таблицу должностей
                    db.execSQL("create table position ("
                            + "id integer primary key,"
                            + "name text, salary integer);");

                    // заполняем ее
                    for (int i = 0; i < position_id.length; i++) {
                        cv.clear();
                        cv.put("id", position_id[i]);
                        cv.put("name", position_name[i]);
                        cv.put("salary", position_salary[i]);
                        db.insert("position", null, cv);
                    }

                    db.execSQL("alter table people add column posid integer;");

                    for (int i = 0; i < position_id.length; i++) {
                        cv.clear();
                        cv.put("posid", position_id[i]);
                        db.update("people", cv, "position = ?",
                                new String[] { position_name[i] });
                    }

                    db.execSQL("create temporary table people_tmp ("
                            + "id integer, name text, position text, posid integer);");

                    db.execSQL("insert into people_tmp select id, name, position, posid from people;");
                    db.execSQL("drop table people;");

                    db.execSQL("create table people ("
                            + "id integer primary key autoincrement,"
                            + "name text, posid integer);");

                    db.execSQL("insert into people select id, name, posid from people_tmp;");
                    db.execSQL("drop table people_tmp;");

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

        }
    }
    void logCursor (Cursor c, String table){
        Log.d (myLogs, "----" + table + "----");
        if(c.moveToFirst()){
            Log.d(myLogs, "№ name position");
            do{
                String row = "";
                for(String cn:c.getColumnNames()) {
                    row += c.getString(c.getColumnIndex(cn));
                    row+=" ";

                }
                Log.d(myLogs, row);
            }
            while(c.moveToNext());
        } else {
            Log.d(myLogs, "Cursor is null!");
        }
    }

    private void writeStaff(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from people", null);
        logCursor(c, "Table people");
        c.close();

        c = db.rawQuery("select * from position", null);
        logCursor(c, "Table position");
        c.close();

        String sqlQuery = "select PL.name as Name, PS.name as Position, salary as Salary "
                + "from people as PL "
                + "inner join position as PS "
                + "on PL.posid = PS.id ";
        c = db.rawQuery(sqlQuery, null);
        logCursor(c, "inner join");
        c.close();
    }
}
