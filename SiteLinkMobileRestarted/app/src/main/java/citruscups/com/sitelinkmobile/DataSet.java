package citruscups.com.sitelinkmobile;

import java.util.ArrayList;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataSet  {
    private ArrayList tables;

    public ArrayList getTables()
    {
        return tables;
    }

    public void addTable(DataTable table)
    {
        tables.add(table);
    }
}
