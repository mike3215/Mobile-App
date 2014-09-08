package citruscups.com.sitelinkmobile.helper;

import java.util.ArrayList;
import java.util.Map;

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
            ArrayList<Map<String, Object>> rows = rtTable.getRows();
            if (rows.size() > 0)
            {
                return Integer.parseInt((String) rows.get(0).get("Ret_Code"));
            }
        }
        return 0;
    }

    public static int getRtValue(DataTable dt)
    {
        if (dt == null) return -99;
        if (dt.getName().equals("RT"))
        {
            ArrayList<Map<String, Object>> rows = dt.getRows();
            if (rows.size() > 0)
            {
                return Integer.parseInt(rows.get(0).get("Ret_Code").toString());
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

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isInteger(String str)
    {
        if (isNumeric(str))
        {
            double d = Double.parseDouble(str);
            return ((d % 1) == 0);
        }

        return false;
    }

    public static String formatDate(int day, int month, int year)
    {
        return month + "-" + day + "-" + year + "T00:00:00";
    }

    public static String formatDate(String date, String format)
    {
        if (date.equals("")) return date;

        int length = format.length();

        int yearStart = format.indexOf("y");
        int yearEnd = format.lastIndexOf("y", length) + 1;

        int monthStart = format.indexOf("M");
        int monthEnd = format.lastIndexOf("M", length) + 1;

        int dayStart = format.indexOf("d");
        int dayEnd = format.lastIndexOf("d", length) + 1;

        String year = date.substring(yearStart, yearEnd);
        String month = date.substring(monthStart, monthEnd);
        String day = date.substring(dayStart, dayEnd);

        return year + "-" + day + "-" + month + "T00:00:00";
    }

}
