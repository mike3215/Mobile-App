package citruscups.com.sitelinkmobile.dataStructures;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataSet implements Parcelable
{
    public static final Creator CREATOR = new Creator()
    {
        @Override
        public DataSet createFromParcel(Parcel parcel)
        {
            return new DataSet(parcel);
        }

        @Override
        public DataSet[] newArray(int size)
        {
            return new DataSet[size];
        }
    };
    private ArrayList<DataTable> mTables;
    public DataSet()
    {
        mTables = new ArrayList<DataTable>();
    }

    public DataSet(Parcel in)
    {
        mTables = new ArrayList<DataTable>();
        readFromParcel(in);
    }

    public ArrayList<DataTable> getTables()
    {
        return mTables;
    }

    public DataTable getTableByName(String name)
    {
        if (mTables == null) return null;

        for (DataTable table : mTables)
        {
            if (table.getName().equals(name))
                return table;
        }
        return null;
    }

    public void addTable(DataTable table)
    {
        mTables.add(table);
    }

    @Override
    public String toString()
    {
        String result = "DataSet: ";
        for (DataTable table : mTables)
        {
            result += table.toString();
        }

        return result;
    }

    public Boolean containsTable(String name)
    {
        if (mTables == null) return false;

        for (DataTable table : mTables)
        {
            if (table.getName().equals(name))
                return true;
        }

        return false;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        Bundle b = new Bundle();
        b.putSerializable("tables", mTables);
        dest.writeBundle(b);
    }

    public void readFromParcel(Parcel in)
    {
        Bundle b = in.readBundle();
        mTables = (ArrayList<DataTable>) b.getSerializable("mTables");
    }
}
