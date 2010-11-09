/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.profile.output;

/**
 * Represents and lists (in order) the columns in the reports generated by the ProfileManager.
 */
public enum ProfileColumn {
    DATABASE("Database"),
    CATALOG("Catalog"),
    SCHEMA("Schema"),
    TABLE("Table"),
    COLUMN("Column"),
    RUNDATE("Run Date"),
    RECORD_COUNT("Record Count"),
    DATA_TYPE("Data Type"),
    NULL_COUNT("# Null"),
    PERCENT_NULL("% Null"),
    UNIQUE_COUNT("# Unique"),
    PERCENT_UNIQUE("% Unique"),
    MIN_LENGTH("Min Length"),
    MAX_LENGTH("Max Length"),
    AVERAGE_LENGTH("Avg. Length"),
    MIN_VALUE("Min Value"),
    MAX_VALUE("Max Value"),
    AVERAGE_VALUE("Avg. Value"),
    TOP_VALUE("Most Frequent");

    String name;

    ProfileColumn(String name)  {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}