package com.dsce.eventmanager;

import java.util.ArrayList;
import java.util.List;

public class Event
{
    String Admin;
    String Title;
    String EventDetails;
    String Location;
    String Duration;
    String Date;
    String Time;
    String PDF_URL;
    String FileName;
    int Year,Month,Day,Hour,Minute;
    ArrayList<String> EventMembers;


    public Event()
    {

    }

    public Event(String admin, String title, String eventDetails, String location, String duration, String date, String time, int year, int month, int day, int hour, int minute, ArrayList<String> eventMembers, String url, String filename)
    {
        Admin = admin;
        Title = title;
        EventDetails = eventDetails;
        Location = location;
        Duration = duration;
        Date = date;
        Time = time;
        Year=year;
        Month=month;
        Day = day;
        Hour = hour;
        Minute = minute;
        EventMembers=eventMembers;
        PDF_URL=url;
        FileName=filename;
    }

    public void setYear(int year) {
        Year = year;
    }

    public void setMonth(int month) {
        Month = month;
    }

    public void setDay(int day) {
        Day = day;
    }

    public void setHour(int hour) {
        Hour = hour;
    }

    public void setMinute(int minute) {
        Minute = minute;
    }
    public List<String> getEventMembers() {
        return EventMembers;
    }

    public void setEventMembers(ArrayList<String> eventMembers) {
        EventMembers = eventMembers;
    }
    public int getYear() {
        return Year;
    }

    public int getMonth() {
        return Month;
    }

    public int getDay() {
        return Day;
    }

    public int getHour() {
        return Hour;
    }

    public int getMinute() {
        return Minute;
    }

    public String getAdmin() {
        return Admin;
    }

    public String getTitle() {
        return Title;
    }

    public String getEventDetails() {
        return EventDetails;
    }

    public String getLocation() {
        return Location;
    }

    public String getDuration() {
        return Duration;
    }

    public String getDate() {
        return Date;
    }

    public String getTime() {
        return Time;
    }

    public void setAdmin(String admin) {
        Admin = admin;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setEventDetails(String eventDetails) {
        EventDetails = eventDetails;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getPDF_URL() {
        return PDF_URL;
    }

    public void setPDF_URL(String PDF_URL) {
        this.PDF_URL = PDF_URL;
    }

}
