package fi.liikennevirasto.tatu.service;

import java.util.List;

import fi.liikennevirasto.tatu.TatuLineGeometry;
import fi.liikennevirasto.tatu.TatuPointGeometry;
import fi.liikennevirasto.tatu.model.Tieosa;

public interface OsoiteverkkoService {

	public Tieosa getByRouteId(int routeId);
	public List<Tieosa> getTieosoitevali(int tie, int ajorata);
	public TatuLineGeometry getTieosoitevaliAsGeometry(int tie, int aosa, double aet, 
			int losa, double let, Integer ajorata);
	//Uusi funktio jossa offset mukana
	public TatuLineGeometry getTieosoitevaliAsGeometry(int tie, int aosa, double aet, 
			int losa, double let, Integer ajorata, int offset);
	public TatuPointGeometry getTieosoiteAsPoint(int tie, int osa, double et, Integer ajorata);
	//Uusi funktio jossa offset mukana
	public TatuPointGeometry getTieosoiteAsPoint(int tie, int osa, double et, Integer ajorata, int offset);
}
