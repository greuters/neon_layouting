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
scout.ResponseQueue = function(session) {
  this.session = session;
  this.queue = [];
  this.nextExpectedSequenceNo = 1;

  this.force = false;
  this.forceTimeoutId = null;
};

scout.ResponseQueue.FORCE_TIMEOUT = 10 * 1000; // in ms

scout.ResponseQueue.prototype.add = function(response) {
  var sequenceNo = response && response['#'];
  if (!sequenceNo || this.queue.length === 0) { // Handle messages without sequenceNo in the order they were received
    this.queue.push(response);
  } else {
    // Insert at correct position (ascending order)
    var pos = null;
    for (var i = 0; i < this.queue.length; i++) {
      var el = this.queue[i];
      if (el['#'] && el['#'] > sequenceNo) {
        pos = i;
        break;
      }
    }
    if (pos === null) {
      // no element with bigger seqNo found -> insert as last element
      this.queue.push(response);
    } else {
      // insert at position
      scout.arrays.insert(this.queue, response, pos);
    }
  }
};

scout.ResponseQueue.prototype.process = function(response) {
  if (response) {
    this.add(response);
  }

  // Process the queue in ascending order
  var responseSuccess = null;
  var missingResponse = false;
  var nonProcessedResponses = [];
  for (var i = 0; i < this.queue.length; i++) {
    var el = this.queue[i];
    var sequenceNo = el['#'];

    // For elements with a sequence number, check if they are in the expected order
    if (sequenceNo) {
      if (!this.force && !missingResponse) {
        missingResponse = (this.nextExpectedSequenceNo !== sequenceNo);
      }
      if (missingResponse) {
        // Sequence is not complete, process those messages later
        nonProcessedResponses.push(el);
        continue;
      }
    }

    // Handle the element
    var success = this.session.processJsonResponseInternal(el);
    // Only return success value of the response that was passed to the process() call
    if (response && el === response) {
      responseSuccess = success;
    }

    // Update the expected next sequenceNo
    if (sequenceNo) {
      this.nextExpectedSequenceNo = sequenceNo + 1;
    }
  }
  // Keep non-processed events (because they are not in sequence) in the queue
  this.queue = nonProcessedResponses;

  this._checkTimeout();

  return responseSuccess;
};

scout.ResponseQueue.prototype.size = function() {
  return this.queue.length;
};

scout.ResponseQueue.prototype._checkTimeout = function() {
  // If there are non-processed elements, schedule a job that forces the processing of those
  // elements after a certain timeout to prevent the "blocked forever syndrome" if a response
  // was lost on the network.
  if (this.queue.length === 0) {
    clearTimeout(this.forceTimeoutId);
    this.forceTimeoutId = null;
  } else if (!this.forceTimeoutId) {
    this.forceTimeoutId = setTimeout(function() {
      try {
        var s = '[';
        for (var i = 0; i < this.queue.length; i++) {
          if (i > 0) {
            s += ', ';
          }
          s += (scout.strings.box('#', this.queue[i]['#']) || '<none>');
        }
        s += ']';
        this.session.sendLogRequest('Expected response #' + this.nextExpectedSequenceNo + ' still missing after ' +
            scout.ResponseQueue.FORCE_TIMEOUT + ' ms. Forcing response queue to process ' + this.size() + ' elements: ' + s);
      } catch (error) {
        // nop
      }
      this.force = true;
      try {
        this.process();
      }
      finally {
        this.force = false;
        this.forceTimeoutId = null;
      }
    }.bind(this), scout.ResponseQueue.FORCE_TIMEOUT);
  }
};
