package rocks.inspectit.ui.rcp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the creation of the
 * objects is done piece after piece, so that an immediate visualization can be seen (important for
 * sub-trees which are very large).
 *
 * @author Marius Oehler
 *
 */
public class DeferredJmxPackageComposite extends DeferredComposite {

	/**
	 * All the classes which are being displayed in the sub-tree.
	 */
	private List<JmxDefinitionDataIdent> childJmxData = new CopyOnWriteArrayList<>();

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * This sensor type ident is used to create the sub-tree.
	 */
	private SensorTypeIdent sensorTypeIdent;

	/**
	 * Sets {@link #sensorTypeIdent}.
	 *
	 * @param sensorTypeIdent
	 *            New value for {@link #sensorTypeIdent}
	 */
	public void setSensorTypeIdent(SensorTypeIdent sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite packageComposite = (Composite) object;
			monitor.beginTask("Loading monitored JMX-Objects...", IProgressMonitor.UNKNOWN);

			Map<String, DeferredJmxObjectComposite> typeNames = new HashMap<>(childJmxData.size());

			for (JmxDefinitionDataIdent jmxIdent : childJmxData) {
				String typeName = jmxIdent.getDerivedTypeName();

				// check if the given package was already added.
				if (!typeNames.containsKey(typeName)) {
					DeferredJmxObjectComposite composite = getNewChild();
					composite.setRepositoryDefinition(repositoryDefinition);
					composite.setName(typeName);

					collector.add(composite, monitor);
					packageComposite.addChild(composite);
					typeNames.put(typeName, composite);
				}

				DeferredJmxObjectComposite composite = typeNames.get(typeName);
				composite.setSensorTypeIdent(sensorTypeIdent);
				composite.addJmxDataToDisplay(jmxIdent);

				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			collector.done();
			monitor.done();
		}
	}

	/**
	 * @return Returns the right implementation of the {@link DeferredJmxPackageComposite} to use
	 *         for the child.
	 */
	protected DeferredJmxObjectComposite getNewChild() {
		return new DeferredJmxObjectComposite();
	}

	/**
	 * Adds a {@link JmxDefinitionDataIdent} which will be displayed in this sub-tree.
	 *
	 * @param jmxIdent
	 *            The {@link JmxDefinitionDataIdent} to be displayed.
	 */
	public void addJmxDataToDisplay(JmxDefinitionDataIdent jmxIdent) {
		childJmxData.add(jmxIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getRepositoryDefinition() {
		return repositoryDefinition;
	}
}
