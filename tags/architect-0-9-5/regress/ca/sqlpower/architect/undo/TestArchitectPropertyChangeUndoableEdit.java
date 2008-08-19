package ca.sqlpower.architect.undo;

import junit.framework.TestCase;
import ca.sqlpower.architect.StubSQLObject;


public class TestArchitectPropertyChangeUndoableEdit extends TestCase {

	private UndoManager undoManager;
	
	private TestSQLObject testObject;
	
	protected void setUp() throws Exception {
		super.setUp();
	
		testObject = new TestSQLObject();
		undoManager = new UndoManager(testObject);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
	}

	public void testUndoAdded()
	{
		assertFalse(undoManager.canUndo());
		testObject.setFoo(1);
		testObject.setBar("New string");
		assertEquals("Wrong number of undoable edits added to undo manager",2,undoManager.getUndoableEditCount());
		assertEquals("Unexpected redoable",0,undoManager.getRedoableEditCount());
	}
	
	
	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoIntProperty() {
		testObject.setFoo(1);
		assertEquals("foo setter didn't work!", 1, testObject.getFoo());
		undoManager.undo();
		assertEquals("undo didn't work!", 0, testObject.getFoo());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoStringPropertyToNull() {
		final String newBarValue = "cows often say moo";
		testObject.setBar(newBarValue);
		assertEquals("bar setter didn't work!", newBarValue, testObject.getBar());
		undoManager.undo();
		assertEquals("undo didn't work!", null, testObject.getBar());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoStringPropertyToNonNull() {
		final String newBarValue1 = "cows often say moo";
		final String newBarValue2 = "chickens don't often say moo";
		testObject.setBar(newBarValue1);
		testObject.setBar(newBarValue2);
		assertEquals("bar setter didn't work!", newBarValue2, testObject.getBar());
		undoManager.undo();
		assertEquals("undo didn't work!", newBarValue1, testObject.getBar());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoIntProperty() {
		testObject.setFoo(1);
		assertEquals("foo setter didn't work!", 1, testObject.getFoo());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo didn't work!", 1, testObject.getFoo());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoStringPropertyToNull() {
		final String newBarValue = "cows often say moo";
		testObject.setBar(newBarValue);
		assertEquals("bar setter didn't work!", newBarValue, testObject.getBar());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo bar didn't work!", newBarValue, testObject.getBar());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoStringPropertyToNonNull() {
		final String newBarValue1 = "cows often say moo";
		final String newBarValue2 = "chickens don't often say moo";
		testObject.setBar(newBarValue1);
		testObject.setBar(newBarValue2);
		assertEquals("bar setter didn't work!", newBarValue2, testObject.getBar());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo bar change didn't work!", newBarValue2, testObject.getBar());
	}

	public void testMultipleUndoRedo()
	{
		testObject.setFoo(1);
		testObject.setFoo(2);
		testObject.setFoo(3);
		testObject.setFoo(4);
		testObject.setFoo(5);
		assertEquals("wrong number of edits",5,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",0,undoManager.getRedoableEditCount());
		undoManager.undo();
		undoManager.undo();
		assertEquals("wrong number of edits",3,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",2,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",3,testObject.getFoo());
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("wrong number of edits",4,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",1,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",4,testObject.getFoo());
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("wrong number of edits",5,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",0,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",5,testObject.getFoo());
		
		
	}
	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.ArchitectPropertyChangeUndoableEdit(SQLObjectEvent)'
	 */
	public void testArchitectPropertyChangeUndoableEdit() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.getPresentationName()'
	 */
	public void testGetPresentationName() {

	}

	public static class TestSQLObject extends StubSQLObject {
		
		private int foo;
		private String bar;

		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			String oldBar = this.bar;
			this.bar = bar;
			fireDbObjectChanged("bar",oldBar,bar);
		}

		public int getFoo() {
			return foo;
		}

		public void setFoo(int foo) {
			int oldFoo = this.foo;
			this.foo = foo;
			fireDbObjectChanged("foo",oldFoo,foo);
		}

	}
}