/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.contacts.server;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.contacts.server.sql.SQLs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.StringArrayHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(10)
public class PlatformListener implements IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(PlatformListener.class);

  private static final String ORGANISATION1 = "Alice's Adventures in Wonderland";

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.BeanManagerValid) {
      autoCreateDatabase();
    }
  }

  public void autoCreateDatabase() {
    if (CONFIG.getPropertyValue(ConfigProperties.DatabaseAutoCreateProperty.class)) {

      try {
        BEANS.get(SuperUserRunContextProducer.class).produce().run(new IRunnable() {

          @Override
          public void run() throws Exception {
            Set<String> tables = getExistingTables();
            createOrganizationTable(tables);
            createPersonTable(tables);
          }
        });
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  protected Set<String> getExistingTables() {
    StringArrayHolder tables = new StringArrayHolder();
    SQL.selectInto(SQLs.SELECT_TABLE_NAMES, new NVPair("result", tables));
    return CollectionUtility.hashSet(tables.getValue());
  }

  private void createOrganizationTable(Set<String> tables) {
    if (!tables.contains("ORGANIZATION")) {
      SQL.insert(SQLs.ORGANIZATION_CREATE_TABLE);
      LOG.info("Database table 'ORGANIZATION' created");

      if (CONFIG.getPropertyValue(ConfigProperties.DatabaseAutoPopulateProperty.class)) {
        createOrganizationEntry(ORGANISATION1, "London", "GB", "http://en.wikipedia.org/wiki/Alice%27s_Adventures_in_Wonderland",
            "https://upload.wikimedia.org/wikipedia/en/3/3f/Alice_in_Wonderland%2C_cover_1865.jpg");
        createOrganizationEntry("BSI Business Systems Integration AG", "Daettwil, Baden", "CH", "https://www.bsi-software.com",
            "https://wiki.eclipse.org/images/4/4f/Bsiag.png");

        LOG.info("Database table 'ORGANIZATION' populated with sample data");
      }
    }
  }

  private void createOrganizationEntry(String name, String city, String country, String url, String logoUrl) {
    SQL.insert(SQLs.ORGANIZATION_INSERT_SAMPLE_DATA, new NVPair("organization_id", UUID.randomUUID().toString()), new NVPair("name", name), new NVPair("city", city), new NVPair("country", country), new NVPair("url", url),
        new NVPair("logoUrl", logoUrl));
  }

  private void createPersonTable(Set<String> tables) {
    if (!tables.contains("PERSON")) {
      SQL.insert(SQLs.PERSON_CREATE_TABLE);
      LOG.info("Database table 'PERSON' created");

      if (CONFIG.getPropertyValue(ConfigProperties.DatabaseAutoPopulateProperty.class)) {
        createPersonEntry("Alice", null, "http://www.uergsel.de/uploads/Alice.png", DateUtility.parse("26.11.1865", "dd.MM.yyyy"), "F", "Daresbury, Cheshire", "GB", "The curious girl", ORGANISATION1);
        createPersonEntry("Rabbit", "the White", "https://upload.wikimedia.org/wikipedia/commons/4/42/The_White_Rabbit_%28Tenniel%29_-_The_Nursery_Alice_%281890%29_-_BL.jpg", DateUtility.parse("26.11.1861", "dd.MM.yyyy"), "F",
            "Daresbury, Cheshire", "GB", "Follow me", ORGANISATION1);

        LOG.info("Database table 'PERSON' populated with sample data");
      }
    }
  }

  private void createPersonEntry(String firstName, String lastName, String pictureUrl, Date dob, String gender, String city, String country, String position, String organizationId) {
    SQL.insert(SQLs.PERSON_INSERT_SAMPLE_DATA,
        new NVPair("person_id", UUID.randomUUID().toString()),
        new NVPair("firstName", firstName),
        new NVPair("lastName", lastName),
        new NVPair("pictureUrl", pictureUrl),
        new NVPair("dob", dob),
        new NVPair("gender", gender),
        new NVPair("city", city),
        new NVPair("country", country),
        new NVPair("position", position),
        new NVPair("organizationId", organizationId));
  }
}
