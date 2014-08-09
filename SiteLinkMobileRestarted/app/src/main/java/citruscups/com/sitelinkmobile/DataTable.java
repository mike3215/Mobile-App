package citruscups.com.sitelinkmobile;

import java.util.ArrayList;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataTable {
    private ArrayList _rows;
    private String _name;

    public DataTable()
    {
        _rows = new ArrayList();
        _name = "Table";
    }

    public String getName() { return _name; }
    public void setName(String val) { _name = val; }

    public ArrayList getRows() { return _rows; }

    public void addRow(ArrayList row)
    {
        _rows.add(row);
    }
}
