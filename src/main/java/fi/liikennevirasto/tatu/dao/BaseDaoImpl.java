package fi.liikennevirasto.tatu.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseDaoImpl {

	@Autowired
	SessionFactory sessionFactory;

	protected Object findByUniqueField(Class<?> classType, String fieldName, Object value) {
		Session session = sessionFactory.getCurrentSession(); 
		Criteria criteria = session.createCriteria(classType).add(Restrictions.eq(fieldName, value));
		criteria.setCacheable(true);
		
		List<?> list = criteria.list();
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	List executeQuery(DetachedCriteria criteria) {
		Criteria ec = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		//ec.setCacheable(true);
		return ec.list();
	}
	
}


