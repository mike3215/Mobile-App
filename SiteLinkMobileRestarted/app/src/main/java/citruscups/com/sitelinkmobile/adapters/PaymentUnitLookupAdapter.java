package citruscups.com.sitelinkmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import citruscups.com.sitelinkmobile.R;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Michael on 8/13/2014.
 */
public class PaymentUnitLookupAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    DataTable mDataTable;

    public PaymentUnitLookupAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mDataTable = new DataTable();
    }

    @Override
    public int getCount() {
        return mDataTable.getCount();
    }

    @Override
    public Map<String, Object> getItem(int i) {
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
            convertView = mInflater.inflate(R.layout.payment_unit_lookup_list_item, null);

            // Create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.unitNameTextView = (TextView) convertView.findViewById(R.id.paymentUnitName);
            holder.rentAmtTextView = (TextView) convertView.findViewById(R.id.paymentUnitRentAmt);
            holder.insuranceAmtTextView = (TextView) convertView.findViewById(R.id.paymentUnitInsuranceAmt);
            holder.feesAmtTextView = (TextView) convertView.findViewById(R.id.paymentUnitFeesAmt);
            holder.otherAmtTextView = (TextView) convertView.findViewById(R.id.paymentUnitOtherAmt);
            holder.balanceAmtTextView = (TextView) convertView.findViewById(R.id.paymentUnitBalanceAmt);
            holder.unitIDTextView = (TextView) convertView.findViewById(R.id.paymentUnitId);
            holder.ledgerIDTextView = (TextView) convertView.findViewById(R.id.paymentLedgerId);

            // Hang ont this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // Skip all the expensive inflation/findViewByID
            // and just get the holder that was already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current acct balance data in DataTable Row form
        Map<String, Object> selectedRow = getItem(i);

        String unitName;
        String rentAmt, insuranceAmt, feesAmt, otherAmt, balanceAmt;
        String unitID, ledgerID;
        Double rent, insurance, fees, other;

        unitName = (String) selectedRow.get("sUnitName");

        rent = (Double) selectedRow.get("RentBal");
        rentAmt = "$" + String.format("%.2f", rent);

        insurance = (Double) selectedRow.get("InsuranceBal");
        insuranceAmt = "$" + String.format("%.2f", insurance);

        // Combine all fee columns into one total
        fees = (Double) selectedRow.get("RecurringBal") +
                (Double) selectedRow.get("LateFeeBal") + (Double) selectedRow.get("OtherFeesBal");
        feesAmt = "$" + String.format("%.2f", fees);

        // Group merchandise and sec dep into an Other amt
        other = (Double) selectedRow.get("POSBal") + (Double) selectedRow.get("SecDepBal");
        otherAmt = "$" + String.format("%.2f", other);

        balanceAmt = "$" + String.format("%.2f", (rent + insurance + fees + other));

        unitID = (String) selectedRow.get("UnitID");
        ledgerID = (String) selectedRow.get("LedgerID");

        holder.unitNameTextView.setText(unitName);
        holder.rentAmtTextView.setText(rentAmt);
        holder.insuranceAmtTextView.setText(insuranceAmt);
        holder.feesAmtTextView.setText(feesAmt);
        holder.otherAmtTextView.setText(otherAmt);
        holder.balanceAmtTextView.setText(balanceAmt);
        holder.unitIDTextView.setText(unitID);
        holder.ledgerIDTextView.setText(ledgerID);

        return convertView;
    }

    public void updateData(DataTable dataTable) {
        mDataTable = dataTable;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView unitNameTextView;
        public TextView rentAmtTextView;
        public TextView insuranceAmtTextView;
        public TextView feesAmtTextView;
        public TextView otherAmtTextView;
        public TextView balanceAmtTextView;
        public TextView unitIDTextView;
        public TextView ledgerIDTextView;
    }
}
