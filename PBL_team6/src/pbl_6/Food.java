package pbl_6;
import java.time.*;
public class Food {
	String name;
	String id;
	String type;
	LocalDate expirationDate;
	int volume;
	
	public LocalDate getExpirationDate() {
        return this.expirationDate;
    }
    
    public String getName() {
        return this.name;
    }
}
