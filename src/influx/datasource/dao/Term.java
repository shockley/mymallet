/**
 * @Shockley Xiang Li
 * 2012-3-29
 */
package influx.datasource.dao;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Shockley
 *
 */
@Entity
@Table(name = "onto_terms")
public class Term {
	@Id @GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;

	/*@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "forge_id") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Forge forge;
	
	public void setForge(Forge forge) {
	this.forge = forge;
}

public Forge getForge() {
	return forge;
}*/
	
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	
	
	public int hashCode(){
		return id.intValue();
	}
	public boolean equals(Object o){
		if(o==null || !(o instanceof Term))
			return false;
		Term that = (Term) o;
		if(id == null){
			if(that.id != null)
				return false;
		}else{
			if(!id.equals(that.id))
				return false;
		}
		return true;
	}
}