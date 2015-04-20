package models;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Message {
	@Id
	private int		UUID;
	private String	message;
	
	@ManyToOne
	private User	sender;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date	sent;
	
	public Message(String message, User sender) {
		this.message	= message;
		this.sender		= sender;
		this.sent 		= new Date(System.currentTimeMillis());
		String toHash	= this.sender.getUsername() + this.sent.getTime();
		this.UUID		= toHash.hashCode();
	}
	
	public Message(int UUID, String message, User sender, Date sent) {
		this.UUID		= UUID;
		this.message	= message;
		this.sender		= sender;
		this.sent		= sent;
	}
	
	public int getUUID() {
		return this.UUID;
	}
}
