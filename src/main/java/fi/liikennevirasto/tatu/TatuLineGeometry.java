package fi.liikennevirasto.tatu;

import org.hibernate.spatial.jts.mgeom.MultiMLineString;

import com.vividsolutions.jts.geom.Geometry;

public class TatuLineGeometry extends TatuGeometry {

	MultiMLineString multiMLineString;
	
	public TatuLineGeometry(MultiMLineString multiMLineString, Error error) {
		super(error);
		this.multiMLineString = multiMLineString;
	}

	public MultiMLineString getMultiMLineString() {
		return multiMLineString;
	}

	@Override
	public Geometry getGeometry() {
		return multiMLineString;
	}
	
	
}
