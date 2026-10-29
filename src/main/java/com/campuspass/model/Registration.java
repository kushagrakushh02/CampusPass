package com.campuspass.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Registration {
    private int id;
    private int userId;
    private int eventId;
    private int typeId;
    private int quantity;
    private String ticketCode;
    private String status;
    private Timestamp bookedAt;

    // joined for display
    private String eventTitle;
    private String venue;
    private String category;
    private Timestamp eventDate;
    private String typeLabel;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String userName;
    private String userEmail;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getBookedAt() { return bookedAt; }
    public void setBookedAt(Timestamp bookedAt) { this.bookedAt = bookedAt; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Timestamp getEventDate() { return eventDate; }
    public void setEventDate(Timestamp eventDate) { this.eventDate = eventDate; }
    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public boolean isFree() { return getTotal().signum() == 0; }
    public boolean isConfirmed() { return "CONFIRMED".equals(status); }
    public boolean isUpcoming() { return eventDate != null && eventDate.after(new Timestamp(System.currentTimeMillis())); }
    public boolean isCancellable() { return isConfirmed() && isUpcoming(); }

    /** Same helper as Event.getCategoryClass(), so a ticket can reuse the banner colours. */
    public String getCategoryClass() {
        return category == null ? "general" : category.toLowerCase().replaceAll("[^a-z]", "");
    }
}
