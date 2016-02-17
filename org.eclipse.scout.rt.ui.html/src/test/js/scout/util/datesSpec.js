/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scout.dates", function() {

  describe("shift", function() {

    // Note: Test dates need explicit time zone setting, otherwise the result of toISOString (which always
    // returns UTC dates) would depend on the browser's time zone. For convenience, we use UTC as well ("+00:00").

    it("shifts year or month or day", function() {
      var date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

      date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-21 00:00:00.000').toISOString());

      date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 0, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-22 00:00:00.000').toISOString());
    });

    it("shifts year and month if both provided", function() {
      var date = scout.dates.create('2014-01-01');
      expect(scout.dates.shift(date, 1, 1).toISOString()).toBe(scout.dates.create('2015-02-01 00:00:00.000').toISOString());
    });

    it("shifts year and month and day if all provided", function() {
      var date = scout.dates.create('2014-01-01');
      expect(scout.dates.shift(date, 1, 1, 1).toISOString()).toBe(scout.dates.create('2015-02-02 00:00:00.000').toISOString());
    });

    describe("shift year", function() {

      it("adds or removes years", function() {
        var date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, -1).toISOString()).toBe(scout.dates.create('2013-11-01 00:00:00.000').toISOString());
      });

      it("handles edge case leap year", function() {
        var date = scout.dates.create('2016-02-29T00:00:00.000');
        expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2017-02-28 00:00:00.000').toISOString());
      });

    });

    describe("shift month", function() {

      it("adds or removes months", function() {
        var date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-12-21');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (saving->normal)
        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-10-21 00:00:00.000').toISOString());
        date = scout.dates.create('2014-10-21');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (normal->saving)
        date = scout.dates.create('2014-04-10');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-03-10 00:00:00.000').toISOString());
        date = scout.dates.create('2014-03-10');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-04-10 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe(scout.dates.create('2013-11-21 00:00:00.000').toISOString());
      });

      it("handles edge case start month", function() {
        var date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-10-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-11-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe(scout.dates.create('2013-11-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe(scout.dates.create('2015-12-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe(scout.dates.create('2013-10-01 00:00:00.000').toISOString());
      });

      it("handles edge case end month", function() {
        var date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-09-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-10-31 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe(scout.dates.create('2015-11-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe(scout.dates.create('2013-09-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -25).toISOString()).toBe(scout.dates.create('2012-09-30 00:00:00.000').toISOString());
      });

      it("handles edge case leap year", function() {
        var date = scout.dates.create('2016-02-29');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2017-02-28 00:00:00.000').toISOString());
      });
    });

  });

  describe("shiftToNextDayOfType", function() {

    it("shifts to next day of type", function() {
      var date = scout.dates.create('2015-07-09');
      expect(scout.dates.shiftToNextDayOfType(date, 1).toISOString()).toBe(scout.dates.create('2015-07-13 00:00:00.000').toISOString());

      date = scout.dates.create('2015-07-09');
      expect(scout.dates.shiftToNextDayOfType(date, 6).toISOString()).toBe(scout.dates.create('2015-07-11 00:00:00.000').toISOString());
    });
  });

  describe("shiftToPreviousDayOfType", function() {

    it("shifts to previous day of type", function() {
      var date = scout.dates.create('2015-07-09');
      expect(scout.dates.shiftToPreviousDayOfType(date, 1).toISOString()).toBe(scout.dates.create('2015-07-06 00:00:00.000').toISOString());

      date = scout.dates.create('2015-07-09');
      expect(scout.dates.shiftToPreviousDayOfType(date, 6).toISOString()).toBe(scout.dates.create('2015-07-04 00:00:00.000').toISOString());
    });
  });

  describe("isSameDay", function() {
    it("returns true if day, month and year matches", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2014-11-21 11:13');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-11-20');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-10-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2013-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = new Date('2014-11-21');
      date2 = new Date('2014-11-20T22:00:00.000-02:00');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);
    });

  });


  describe("compareMonths", function() {
    it("returns the differences in number of months", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("ignores time", function() {
      var date = scout.dates.create('2014-11-21T23:00');
      var date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-12-21T15:15');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = scout.dates.create('2014-11-21T20:20');
      date2 = scout.dates.create('2014-10-21T15:10');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("works with different years", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2013-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(12);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2015-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-12);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2013-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(13);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2015-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-13);
    });

  });

  describe("timestamp", function() {

    it("returns a string of the expected length withonly digits", function() {
      var ts = scout.dates.timestamp();
      expect(typeof ts).toBe('string');
      expect(ts.length).toBe(17);
      expect(/^\d+$/.test(ts)).toBe(true);

      var date = scout.dates.create('2014-11-21 00:33:00.000Z');
      expect(scout.dates.timestamp(date, true)).toBe('20141121003300000');
    });

  });

  describe("orderWeekdays", function() {

    it("orders weekdays", function() {
      var weekdays = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
      var check0 = scout.dates.orderWeekdays(weekdays, 0);
      var check1 = scout.dates.orderWeekdays(weekdays, 1);
      var check2 = scout.dates.orderWeekdays(weekdays, 2);
      var check3 = scout.dates.orderWeekdays(weekdays, 3);
      expect(check0.join('-')).toBe('So-Mo-Di-Mi-Do-Fr-Sa');
      expect(check1.join('-')).toBe('Mo-Di-Mi-Do-Fr-Sa-So');
      expect(check2.join('-')).toBe('Di-Mi-Do-Fr-Sa-So-Mo');
      expect(check3.join('-')).toBe('Mi-Do-Fr-Sa-So-Mo-Di');
    });

  });

  describe("toJsonDate / parseJsonDate", function() {

    it("can handle missing or invalid inputs", function() {
      expect(scout.dates.toJsonDate()).toBe(null);
      expect(scout.dates.parseJsonDate()).toBe(null);
      expect(function() {
        scout.dates.parseJsonDate('invalid date string');
      }).toThrow();
    });

    it("can convert JSON and JS dates", function() {
      var date, jsonDate;

      // Test 1 - UTC
      date = scout.dates.parseJsonDate('2014-11-21 00:00:00.000Z');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      jsonDate = scout.dates.toJsonDate(date, true);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000Z');
      // Date only
      date = scout.dates.parseJsonDate('2014-11-21Z');
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      // Time only
      date = scout.dates.parseJsonDate('15:23:00.123Z');
      expect(date.toISOString()).toBe('1970-01-01T15:23:00.123Z');
      expect(function() {
        scout.dates.parseJsonDate('15:23:00');
      }).toThrow(); // missing millis

      // Test 2 - local time zone
      date = scout.dates.parseJsonDate('2014-11-21 00:00:00.000');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      // We cannot check for the exact value of date, because we don't know the executing browser's time zone.
      // But we can convert it back to JSON, which should result in the original string (because TZ are the same).
      jsonDate = scout.dates.toJsonDate(date);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');
      jsonDate = scout.dates.toJsonDate(date, false); // should be the same as above
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');

      // Test 3 - special cases (UTC)
      date = scout.dates.parseJsonDate('0025-11-21 00:00:00.000Z');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      expect(date.toISOString()).toBe('0025-11-21T00:00:00.000Z');
      jsonDate = scout.dates.toJsonDate(date, true);
      expect(jsonDate).toBe('0025-11-21 00:00:00.000Z');
      // Date only
      date = scout.dates.parseJsonDate('0025-11-21Z');
      expect(date.toISOString()).toBe('0025-11-21T00:00:00.000Z');
    });

  });

  describe("create", function() {

    it("can create dates", function() {
      expect(scout.dates.create()).toBe(undefined);
      expect(scout.dates.create('')).toBe(undefined);
      expect(function() {
        scout.dates.create('invalid date string');
      }).toThrow();

      expect(scout.dates.create('2014').toISOString()).toBe(scout.dates.create('2014-01-01 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10').toISOString()).toBe(scout.dates.create('2014-10-01 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31').toISOString()).toBe(scout.dates.create('2014-10-31 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23').toISOString()).toBe(scout.dates.create('2014-10-31 23:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58.882').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.882').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58.882Z').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.882Z').toISOString());
    });

  });

  describe("weekInYear", function() {

    it("can calculate week in year", function() {
      expect(scout.dates.weekInYear()).toBe(undefined);
      expect(scout.dates.weekInYear(undefined)).toBe(undefined);
      expect(scout.dates.weekInYear(null)).toBe(undefined);

      // Check week with firstDayOfWeek = monday (1)
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-27'), 1)).toBe(52);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-28'), 1)).toBe(52);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-29'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-30'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-31'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-01'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-02'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-03'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-04'), 1)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-05'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-06'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-07'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-08'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-09'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-10'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-11'), 1)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-12'), 1)).toBe(3);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-26'), 1)).toBe(5);

      // Check week with firstDayOfWeek = sunday (0)
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-27'), 0)).toBe(52);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-28'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-29'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-30'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2014-12-31'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-01'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-02'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-03'), 0)).toBe(53);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-04'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-05'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-06'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-07'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-08'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-09'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-10'), 0)).toBe(1);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-11'), 0)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-12'), 0)).toBe(2);
      expect(scout.dates.weekInYear(scout.dates.create('2015-01-26'), 0)).toBe(4);
    });

  });

  describe("isLeapYear", function() {

    it("correctly identifies leap years", function() {
      expect(scout.dates.isLeapYear()).toBe(false);
      expect(scout.dates.isLeapYear(undefined)).toBe(false);
      expect(scout.dates.isLeapYear(null)).toBe(false);

      expect(scout.dates.isLeapYear(1900)).toBe(false);
      expect(scout.dates.isLeapYear(1996)).toBe(true);
      expect(scout.dates.isLeapYear(1997)).toBe(false);
      expect(scout.dates.isLeapYear(1998)).toBe(false);
      expect(scout.dates.isLeapYear(1999)).toBe(false);
      expect(scout.dates.isLeapYear(2000)).toBe(true);
      expect(scout.dates.isLeapYear(2001)).toBe(false);
      expect(scout.dates.isLeapYear(2002)).toBe(false);
      expect(scout.dates.isLeapYear(2003)).toBe(false);
      expect(scout.dates.isLeapYear(2004)).toBe(true);
      expect(scout.dates.isLeapYear(2005)).toBe(false);
      expect(scout.dates.isLeapYear(2006)).toBe(false);
      expect(scout.dates.isLeapYear(2007)).toBe(false);
      expect(scout.dates.isLeapYear(2008)).toBe(true);
      expect(scout.dates.isLeapYear(2100)).toBe(false);
    });

  });

});
