package citruscups.com.sitelinkmobile;

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

        for (int i = 0; i < tables.size(); i++)
        {
            if (tables.get(i).getName().equals(name))
                return tables.get(i);
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
        for (int i = 0; i < tables.size(); i++)
        {
            result += tables.get(i).toString();
        }

        return  result;
    }

    public Boolean containsTable(String name) {
        if (tables == null) return false;

        for (int i = 0; i < tables.size(); i++)
        {
            if (tables.get(i).getName().equals(name))
                return true;
        }

        return false;
    }
}
