package me.bryanlau.goalbuddiesandroid.Goals;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.bryanlau.goalbuddiesandroid.R;

public class GoalListAdapter extends ArrayAdapter<Goal> {
    private final Context context;
    private final ArrayList<Goal> values;

    public GoalListAdapter(Context context, ArrayList<Goal> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_goal, parent, false);
        TextView bodyTextView = (TextView) rowView.findViewById(R.id.goalBody);
        TextView etaTextView = (TextView) rowView.findViewById(R.id.goalEta);
        ImageView iconImageView = (ImageView) rowView.findViewById(R.id.goalIcon);

        bodyTextView.setText(values.get(position).m_description);


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        SimpleDateFormat cleanFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

        try {
            Date date = format.parse(values.get(position).m_finished);
            String etaString = "ETA : " + cleanFormat.format(date);
            etaTextView.setText(etaString);

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 3);

            if(date.before(new Date()) && values.get(position).m_pending) {
                rowView.setBackgroundColor(Color.RED);
            } else if(date.before(cal.getTime()) && values.get(position).m_pending) {
                rowView.setBackgroundColor(Color.YELLOW);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //etaTextView.setText(values.get(position).m_eta);

        /*
        if (s.startsWith("iPhone")) {
            imageView.setImageResource(R.drawable.no);
        } else {
            imageView.setImageResource(R.drawable.ok);
        }*/

        return rowView;
    }
}
