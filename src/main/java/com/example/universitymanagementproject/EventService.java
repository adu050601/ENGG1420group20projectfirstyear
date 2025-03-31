package com.example.universitymanagementproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventService {
    private List<Event> events;
    private ExcelDataManager excelDataManager;

    public EventService(ExcelDataManager excelDataManager) {
        this.excelDataManager = excelDataManager;
        this.events = loadEventsFromExcel();
    }

    public List<Event> getAllEvents() {
        return events;
    }

    private List<Event> loadEventsFromExcel() {
        this.events = new ArrayList<>();
        try {
            this.events = excelDataManager.readEvents();
        } catch (IOException e) {
            System.out.println("Error reading events from Excel file.");
        }
        return this.events;
    }

    private void saveEventsToExcel() {
        try {
            excelDataManager.writeEvents(events);
        } catch (IOException e) {
            System.out.println("Error writing events to Excel file.");
        }
    }

    public boolean addEvent(Event event) {
        for (Event existingEvent : events) {
            if (existingEvent.getEventCode().equals(event.getEventCode())) {
                System.out.println("Event with code " + event.getEventCode() + " already exists.");
                return false;
            }
        }
        events.add(event);
        saveEventsToExcel();
        return true;
    }

    public Optional<Event> getEventByCode(String eventCode) {
        for (Event event : events) {
            if (event.getEventCode().equals(eventCode)) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    public boolean updateEvent(Event updateEvent) {
        Optional<Event> existingEvent =  getEventByCode(updateEvent.getEventCode());
        if (existingEvent.isPresent()){
            int index = events.indexOf(existingEvent.get());
            events.set(index, updateEvent);
            saveEventsToExcel();
            return true;
        }
        return false;
    }

    public boolean deleteEvent(String eventCode) {
        Optional<Event> existingEvent =  getEventByCode(eventCode);
        if (existingEvent.isPresent()){
            events.remove(existingEvent.get());
            saveEventsToExcel();
            return true;
        }
        return false;
    }

    public boolean registerStudent(String eventCode, String studentId) {
        Optional<Event> optionalEvent = getEventByCode(eventCode);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (event.isRegistered(studentId)) {
                return false;
            }

            if (event.isFull() && !event.isRegistered(studentId)) {
                return false;
            }

            if (event.registerStudent(studentId)) {
                saveEventsToExcel();
                return true;
            }
        }
        return false;
    }

    public boolean unregisterStudent(String eventCode, String studentId) {
        Optional<Event> optionalEvent = getEventByCode(eventCode);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (!event.isRegistered(studentId)) {
                return false;
            }
            event.unregisterStudent(studentId);
            saveEventsToExcel();
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        ExcelDataManager excelDataManager = new ExcelDataManager();
        EventService eventService = new EventService(excelDataManager);
        List<Event> events = eventService.getAllEvents();
        for (Event event : events) {
            System.out.println(event);
        }
    }
}
