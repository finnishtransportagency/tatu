package fi.liikennevirasto.tatu.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.liikennevirasto.tatu.model.Tieosa;

@Repository("OsoiteverkkoDao")
@Transactional(readOnly=true)
public class OsoiteverkkoDaoImpl extends BaseDaoImpl implements OsoiteverkkoDao {

	@Override
	public Tieosa getByRouteId(int routeId) {
		return (Tieosa) findByUniqueField(Tieosa.class, "routeId", routeId);
	}

	@Override
	public List<Tieosa> getTieosoitevali(int tie, int tie2) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Tieosa.class);

		criteria.add(Restrictions.ge("tie", tie));
		criteria.add(Restrictions.le("tie", tie2));
		criteria.addOrder(Order.asc("tie"));
		criteria.addOrder(Order.asc("osa"));
		criteria.addOrder(Order.asc("ajorata"));
		
		return getTieosas(criteria);
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Tieosa> getTieosas(DetachedCriteria criteria) {
		return executeQuery(criteria);
	}

}
