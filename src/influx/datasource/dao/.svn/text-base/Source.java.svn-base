/**
 * @Shockley Xiang Li
 * 2012-3-29
 */
package influx.datasource.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Shockley
 *
 */
@Entity
@Table(name = "onto_sources")
public class Source {
	@Id @GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "description")
	private String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
