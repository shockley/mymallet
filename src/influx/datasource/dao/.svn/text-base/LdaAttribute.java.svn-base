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
@Table(name = "onto_ldarecorder")
public class LdaAttribute {
	@Id @GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "attribute_name")
	String attributeName;
	
	@Column(name = "index1")
	int index1;
	
	@Column(name = "index2")
	int index2;
	
	@Column(name = "value")
	String value;
	
	@Column(name = "description")
	String description;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public int getIndex1() {
		return index1;
	}

	public void setIndex1(int index1) {
		this.index1 = index1;
	}

	public int getIndex2() {
		return index2;
	}

	public void setIndex2(int index2) {
		this.index2 = index2;
	}

	
}
