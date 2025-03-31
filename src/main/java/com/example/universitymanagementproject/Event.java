package com.example.universitymanagementproject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventName;
    private String eventCode;
    private String description;
    private String headerImagePath;
    private String location;
    private LocalDateTime dateTime;
    private int capacity;
    private double cost;
    private List<String> registeredStudents;

    public Event(String eventName, String eventCode, String description, String headerImagePath, String location, LocalDateTime dateTime, int capacity, double cost) {
        this.eventName = eventName;
        this.eventCode = eventCode;
        this.description = description;
        this.headerImagePath = headerImagePath;
        this.location = location;
        this.dateTime = dateTime;
        this.capacity = capacity;
        this.cost = cost;
        this.registeredStudents = new ArrayList<>();
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventCode() {
        return eventCode;
    }

    public String getDescription() {
        return description;
    }

    public String getHeaderImagePath() {
        return headerImagePath;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getCost() {
        return cost;
    }

    public List<String> getRegisteredStudents() {
        return registeredStudents;
    }

    public boolean isRegistered(String username) {
        return registeredStudents.contains(username);
    }

    public boolean registerStudent(String username) {
        if (registeredStudents.size() < capacity) {
            registeredStudents.add(username);
            return true;
        }
        return false;
    }

    public void unregisterStudent(String username) {
        registeredStudents.remove(username);
    }

    public boolean isFull() {
        return registeredStudents.size() >= capacity;
    }

    public int getAvailableSeats() {
        return capacity - registeredStudents.size();
    }

    public int getCurrentNumberOfRegisteredStudents() {
        return registeredStudents.size();
    }

    public void setRegisteredStudents(List<String> registeredStudents) {
        if (registeredStudents == null) {
            registeredStudents = new ArrayList<>();
        }
        if (registeredStudents.size() > capacity) {
            throw new IllegalArgumentException("Number of registered students exceeds capacity");
        }
        this.registeredStudents = registeredStudents;
    }

    public String toString() {
        return "Event{" +
                "eventName='" + eventName + '\'' +
                ", eventCode='" + eventCode + '\'' +
                ", description='" + description + '\'' +
                ", headerImagePath='" + headerImagePath + '\'' +
                ", location='" + location + '\'' +
                ", dateTime=" + dateTime +
                ", capacity=" + capacity +
                ", cost=" + cost +
                ", registeredStudents=" + registeredStudents +
                '}';
    }

    public static void main(String[] args) {
        Event halloweenParty = new Event("Halloween Party", "HALLOWEEN", "A spooky party for everyone!", "halloween.jpg", "Main Hall", LocalDateTime.of(2021, 10, 31, 20, 0), 100, 10.0);

        System.out.println(halloweenParty.getAvailableSeats());
        halloweenParty.registerStudent("alice");
        halloweenParty.registerStudent("bob");
        System.out.println(halloweenParty.getAvailableSeats());
    }
}
