package fi.liikennevirasto.tatu.dao;

import java.util.List;

import fi.liikennevirasto.tatu.model.Tieosa;

public interface OsoiteverkkoDao {

	public Tieosa getByRouteId(int routeId);
	public List<Tieosa> getTieosoitevali(int tie, int tie2);
}
