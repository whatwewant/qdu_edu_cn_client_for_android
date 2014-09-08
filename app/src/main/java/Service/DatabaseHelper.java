package Service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

/**
 * Created by potter on 14-8-6.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DEFAULT_DATABASE_NAME = "DefaultDatabaseName";
    private static final String TABLE_NAME = "user";
    private static final String SQLCLAUSE = "CREATE TABLE " + TABLE_NAME + " (" +
            "id integer NOT NULL PRIMARY KEY, " +
            "username varchar(128) NOT NULL, " +
            "password varchar(128) NOT NULL, " +
            "save bool NOT NULL DEFAULT FALSE, " +
            "active bool NOT NULL DEFAULT FALSE)";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DatabaseHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public DatabaseHelper (Context context) {
        this(context, DEFAULT_DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("Create a Database");
        // execSQL函数用于执行SQL语句
        sqLiteDatabase.execSQL(SQLCLAUSE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        System.out.println("Upgrade a Database");
    }
}
