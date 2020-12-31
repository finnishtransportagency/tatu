package fi.liikennevirasto.tatu;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class TatuPointGeometry extends TatuGeometry {

	Point point;
	
	public TatuPointGeometry(Point point, Error error) {
		super(error);
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}

	@Override
	public Geometry getGeometry() {
		return point;
	}
}
