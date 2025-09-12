package net.sf.openrocket.gui.main;


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.defaults.DefaultAppearance;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.dialogs.ScaleDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rw.Paraminfo;
import net.sf.openrocket.rw.ReadNormalFile;
import net.sf.openrocket.rw.WriteNormalFile;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.ORColor;
import net.sf.openrocket.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.sf.openrocket.startup.SwingStartup.*;
import static net.sf.openrocket.startup.SwingStartup.noseCone3DPoint2d;


/**
 * A class that holds Actions for common rocket and simulation operations such as
 * cut/copy/paste/delete etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketActions {

	public static final KeyStroke CUT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_X,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke COPY_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_C,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke PASTE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_V,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke DUPLICATE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_D,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke EDIT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_E,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke DELETE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	
	private final OpenRocketDocument document;
	private final Rocket rocket;
	private final BasicFrame parentFrame;
	private final DocumentSelectionModel selectionModel;
	private final SimulationPanel simulationPanel;


	private final RocketAction deleteAction;
	private final RocketAction cutAction;
	private final RocketAction copyAction;
	private final RocketAction pasteAction;
	private final RocketAction duplicateAction;
	private final RocketAction editAction;
	private final RocketAction selectSameColorAction;
	private final RocketAction deselectAllAction;
	private final RocketAction scaleAction;
	private final RocketAction moveUpAction;
	private final RocketAction moveDownAction;
	private final RocketAction exportOBJAction;
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(RocketActions.class);
	private final SaveAction saveAction;


	public RocketActions(OpenRocketDocument document, DocumentSelectionModel selectionModel,
			BasicFrame parentFrame, SimulationPanel simulationPanel) {
		this.document = document;
		this.rocket = document.getRocket();
		this.selectionModel = selectionModel;
		this.parentFrame = parentFrame;
		this.simulationPanel = simulationPanel;

		// Add action also to updateActions()
		this.deleteAction = new DeleteAction();
		this.cutAction = new CutAction();
		this.copyAction = new CopyAction();
		this.pasteAction = new PasteAction();
		this.duplicateAction = new DuplicateAction();
		this.editAction = new EditAction();
		this.selectSameColorAction = new SelectSameColorAction();
		this.deselectAllAction = new DeselectAllAction();
		this.scaleAction = new ScaleAction();
		this.moveUpAction = new MoveUpAction();
		this.moveDownAction = new MoveDownAction();
		this.exportOBJAction = new ExportOBJAction();
		this.saveAction = new SaveAction();
		OpenRocketClipboard.addClipboardListener(new ClipboardListener() {
			@Override
			public void clipboardChanged() {
				updateActions();
			}
		});
		updateActions();

		// Update all actions when tree selection, simulation selection or rocket changes

		selectionModel.addDocumentSelectionListener(new DocumentSelectionListener() {
			@Override
			public void valueChanged(int changeType) {
				updateActions();
			}
		});
		simulationPanel.addSimulationTableListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateActions();
			}
		});
		document.getRocket().addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				updateActions();
			}
		});
	}

	/**
	 * Update the state of all of the actions.
	 */
	private void updateActions() {
		simulationPanel.updateActions();
		editAction.clipboardChanged();
		cutAction.clipboardChanged();
		copyAction.clipboardChanged();
		pasteAction.clipboardChanged();
		deleteAction.clipboardChanged();
		duplicateAction.clipboardChanged();
		selectSameColorAction.clipboardChanged();
		deselectAllAction.clipboardChanged();
		scaleAction.clipboardChanged();
		moveUpAction.clipboardChanged();
		moveDownAction.clipboardChanged();
		exportOBJAction.clipboardChanged();
		saveAction.clipboardChanged();
	}
	



	public Action getDeleteAction() {
		return deleteAction;
	}

	public Action getCutAction() {
		return cutAction;
	}
	
	public Action getCopyAction() {
		return copyAction;
	}
	
	public Action getPasteAction() {
		return pasteAction;
	}

	public Action getDuplicateAction() {
		return duplicateAction;
	}

	public Action getSaveAction() {
		return saveAction;
	}

	
	public Action getEditAction() {
		return editAction;
	}

	public Action getSelectSameColorAction() {
		return selectSameColorAction;
	}

	public Action getDeselectAllAction() {
		return deselectAllAction;
	}

	public Action getScaleAction() {
		return scaleAction;
	}
	
	public Action getMoveUpAction() {
		return moveUpAction;
	}
	
	public Action getMoveDownAction() {
		return moveDownAction;
	}

	public Action getExportOBJAction() {
		return exportOBJAction;
	}

	/**
	 * Tie an action to a JButton, without using the icon or text of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action and then set the
	 * icon to null and set the button text: this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 * @param text text to display on the button
	 */
	public static void tieActionToButtonNoIcon(JButton button, Action action, String text) {
		button.setAction(action);
		button.setIcon(null);
		button.setText(text);
	}

	/**
	 * Tie an action to a JButton, without using the icon of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action and then set the
	 * icon to null: this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 */
	public static void tieActionToButtonNoIcon(JButton button, Action action) {
		button.setAction(action);
		button.setIcon(null);
	}

	/**
	 * Tie an action to a JButton, without using the text of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action:
	 * this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 * @param text text to display on the button
	 */
	public static void tieActionToButton(JButton button, Action action, String text) {
		button.setAction(action);
		button.setText(text);
	}

	/**
	 * Tie an action to a JButton.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action:
	 * this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 */
	public static void tieActionToButton(JButton button, Action action) {
		button.setAction(action);
	}
	
	
	////////  Helper methods for the actions

	private boolean isDeletable(RocketComponent c) {
		// Sanity check
		if (c == null || c.getParent() == null)
			return false;

		// Cannot remove Rocket
		if (c instanceof Rocket)
			return false;

		// Cannot remove last stage, except from Boosters
		if ((c instanceof AxialStage) && !(c instanceof ParallelStage) && (c.getParent().getChildCount() == 1)) {
			return false;
		}

		return true;
	}

	private boolean isDeletable(List<RocketComponent> components) {
		if (components == null || components.size() == 0) return false;
		for (RocketComponent component : components) {
			if (!isDeletable(component)) return false;
		}
		return true;
	}

	private void delete(RocketComponent c) {
		if (!isDeletable(c)) {
			throw new IllegalArgumentException("Report bug!  Component " + c + 
					" not deletable.");
		}

		RocketComponent parent = c.getParent();
		parent.removeChild(c);
	}

	private void delete(List<RocketComponent> components) {
		if (!isDeletable(components)) {
			throw new IllegalArgumentException("Report bug!  Components not deletable.");
		}

		for (RocketComponent component : components) {
			delete(component);
		}
	}

	private boolean isCopyable(RocketComponent c) {
		if (c==null)
			return false;
		if (c instanceof Rocket)
			return false;
		return true;
	}

	private boolean isCopyable(List<RocketComponent> components) {
		if (components == null || components.isEmpty()) return false;
		for (RocketComponent component : components) {
			if (!isCopyable(component)) return false;
		}
		return true;
	}

	/**
	 * Copies components, but with maintaining parent-relations with the newly copied components
	 * @param components components to copy
	 * @return copied components
	 */
	public static List<RocketComponent> copyComponentsMaintainParent(List<RocketComponent> components) {
		if (components == null) return null;
		List<RocketComponent> result = new LinkedList<>();
		for (RocketComponent component : components) {
			result.add(component.copy());
		}

		for (int i = 0; i < components.size(); i++) {
			if (components.contains(components.get(i).getParent())) {
				RocketComponent originalParent = components.get(i).getParent();
				int originalParentIdx = components.indexOf(originalParent);

				result.get(originalParentIdx).addChild(result.get(i), false);
			} else if (RocketComponent.listContainsParent(components, components.get(i))){
				RocketComponent originalParent = components.get(i);
				while (originalParent != components.get(i)) {
					if (components.contains(originalParent.getParent())) {
						originalParent = originalParent.getParent();
					}
				}
				int originalParentIdx = components.indexOf(originalParent);
				result.get(originalParentIdx).addChild(result.get(i), false);
			}
		}

		return result;
	}

	/**
	 * If the children of a parent are not selected, add them to the selection. Do this recursively for the children
	 * of the children as well.
	 * @param selections list of currently selected components
	 * @param parent parent component to parse the children of
	 */
	private void selectAllUnselectedInParent(List<RocketComponent> selections, RocketComponent parent) {
		if (parent.getChildCount() == 0) return;

		boolean noChildrenSelected = true;
		for (RocketComponent child : parent.getChildren()) {
			if (selections.contains(child)) {
				noChildrenSelected = false;
				break;
			}
		}

		// Add children to selection if none of them were selected
		if (noChildrenSelected) {
			selections.addAll(parent.getChildren());
		}

		// Recursively select all unselected children. Unselected children will not undergo this recursive updating.
		for (RocketComponent child : parent.getChildren()) {
			if (!noChildrenSelected && selections.contains(child)) {
				selectAllUnselectedInParent(selections, child);
			}
		}
	}

	/**
	 * This method fills in some selections in selections. If there is a parent where none of its children are selected,
	 * add all the children to the selection. If there is a component whose parent is not selected, but it does have
	 * a super-parent in the selection, add the parent to the selection.
	 * @param selections component selections to be filled up
	 */
	private void fillInMissingSelections(List<RocketComponent> selections) {
		List<RocketComponent> initSelections = new LinkedList<>(selections);
		for (RocketComponent component : initSelections) {
			selectAllUnselectedInParent(selections, component);

			// If there is a component in the selection, but its parent (or the parent of the parent) is still
			// not selected, add it to the selection
			RocketComponent temp = component;
			if (RocketComponent.listContainsParent(selections, temp) && !selections.contains(temp.getParent())) {
				while (!selections.contains(temp.getParent())) {
					selections.add(temp.getParent());
					temp = temp.getParent();
				}
			}
		}
	}

	
	private boolean isSimulationSelected() {
		Simulation[] selection = selectionModel.getSelectedSimulations();
		if (selection != null && selection.length > 0) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (components != null && !components.isEmpty()) {
				throw new IllegalStateException("Both simulation and component selected");
			}
			return true;
		}
		return false;
	}


	/**
	 * Return the component and position to which the current clipboard
	 * should be pasted.  Returns null if the clipboard is empty or if the
	 * clipboard cannot be pasted to the current selection.
	 * 
	 * @param   srcComponent	the component to be copy-pasted.
	 * @param	destComponent	the component where srcComponent should be pasted to.
	 * @return  a Pair with both components defined, or null.
	 */
	private Pair<RocketComponent, Integer> getPastePosition(RocketComponent srcComponent, RocketComponent destComponent) {
		if (destComponent == null)
			return new Pair<>(null, null);

		if (srcComponent == null)
			return new Pair<>(null, null);

		if (destComponent.isCompatible(srcComponent))
			return new Pair<>(destComponent, destComponent.getChildCount());

		RocketComponent parent = destComponent.getParent();
		return getPastePositionFromParent(srcComponent, destComponent, parent);
	}

	private Pair<RocketComponent, Integer> getPastePositionFromParent(RocketComponent srcComponent, RocketComponent destComponent,
																	  RocketComponent parent) {
		if (parent != null && parent.isCompatible(srcComponent)) {
			int index = parent.getChildPosition(destComponent) + 1;
			return new Pair<>(parent, index);
		}

		return new Pair<>(null, null);
	}

	/**
	 * Return the component and position to which the current clipboard
	 * should be pasted.  Returns null if the clipboard is empty or if the
	 * clipboard cannot be pasted to the current selection.
	 *
	 * @param   clipboard	the component on the clipboard.
	 * @return  a Pair with both components defined, or null.
	 */
	private List<Pair<RocketComponent, Integer>> getPastePositions(List<RocketComponent> clipboard) {
		List<Pair<RocketComponent, Integer>> result = new LinkedList<>();
		RocketComponent selected = selectionModel.getSelectedComponent();
		for (RocketComponent component : clipboard) {
			Pair<RocketComponent, Integer> position = getPastePosition(component, selected);
			if (position != null) {
				result.add(position);
			}
		}
		return result;
	}

	
	

	///////  Action classes

	private abstract class RocketAction extends AbstractAction implements ClipboardListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public abstract void clipboardChanged();
	}

	/**
	 * Action to edit the currently selected component.
	 */
	private class EditAction extends RocketAction {
		@Serial
		private static final long serialVersionUID = 1L;

		public EditAction() {
			//// Edit
			this.putValue(NAME, trans.get("RocketActions.EditAct.Edit"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
			this.putValue(ACCELERATOR_KEY, EDIT_KEY_STROKE);
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.EditAct.ttip.Edit"));
			this.putValue(SMALL_ICON, Icons.EDIT_EDIT);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getEditSimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				editComponents();
				// I wouldn't switch to the design tab here, because the user may be editing in the rocket view window
			}
		}

		private void editComponents() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();

			if (components == null || components.isEmpty()) {
				return;
			}

			if (ComponentConfigDialog.isDialogVisible())
				ComponentConfigDialog.disposeDialog();

			RocketComponent component = components.get(0);
			component.clearConfigListeners();
			if (components.size() > 1) {
				for (int i = 1; i < components.size(); i++) {
					RocketComponent listener = components.get(i);
					listener.clearConfigListeners();	// Make sure all the listeners are cleared (should not be possible, but just in case)
					component.addConfigListener(listener);
				}
			}
			ComponentConfigDialog.showDialog(parentFrame, document, component);
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			this.setEnabled(!components.isEmpty() || simulationPanel.getEditSimulationAction().isEnabled());
		}
	}


	
	/**
	 * Action the cuts the selected component (copies to clipboard and deletes).
	 */
	private class CutAction extends RocketAction {
		@Serial
		private static final long serialVersionUID = 1L;

		public CutAction() {
			//// Cut
			this.putValue(NAME, trans.get("RocketActions.CutAction.Cut"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_T);		// Use the 't' in Cut as mnemonic
			this.putValue(ACCELERATOR_KEY, CUT_KEY_STROKE);
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.CutAction.ttip.Cut"));
			this.putValue(SMALL_ICON, Icons.EDIT_CUT);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getCutSimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				cutComponents();
			}
		}

		private void cutComponents() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (!components.isEmpty()) {
				components = new ArrayList<>(components);
				fillInMissingSelections(components);

				components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));
			}

			if (!(isDeletable(components) && isCopyable(components))) {
				return;
			}

			ComponentConfigDialog.disposeDialog();

			if (components.size() == 1) {
				document.addUndoPosition("Cut " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Cut components");
			}

			List<RocketComponent> copiedComponents = new LinkedList<>(copyComponentsMaintainParent(components));
			copiedComponents.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

			OpenRocketClipboard.setClipboard(copiedComponents);
			delete(components);

			parentFrame.selectTab(BasicFrame.DESIGN_TAB);
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			this.setEnabled((isDeletable(components) && isCopyable(components)) ||
					simulationPanel.getCutSimulationAction().isEnabled());
		}
	}



	/**
	 * Action that copies the selected component to the clipboard.
	 */
	private class CopyAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public CopyAction() {
			//// Copy
			this.putValue(NAME, trans.get("RocketActions.CopyAct.Copy"));
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.CopyAct.ttip.Copy"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			this.putValue(ACCELERATOR_KEY, COPY_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_COPY);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getCopySimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				copyComponents();
			}
		}

		private void copyComponents() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (!components.isEmpty()) {
				components = new ArrayList<>(components);
				fillInMissingSelections(components);

				components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));
			}

			if (!isCopyable(components)) {
				return;
			}

			List<RocketComponent> copiedComponents = new LinkedList<>(copyComponentsMaintainParent(components));
			copiedComponents.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

			OpenRocketClipboard.setClipboard(copiedComponents);
			parentFrame.selectTab(BasicFrame.DESIGN_TAB);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isCopyable(selectionModel.getSelectedComponent()) ||
					simulationPanel.getCopySimulationAction().isEnabled());
		}
		
	}



	/**
	 * Action that pastes the current clipboard to the selected position.
	 * It first tries to paste the component to the end of the selected component
	 * as a child, and after that as a sibling after the selected component. 
	 */
	private class PasteAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public PasteAction() {
			//// Paste
			this.putValue(NAME, trans.get("RocketActions.PasteAct.Paste"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_V);
			this.putValue(ACCELERATOR_KEY, PASTE_KEY_STROKE);
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.PasteAct.ttip.Paste"));
			this.putValue(SMALL_ICON, Icons.EDIT_PASTE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getPasteSimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				pasteComponents();
			}
		}

		private void pasteComponents() {
			List<RocketComponent> components = new LinkedList<>(OpenRocketClipboard.getClipboardComponents());

			if (components.isEmpty()) {
				return;
			}
			ComponentConfigDialog.disposeDialog();

			List<RocketComponent> pasted = new LinkedList<>();
			for (RocketComponent component : components) {
				pasted.add(component.copy());
			}

			List<Pair<RocketComponent, Integer>> positions = getPastePositions(pasted);

			if (pasted.size() == 1) {
				document.addUndoPosition("Paste " + pasted.get(0).getComponentName());
			} else {
				document.addUndoPosition("Paste components");
			}

			List<RocketComponent> successfullyPasted = new LinkedList<>();
			for (int i = 0; i < pasted.size(); i++) {
				if (positions.get(i) == null) {
					JOptionPane.showMessageDialog(null,
							String.format(trans.get("RocketActions.PasteAct.invalidPosition.msg"),
									pasted.get(i).getComponentName()),
							trans.get("RocketActions.PasteAct.invalidPosition.title"), JOptionPane.WARNING_MESSAGE);
				} else {
					RocketComponent parent = positions.get(i).getU();
					RocketComponent child = pasted.get(i);
					if (parent != null && parent.isCompatible(child)) {
						parent.addChild(child, positions.get(i).getV());
						successfullyPasted.add(pasted.get(i));
					} else {
						log.warn("Pasted component {} is not compatible with {}", child, parent);
					}
				}
			}

			selectionModel.setSelectedComponents(successfullyPasted);
			parentFrame.selectTab(BasicFrame.DESIGN_TAB);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(!getPastePositions(OpenRocketClipboard.getClipboardComponents()).isEmpty() ||
					simulationPanel.getPasteSimulationAction().isEnabled());
		}
	}



	/**
	 * Action that duplicates the selected component.
	 */
	private class DuplicateAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DuplicateAction() {
			//// Copy
			this.putValue(NAME, trans.get("RocketActions.DuplicateAct.Duplicate"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			this.putValue(ACCELERATOR_KEY, DUPLICATE_KEY_STROKE);
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DuplicateAct.ttip.Duplicate"));
			this.putValue(SMALL_ICON, Icons.EDIT_DUPLICATE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getDuplicateSimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				duplicateComponents();
			}
		}

		private void duplicateComponents() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			List<RocketComponent> topComponents = new LinkedList<>();		// Components without a parent component in <components>
			if (components.size() > 0) {
				components.sort(Comparator.comparing(c -> c.getParent() != null ? c.getParent().getChildPosition(c) : 0));
				components = new ArrayList<>(components);
				fillInMissingSelections(components);
			} else {
				return;
			}
			for (RocketComponent c: components) {
				if (!RocketComponent.listContainsParent(components, c)) {
					topComponents.add(c);
				}
			}

			if (!isCopyable(components)) {
				return;
			}

			if (ComponentConfigDialog.isDialogVisible())
				ComponentConfigDialog.disposeDialog();

			List<RocketComponent> copiedComponents = copyComponentsMaintainParent(components);
			OpenRocketClipboard.setClipboard(copiedComponents);
			copiedComponents = new LinkedList<>(OpenRocketClipboard.getClipboardComponents());

			List<RocketComponent> duplicateComponents = new LinkedList<>();
			for (RocketComponent component : copiedComponents) {
				duplicateComponents.add(component.copy());
			}

			List<Pair<RocketComponent, Integer>> positions = new LinkedList<>();
			for (RocketComponent component : duplicateComponents) {
				Pair<RocketComponent, Integer> pos;
				if (RocketComponent.listContainsParent(duplicateComponents, component)) {
					pos = getPastePosition(component, component.getParent());
				} else {
					int compIdx = duplicateComponents.indexOf(component);
					RocketComponent pasteParent = topComponents.get(compIdx).getParent();
					pos = getPastePosition(component, pasteParent);
				}
				positions.add(pos);
			}

			if (duplicateComponents.size() == 1) {
				document.addUndoPosition("Duplicate " + duplicateComponents.get(0).getComponentName());
			} else {
				document.addUndoPosition("Duplicate components");
			}

			Collections.reverse(duplicateComponents);
			Collections.reverse(positions);

			for (int i = 0; i < duplicateComponents.size(); i++) {
				positions.get(i).getU().addChild(duplicateComponents.get(i), positions.get(i).getV());
			}

			selectionModel.setSelectedComponents(duplicateComponents);
			parentFrame.selectTab(BasicFrame.DESIGN_TAB);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isCopyable(selectionModel.getSelectedComponent()) ||
					simulationPanel.getDuplicateSimulationAction().isEnabled());
		}

	}



	/**
	 * Action that deletes the selected component.
	 */
	private class DeleteAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DeleteAction() {
			//// Delete
			this.putValue(NAME, trans.get("RocketActions.DelAct.Delete"));
			//// Delete the selected component or simulation.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DelAct.ttip.Delete"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_DELETE);
			this.putValue(ACCELERATOR_KEY, DELETE_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				simulationPanel.getDeleteSimulationAction().actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				deleteComponents();
			}
		}

		private void deleteComponents() {
			List<RocketComponent> components = new ArrayList<>(selectionModel.getSelectedComponents());
			if (components.size() == 0) return;
			components.sort(Comparator.comparing(c -> c.getParent().getChildPosition(c)));

			if (components.size() == 1) {
				document.addUndoPosition("Delete " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Delete components");
			}

			for (RocketComponent component : components) {
				deleteComponent(component);
			}

			parentFrame.selectTab(BasicFrame.DESIGN_TAB);
		}

		private void deleteComponent(RocketComponent component) {
			if (isDeletable(component)) {
				ComponentConfigDialog.disposeDialog();

				try {
					component.getRocket().removeComponentChangeListener(ComponentConfigDialog.getDialog());

					delete(component);
				} catch (IllegalStateException ignored) { }
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isDeletable(selectionModel.getSelectedComponent()) ||
					simulationPanel.getDeleteSimulationAction().isEnabled());
		}
	}




	/**
	 * Action to select all components with the same color as the currently selected component.
	 */
	private class SelectSameColorAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public SelectSameColorAction() {
			//// Select same color
			this.putValue(NAME, trans.get("RocketActions.Select.SelectSameColorAct"));
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.Select.SelectSameColorAct.ttip"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (components.isEmpty()) {
				return;
			}

			RocketComponent component = components.get(0);
			List<RocketComponent> sameColorComponents = getComponentsSameColor(component);
			selectionModel.setSelectedComponents(sameColorComponents);
		}

		private List<RocketComponent> getComponentsSameColor(RocketComponent component) {
			List<RocketComponent> components = new ArrayList<>();
			components.add(component);

			for (RocketComponent c : rocket) {
				if (c == component) {
					continue;
				}
				if (isAppearanceEqual(component, c)) {
					components.add(c);
				}
			}

			return components;
		}

		private boolean isAppearanceEqual(RocketComponent component1, RocketComponent component2) {
			Appearance appearance1 = component1.getAppearance();
			Appearance appearance2 = component2.getAppearance();

			// Both components must have the same default material state
			if ((appearance1 == null && appearance2 != null) || (appearance1 != null && appearance2 == null)) {
				return false;
			}

			appearance1 = appearance1 == null ? DefaultAppearance.getDefaultAppearance(component1) : appearance1;
			appearance2 = appearance2 == null ? DefaultAppearance.getDefaultAppearance(component2) : appearance2;

			return isAppearanceEqual(appearance1, appearance2);
		}

		private boolean isAppearanceEqual(Appearance app1, Appearance app2) {
			ORColor color1 = app1.getPaint();
			ORColor color2 = app2.getPaint();

			if (color1 == null && color2 == null) {
				return true;
			}
			if (color1 == null || color2 == null) {
				return false;
			}

			// Add components with the same RGB values (ignore alpha)
			return color1.getRed() == color2.getRed() &&
					color1.getGreen() == color2.getGreen() &&
					color1.getBlue() == color2.getBlue();
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			this.setEnabled(components.size() == 1 && components.get(0).isMassive());
		}
	}

	/**
	 * Action to deselect all currently selected components.
	 */
	private class DeselectAllAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DeselectAllAction() {
			//// Deselect all
			this.putValue(NAME, trans.get("RocketActions.Select.DeselectAllAct"));
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.Select.DeselectAllAct.ttip"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectionModel.clearComponentSelection();
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			this.setEnabled(components.size() > 0);
		}
	}



	/**
	 * Action to scale the currently selected component.
	 */
	private class ScaleAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public ScaleAction() {
			//// Scale
			this.putValue(NAME, trans.get("RocketActions.ScaleAct.Scale"));
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.ScaleAct.ttip.Scale"));
			this.putValue(SMALL_ICON, Icons.EDIT_SCALE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			log.info(Markers.USER_MARKER, "Scale... selected");
			ScaleDialog dialog = new ScaleDialog(document, selectionModel.getSelectedComponents(), null);
			dialog.setVisible(true);
			dialog.dispose();
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(true);
		}
	}
	


	
	/**
	 * Action to move the selected component upwards in the parent's child list.
	 */
	private class MoveUpAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public MoveUpAction() {
			//// Move up
			this.putValue(NAME, trans.get("RocketActions.MoveUpAct.Moveup"));
			this.putValue(SMALL_ICON, Icons.UP);
			//// Move this component upwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveUpAct.ttip.Moveup"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components =  selectionModel.getSelectedComponents();
			if (components.size() == 0) return;
			components = new ArrayList<>(components);
			components.sort(Comparator.comparing(c -> c.getParent() != null ? c.getParent().getChildPosition(c) : 0));

			if (components.size() == 1) {
				document.addUndoPosition("Move " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Move components");
			}

			for (RocketComponent component : components) {
				// Only move top components, don't move its children
				if (!RocketComponent.listContainsParent(components, component)) {
					moveUp(component);
				}
			}
			rocket.fireComponentChangeEvent(ComponentChangeEvent.TREE_CHANGE);
			selectionModel.setSelectedComponents(components);
		}

		private void moveUp(RocketComponent component) {
			if (!canMove(component))
				return;

			ComponentConfigDialog.disposeDialog();

			RocketComponent parent = component.getParent();
			parent.moveChild(component, parent.getChildPosition(component) - 1);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponents()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) > 0)
				return true;
			return false;
		}

		private boolean canMove(List<RocketComponent> components) {
			if (components == null || components.size() == 0)
				return false;

			for (RocketComponent component : components) {
				if (!RocketComponent.listContainsParent(components, component) && !canMove(component))
					return false;
			}
			return true;
		}
	}



	/**
	 * Action to move the selected component down in the parent's child list.
	 */
	private class MoveDownAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public MoveDownAction() {
			//// Move down
			this.putValue(NAME, trans.get("RocketActions.MoveDownAct.Movedown"));
			this.putValue(SMALL_ICON, Icons.DOWN);
			//// Move this component downwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveDownAct.ttip.Movedown"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components =  selectionModel.getSelectedComponents();
			if (components.size() == 0) return;
			components = new ArrayList<>(components);
			components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

			if (components.size() == 1) {
				document.addUndoPosition("Move " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Move components");
			}

			for (RocketComponent component : components) {
				// Only move top components, don't move its children
				if (!RocketComponent.listContainsParent(components, component)) {
					moveDown(component);
				}
			}
			rocket.fireComponentChangeEvent(ComponentChangeEvent.TREE_CHANGE);
			selectionModel.setSelectedComponents(components);
		}

		private void moveDown(RocketComponent component) {
			if (!canMove(component))
				return;

			ComponentConfigDialog.disposeDialog();

			RocketComponent parent = component.getParent();
			parent.moveChild(component, parent.getChildPosition(component) + 1);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponents()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) < parent.getChildCount()-1)
				return true;
			return false;
		}

		private boolean canMove(List<RocketComponent> components) {
			if (components == null || components.size() == 0)
				return false;

			for (RocketComponent component : components) {
				if (!RocketComponent.listContainsParent(components, component) && !canMove(component))
					return false;
			}
			return true;
		}
	}


	private class ExportOBJAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public ExportOBJAction() {
			this.putValue(NAME, trans.get("RocketActions.ExportOBJAct.ExportOBJ"));
			this.putValue(SMALL_ICON, Icons.EXPORT_3D);
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.ExportOBJAct.ttip.ExportOBJ"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (components.isEmpty()) {
				return;
			}

			ComponentConfigDialog.disposeDialog();
			parentFrame.exportWavefrontOBJAction();
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			boolean containsMassiveComponent = containsMassiveComponent(components);
			this.setEnabled(containsMassiveComponent);
		}

		private static boolean containsMassiveComponent(List<RocketComponent> components) {
			for (RocketComponent component : components) {
				if (component.isMassive()) {
					return true;
				}
				for (RocketComponent child : component.getAllChildren()) {
					if (child.isMassive()) {
						return true;
					}
				}
			}
			return false;
		}
	}


	/**
	 * Action to save the current document.
	 */
	private class SaveAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public SaveAction() {
			//// Save
			this.putValue(NAME, "保存模型");
//			this.putValue(MNEMONIC_KEY, KeyEvent.VK_S);
//			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
//					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
//			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.SaveAct.ttip.Save"));
			this.putValue(SMALL_ICON, Icons.FILE_SAVE);
//			clipboardChanged();
		}



		@Override
		public void actionPerformed(ActionEvent e) {
			List<double[]> point3ds = new ArrayList<>();
			List<double[]> point2ds = new ArrayList<>();
			List Finlist = new ArrayList<>();
			List<double[]> Finxlist = new ArrayList<>();
			try {
				ArrayList<Object>  result = serializeObjectToJsonFile(OpenRocketDocumentFactory.mydoc.getRocket().getSelectedConfiguration().getActiveInstances(), "./object.json");

				// System.out.println(result);


				//序列化密集点
				File file = new File("./rocket.txt");
				File file2 = new File("./finset.txt");
				File file3 = new File("./rocket2D.txt");
				if (file.exists()) {
					file.delete();
				}
				if (file2.exists()) {
					file2.delete();
				}
				if (file3.exists()) {
					file3.delete();
					System.out.println("文件3删除");
				}
				List<RocketComponent> allChildren = OpenRocketDocumentFactory.mydoc.getRocket().getAllChildren();
				//待密集组件list
				ArrayList<RocketComponent> components = new ArrayList<>();
				for (RocketComponent component : allChildren) {
					if (component instanceof Transition || component instanceof FinSet || component instanceof BodyTube) {
						components.add(component);
					}
				}
				double beginX = 0;
				// 圆周旋转点的个数
				int rho_point = 180;


				for (RocketComponent c : components) {
					List<double[]> pointi = new ArrayList<>();
					List<double[]> point2di = new ArrayList<>();
					if (c instanceof BodyTube) {
						beginX = bodyTube3D(c.getLength(), ((BodyTube) c).getOuterRadius(), rho_point, 100, beginX);
						pointi = bodyTube3DPoint3d(c.getLength(), ((BodyTube) c).getOuterRadius(), rho_point, 100, beginX);
						point2di = bodyTube3DPoint2d(c.getLength(), ((BodyTube) c).getOuterRadius(), rho_point, 100, beginX);
					}
					if (c instanceof NoseCone) {
						double endX = noseCone3D(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((NoseCone) c).getBaseRadius(), 100, beginX);
						pointi = noseCone3DPoint3d(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((NoseCone) c).getBaseRadius(), 100, beginX);
						point2di = noseCone3DPoint2d(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((NoseCone) c).getBaseRadius(), 100, beginX);
						beginX = endX;
					}
					if (c instanceof Transition && !(c instanceof NoseCone)) {
						double endX = transition3D(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((Transition) c).getForeRadius(), ((Transition) c).getAftRadius(), 100, beginX);
						pointi = transition3DPoint3d(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((Transition) c).getForeRadius(), ((Transition) c).getAftRadius(), 100, beginX);
						point2di = transition3DPoint2d(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((Transition) c).getForeRadius(), ((Transition) c).getAftRadius(), 100, beginX);
						beginX = endX;
					}
					if (c instanceof FinSet) {
						try (FileWriter writer = new FileWriter("./finset.txt", true)) {
							writer.write("--- FIN SET ---\n");
							Coordinate[] finPoints = ((FinSet) c).getFinPoints();
							List<double[]> finpoint = new ArrayList<>();
							Finxlist.add(new double[] {c.getPosition().x});
							for (Coordinate coordinate : finPoints) {
								writer.write(coordinate.x + " " + coordinate.y + " " + coordinate.z + "\n");
								finpoint.add(new double[] {coordinate.x, coordinate.y, coordinate.z});
							}
							Finlist.add(finpoint);
							System.out.println("finset write success");
						} catch (IOException exception) {
							throw new RuntimeException(exception);
						}
					}
					point3ds.addAll(pointi);
					point2ds.addAll(point2di);
				}

			} catch(Exception ex){
				ex.printStackTrace();
			}

			//todo
			//读取当前目录json txt
			//便利Map

			InstanceMap map = OpenRocketDocumentFactory.mydoc.getRocket().getSelectedConfiguration().getActiveInstances();



			// 创建 ObjectMapper 实例
			ObjectMapper mapper = new ObjectMapper();
			// 配置 ObjectMapper 为无转义输出
			mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);

			List<Paraminfo> paraminfoList = ReadNormalFile.readParams(new File("input.txt"));

			File file = new File("./output");
			if (!file.exists()) {
				boolean created = file.mkdirs();
			} else {
				// 删除当前目录下的output文件夹
				String folderPath = "./output";

				// 方法1: 使用NIO API（推荐）
				boolean success = deleteFolder(folderPath);

				if (!success) {
					System.out.println("使用NIO方法删除失败，尝试使用传统方法...");

					// 方法2: 使用传统File API
					File folder = new File(folderPath);
					success = deleteFolderLegacy(folder);

					if (success) {
						System.out.println("传统方法删除成功");
					} else {
						System.out.println("删除失败，请检查文件夹是否存在或是否有足够权限");
					}
				}
				boolean created = file.mkdirs();
			}


			// 先写2d跟3d的结果
			List<Paraminfo> result0 = new ArrayList<>();
			Paraminfo paraminfo = new Paraminfo();
			paraminfo.setName("2Dpoint");
			paraminfo.setType("FLT");
			paraminfo.setSign("%ComArray");
			paraminfo.setColumnNames("x y");
			paraminfo.setValue(doubleListToString(point2ds));
			result0.add(paraminfo);
			Paraminfo paraminfo1 = new Paraminfo();
			paraminfo1.setName("3Dpoint");
			paraminfo1.setType("FLT");
			paraminfo1.setSign("%ComArray");
			paraminfo1.setColumnNames("x y z");
			paraminfo1.setValue(doubleListToString(point3ds));
			result0.add(paraminfo1);
			WriteNormalFile.writeParams(result0, "./output/00points.txt");

			// 然后写翼型
			int i = 0;
			List<Paraminfo> finre = new ArrayList<>();
			for (Object finpointi : Finlist) {
				Paraminfo paraminfo2 = new Paraminfo();
				double[] finxi = Finxlist.get(i);
				String finxi_str = String.format("%06f", finxi[0]);
				paraminfo2.setName(finxi_str);
				paraminfo2.setType("FLT");
				paraminfo2.setSign("%ComArray");
				paraminfo2.setColumnNames("x y z");
				paraminfo2.setValue(doubleListToString((List<double[]>) finpointi));
				i++;
				finre.add(paraminfo2);
//				System.out.println(finpointi);
			}

			WriteNormalFile.writeParams(finre, "./output/00Finset.txt");


			for (RocketComponent keyi : map.keySet()) {

				List<Paraminfo> result = new ArrayList<>();

				try {
					if (!(keyi instanceof Rocket)) {
						// 直接将键序列化为对象形式，避免多层嵌套的字符串
//						System.out.println(keyi);

						Map mapi = mapper.convertValue(keyi, Map.class);
						mapi.put("rocketComponent", keyi.getClass().getSimpleName());
//						System.out.println(mapi);

						for (Object key : mapi.keySet()) {
							Object value = mapi.get(key);
							processValue(key.toString(), value, result);
						}
						Object position = mapi.get("position");
						Map positionM = mapper.convertValue(position, Map.class);
						String x = String.format("%06f", positionM.get("x"));
						String component = keyi.getClass().getSimpleName();

						String filename = "./output/out_" + x + component + ".txt";
						WriteNormalFile.writeParams(result, filename);
					}
				} catch (Exception E) {
					Map mapi = mapper.convertValue(keyi, Map.class);
					E.printStackTrace();
				}

			}
		}

		@Override
		public void clipboardChanged() {
//			// Save action is always enabled when there's a document
//			this.setEnabled(document != null);
		}

		// 使用传统File类的递归删除方法
		public static boolean deleteFolderLegacy(File folder) {
			if (folder == null || !folder.exists()) {
				return false;
			}

			if (!folder.isDirectory()) {
				return folder.delete();
			}

			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolderLegacy(file);
					} else {
						file.delete();
					}
				}
			}

			return folder.delete();
		}

		public static boolean deleteFolder(String folderPath) {
			Path path = Paths.get(folderPath);

			if (!Files.exists(path)) {
				System.out.println("文件夹不存在: " + folderPath);
				return false;
			}

			if (!Files.isDirectory(path)) {
				System.out.println("路径不是文件夹: " + folderPath);
				return false;
			}

			try {
				// 使用Files.walkFileTree递归删除文件夹及其内容
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						System.out.println("正在删除文件: " + file);
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						System.out.println("正在删除文件夹: " + dir);
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});

				System.out.println("文件夹删除成功: " + folderPath);
				return true;
			} catch (IOException e) {
				System.err.println("删除文件夹时出错: " + e.getMessage());
				return false;
			}
		}

		public static String doubleListToString(List<double[]> list) {
			StringBuilder sb = new StringBuilder();

			// 将List<double[]>转换为字符串，每行用换行符分隔，每列用空格分隔
			for (int i = 0; i < list.size(); i++) {
				double[] row = list.get(i);
				for (int j = 0; j < row.length; j++) {
					sb.append(row[j]);
					if (j < row.length - 1) {
						sb.append(" "); // 列之间用空格分隔
					}
				}
				if (i < list.size() - 1) {
					sb.append("\n"); // 行之间用换行符分隔
				}
			}

			return sb.toString();
		}


		// 添加递归处理嵌套Map的辅助方法
		private void processValue(String prefix, Object value, List<Paraminfo> result) {
			if (value instanceof Map) {
				// 如果是Map，递归处理每个键值对
				Map<?, ?> nestedMap = (Map<?, ?>) value;
				for (Object nestedKey : nestedMap.keySet()) {
					Object nestedValue = nestedMap.get(nestedKey);
					String newPrefix = prefix + "_" + nestedKey.toString();
					processValue(newPrefix, nestedValue, result);
				}
			} else if (value instanceof List) {
				// 如果是List，处理每个元素
				List<?> valueList = (List<?>) value;
				for (int i = 0; i < valueList.size(); i++) {
					Object item = valueList.get(i);
					processValue(prefix + "_" + i, item, result);
				}
			} else {
				// 基本类型或简单对象
				Paraminfo valueParam = new Paraminfo();
				valueParam.setName(prefix);
				// 增加分类
				if (value instanceof String) {
					valueParam.setType("STR");
				} else if (value instanceof Boolean) {
					valueParam.setType("STR");
				} else {
					valueParam.setType("FLT");
				}

				valueParam.setSign("%single");
				valueParam.setValue(value != null ? value.toString() : "null");
				result.add(valueParam);
			}
		}

	}

}
