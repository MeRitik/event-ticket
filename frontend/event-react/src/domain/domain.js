const EventStatus = {
  DRAFT: "DRAFT",
  PUBLISHED: "PUBLISHED",
  CANCELLED: "CANCELLED",
  COMPLETED: "COMPLETED"
};

const TicketStatus = {
  PURCHASED: "PURCHASED",
  CANCELLED: "CANCELLED"
};

const TicketValidationMethod = {
  QR_SCAN: "QR_SCAN",
  MANUAL: "MANUAL"
};

const TicketValidationStatus = {
  VALID: "VALID",
  INVALID: "INVALID",
  EXPIRED: "EXPIRED"
};

function isErrorResponse(obj) {
  return (
    obj &&
    typeof obj === "object" &&
    "error" in obj &&
    typeof obj.error === "string"
  );
}

// Event Management Class
class EventManager {
  constructor() {
    this.events = new Map();
    this.tickets = new Map();
    this.nextEventId = 1;
    this.nextTicketId = 1;
  }

  // Create a new event
  createEvent(eventData) {
    try {
      // Validate required fields
      if (!eventData.name || !eventData.venue || !eventData.status) {
        return { error: "Missing required fields: name, venue, or status" };
      }

      if (!eventData.ticketTypes || !Array.isArray(eventData.ticketTypes)) {
        return { error: "ticketTypes must be an array" };
      }

      // Validate ticket types
      for (const ticketType of eventData.ticketTypes) {
        if (!ticketType.name || typeof ticketType.price !== 'number' || !ticketType.description) {
          return { error: "Each ticket type must have name, price, and description" };
        }
      }

      const eventId = this.nextEventId++;
      const now = new Date();

      // Process ticket types with IDs
      const ticketTypes = eventData.ticketTypes.map((ticketType, index) => ({
        id: `${eventId}-tt-${index + 1}`,
        name: ticketType.name,
        price: ticketType.price,
        description: ticketType.description,
        totalAvailable: ticketType.totalAvailable
      }));

      const event = {
        id: eventId.toString(),
        name: eventData.name,
        start: eventData.start ? new Date(eventData.start) : undefined,
        end: eventData.end ? new Date(eventData.end) : undefined,
        venue: eventData.venue,
        salesStart: eventData.salesStart ? new Date(eventData.salesStart) : undefined,
        salesEnd: eventData.salesEnd ? new Date(eventData.salesEnd) : undefined,
        status: eventData.status,
        ticketTypes: ticketTypes,
        createdAt: now,
        updatedAt: now
      };

      this.events.set(eventId.toString(), event);
      return event;
    } catch (error) {
      return { error: `Failed to create event: ${error.message}` };
    }
  }

  // Update an existing event
  updateEvent(eventData) {
    try {
      if (!eventData.id) {
        return { error: "Event ID is required for update" };
      }

      const existingEvent = this.events.get(eventData.id);
      if (!existingEvent) {
        return { error: "Event not found" };
      }

      // Process ticket types
      const ticketTypes = eventData.ticketTypes.map((ticketType, index) => ({
        id: ticketType.id || `${eventData.id}-tt-${index + 1}`,
        name: ticketType.name,
        price: ticketType.price,
        description: ticketType.description,
        totalAvailable: ticketType.totalAvailable
      }));

      const updatedEvent = {
        ...existingEvent,
        name: eventData.name,
        start: eventData.start ? new Date(eventData.start) : undefined,
        end: eventData.end ? new Date(eventData.end) : undefined,
        venue: eventData.venue,
        salesStart: eventData.salesStart ? new Date(eventData.salesStart) : undefined,
        salesEnd: eventData.salesEnd ? new Date(eventData.salesEnd) : undefined,
        status: eventData.status,
        ticketTypes: ticketTypes,
        updatedAt: new Date()
      };

      this.events.set(eventData.id, updatedEvent);
      return updatedEvent;
    } catch (error) {
      return { error: `Failed to update event: ${error.message}` };
    }
  }

  // Get all events with pagination
  getEvents(page = 0, size = 10) {
    const allEvents = Array.from(this.events.values());
    const start = page * size;
    const end = start + size;
    const pageContent = allEvents.slice(start, end);

    return {
      content: pageContent,
      pageable: {
        sort: { empty: true, sorted: false, unsorted: true },
        offset: start,
        pageNumber: page,
        pageSize: size,
        paged: true,
        unpaged: false
      },
      last: end >= allEvents.length,
      totalElements: allEvents.length,
      totalPages: Math.ceil(allEvents.length / size),
      size: size,
      number: page,
      sort: { empty: true, sorted: false, unsorted: true },
      first: page === 0,
      numberOfElements: pageContent.length,
      empty: pageContent.length === 0
    };
  }

  // Get published events only
  getPublishedEvents() {
    return Array.from(this.events.values())
      .filter(event => event.status === EventStatus.PUBLISHED)
      .map(event => ({
        id: event.id,
        name: event.name,
        start: event.start,
        end: event.end,
        venue: event.venue
      }));
  }

  // Get event details by ID
  getEventDetails(eventId) {
    const event = this.events.get(eventId);
    if (!event) {
      return { error: "Event not found" };
    }
    return event;
  }

