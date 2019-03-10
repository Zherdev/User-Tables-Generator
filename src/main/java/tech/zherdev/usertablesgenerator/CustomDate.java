/*
 * CustomDate
 *
 * Ivan Zherdev, 2019
 */
package tech.zherdev.usertablesgenerator;

import java.util.GregorianCalendar;
import java.security.SecureRandom;

/**
 * Класс CustomDate предназначен работы с датами.
 *
 * @author Ivan Zherdev
 */
public class CustomDate {

    private static final SecureRandom random = new SecureRandom();
    private static final int lowestDate = 1918;
    private static final int highestBound = 100;

    /* Работа с датой ведется при помощи класса GregorianCalendar */
    private GregorianCalendar calendar;

    /** Конструктор класса CustomDate для случайной даты */
    CustomDate() {
        int year = random.nextInt(highestBound) + lowestDate;
        int day;
        calendar = new GregorianCalendar();
        calendar.set(calendar.YEAR, year);
        day = random.nextInt(calendar.getActualMaximum(calendar.DAY_OF_YEAR));
        calendar.set(calendar.DAY_OF_YEAR, day);
    }

    /** Конструктор класса CustomDate для заданной в секундах даты */
    CustomDate(long timestamp) {
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp * 1000);
    }

    /** @return возвращает хранящуюся дату в календаре */
    public GregorianCalendar getCalendar() {
        return calendar;
    }

    /**
     * Метод countPassedYears() вычисляет, сколько лет прошло с даты,
     * хранящейся в объете CustomDate, до сегодняшнего дня.
     *
     * @return Количество лет
     */
    public int countPassedYears() {
        GregorianCalendar today = new GregorianCalendar();
        int diff = today.get(calendar.YEAR) - calendar.get(calendar.YEAR);
        if (calendar.get(calendar.MONTH) > today.get(calendar.MONTH) ||
                (calendar.get(calendar.MONTH) == today.get(calendar.MONTH) && calendar.get(calendar.DATE) > today.get(calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    /**
     * Переопределенный метод toString() преобразует дату в строку.
     *
     * @return ДД-ММ-ГГГГ
     */
    @Override
    public String toString() {
        return String.format("%02d", calendar.get(calendar.DAY_OF_MONTH)) + "-"
               + String.format("%02d", calendar.get(calendar.MONTH)) + "-"
               + String.format("%04d", calendar.get(calendar.YEAR));
    }

}
