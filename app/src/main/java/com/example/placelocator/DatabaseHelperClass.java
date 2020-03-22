package com.example.placelocator;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DatabaseHelperClass extends SQLiteOpenHelper {
    private Context context;

    public  DatabaseHelperClass(Context context)
    {
        super(context,"database",null,1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE `place_temples` (`place_id` varchar(50) NOT NULL, `latitude` double NOT NULL, `longitude` double NOT NULL, `name` varchar(500) NOT NULL, `rating` double DEFAULT NULL, `types` varchar(200) DEFAULT NULL, `added_date` timestamp NOT NULL, `updated_date` timestamp NOT NULL, `id` int(10)  NOT NULL ,  PRIMARY KEY (`id`,`place_id`))");

            Toast.makeText(context, "Table created", Toast.LENGTH_SHORT).show();
            copyCSVtoDatabase(db);

        } catch (SQLException e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
        private void copyCSVtoDatabase(SQLiteDatabase db) {

            try {
                InputStream is = context.getAssets().open("place_temples.csv");

                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                String tableName ="place_temples";
                String columns = "place_id,latitude,longitude,name,rating,types,added_date,updated_date,id";
                String str1 = "INSERT INTO " + tableName + " ( " + columns + " ) values(";
                String str2 = ");";

                db.beginTransaction();
                int i=0;
                while ((line = br.readLine()) != null) {
                    if(i>0)
                    {
                        StringBuilder sb = new StringBuilder(str1);
                        String[] str = line.split(",");

                        int requiredColumns=9; int tokens=str.length;
                        if(requiredColumns==tokens)
                        {
                            sb.append("'"+str[0].trim() + "',");
                            sb.append(str[1].trim() + ",");
                            String temp=str[3].trim();
                            if(temp.contains("'")) {
                                sb.append(str[2].trim() + ",");
                                String temp2 = temp.replaceAll("'", "\'");
                                sb.append(temp2.trim() + ",");

                            }
                            else
                            {
                                sb.append(str[2].trim() + ",'");
                                String temp3=(str[3]).replaceAll("\"","");
                                sb.append(temp3.trim() + "',");
                            }
                            sb.append(str[4].trim() + ",'");
                            String tstr;
                            if((str[5]).contains("\""))
                            {
                                tstr=(str[5]).replaceAll("\"","");
                                sb.append(tstr + "',");
                            }
                            else{sb.append(str[5] + "',");}
                            sb.append("datetime()" + ",");
                            sb.append(str[7] + ",");
                            sb.append(str[8].trim());
                            sb.append(str2);
                        }
                        else //(tokens-requiredColumns>0)
                        {
                            int extraComma=tokens-requiredColumns;
                            sb.append("'"+str[0].trim() + "',");
                            sb.append(str[1].trim() + ",");
                            String temp="";
                            sb.append(str[2].trim() + ",");
                            int k=0;
                            while(k<=extraComma-1)
                            {
                                String temp2=str[3+k].trim();
                                if(temp2.contains("'")) {
                                    String temp3 = temp2.replaceAll("'", "\'");
                                    //Log.d("replaced==", temp2);
                                    temp+=temp3.trim() + ",";

                                }
                                else
                                {
                                    temp+=temp2.trim() + ",";
                                }
                                k++;
                            }
                            String temp2=str[3+k];
                            if(temp2.contains("'")) {
                                String temp3 = temp2.replaceAll("'", "\'");
                                temp+=temp3.trim();

                            }
                            else
                            {
                                temp+=temp2.trim();
                            }
                            sb.append(temp.trim() + ",");
                            sb.append(str[3+k+1].trim() + ",'");
//                            sb.append(str[3+k+2] + "',");
                            String tstr;
                            if((str[3+k+2]).contains("\""))
                            {
                                tstr=(str[3+k+2]).replaceAll("\"","");
                                sb.append(tstr + "',");
                            }
                            else{sb.append(str[3+k+2] + "',");}
                            sb.append("datetime()" + ",");
                            sb.append(str[3+k+4] + ",");
                            sb.append(str[3+k+5].trim());
                            sb.append(str2);


                        }
                        db.execSQL(sb.toString());
                    }
                    i++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    sqLiteDatabase.execSQL("drop table if exists place_temples");
    onCreate(sqLiteDatabase);
    }
}
