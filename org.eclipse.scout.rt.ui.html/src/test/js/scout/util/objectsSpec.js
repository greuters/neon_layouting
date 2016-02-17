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
describe("scout.objects", function() {

  describe("copyProperties", function() {

    it("check if all properties are copied", function() {
      var dest = {}, source = {
          foo: 6,
          bar: 7
        };
      scout.objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it("check if properties from prototype are copied", function() {
      var dest = {};
      var TestConstructor = function() {
        this.foo = 6;
        this.bar = 7;
      };
      var source = new TestConstructor();
      source.qux = 8;

      scout.objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
      expect(dest.qux).toBe(8);
    });

  });

  describe("countProperties", function() {

    it("check if all properties are counted", function() {
      var o = {
          first: 1,
          second: 2
      };
      var F = function() {
        this.foo = 66;
        this.bar = 777;
      };
      F.myProp = 'hello';
      F.prototype.anotherProp = 'goodbye';
      var x = new F();
      var y = {};
      scout.objects.copyProperties(x, y);
      y.qux = 9999;

      expect(scout.objects.countProperties(o)).toBe(2); // first, second
      expect(scout.objects.countProperties(F)).toBe(1); // myProp
      expect(scout.objects.countProperties(x)).toBe(2); // foo, bar (but not myProp or anotherProp)
      expect(scout.objects.countProperties(y)).toBe(4); // foo, bar, anotherProp, qux (because copyProperties also copies properties from prototype)
    });

  });

  describe("valueCopy", function() {

    it("copies an object by value", function() {
      var o = {
          first: 1,
          second: 2,
          arr: [],
          arr2: [ {name: 'Hans'}, {name: 'Linda'} ],
          hamlet: {
            type: 'Book',
            title: { shortTitle: 'Hamlet', longTitle: 'The Tragicall Historie of Hamlet, Prince of Denmarke' },
            author: 'Shakespeare',
            refs: [ { type: 'Book', author: 'Dickens', title: '???' }, { type: 'Audio', author: 'Shakespeare', title: 'Hamlet on CD' } ]
          }
      };
      var o2 = scout.objects.valueCopy(o);
      o.first = 'one';
      o.second = 'two';
      o.arr.push('test');
      o.arr2[0].name = 'Dagobert';
      o.hamlet.author = 'Unknown';
      o.hamlet.title.longTitle = 'NO LONG TITLE';
      o.hamlet.refs.push({});

      expect(o2).not.toBe(o);
      expect(o2.first).toBe(1);
      expect(o2.second).toBe(2);
      expect(o2.arr).toEqual([]);
      expect(o2.arr2[0].name).toBe('Hans');
      expect(o2.hamlet.author).toBe('Shakespeare');
      expect(o2.hamlet.title.longTitle).toBe('The Tragicall Historie of Hamlet, Prince of Denmarke');
      expect(o2.hamlet.refs.length).toBe(2);
    });

  });

  describe('isNumeric', function() {
    it('returns true when argument is a number', function() {
      expect(scout.objects.isNumber(0)).toBe(true);
      expect(scout.objects.isNumber(1)).toBe(true);
      expect(scout.objects.isNumber(1.0)).toBe(true);
      expect(scout.objects.isNumber(-1)).toBe(true);
      expect(scout.objects.isNumber('0x0a')).toBe(true); // valid hex-value
      expect(scout.objects.isNumber(null)).toBe(false); // a number reference could be null

      expect(scout.objects.isNumber(undefined)).toBe(false);
      expect(scout.objects.isNumber('foo')).toBe(false);
    });
  });

  describe('values', function() {
    it('returns object values', function() {
      var Class = function() {
        this.a = 'A';
        this.b = 'B';
      };
      var o1 = {
          a: 'X',
          b: 'Y',
          c: 'Z'
      };
      var o2 = new Class();
      o2.a = 'X';
      o2.c = 'C';

      expect(scout.objects.values()).toEqual([]);
      expect(scout.objects.values(null)).toEqual([]);
      expect(scout.objects.values(undefined)).toEqual([]);
      expect(scout.objects.values({})).toEqual([]);
      expect(scout.objects.values(o1).length).toBe(3);
      expect(scout.objects.values(o2).length).toBe(3);
      expect(scout.objects.values(o1)).toContain('X');
      expect(scout.objects.values(o1)).toContain('Y');
      expect(scout.objects.values(o1)).toContain('Z');
      expect(scout.objects.values(o2)).toContain('X'); // not A
      expect(scout.objects.values(o2)).toContain('B');
      expect(scout.objects.values(o2)).toContain('C');
    });
  });

});
