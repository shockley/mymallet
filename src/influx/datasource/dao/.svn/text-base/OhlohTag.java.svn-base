package influx.datasource.dao;

import java.util.Date;

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
 * Dao class of Ohloh tagged
 * 
 * @author ZhaoBiaocheng
 * 
 */
@Entity
@Table(name = "ohloh_tagged")
public class OhlohTag {
	@Id
	@GeneratedValue
	@Column(name = "id")
	public long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "proj_id")
	// necessary
	@Basic(fetch = FetchType.LAZY)
	private Project project;
	
	@Column(name = "description")
	private String desc;

	@Column(name = "date_collected")
	private Date timeStamp;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return desc;
	}

	public void setDescription(String desc) {
		this.desc = desc;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}
	
	public int hashCode(){
		int hash = 0;
		if(desc != null)
			hash += desc.hashCode();
		if(desc != null)
			hash += 31 * project.hashCode();
		return hash;
	}
	public boolean equals(Object o){
		if(o==null || !(o instanceof OhlohTag))
			return false;
		OhlohTag that = (OhlohTag) o;
		if(desc == null){
			if(that.desc != null)
				return false;
		}else{
			if(!desc.equals(that.desc))
				return false;
		}
		
		if(project == null){
			if(that.project != null)
				return false;
		}else{
			if(!project.equals(that.project))
				return false;
		}
		
		return true;
	}
}
