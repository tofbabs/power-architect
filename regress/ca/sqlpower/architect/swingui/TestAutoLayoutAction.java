package regress.ca.sqlpower.architect.swingui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.AutoLayoutAction;
import ca.sqlpower.architect.swingui.BasicRelationshipUI;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.RelationshipUI;
import ca.sqlpower.architect.swingui.TablePane;
import junit.framework.TestCase;

public class TestAutoLayoutAction extends TestCase {
	
	private AutoLayoutAction action;
	
	/**
	 * Shows the GUI on the screen.  Helpful when you want to see why a test is failing.
	 * @param message The message to show in the JOptionPane
	 */
	private void showGUI(String message) {
		ArchitectFrame.getMainInstance().setVisible(true);
		action.getPlayPen().repaint();
		JOptionPane.showMessageDialog(null, message != null ? message : "Cows often say moo");
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		action = af.getAutoLayoutAction();
		action.setAnimationEnabled(false);
	}
	
	public void testIcon() {
		assertNotNull(action.getValue(AbstractAction.SMALL_ICON));
	}
	
	public void testNoOverlaps() throws ArchitectException {
		PlayPen pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		SQLTable t2 = new SQLTable(ppdb, "This table is way cooler than the first one", "", "TABLE", true);
		
		TablePane tp1 = new TablePane(t1, pp);
		TablePane tp2 = new TablePane(t2, pp);
		
		pp.addTablePane(tp1, new Point(10,10));
		pp.addTablePane(tp2, new Point(20,20));
		
		// they start off overlapping
		assertTrue(tp1.getBounds().intersects(tp2.getBounds()));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getBounds().intersects(tp2.getBounds()));
	}
	
	public void testNoCrossingLinesEasy() throws ArchitectException {
		PlayPen pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		
		SQLTable tables[] = new SQLTable[4];
		TablePane tablePanes[] = new TablePane[tables.length];
		
		for (int i = 0; i < tables.length; i++) {
			tables[i] = new SQLTable(ppdb, "Table "+i, "", "TABLE", true);
			tablePanes[i] = new TablePane(tables[i], pp);
		}
		
		pp.addTablePane(tablePanes[0], new Point(100, 0));
		pp.addTablePane(tablePanes[1], new Point(300, 100));
		pp.addTablePane(tablePanes[2], new Point(150, 200));
		pp.addTablePane(tablePanes[3], new Point(0, 100));
		
		SQLRelationship sr1 = new SQLRelationship();
		sr1.setPkTable(tables[0]);
		sr1.setFkTable(tables[2]);
		tables[0].addExportedKey(sr1); // FIXME: should update Relationship component to do this for us!
		tables[2].addImportedKey(sr1);

		SQLRelationship sr2 = new SQLRelationship();
		sr2.setPkTable(tables[1]);
		sr2.setFkTable(tables[3]);
		tables[1].addExportedKey(sr2);
		tables[3].addImportedKey(sr2);

		Relationship r1 = new Relationship(pp, sr1);
		Relationship r2 = new Relationship(pp, sr2);
		
		pp.addRelationship(r1);
		pp.addRelationship(r2);
		
		// the relationships init their paths only when painted
		r1.paint((Graphics2D) pp.getGraphics());
		r2.paint((Graphics2D) pp.getGraphics());
		
		// check that the relationships start out crossed
		assertTrue(((RelationshipUI) r1.getUI()).intersectsShape(((RelationshipUI) r2.getUI()).getShape()));

		
		// check that neither of the relationships intersects any of the 4 tables to start
		Rectangle b = new Rectangle();
		for (int i = 0; i < tablePanes.length; i++) {
			tablePanes[i].getBounds(b);
			if (tablePanes[i] != r1.getPkTable() && tablePanes[i] != r1.getFkTable()) {
				assertFalse("Table "+i+" crosses r1", ((RelationshipUI) r1.getUI()).intersects(b));
			}
			if (tablePanes[i] != r2.getPkTable() && tablePanes[i] != r2.getFkTable()) {
				assertFalse("Table "+i+" crosses r2", ((RelationshipUI) r2.getUI()).intersects(b));
			}
		}
		
		action.actionPerformed(new ActionEvent(this, 0, null));

		// make the paths update
		r1.paint((Graphics2D) pp.getGraphics());
		r2.paint((Graphics2D) pp.getGraphics());
		
		// check that the relationships are uncrossed
		assertFalse(((RelationshipUI) r1.getUI()).intersectsShape(((RelationshipUI) r2.getUI()).getShape()));

		
		// check that neither of the relationships intersects any of the 4 tables to start
		for (int i = 0; i < tablePanes.length; i++) {
			tablePanes[i].getBounds(b);
			if (tablePanes[i] != r1.getPkTable() && tablePanes[i] != r1.getFkTable()) {
				assertFalse("Table "+i+" crosses r1", ((RelationshipUI) r1.getUI()).intersects(b));
			}
			if (tablePanes[i] != r2.getPkTable() && tablePanes[i] != r2.getFkTable()) {
				assertFalse("Table "+i+" crosses r2", ((RelationshipUI) r2.getUI()).intersects(b));
			}
		}

	}
}
