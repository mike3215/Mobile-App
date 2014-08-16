package citruscups.com.sitelinkmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Jakkyl on 8/16/2014.
 */
public class UnitLookupAdapter extends BaseAdapter implements Filterable
{
    private Context mContext;
    private LayoutInflater mInflater;
    private DataTable mDataTable;

    public UnitLookupAdapter(Context context, LayoutInflater inflater)
    {
        mContext = context;
        mInflater = inflater;
        mDataTable = new DataTable();
    }

    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public Filter getFilter()
    {
        return null;
    }

    public void updateData(DataTable dataTable)
    {
        mDataTable = dataTable;
        notifyDataSetChanged();
    }
}
