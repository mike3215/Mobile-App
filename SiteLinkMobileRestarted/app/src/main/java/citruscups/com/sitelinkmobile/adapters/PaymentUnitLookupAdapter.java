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

/**
 * Created by Michael on 8/13/2014.
 */
public class PaymentUnitLookupAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    ArrayList<Map<String, String>> mRows;

    public PaymentUnitLookupAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mRows = new ArrayList<Map<String, String>>();
    }

    @Override
    public int getCount() {
        return mRows.size();
    }

    @Override
    public Map<String, String> getItem(int i) {
        return mRows.get(i);
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
        Map<String, String> selectedRow = getItem(i);

        String unitName = "A123";
        String rentAmt = "$0.00";
        String insuranceAmt = "$0.00";
        String feesAmt = "$0.00";
        String otherAmt = "$0.00";
        String balanceAmt = "$0.00";
        String unitID = "-999";
        String ledgerID = "-999";

        if (selectedRow.containsKey("sUnitName")) {
            unitName = selectedRow.get("sUnitName");
        }

        if (selectedRow.containsKey("sRent")) {
            rentAmt = "$" + selectedRow.get("sRent");
        }

        if (selectedRow.containsKey("sInsurance")) {
            insuranceAmt = "$" + selectedRow.get("sInsurance");
        }

        if (selectedRow.containsKey("sFees")) {
            feesAmt = "$" + selectedRow.get("sFees");
        }

        if (selectedRow.containsKey("sOther")) {
            otherAmt = "$" + selectedRow.get("sOther");
        }

        if (selectedRow.containsKey("sBalance")) {
            balanceAmt = "$" + selectedRow.get("sBalance");
        }

        if (selectedRow.containsKey("UnitID")) {
            unitID = selectedRow.get("UnitID");
        }

        if (selectedRow.containsKey("LedgerID")) {
            ledgerID = selectedRow.get("LedgerID");
        }

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

    public void updateData(ArrayList<Map<String, String>> rows) {
        mRows = rows;
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
