package models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Message {
	@Id
	@GeneratedValue
	private long	id;
	
	private int		UUID;
	private String	message;
	
	@ManyToOne
	private User	sender;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp	sent;
	
	public Message(String message, User sender) {
		this.message	= message;
		this.sender		= sender;
		this.sent 		= new Timestamp(System.currentTimeMillis());
		String toHash	= this.sender.getUsername() + this.sent.getTime();
		this.UUID		= toHash.hashCode();
	}
	
	public Message(int UUID, String message, User sender, Timestamp sent) {
		this.UUID		= UUID;
		this.message	= message;
		this.sender		= sender;
		this.sent		= sent;
	}
	
	public long getId() {
		return this.id;
	}
	
	public int getUUID() {
		return this.UUID;
	}
	public String getMessage() {
		return this.message;
	}
	public String getSender() {
		return this.sender.getUsername();
	}
	public String getSentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		return sdf.format(this.sent);
	}
}
