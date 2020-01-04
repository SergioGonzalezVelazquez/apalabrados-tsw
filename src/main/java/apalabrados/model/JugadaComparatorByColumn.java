package apalabrados.model;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

public class JugadaComparatorByColumn implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject a, JSONObject b) {
		try {
			int colA = a.getInt("col");
			int colB = b.getInt("col");
			return colA-colB;
		} catch (JSONException e) {
			return 0;
		}
	}

}
