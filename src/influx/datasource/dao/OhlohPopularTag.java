package influx.datasource.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dao class of Ohloh tagged
 * 
 * @author ZhaoBiaocheng
 * 
 */
@Entity
@Table(name = "ohloh_popular_tag")
public class OhlohPopularTag {
	@Id
	@GeneratedValue
	@Column(name = "id")
	public long id;

	@Column(name = "description")
	private String desc;

	@Column(name = "proj_count")
	private String projCount;
	
	public String getDescription() {
		return desc;
	}

	public void setDescription(String desc) {
		this.desc = desc;
	}

	public void setProjCount(String projCount) {
		this.projCount = projCount;
	}

	public String getProjCount() {
		return projCount;
	}

}
