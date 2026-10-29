package com.campuspass.model;

import java.math.BigDecimal;

public class TicketType {
    private int id;
    private int eventId;
    private String label;
    private BigDecimal price = BigDecimal.ZERO;
    private int totalSeats;
    private int seatsLeft;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getSeatsLeft() { return seatsLeft; }
    public void setSeatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; }

    public boolean isFree() { return price == null || price.signum() == 0; }
    public boolean isSoldOut() { return seatsLeft <= 0; }
    public int getSold() { return totalSeats - seatsLeft; }
}