  // Get published event details
  getPublishedEventDetails(eventId) {
    const event = this.events.get(eventId);
    if (!event) {
      return { error: "Event not found" };
    }
    
    if (event.status !== EventStatus.PUBLISHED) {
      return { error: "Event is not published" };
    }

    return {
      id: event.id,
      name: event.name,
      start: event.start,
      end: event.end,
      venue: event.venue,
      ticketTypes: event.ticketTypes.map(tt => ({
        id: tt.id,
        name: tt.name,
        price: tt.price,
        description: tt.description
      }))
    };
  }

  // Purchase ticket
  purchaseTicket(eventId, ticketTypeId, quantity = 1) {
    const event = this.events.get(eventId);
    if (!event) {
      return { error: "Event not found" };
    }

    if (event.status !== EventStatus.PUBLISHED) {
      return { error: "Event is not available for ticket purchase" };
    }

    const ticketType = event.ticketTypes.find(tt => tt.id === ticketTypeId);
    if (!ticketType) {
      return { error: "Ticket type not found" };
    }

    // Check availability if totalAvailable is set
    if (ticketType.totalAvailable) {
      const soldTickets = Array.from(this.tickets.values())
        .filter(ticket => ticket.ticketType.id === ticketTypeId && ticket.status === TicketStatus.PURCHASED)
        .length;
      
      if (soldTickets + quantity > ticketType.totalAvailable) {
        return { error: "Not enough tickets available" };
      }
    }

    // Check sales period
    const now = new Date();
    if (event.salesStart && now < event.salesStart) {
      return { error: "Sales have not started yet" };
    }
    if (event.salesEnd && now > event.salesEnd) {
      return { error: "Sales have ended" };
    }

    const tickets = [];
    for (let i = 0; i < quantity; i++) {
      const ticketId = this.nextTicketId++;
      const ticket = {
        id: ticketId.toString(),
        status: TicketStatus.PURCHASED,
        price: ticketType.price,
        description: ticketType.description,
        eventName: event.name,
        eventVenue: event.venue,
        eventStart: event.start,
        eventEnd: event.end,
        ticketType: {
          id: ticketType.id,
          name: ticketType.name,
          price: ticketType.price
        }
      };
      
      this.tickets.set(ticketId.toString(), ticket);
      tickets.push(ticket);
    }

    return tickets;
  }

  // Get user tickets
  getUserTickets(userId) {
    // In a real system, you'd filter by userId
    return Array.from(this.tickets.values()).map(ticket => ({
      id: ticket.id,
      status: ticket.status,
      ticketType: ticket.ticketType
    }));
  }

  // Get ticket details
  getTicketDetails(ticketId) {
    const ticket = this.tickets.get(ticketId);
    if (!ticket) {
      return { error: "Ticket not found" };
    }
    return ticket;
  }

  // Cancel ticket
  cancelTicket(ticketId) {
    const ticket = this.tickets.get(ticketId);
    if (!ticket) {
      return { error: "Ticket not found" };
    }

    if (ticket.status === TicketStatus.CANCELLED) {
      return { error: "Ticket is already cancelled" };
    }

    ticket.status = TicketStatus.CANCELLED;
    this.tickets.set(ticketId, ticket);
    return ticket;
  }

  // Validate ticket
  validateTicket(ticketId, method = TicketValidationMethod.MANUAL) {
    const ticket = this.tickets.get(ticketId);
    if (!ticket) {
      return {
        ticketId: ticketId,
        status: TicketValidationStatus.INVALID
      };
    }

    if (ticket.status === TicketStatus.CANCELLED) {
      return {
        ticketId: ticketId,
        status: TicketValidationStatus.INVALID
      };
    }

    // Check if event has ended (expired)
    if (ticket.eventEnd && new Date() > ticket.eventEnd) {
      return {
        ticketId: ticketId,
        status: TicketValidationStatus.EXPIRED
      };
    }

    return {
      ticketId: ticketId,
      status: TicketValidationStatus.VALID
    };
  }
}

// Usage Example
const eventManager = new EventManager();

// Create an event
const newEvent = eventManager.createEvent({
  name: "Summer Music Festival",
  start: new Date("2024-07-15T18:00:00"),
  end: new Date("2024-07-15T23:00:00"),
  venue: "Central Park",
  salesStart: new Date("2024-06-01T00:00:00"),
  salesEnd: new Date("2024-07-14T23:59:59"),
  status: EventStatus.PUBLISHED,
  ticketTypes: [
    {
      name: "General Admission",
      price: 50,
      description: "General admission to the festival",
      totalAvailable: 1000
    },
    {
      name: "VIP",
      price: 150,
      description: "VIP access with premium seating",
      totalAvailable: 100
    }
  ]
});

console.log("Created Event:", newEvent);

// Purchase tickets
if (!isErrorResponse(newEvent)) {
  const tickets = eventManager.purchaseTicket(newEvent.id, newEvent.ticketTypes[0].id, 2);
  console.log("Purchased Tickets:", tickets);
  
  // Validate a ticket
  if (!isErrorResponse(tickets) && tickets.length > 0) {
    const validation = eventManager.validateTicket(tickets[0].id, TicketValidationMethod.QR_SCAN);
    console.log("Ticket Validation:", validation);
  }
}

// Get all events with pagination
const paginatedEvents = eventManager.getEvents(0, 5);
console.log("Paginated Events:", paginatedEvents);

if (typeof window !== 'undefined') {
  window.EventTicketingSystem = {
    EventManager,
    EventStatus,
    TicketStatus,
    TicketValidationMethod,
    TicketValidationStatus,
    isErrorResponse
  };
}