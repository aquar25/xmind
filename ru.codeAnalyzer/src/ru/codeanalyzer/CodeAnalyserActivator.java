package ru.codeanalyzer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class CodeAnalyserActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.codeAnalyzer"; //$NON-NLS-1$

	//PARTITIONING
	public final static String PARTITIONING = "___my__partitioning____";
//	private DocumentPartitionScanner fPartitionScanner;

//	public DocumentPartitionScanner getMyPartitionScanner() {
//		if (fPartitionScanner == null)
//			fPartitionScanner = new DocumentPartitionScanner();
//		return fPartitionScanner;
//	}

	// The shared instance
	private static CodeAnalyserActivator plugin;
	
	/**
	 * The constructor
	 */
	public CodeAnalyserActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
//		Command command = Utils.getCommand(CommandConstants.loadConfiguration);
//		State state = command.getState(CommandConstants.loadConfigurationState);
//		state.setValue(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

//		plugin.getDefault().getDialogSettings();
//		Command command = Utils.getCommand(CommandConstants.loadConfiguration);		
//		State state = command.getState(CommandConstants.loadConfigurationState);
//		state.setValue(false);		
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CodeAnalyserActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
