package citruscups.com.sitelinkmobile.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Map;

import citruscups.com.sitelinkmobile.dataStructures.DataTable;
import citruscups.com.sitelinkmobile.interfaces.ICommand;

/**
 * Created by Jakkyl on 8/16/2014.
 */
public class UnitLookupAdapter extends BaseAdapter implements Filterable
{
    private final Object lock = new Object();
    private String mColumns[] = new String[]{"sFName", "sLName", "sCompany", "sCity", "sPostalCode", "sPhone", "TenantID"};
    private int mTo[];
    private Map<Integer, ICommand> mColorMap;
    private Map<Integer, ICommand> mTextFormatMap;
    private LayoutInflater mInflater;
    private DataTable mDataTable;
    private DataTable mDataTableOriginal;
    private Filter mFilter;
    private int mResource;
    private SimpleAdapter.ViewBinder mViewBinder;

    public UnitLookupAdapter(Context context, DataTable data, int resource, String[] columns, int[] to)
    {
        mDataTable = data;
        mColumns = columns;
        mTo = to;
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewBinder = new CitrusViewBinder();
    }

    @Override
    public int getCount()
    {
        return mDataTable == null ? 0 : mDataTable.getCount();
    }

    @Override
    public Filter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new CitrusFilter();
        }
        return mFilter;
    }

    @Override
    public Object getItem(int i)
    {
        return mDataTable.getRow(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    public View createViewFromResource(int position, View convertView, ViewGroup parent, int resource)
    {
        // Check if the view already exists
        // if so, no need to inflate and findViewByID again.
        if (convertView == null)
        {
            // Inflate the custom row layout from the XML
            convertView = mInflater.inflate(resource, parent, false);

            // Create a new "Holder" with subviews
            final int count = mTo.length;
            final View[] holder = new View[count];
            for (int i = 0; i < count; i++)
            {
                holder[i] = convertView.findViewById(mTo[i]);
            }

            // Hang ont this holder for future recyclage
            convertView.setTag(holder);
        }

        bindView(position, convertView);

        return convertView;
    }

    private void bindView(int position, View view)
    {
        final Map row = mDataTable.getRows().get(position);
        if (row == null) return;

        final int count = mTo.length;
        final View[] holder = (View[]) view.getTag();
        final String[] columns = mColumns;
        final SimpleAdapter.ViewBinder binder = mViewBinder;

        for (int i = 0; i < count; i++)
        {
            final View v = holder[i];
            if (v == null) continue;

            final Object data = row.get(columns[i]);
            String text = data == null ? "" : data.toString();
            if (text == null) text = "";

            boolean bound = false;
            if (binder != null)
            {
                bound = binder.setViewValue(v, row, text);
            }

            if (bound)
            {
                if (v instanceof Checkable)
                {
                    if (data instanceof String)
                    {
                        ((Checkable) v).setChecked(text.equals("true"));
                    }
                    else
                    {
                        throw new IllegalStateException(v.getClass().getName() + " should not be bound to a boolea;" + data.getClass());
                    }
                }
                else if (v instanceof TextView)
                {
                    setViewText((TextView) v, row, text);
                }
                else if (v instanceof ImageView)
                {
                    if (data instanceof Integer)
                    {
                        setViewImage((ImageView) v, (Integer) data);
                    }
                    else
                    {
                        setViewImage((ImageView) v, text);
                    }
                }
                else
                {
                    throw new IllegalStateException(v.getClass().getName() + " is not a view that can be bound.");
                }
            }
        }
    }

    public void setViewImage(ImageView view, int id)
    {
        view.setImageResource(id);
    }

    public void setViewImage(ImageView view, String value)
    {
        try
        {
            view.setImageResource(Integer.parseInt(value));
        }
        catch (NumberFormatException nfe)
        {
            view.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView view, Object data, String value)
    {
        if (mTextFormatMap != null)
        {
            final int id = view.getId();
            if (mTextFormatMap.containsKey(id))
            {
                value = mTextFormatMap.get(id).executeText(value, data);
            }
        }
        view.setText(value);
        if (mColorMap != null)
        {
            final int id = view.getId();
            if (mColorMap.containsKey(id))
            {
                view.setTextColor(mColorMap.get(id).executeColor(value));
            }
        }
    }

    public void updateData(DataTable dataTable)
    {
        mDataTable = dataTable;
        notifyDataSetChanged();
    }

    public void setColorMap(Map<Integer, ICommand> colorMap)
    {
        mColorMap = colorMap;
    }

    public void setTextFormatMap(Map<Integer, ICommand> textFormatMap)
    {
        mTextFormatMap = textFormatMap;
    }

    private class CitrusFilter extends Filter
    {
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {

            mDataTable = (DataTable) results.values; // has the filtered values
            if (results.count > 0)
            {
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
            DataTable FilteredDataTable = new DataTable();

            if (mDataTableOriginal == null)
            {
                synchronized (lock)
                {
                    mDataTableOriginal = mDataTable; // saves the original data in mOriginalValues
                }
            }

            /********
             *
             *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
             *  else does the Filtering and returns FilteredArrList(Filtered)
             *
             ********/
            if (constraint == null || constraint.length() == 0)
            {
                // set the Original result to return
                synchronized (lock)
                {
                    results.count = mDataTableOriginal.getCount();
                    results.values = mDataTableOriginal;
                }
            }
            else
            {
                constraint = constraint.toString().toLowerCase();

                for (int i = 0; i < mDataTableOriginal.getCount(); i++)
                {
                    for (String columnName : mColumns)
                    {
                        String data = (String) mDataTableOriginal.getRow(i).get(columnName);
                        if (data == null) continue;

                        if (data.toLowerCase().contains(constraint.toString()))
                        {
                            FilteredDataTable.addRow(mDataTableOriginal.getRow(i));
                            break;
                        }
                    }
                }
                // set the Filtered result to return
                results.count = FilteredDataTable.getCount();
                results.values = FilteredDataTable;
            }
            return results;
        }
    }

    class CitrusViewBinder implements SimpleAdapter.ViewBinder
    {

        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation)
        {
            if (view instanceof Checkable)
            {
                ((Checkable) view).setChecked(textRepresentation.equals("true"));
            }
            else if (view instanceof TextView)
            {
                setViewText((TextView) view, data, textRepresentation);
            }

            return true;
        }
    }
}

