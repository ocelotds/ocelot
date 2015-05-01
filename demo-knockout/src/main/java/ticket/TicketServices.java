package ticket;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;

/**
 *
 * @author hhfrancois
 */
@Singleton
@DataService(resolver = Constants.Resolver.CDI)
public class TicketServices {

	private List<Ticket> tickets;
	
	@PostConstruct
	private void init() {
		tickets = new ArrayList<>();
		tickets.add(new Ticket("Economy", 199.95));
		tickets.add(new Ticket("Business", 449.22));
		tickets.add(new Ticket("First Class", 1199.99));
	}

	public List<Ticket> getTickets() {
		return tickets;
	}
	
	
}
