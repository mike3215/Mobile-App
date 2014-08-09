package citruscups.com.sitelinkmobile.DataStructures;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataTable
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
}
