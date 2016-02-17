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
package org.eclipse.scout.rt.ui.html.res;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.res.loader.DynamicResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryResourceUrlUtility {
  private static final Logger LOG = LoggerFactory.getLogger(BinaryResourceUrlUtility.class);

  /**
   * Regular expression pattern to find icons, e.g. to find &lt;img src="iconid:some_icon"&gt;.
   * <p>
   * Pattern does a search for iconid:some_icon (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. Icon name, in the example <b>some_icon</b>
   */
  public static final Pattern ICON_REGEX_PATTERN = Pattern.compile("([\"'])iconid:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * Regular expression pattern to find icons, e.g. to find &lt;img src="binaryResource:some_res"&gt;.
   * <p>
   * Pattern does a search for binaryResource:some_res (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. Icon name, in the example <b>some_res</b>
   */
  public static final Pattern BINARY_RESOURCE_REGEX_PATTERN = Pattern.compile("([\"'])binaryResource:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * Pattern to determine if the provided url path is a dynamic resource path.
   *
   * @see #createDynamicAdapterResourceUrl(IJsonAdapter, String)
   */
  public static final Pattern PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH = Pattern.compile("^/dynamic/([^/]*)/([^/]*)/(.*)$");

  // FIXME awe: (font icons) extend syntax for icon-ID so a font-name can be configured
  // font:[char] --> uses default scoutIcons.ttf (CSS class .font-icon)
  // font.crm:[char] --> uses crmIcons.ttf (CSS class .crm-font-icon)

  /**
   * @return a relative URL for a configured logical icon-name or a font-based icon. For instance:
   *         <ul>
   *         <li>input: <code>"bookmark"</code>, output: <code>"icon/bookmark.png"</code> (the file extension is
   *         included to support auto-detection of the MIME type without looking at the file contents)</li>
   *         <li>input: <code>"font:X"</code>, output: <code>"font:X"</code></li>
   *         </ul>
   *         The file extension is included to be able to auto-detect the MIME type based on it.
   *         <p>
   *         Use this method for image-files located in the /resource/icons directories of all jars on the classpath.
   */
  public static String createIconUrl(String iconId) {
    if (!StringUtility.hasText(iconId) || AbstractIcons.Null.equals(iconId)) {
      return null;
    }
    if (iconId.startsWith("font:")) {
      return iconId;
    }
    IconSpec iconSpec = IconLocator.instance().getIconSpec(iconId);
    if (iconSpec != null) {
      return "icon/" + iconSpec.getName(); // includes file extension
    }
    LOG.warn("iconId '{}' could not be resolved", iconId);
    return null; // may happen, when no icon is available for the requested iconName
  }

  /**
   * Helper method for {@link #ICON_REGEX_PATTERN} to replace all occurrences with the proper url.
   */
  public static String replaceIconIdHandlerWithUrl(String str) {
    if (str == null) {
      return null;
    }
    Matcher m = BinaryResourceUrlUtility.ICON_REGEX_PATTERN.matcher(String.valueOf(str));
    StringBuffer ret = new StringBuffer(); // StringBuffer must be used, Java API does not accept a StringBuilder
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + BinaryResourceUrlUtility.createIconUrl(m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  /**
   * @return a relative URL for a resource handled by an adapter, see {@link DynamicResourceLoader}.
   *         <p>
   *         The calling adapter must implement {@link IBinaryResourceProvider}.
   * @see #PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH
   */
  public static String createDynamicAdapterResourceUrl(IJsonAdapter<?> jsonAdapter, String filename) {
    if (jsonAdapter == null) {
      return null;
    }
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      LOG.warn("adapter {} is not implementing {}", jsonAdapter, IBinaryResourceProvider.class.getName());
      return null;
    }
    if (filename == null) {
      return null;
    }

    String encodedFilename = IOUtility.urlEncode(filename);
    // / was encoded by %2F, revert this encoding otherwise filename doesn't look nice in browser download
    // Example for filesnames containing a /:
    // - relative reference from a unzipped zip file
    // - another path segment was explicitly added to distinguish between same filenames
    encodedFilename = encodedFilename.replace("%2F", "/");

    // do not change this dynamic resource url format without updating the recognition pattern (@see in JavaDoc)
    return "dynamic/" + jsonAdapter.getUiSession().getUiSessionId() + "/" + jsonAdapter.getId() + "/" + encodedFilename;
  }

  /**
   * Helper method for {@link #BINARY_RESOURCE_REGEX_PATTERN} to replace all occurrences with the proper url.
   */
  public static String replaceBinaryResourceHandlerWithUrl(IJsonAdapter<?> jsonAdapter, String str) {
    if (str == null) {
      return null;
    }
    Matcher m = BinaryResourceUrlUtility.BINARY_RESOURCE_REGEX_PATTERN.matcher(str);
    StringBuffer ret = new StringBuffer(); // StringBuffer must be used, Java API does not accept a StringBuilder
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(jsonAdapter, m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  /**
   * Calculate the MD5 hash of a filename. This provides a (seemingly) dynamic name of constant length with no special
   * meaning or characters.
   * <p>
   * MD5 was chosen as hash algorithm because of the relative short result strings. Security is not a concern when
   * downloading dynamic binary resources (because download handlers are only valid for a single UI session and
   * adapter).
   * <p>
   * Example: The MD5 hash of "foo.txt" is <code>4fd8cc85ca9eebd2fa3c550069ce2846</code>. The more secure SHA-512 hash (
   * {@link SecurityUtility#hash(byte[], byte[])}) however would be much longer:
   * <code>b0370324e322ccbaf4b749dc85a0de8e4b614c402748b40d4120ffb3c6c05c90eec13cabe7460479c31e508fb8b99324dbbb4810b45612b980d67f818ce9dfbe</code>
   * .
   */
  public static String getFilenameHash(String filename) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to calculate MD5 hash", e);
    }
    md.reset();

    byte[] filenameBytes = StringUtility.nvl(filename, "").getBytes(StandardCharsets.UTF_8);
    String filenameHash = HexUtility.encode(md.digest(filenameBytes));
    return filenameHash;
  }
}
