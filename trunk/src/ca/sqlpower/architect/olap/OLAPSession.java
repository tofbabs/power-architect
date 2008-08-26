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

package ca.sqlpower.architect.olap;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;

/**
 * The collection of objects that support editing and use of an OLAP schema.
 * Contains the schema as its one and only child, and belongs to an OLAPRootObject.
 */
public class OLAPSession extends OLAPObject {

    /**
     * The database this session's schema uses.
     */
    private SQLDatabase database;
    
    /**
     * The schema that belongs to this session.
     */
    private final Schema schema;
    
    /**
     * Watches over the schema and makes updates to DimensionUsages,
     * VirtualCubeDimensions and CubeUsages as the object they reference makes a
     * name change or gets removed.
     */
    private final SchemaWatcher schemaWatcher;
    
    /**
     * Creates the OLAP Session for the given schema. That schema must
     * not already belong to another session.
     * 
     * @param schema The schema this session owns.
     */
    public OLAPSession(Schema schema) {
        if (schema.getParent() != null) {
            throw new IllegalStateException(
                    "The given schema already belongs to an OLAP Session");
        }
        schema.setParent(this);
        this.schema = schema;
        schemaWatcher = new SchemaWatcher(schema);
    }

    /**
     * Returns the SQLDatabase this session's schema works with.
     * @return
     */
    public SQLDatabase getDatabase() {
        return database;
    }

    /**
     * Sets the SQLDatabase this session's schema works with.
     * <p>
     * Note that many parts of the OLAP model refer to tables, columns, views,
     * and so on within one single database connection. Changing which database
     * this session's schema works with is not possible, in general, without a
     * lot of cleanup work. Currently, no effort is made in this method to
     * validate that the new database has the appropriate structure.
     * <p>
     * Similarly, if the tables and columns within this database change over
     * time, the OLAP schema may become invalid, since it will still be
     * referencing the old objects. It would make sense for this session to
     * attach SQLObjectPreEventListeners to the database and all its children,
     * although this is not currently implemented.
     * 
     * @param database
     *            The new database to use with this session's schema.
     */
    public void setDatabase(SQLDatabase database) {
        this.database = database;
    }
    
    /**
     * Returns this session's schema.
     */
    public Schema getSchema() {
        return schema;
    }
    
    @Override
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns a list with exactly one entry: this session's schema.
     */
    @Override
    public List<Schema> getChildren() {
        return Collections.singletonList(schema);
    }
    
    /**
     * Throws an exception, because you can't add or remove children from this
     * type of OLAP Object.
     */
    @Override
    public void addChild(OLAPObject child) {
        throw new UnsupportedOperationException(
                "OLAPSession has exactly one child (the Schema) for its entire lifetime");
    }
    
    /**
     * Throws an exception, because you can't add or remove children from this
     * type of OLAP Object.
     */
    @Override
    public boolean removeChild(OLAPObject child) {
        throw new UnsupportedOperationException(
                "OLAPSession has exactly one child (the Schema) for its entire lifetime");
    }

    /**
     * Returns a public dimension in the child schema with the given name.
     * 
     * @param name The name to search by.
     * @return A public Dimension with the name, or null if not found.
     */
    public Dimension findPublicDimension(String name) {
        return schemaWatcher.findPublicDimension(name);
    }
    
    /**
     * Returns a CubeDimension in the child schema with the given name and
     * parent Cube.
     * 
     * @param cubeName Name of the Cube to search in.
     * @param dimName Name of the CubeDimension to search for.
     * @return A CubeDimension in the Cube with the name, or null if not found.
     */
    public CubeDimension findCubeDimension(String cubeName, String dimName) {
        return schemaWatcher.findCubeDimension(cubeName, dimName);
    }
    
    /**
     * Returns a Cube in the child schema with the given name.
     * 
     * @param name The name to search by.
     * @return A Cube with the name, or null if not found.
     */
    public Cube findCube(String name) {
        return schemaWatcher.findCube(name);
    }
    
    /**
     * Returns a VirtualCube in the child schema with the given name.
     * 
     * @param name The name to search by.
     * @return A VirtualCube with the name, or null if not found.
     */
    public VirtualCube findVirtualCube(String name) {
        return schemaWatcher.findVirtualCube(name);
    }
}