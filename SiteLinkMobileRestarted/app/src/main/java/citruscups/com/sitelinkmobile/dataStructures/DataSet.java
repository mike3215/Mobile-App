package citruscups.com.sitelinkmobile.dataStructures;

import java.util.ArrayList;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class DataSet  {
    private ArrayList<DataTable> tables;

    public DataSet()
    {
        tables = new ArrayList<DataTable>();
    }

    public ArrayList<DataTable> getTables()
    {
        return tables;
    }
    public DataTable getTableByName(String name)
    {
        if (tables == null) return null;

        for (DataTable table : tables)
        {
            if (table.getName().equals(name))
                return table;
        }
        return null;
    }
    public void addTable(DataTable table)
    {
        tables.add(table);
    }

    @Override
    public String toString()
    {
        String result = "DataSet: ";
        for (DataTable table : tables)
        {
            result += table.toString();
        }

        return  result;
    }

    public Boolean containsTable(String name) {
        if (tables == null) return false;

        for (DataTable table : tables)
        {
            if (table.getName().equals(name))
                return true;
        }

        return false;
    }
}
