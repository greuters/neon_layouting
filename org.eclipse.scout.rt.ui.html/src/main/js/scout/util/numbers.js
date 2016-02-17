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
scout.numbers = {

  /**
   * Converts the given decimal number to base-62 (i.e. the same value, but
   * represented by [a-zA-Z0-9] instead of only [0-9].
   */
  toBase62: function(number) {
    if (number === undefined) {
      return undefined;
    }
    var symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
    var base = 62;
    var s = '';
    var n;
    while (number >= 1) {
      n = Math.floor(number / base);
      s = symbols[(number - (base * n))] + s;
      number = n;
    }
    return s;
  },

  /**
   * Returns a random sequence of characters out of the set [a-zA-Z0-9] with the
   * given length. The default length is 8.
   */
  randomId: function(length) {
    length = (length !== undefined) ? length : 8;
    var charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    var s = '';
    for (var i = 0; i < length; i++) {
      s += charset[Math.floor(Math.random() * charset.length)];
    }
    return s;
  },

  /**
   * Rounds a number to the given number of decimal places.
   *
   * Numbers should not be rounded with the built-in Number.toFixed() method, since it
   * behaves differently on different browsers. However, it is safe to apply toFixed()
   * to the result of this method to ensure a fixed number of decimal places (filled up
   * with 0's) because this operation does not involve any rounding anymore.
   * <p>
   * If decimalPlaces is omitted, the number will be rounded to integer by default.
   * Rounding mode {@link scout.numbers.RoundingMode.HALF_UP} is used as default.
   */
  round: function(number, roundingMode, decimalPlaces) {
    if (number === null || number === undefined) {
      return number;
    }
    decimalPlaces = decimalPlaces || 0;

    // Do _not_ multiply with powers of 10 here, because that might cause rounding errors!
    // Example: 1.005 with 2 decimal places would result in 100.49999999999999
    number = this.shiftDecimalPoint(number, decimalPlaces);

    switch (roundingMode) {
      case scout.numbers.RoundingMode.UP:
        if (number < 0) {
          number = -Math.ceil(Math.abs(number));
        } else {
          number = Math.ceil(number);
        }
        break;
      case scout.numbers.RoundingMode.DOWN:
        if (number < 0) {
          number = -Math.floor(Math.abs(number));
        } else {
          number = Math.floor(number);
        }
        break;
      case scout.numbers.RoundingMode.CEILING:
        number = Math.ceil(number);
        break;
      case scout.numbers.RoundingMode.FLOOR:
        number = Math.floor(number);
        break;
      case scout.numbers.RoundingMode.HALF_DOWN:
        if (number < 0) {
          number = Math.round(number);
        } else {
          number = -Math.round(-number);
        }
        break;
        // case scout.numbers.RoundingMode.HALF_EVEN:
        // case scout.numbers.RoundingMode.UNNECESSARY:
        // not implemented, default is used.
      default:
        // scout.numbers.RoundingMode.HALF_UP is used as default
        if (number < 0) {
          number = -Math.round(Math.abs(number));
        } else {
          number = Math.round(number);
        }
    }

    number = this.shiftDecimalPoint(number, -decimalPlaces);
    return number;
  },

  /**
   * Shifts the decimal point in the given number by a certain distance. While the result is also
   * number, the method uses string operations to move the decimal point. This prevents rounding
   * errors as long as the number does not exceed JavaScript's Number precision.
   *
   * The argument 'move' describes the distance how far the decimal point should be moved:
   *     0 = do no move      (1.57 --> 1.57)
   *   > 0 = move to right   (1.57 --> 15.7)
   *   < 0 = move to left    (1.57 --> 0.157)
   */
  shiftDecimalPoint: function(number, move) {
    if (number === null || number === undefined || !move) {
      return number;
    }

    var sign = (number ? (number < 0 ? -1 : 1) : 0);
    var distance = Math.abs(move);

    number = Math.abs(number);
    var s = scout.strings.asString(number);
    var a;
    if (move < 0) {
      // move to left
      s = scout.strings.repeat('0', distance) + s;
      a = s.split('.', 2);
      if (a.length === 1) {
        s = s.substr(0, s.length - distance) + '.' + s.substr(s.length - distance);
      } else {
        s = a[0].substr(0, a[0].length - distance) + '.' + a[0].substr(a[0].length - distance) + a[1];
      }
    } else if (move > 0) {
      // move to right
      s += scout.strings.repeat('0', distance);
      a = s.split('.', 2);
      if (a.length === 2) {
        s = a[0] + a[1].substr(0, distance) + '.' + a[1].substr(distance);
      }
    }
    // Remove multiple leading zeros to prevent interpretation as octal number
    s = s.replace(/^0*(\d)/g, '$1');
    return Number(s) * sign;
  }

};

/**
 * Enum providing rounding-modes for number columns and fields.
 *
 * @see RoundingMode.java
 */
scout.numbers.RoundingMode = {
  UP: 'UP',
  DOWN: 'DOWN',
  CEILING: 'CEILING',
  FLOOR: 'FLOOR',
  HALF_UP: 'HALF_UP',
  HALF_DOWN: 'HALF_DOWN',
  HALF_EVEN: 'HALF_EVEN',
  UNNECESSARY: 'UNNECESSARY'
};
