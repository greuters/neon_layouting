scout.LayoutValidator = function() {
  this._invalidComponents = [];
};

// FIXME CGU maybe it is necessary to sort the list so that the top most root is layouted first.
// Testcase: Field in scrollable groupbox gets invisible and also a field outside the groupbox.
// If scrollable is layouted first it may be relayouted again when the form gets layouted
scout.LayoutValidator.prototype.invalidateTree = function(htmlComp) {
  var validateRoot,
    htmlCompParent = htmlComp;

  // Mark every parent as invalid until validate root
  while (htmlCompParent) {
    htmlComp = htmlCompParent;
    htmlComp.invalidate();
    if (htmlComp.isValidateRoot()) {
      validateRoot = htmlComp;
      break;
    }
    htmlCompParent = htmlComp.getParent();
  }

  if (!htmlCompParent) {
    validateRoot = htmlComp;
  }

  // Add validate root to list of invalid components. These are the starting point for a subsequent call to validate().
  if (this._invalidComponents.indexOf(htmlComp) < 0) {
    this._invalidComponents.push(validateRoot);
  }
};

scout.LayoutValidator.prototype.validate = function() {
  for (var i=0; i < this._invalidComponents.length; i++) {
    if (this._invalidComponents[i].isRemoved()) {
      // Don't layout components which don't exist anymore
      continue;
    }

    this._invalidComponents[i].layout();
  }
  this._invalidComponents = [];
};
