package fi.liikennevirasto.tatu;

import java.util.ArrayList;

public class DataColumn {

	private ArrayList<Object> data;
	
	public DataColumn() {
		data = new ArrayList<Object>();
	}
	
	public void addData(Object object) {
		data.add(object);
	}
}
