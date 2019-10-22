package synergix.plugin.intellj.wm;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.pom.Navigatable;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import synergix.plugin.intellj.structure.SynergixScreensBuilder;
import synergix.plugin.intellj.structure.SynergixScreensStructure;
import synergix.plugin.intellj.structure.descriptor.SynergixNodeDescriptor;
import synergix.plugin.intellj.structure.node.NavigatableFileNode;

public class SynergixScreensPanel extends SimpleToolWindowPanel implements DataProvider, Disposable {
	private Project myProject;
	private Tree myTree;

	public SynergixScreensPanel(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		super(true, true);
		this.myProject = project;

		toolWindow.setTitle("Building tree...");
		toolWindow.activate(() -> {
			this.buildTree();
			this.buildToolbarAndCreateContent();
			toolWindow.setTitle(null);
		});
	}

	private void buildTree() {
		SynergixScreensBuilder treeBuilder = new SynergixScreensBuilder(this.myProject);
		SynergixScreensStructure myScreenStructure = treeBuilder.getMyTreeStructure();
		final StructureTreeModel treeModel = new StructureTreeModel<>(myScreenStructure, this);
		this.myTree = new Tree(new AsyncTreeModel(treeModel, this));

		this.myTree.setRootVisible(true);
		this.myTree.setShowsRootHandles(true);
		this.myTree.setCellRenderer(new NodeRenderer());

		// set to -1 to disable open leaf when click
		this.myTree.setToggleClickCount(-1);

		// registry event
		EditSourceOnDoubleClickHandler.install(this.myTree);

		// registry search
		Convertor<TreePath, String> TO_STRING = (path) -> path.getLastPathComponent().toString();
		new TreeSpeedSearch(this.myTree, TO_STRING, true);
	}

	private void buildToolbarAndCreateContent() {
		final ActionManager actionManager = ActionManager.getInstance();
		ActionToolbar actionToolbar = actionManager.createActionToolbar("Synergix Screen Navigator Toolbar",
				(DefaultActionGroup) actionManager
						.getAction("Synergix.ScreenNavigtorToolbar"),
				true);

		actionToolbar.setTargetComponent(this.myTree);
		this.setToolbar(actionToolbar.getComponent());
		this.setContent(ScrollPaneFactory.createScrollPane(this.myTree));
	}

	@Override
	public final Object getData(@NotNull final String dataId) {
		if (CommonDataKeys.NAVIGATABLE_ARRAY.is(dataId)) {
			return this.getNavigatables();
		}
		return super.getData(dataId);
	}

	private Navigatable[] getNavigatables() {
		final SynergixNodeDescriptor[] selectedDescriptors = this.getSelectedDescriptors();
		if (selectedDescriptors.length == 0) return null;
		final ArrayList<Navigatable> result = new ArrayList<>();
		for (SynergixNodeDescriptor descriptor : selectedDescriptors) {
			Navigatable navigatable = this.getNavigatable(descriptor);
			if (navigatable != null) {
				result.add(navigatable);
			}
		}
		return result.toArray(new Navigatable[0]);
	}

	private SynergixNodeDescriptor[] getSelectedDescriptors() {
		if (this.myTree == null) {
			return new SynergixNodeDescriptor[0];
		}
		final TreePath[] paths = this.myTree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return new SynergixNodeDescriptor[0];
		}
		final ArrayList<SynergixNodeDescriptor> list = new ArrayList<>(paths.length);
		for (final TreePath path : paths) {
			final Object lastPathComponent = path.getLastPathComponent();
			if (lastPathComponent instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;
				SynergixNodeDescriptor descriptor = this.getDescriptor(node);
				if (descriptor != null) {
					list.add(descriptor);
				}
			}
		}
		return list.toArray(new SynergixNodeDescriptor[0]);
	}

	@Nullable
	protected SynergixNodeDescriptor getDescriptor(DefaultMutableTreeNode node) {
		final Object userObject = node != null ? node.getUserObject() : null;
		if (userObject instanceof SynergixNodeDescriptor) {
			return (SynergixNodeDescriptor) userObject;
		}
		return null;
	}

	private Navigatable getNavigatable(SynergixNodeDescriptor descriptor) {
		Object nodeElement = descriptor.getElement();
		if (nodeElement instanceof NavigatableFileNode) {
			return ((NavigatableFileNode) nodeElement).getNavigatablePsiElement();
		} else return null;
	}

	@Override
	public void dispose() {
		this.myTree = null;
		this.myProject = null;
	}
}
