package edu.asu.fall15.cse535.group42.temponinja;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by royster on 11/14/15.
 */
public class PlayListAdapter extends ArrayAdapter<String> {

	protected int			curSelectedItem = -1;
	private final Context 	context;
	private final int 		textViewResourceId;
	private final String[]	items;

	public PlayListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
		this.items = null;
	}

	public PlayListAdapter(Context context, int resource, String[] items) {
		super(context, resource, items);
		this.context = context;
		this.textViewResourceId = resource;
		this.items = items;
	}

	public void setSelectedItem (int i) {
		curSelectedItem = i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//View v = convertView;
		View v = super.getView(position, convertView, parent);
//		if (v == null) {
//			LayoutInflater vi;
//			vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			//vi = LayoutInflater.from(getContext());
//			v = vi.inflate(textViewResourceId, null);
//		}

		String p = items[position];

		TextView textView = null;
		if (position == curSelectedItem) {
			textView = (TextView) v.findViewById(R.id.tv);
			textView.setText(p);
			textView.setTextColor(Color.RED);
		} else {
			textView = (TextView) v.findViewById(R.id.tv);
			textView.setText(p);
			textView.setTextColor(Color.BLACK);
		}

		return v;
	}


}
