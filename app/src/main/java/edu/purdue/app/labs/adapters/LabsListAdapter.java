package edu.purdue.app.labs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.purdue.app.R;
import edu.purdue.app.labs.model.AvailableLab;

/**
 * Created by david on 9/2/14.
 */
public class LabsListAdapter extends BaseAdapter {
    List<AvailableLab> labs;
    Context context;
    LayoutInflater inflater;

    public LabsListAdapter(Context context, List<AvailableLab> labs) {
        this.labs = labs;
        this.context = context;
    }

    private LayoutInflater getInflater() {
        if(inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return inflater;
    }

    @Override
    public int getCount() {
        return labs.size();
    }

    @Override
    public AvailableLab getItem(int position) {
        return labs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean isEmpty() {
        return labs == null || labs.size() == 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = getInflater().inflate(R.layout.labs_lab_listitem, parent, false);
            holder = new ViewHolder();
            holder.available = (TextView) convertView.findViewById(R.id.lab_txt_available);
            holder.location = (TextView) convertView.findViewById(R.id.lab_txt_location);
//            holder.colorBar = (View) convertView.findViewById(R.id.color_indicator);
            convertView.setTag(holder);
        }

        holder.available.setText( "" + getItem(position).getAvailableStations() );
        holder.location.setText( getItem(position).getLocation() );
        // holder.colorBar.setBackground(context.getResources().getDrawable(R.drawable.labs_color_bar));
        return convertView;
    }


    class ViewHolder {
        TextView location;
        TextView available;
//        View colorBar;
    }
}