package ru.codeanalyzer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.internal.editor.MindMapEditor;

import ru.codeanalyzer.core.model.ConnectionTo1C;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IColorManager.TOKENS;
import ru.codeanalyzer.views.ProcCall;
import ru.codeanalyzer.views.core.LineInfo;

@SuppressWarnings("restriction")
public class Utils {

	public static void setStringExtension(ITopic topic, String selectionString) {
		ITopicExtension extension = topic.getExtension(Const.TOPIC_EXTENSION);
		if (extension == null) {
			extension = topic.createExtension(Const.TOPIC_EXTENSION);
		}
		ITopicExtensionElement content = extension.getContent();
		if (content != null) {
			content.setTextContent(selectionString);
		}
	}

	public static String getStringExtension(ITopic topic) {
		String selection = null;
		ITopicExtension extension = topic.getExtension(Const.TOPIC_EXTENSION);
		if (extension != null) {
			ITopicExtensionElement content = extension.getContent();
			if (content != null) {
				selection = content.getTextContent();
			}
		}
		return selection;
	}

	public static void removeStringExtension(ITopic topic) {
		topic.deleteExtension(Const.TOPIC_EXTENSION);
	}
		
	/**
	 * get selected topic via ExecutionEvent
	 */
	public static ITopic getSelectedTopic(ExecutionEvent event)
			throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil
				.getCurrentSelectionChecked(event);
		if (selection.getFirstElement() instanceof ITopic) {
			ITopic topic = (ITopic) selection.getFirstElement();
			return topic;
		}
		return null;
	}

	public static void selectTopic(ITopic topic)
	{
		MindMapEditor editor = (MindMapEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		StructuredSelection sel = new StructuredSelection(topic); 
		editor.setSelection(sel, true, true);
	}
	
	public static void OpenLink(String link) {
		
		try {
			BrowserSupport.getInstance().createBrowser().openURL(link);
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
	}

	private static IPath getPath(Text field) {
		String text = field.getText().trim();
		if (text.length() == 0)
			return null;
		IPath path = new Path(text);
		
		return Utils.getAbsolute(path);
	}
	
	public static void browseForPath(Text field, Shell shell) {
		IPath path = Utils.browseDirectory(getPath(field), shell);
		if (path == null)
			return;
		IPath rootLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if (rootLoc.isPrefixOf(path))
			path = path.setDevice(null).removeFirstSegments(
					rootLoc.segmentCount());
		field.setText(path.toString());
	}
	
	public static void browseForFile(Text field, Shell shell) {
		IPath path = Utils.browseFile(getPath(field), shell, "Выберите файл базы данных", "*.db");
		if (path == null)
			return;
		IPath rootLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if (rootLoc.isPrefixOf(path))
			path = path.setDevice(null).removeFirstSegments(
					rootLoc.segmentCount());
		field.setText(path.toString());
	}
	
	private static IPath browseDirectory(IPath path, Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setText("Выберите каталог");

		if (path != null) {
			dialog.setFilterPath(path.toString());
		}
		String result = dialog.open();
		if (result == null)
			return null;
		return new Path(result);
	}

	public static IPath browseFile(IPath path, Shell shell, String title, String filter_name) {
		FileDialog dialog = new FileDialog(shell);
		dialog.setText(title);
		String[] filter = new String[1];
		filter[0] = filter_name;
		dialog.setFilterExtensions(filter);

		if (path != null) {
			dialog.setFilterPath(path.toString());
		}
		String result = dialog.open();
		if (result == null)
			return null;
		return new Path(result);
	}

	public static String getExtension(File pathname) {
		String extension = "";
		String fileName = pathname.getName();

		int i = fileName.lastIndexOf('.');
		if (i > 0)
			extension = fileName.substring(i + 1);

		return extension;
	}

	public static IPath getAbsolute(IPath path) {
		if (!path.isAbsolute())
			path = ResourcesPlugin.getWorkspace().getRoot().getLocation()
					.append(path);
		return path;
	}

	public static void message(ExecutionEvent event, String msg)
			throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		if (window == null)
			return;

		MessageDialog.openInformation(window.getShell(), Const.STRING_MESSAGE_TITLE, msg);
	}

	public static void proMessage(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return;

		ProMessageDlg.open(window.getShell(), Const.STRING_MESSAGE_TITLE, Const.ERROR_PRO_ACCESS);
	}
	
	public static void message(String msg){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return;

		MessageDialog
				.openInformation(window.getShell(), Const.STRING_MESSAGE_TITLE, msg);
	}
	
	// retrieve the style for a topic. If the topic does not have a
	// style, create one
	public static IStyle getStyle(ITopic topic, IWorkbook workbook) {
		IStyleSheet styleSheet = workbook.getStyleSheet();
		IStyle style = styleSheet.findStyle(topic.getStyleId());
		if (style == null) {
			style = styleSheet.createStyle(IStyle.TOPIC);
			styleSheet.addStyle(style, IStyleSheet.NORMAL_STYLES);
		}
		return style;
	}

	// update the style on the topic, firing off the core event listeners
	// to display the change
	public static void updateStyle(IStyle style, ITopic topic) {
		String oldStyleId = topic.getStyleId() == null ? "" : topic
				.getStyleId();
		if (oldStyleId.equals(style.getId())) {
			if (topic instanceof ICoreEventSource) {
				ICoreEventSource source = (ICoreEventSource) topic;
				ICoreEventSupport coreEventSupport = source
						.getCoreEventSupport();
				coreEventSupport.dispatchValueChange(source, Core.Style, "",
						style.getId());
			} else {
				topic.setStyleId("");
				topic.setStyleId(style.getId());
			}
		} else {
			topic.setStyleId(style.getId());
		}
	}

	@Deprecated
	public static ConnectionTo1C getConnectionTo1C()
	{
		
//		IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
//		
//		ConnectionTo1C con = new ConnectionTo1C();
//		con.exe = store.getString(PreferenceConstants.P_1CEXE).trim();
//		con.directory = store.getString(PreferenceConstants.P_1CDIRCTORY).trim();
//		con.server = store.getString(PreferenceConstants.P_1CSERVER).trim();
//		con.ref = store.getString(PreferenceConstants.P_1CREF).trim();
//		con.login = store.getString(PreferenceConstants.P_1CLOGIN).trim();
//		con.password = store.getString(PreferenceConstants.P_1CPASSWORD).trim();
//		return con;
		return null;
	}
	
	public static void openProcCallView(ArrayList<LineInfo> list) {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;
		IViewPart view = null;

		try {
			if (!pico.get(IEvents.class).activeConfigLoadedCheck())
				return;

			view = page.showView(ProcCall.ID);
			((ProcCall) view).setInput(list);
		
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}

	public static void addCompareRules(List<IRule> rules) {
		
		IColorManager provider = pico.get(IColorManager.class);
//		rules.add(new SingleLineRule(Const.COMPARE_ADDED_MARKER, Const.COMPARE_ADDED_MARKER, provider.getToken(TOKENS.COMPARE_ADDED)));
//		rules.add(new SingleLineRule(Const.COMPARE_CHANGED_MARKER, Const.COMPARE_CHANGED_MARKER, provider.getToken(TOKENS.COMPARE_CHANGED)));
//		rules.add(new SingleLineRule(Const.COMPARE_REMOVED_MARKER, Const.COMPARE_REMOVED_MARKER, provider.getToken(TOKENS.COMPARE_REMOVED)));
		
		rules.add(new EndOfLineRule(Const.COMPARE_ADDED_MARKER, provider.getToken(TOKENS.COMPARE_ADDED)));
		rules.add(new EndOfLineRule(Const.COMPARE_CHANGED_MARKER, provider.getToken(TOKENS.COMPARE_CHANGED)));
		rules.add(new EndOfLineRule(Const.COMPARE_REMOVED_MARKER, provider.getToken(TOKENS.COMPARE_REMOVED)));
		
	}
	
}





























