package apalabrados.model;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

public class JugadaComparatorByRow implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject a, JSONObject b) {
		try {
			int rowA = a.getInt("row");
			int rowB = b.getInt("row");
			return rowA-rowB;
		} catch (JSONException e) {
			return 0;
		}
	}

}
