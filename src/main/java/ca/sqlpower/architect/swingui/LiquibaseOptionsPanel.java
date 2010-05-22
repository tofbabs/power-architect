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
package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ddl.LiquibaseDDLGenerator;
import ca.sqlpower.swingui.DataEntryPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Thomas Kellerer
 */
public class LiquibaseOptionsPanel
  implements DataEntryPanel, ActionListener {

	private JPanel panel;
	private LiquibaseDDLGenerator ddlGenerator;

	private JCheckBox useChangeSets;
	private JTextField authorField;
	private JLabel authorLabel;
	private JCheckBox generateId;
	private JSpinner startId;
	private JLabel startValueLabel;

	public LiquibaseOptionsPanel() {
		setup();
	}

	public void setGenerator(LiquibaseDDLGenerator generator) {
		ddlGenerator = generator;
	}

	protected int getIdStart() {
		Integer start = (Integer)startId.getValue();
		return start == null ? 0 : start.intValue();
	}
	
	protected boolean getUseChangeSets() {
		return useChangeSets.isSelected();
	}

	protected String getAuthor() {
		return authorField.getText();
	}

	protected boolean getGenerateId() {
		return generateId.isSelected();
	}

	private void setup() {
		panel = new JPanel(new MigLayout());
		useChangeSets = new JCheckBox(Messages.getString("LiquibaseOptionsPanel.useChangeSet")); //$NON-NLS-1$
		useChangeSets.addActionListener(this);
		generateId = new JCheckBox(Messages.getString("LiquibaseOptionsPanel.generateID")); //$NON-NLS-1$
		generateId.addActionListener(this);
		authorLabel = new JLabel(Messages.getString("LiquibaseOptionsPanel.authorName")); //$NON-NLS-1$
		authorField = new JTextField(20);
		startValueLabel = new JLabel(Messages.getString("LiquibaseOptionsPanel.idStart")); //$NON-NLS-1$
		SpinnerNumberModel model = new SpinnerNumberModel(1,1,9999,1);
		startId = new JSpinner(model);
		panel.add(useChangeSets);
		panel.add(authorLabel);
		panel.add(authorField, "wrap");
		panel.add(generateId);
		panel.add(startValueLabel);
		panel.add(startId);
		checkStates();
	}

	public void restoreSettings(LiquibaseSettings settings) {
		if (settings == null) return;
		String author = settings.getAuthor();
		authorField.setText(author == null ? "" : author);
		useChangeSets.setSelected(settings.getUseSeparateChangeSets());
		generateId.setSelected(settings.getGenerateId());
		int start = settings.getIdStart();
		if (start > 0) {
			startId.setValue(Integer.valueOf(start));
		}
		checkStates();
	}
	
	public LiquibaseSettings getLiquibaseSettings() {
		LiquibaseSettings settings = new LiquibaseSettings();
		settings.setAuthor(getAuthor());
		settings.setGenerateId(getGenerateId());
		settings.setUseSeparateChangeSets(getUseChangeSets());
		settings.setIdStart(getIdStart());
		return settings;
	}
	
	public boolean applyChanges() {
		ddlGenerator.setUseSeparateChangeSets(getUseChangeSets());
		ddlGenerator.setAuthor(getAuthor());
		ddlGenerator.setGenerateId(getGenerateId());
		ddlGenerator.setIdStart(getIdStart());
		return true;
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		checkStates();
	}

	private void checkStates() {
//		authorField.setEnabled(useChangeSets.isSelected());
//		generateId.setEnabled(useChangeSets.isSelected());
//		startId.setEnabled(useChangeSets.isSelected() && generateId.isSelected());
	}
}
