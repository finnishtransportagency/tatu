package fi.liikennevirasto.tatu.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "tatu_settings")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Settings implements Serializable {

	private static final long serialVersionUID = 4094488287063311880L;

	@Id
	@Column(name = "id", nullable = false)
    private String id;

	@Column(name = "ttl", nullable = true)
    private int ttl;

	public int getTtl() {
		return ttl;
	}
}
