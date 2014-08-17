package citruscups.com.sitelinkmobile.dataStructures;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataTable implements Parcelable
{
    private ArrayList<Map<String, Object>> _rows;
    private String _name;
    private boolean _closed;

    public DataTable()
    {
        _rows = new ArrayList<Map<String, Object>>();
        _name = "Table";
    }

    public boolean getClosed()
    {
        return _closed;
    }

    public void setClosed(boolean val)
    {
        _closed = val;
    }


    public String getName()
    {
        return _name;
    }

    public void setName(String val)
    {
        _name = val;
    }

    public ArrayList<Map<String, Object>> getRows()
    {
        return _rows;
    }

    public void addRow(Map<String, Object> row)
    {
        _rows.add(row);
    }

    public Map<String, Object> getRow(int i) {return _rows.get(i);}

    public Integer getCount() { return _rows.size();}

    @Override
    public String toString()
    {
        String result = _name + ": ";

        for (Map<String, Object> map : _rows)
        {
            result += "Row: { ";
            result += map.toString() + "/n";
        }

        return result + " }";
    }

    public DataTable(Parcel in) {
        _rows = new ArrayList<Map<String, Object>>();
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle b = new Bundle();
        b.putString("_name", _name);
        b.putSerializable("_rows", _rows);
        dest.writeBundle(b);
    }

    public void readFromParcel(Parcel in) {
        Bundle b = in.readBundle();
        _name = b.getString("_name");
        _rows = (ArrayList<Map<String, Object>>) b.getSerializable("_rows");
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public DataTable createFromParcel(Parcel parcel) {
            return new DataTable(parcel);
        }

        @Override
        public DataTable[] newArray(int size) {
            return new DataTable[size];
        }
    };
}
