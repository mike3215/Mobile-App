package citruscups.com.sitelinkmobile.helper;

import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Jakkyl on 8/9/2014.
 */
public class Helper
{
    public static int getRtValue(DataSet ds)
    {
        if (ds == null) return -99;
        DataTable rtTable = ds.getTableByName("RT");
        if (rtTable != null)
        {
            if (rtTable.getRows().size() > 0)
            {
                return Integer.parseInt((String) rtTable.getRows().get(0).get("Ret_Code"));
            }
        }
        return 0;
    }

    public static int getRtValue(DataTable dt)
    {
        if (dt == null) return -99;
        if (dt.getName().equals("RT"))
        {
            if (dt.getRows().size() > 0)
            {
                return (Integer) dt.getRows().get(0).get("Ret_Code");
            }
        }

        return 0;
    }

    public static String getMessageFromRetCode(int retCode)
    {
        switch (retCode)
        {
            case -96:
                return "Invalid web access login (contact SiteLink support)";
            case -97:
                return "Invalid location code";
            case -98:
                return "Invalid logon credentials";
            case -99:
                return "General Exception";
            default:
                return "Unmapped return code";
        }
    }
}

