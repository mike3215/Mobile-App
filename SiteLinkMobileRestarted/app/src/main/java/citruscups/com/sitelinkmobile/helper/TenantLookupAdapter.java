package citruscups.com.sitelinkmobile.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.HashMap;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Michael on 8/11/2014.
 */
public class TenantLookupAdapter extends BaseAdapter implements Filterable{

    Context mContext;
    LayoutInflater mInflater;
    DataTable mDataTable;
    DataTable mDataTableOriginal;

    public TenantLookupAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mDataTable = new DataTable();
    }

    @Override
    public int getCount() {
        return mDataTable.getCount();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDataTable = (DataTable) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                DataTable FilteredDataTable = new DataTable();

                if (mDataTableOriginal == null) {
                    mDataTableOriginal = mDataTable; // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mDataTableOriginal.getCount();
                    results.values = mDataTableOriginal;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mDataTableOriginal.getCount(); i++) {
                        String data = (String) mDataTableOriginal.getRow(i).get("sLName");
                        if (data.toLowerCase().contains(constraint.toString()))
                        {
                            FilteredDataTable.addRow(mDataTableOriginal.getRow(i));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredDataTable.getCount();
                    results.values = FilteredDataTable;
                }
                return results;
            }
        };
        return filter;
    }

    @Override
    public Object getItem(int i) {
        return mDataTable.getRow(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Check if the view already exists
        // if so, no need to inflate and findViewByID again.
        if (convertView == null) {
            // Inflate the custom row layout from the XML
            convertView = mInflater.inflate(R.layout.tenant_lookup_list_item, null);

            // Create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.lastNameTextView = (TextView) convertView.findViewById(R.id.lastName);
            holder.firstNameTextView = (TextView) convertView.findViewById(R.id.firstName);
            holder.companyTextView = (TextView) convertView.findViewById(R.id.company);
            holder.cityTextView = (TextView) convertView.findViewById(R.id.city);
            holder.postalCodeTextView = (TextView) convertView.findViewById(R.id.postalCode);
            holder.phoneTextView = (TextView) convertView.findViewById(R.id.phone);
            holder.tenantIDTextView = (TextView) convertView.findViewById(R.id.tenantId);

            // Hang ont this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // Skip all the expensive inflation/findViewByID
            // and just get the holder that was already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current tenants data in DataTable Row form
        HashMap<String, Object> dataTableItem = (HashMap<String, Object>) getItem(i);

        String lastName = "";
        String firstName = "";
        String company = "";
        String city = "";
        String postalCode = "";
        String phone = "";
        String tenantID = "";

        if (dataTableItem.containsKey("sLName")) {
            lastName = (String) dataTableItem.get("sLName");
        }

        if (dataTableItem.containsKey("sFName")) {
            firstName = (String) dataTableItem.get("sFName");
        }

        if (dataTableItem.containsKey("sCompany")) {
            company = (String) dataTableItem.get("sCompany");
        }

        if (dataTableItem.containsKey("sCity")) {
            city = (String) dataTableItem.get("sCity");
        }

        if (dataTableItem.containsKey("sPostalCode")) {
            postalCode = (String) dataTableItem.get("sPostalCode");
        }

        if (dataTableItem.containsKey("sPhone")) {
            phone = (String) dataTableItem.get("sPhone");
        }

        if (dataTableItem.containsKey("TenantID")) {
            tenantID = (String) dataTableItem.get("TenantID");
        }

        holder.lastNameTextView.setText(lastName);
        holder.firstNameTextView.setText(firstName);
        holder.companyTextView.setText(company);
        holder.cityTextView.setText(city);
        holder.postalCodeTextView.setText(postalCode);
        holder.phoneTextView.setText(phone);
        holder.tenantIDTextView.setText(tenantID);

        return convertView;
    }

    public void updateData(DataTable dataTable) {
        mDataTable = dataTable;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView lastNameTextView;
        public TextView firstNameTextView;
        public TextView companyTextView;
        public TextView cityTextView;
        public TextView postalCodeTextView;
        public TextView phoneTextView;
        public TextView tenantIDTextView;
    }
}
