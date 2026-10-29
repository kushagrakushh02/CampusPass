package com.campuspass.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String title;
    private String category = "General";
    private String venue;
    private String description;
    private String status = "ACTIVE";
    private Timestamp eventDate;

    // numbers worked out by SQL (sums over ticket_types / registrations)
    private int totalSeats;
    private int seatsLeft;
    private int ticketsSold;
    private BigDecimal minPrice = BigDecimal.ZERO;
    private BigDecimal revenue = BigDecimal.ZERO;

    private List<TicketType> ticketTypes = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getEventDate() { return eventDate; }
    public void setEventDate(Timestamp eventDate) { this.eventDate = eventDate; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getSeatsLeft() { return seatsLeft; }
    public void setSeatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; }
    public int getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(int ticketsSold) { this.ticketsSold = ticketsSold; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public List<TicketType> getTicketTypes() { return ticketTypes; }
    public void setTicketTypes(List<TicketType> ticketTypes) { this.ticketTypes = ticketTypes; }

    public boolean isCancelled() { return "CANCELLED".equals(status); }

    public boolean isPast() {
        return eventDate != null && eventDate.before(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isFree() { return minPrice == null || minPrice.signum() == 0; }

    /** True when a visitor could still buy at least one ticket. */
    public boolean isBookable() { return !isCancelled() && !isPast() && seatsLeft > 0; }

    public int getPercentSold() {
        return totalSeats == 0 ? 0 : (int) Math.round(100.0 * (totalSeats - seatsLeft) / totalSeats);
    }

    /** Short code used by CSS for the badge colour. */
    public String getAvailability() {
        if (isCancelled()) return "cancelled";
        if (isPast()) return "ended";
        if (totalSeats == 0) return "soon";
        if (seatsLeft <= 0) return "soldout";
        if (seatsLeft * 5 <= totalSeats) return "limited";
        return "open";
    }

    public String getAvailabilityLabel() {
        switch (getAvailability()) {
            case "cancelled": return "Cancelled";
            case "ended":     return "Ended";
            case "soon":      return "Tickets soon";
            case "soldout":   return "Sold out";
            case "limited":   return "Few seats left";
            default:          return "Open";
        }
    }

    /** Lower-case letters only, e.g. "Open Mic" becomes "openmic"; matches the .cat-xxx CSS classes. */
    public String getCategoryClass() {
        return category == null ? "general" : category.toLowerCase().replaceAll("[^a-z]", "");
    }

    public String getIcon() {
        switch (getCategoryClass()) {
            case "technology": return "\uD83D\uDCBB";        // laptop
            case "cultural":   return "\uD83C\uDFAD";        // masks
            case "sports":     return "\uD83C\uDFC6";        // trophy
            case "workshop":   return "\uD83D\uDEE0\uFE0F";  // tools
            case "seminar":    return "\uD83C\uDFA4";        // microphone
            case "fest":       return "\uD83C\uDF89";        // party popper
            case "career":     return "\uD83D\uDCBC";        // briefcase
            case "music":      return "\uD83C\uDFB5";        // note
            default:           return "\u2728";              // sparkles
        }
    }

    /** Recalculates the seat totals from the loaded ticket types (used on the detail page). */
    public void recalcFromTypes() {
        int total = 0, left = 0;
        BigDecimal min = null;
        for (TicketType t : ticketTypes) {
            total += t.getTotalSeats();
            left += t.getSeatsLeft();
            if (min == null || t.getPrice().compareTo(min) < 0) min = t.getPrice();
        }
        totalSeats = total;
        seatsLeft = left;
        minPrice = min == null ? BigDecimal.ZERO : min;
    }
}
