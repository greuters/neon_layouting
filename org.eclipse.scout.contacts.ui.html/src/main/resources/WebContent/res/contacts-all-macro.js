__include("scout-module.js");
__include("svg-module.js");

scout.HtmlEnvironment = {
      // -------------------------------
      // IMPORTANT:
      // Some of the following constants are also defined in sizes.css. If you change
      // them, be sure to apply them at both places. (Remember to consider margins)
     // -------------------------------
      formRowHeight: 30, // @logical-grid-height
      formRowGap: 10,
//      formColumnWidth: 420,
      formColumnWidth: 350,
      formColumnGap: 32, // 40 pixel actual form gap - fieldMandatoryIndicatorWidth
      smallColumnGap: 4,

//      fieldLabelWidth: 140,
      fieldLabelWidth: 100,
      fieldMandatoryIndicatorWidth: 8, // @mandatory-indicator-width
      fieldStatusWidth: 20
    };
