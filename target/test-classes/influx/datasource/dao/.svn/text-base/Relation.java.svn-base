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
@Table(name = "onto_relations")
public class Relation {
	@Id @GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "source_id") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Source source;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "from_concept") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Term from;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "to_concept") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Term to;
	
	@Column(name = "is_broader")
	private boolean isBroader;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Term getFrom() {
		return from;
	}

	public void setFrom(Term from) {
		this.from = from;
	}

	public Term getTo() {
		return to;
	}

	public void setTo(Term to) {
		this.to = to;
	}

	public boolean isBroader() {
		return isBroader;
	}

	public void setBroader(boolean isBroader) {
		this.isBroader = isBroader;
	}
}
