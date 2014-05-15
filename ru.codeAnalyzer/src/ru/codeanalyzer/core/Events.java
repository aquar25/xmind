//DONE выделение процедур цветом согласно по типу
//DONE привязка модулей/процерур к идентификатору конфигурации
//DONE построение списка вызываемых/вызывающих
//DONE переход к тексту модуля по Alt+F1

//DONE удалить старое

package ru.codeanalyzer.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.wb.swt.ResourceManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.style.IStyle;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.Editor;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.IEditorFactory;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.IHistory;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.perspectives.EditorPerspective;
import ru.codeanalyzer.preferences.PreferenceConstants;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.utils.Utils;

public class Events implements IEvents {

	HashMap<searchType, searchTypeData> searchTypeCaptions = new HashMap<searchType, searchTypeData>();
	
	
	public Events(){
		searchTypeCaptions.put(
				searchType.meta,
				new searchTypeData(2, "Объект метаданных", ResourceManager
						.getPluginImage("ru.codeAnalyzer",
								"icons/markers/codeanalyzer-search_meta.png"), Const.MARKER_SEARCH_META));
		searchTypeCaptions.put(
				searchType.proc,
				new searchTypeData(1, "Имя процедуры", ResourceManager
						.getPluginImage("ru.codeAnalyzer",
								"icons/markers/codeanalyzer-search_procs.png"), Const.MARKER_SEARCH_PROC));
		searchTypeCaptions.put(
				searchType.text,
				new searchTypeData(0, "Вхождения текста", ResourceManager
						.getPluginImage("ru.codeAnalyzer",
								"icons/markers/codeanalyzer-search_text.png"), Const.MARKER_SEARCH_TEXT));

	}
	
	IDbManager dbManager = pico.get(IDbManager.class);
	IEditorFactory factory = pico.get(IEditorFactory.class); 
	IHistory history = pico.get(IHistory.class);
	IColorManager colorManager = pico.get(IColorManager.class);
	
	@Override
	public searchTypeData searchTypeData(searchType type) {		
		return searchTypeCaptions.get(type);
	}
	
	//EVENTS ************************************************************************************
	
	@Override
	public void search(final searchType type, final String text, final ITopic selectedTopic) {

		if (!activeConfigLoadedCheck()) return;

		if(searchEmptyCheck(text)) return;
		
		try {

			PlatformUI.getWorkbench().getProgressService()

			.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

					IWorkbook workbook = getCurrentWorkbook();
					if(workbook==null) return;
					
					monitor.beginTask(Const.MSG_CONFIG_SEARCH + text,0);
					
					List<BuildInfo> list = dbManager.getActive().search(type, text, monitor); 
					if(buildEmptyCheck(list)) return;
					
					ITopic resultTopic;
					if(selectedTopic == null)
					{
						ITopic rootTopic = workbook.getPrimarySheet().getRootTopic();
					
						resultTopic = workbook.createTopic();
					
						rootTopic.add(resultTopic, ITopic.DETACHED);	
					} else
						resultTopic = clear(true);
					
					selectTopic(resultTopic);
//					resultTopic = clear();
//					selectTopic(resultTopic);
					
					for (IMarkerRef ref : resultTopic.getMarkerRefs())
						resultTopic.removeMarker(ref.getMarkerId());

					searchTypeData data = searchTypeData(type); 
					resultTopic.addMarker(data.markerId);
					//resultTopic.addLabel(data.caption);
					resultTopic.setTitleText(text);
					
					monitor.beginTask(Const.MSG_CONFIG_BUILD, list.size());
					monitor.subTask("");
					createTopicsFromList(resultTopic, topicRole.root, list, monitor);
					
					monitor.done();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void searchByText() {
		if (!activeConfigLoadedCheck()) return;
		
		ITopic topic = getSelectedTopic();
		if(topic==null) return;
		
		search(searchType.text, topic.getTitleText(), topic);
	}
	
	@Override
	public void openModule() {
		if (!activeConfigLoadedCheck())
			return;
		
		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;
		
		// MODULE
		if (!topic.hasMarker(Const.MARKER_MODULE)
				&&!topic.hasMarker(Const.MARKER_PROC)) {
			return;
		}
		
		String _data = Utils.getStringExtension(topic);
		BuildInfo data = BuildInfo.readExtension(_data); 
		
		if (data==null) return;
		
		factory.openEditor(data);
		
	}
	
	@Override
	public void callHierarchy() {
		if (!activeConfigLoadedCheck())
			return;

		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;

		 setPageDurty(topic);
		 
		// PROCEDURE		
		if (topic.hasMarker(Const.MARKER_PROC))
		{
			build(topic, topicRole.hierarchy);
			return;
		}
		
	}
	
	@Override
	public void paramList() {
		if (!activeConfigLoadedCheck())
			return;

		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;

		 setPageDurty(topic);
		 
		// PROCEDURE		
		if (topic.hasMarker(Const.MARKER_PROC))
		{
			build(topic, topicRole.param_list);
			return;
		}
		
	}
	
	@Override
	public ITopic clear(boolean detached) {
		
		if (!activeConfigLoadedCheck()) return null;
		
		final ITopic rootTopic = getSelectedTopic();
		if (rootTopic == null) return null;

		setPageDurty(rootTopic);
		 
		
		ITopic parent = rootTopic.getParent();
		if (parent != null) {

			IWorkbook workbook = rootTopic.getOwnedWorkbook();
			ITopic topic = workbook.createTopic();
			topic.setTitleText(rootTopic.getTitleText());
			for (String label : rootTopic.getLabels())
				topic.addLabel(label);
			for (IMarkerRef ref : rootTopic.getMarkerRefs())
				topic.addMarker(ref.getMarkerId());
			Utils.setStringExtension(topic, Utils.getStringExtension(rootTopic));			

			int index = rootTopic.getIndex();
			parent.remove(rootTopic);
			
			parent.add(topic, index, detached ? ITopic.DETACHED : ITopic.ATTACHED);

			selectTopic(topic);
			
			return topic;

		} else {
			
			List<ITopic> list = rootTopic.getAllChildren();
			for (ITopic child : list)
				rootTopic.remove(child);
			
			return rootTopic;
//			try {
//				PlatformUI.getWorkbench().getProgressService()
//						.run(false, false, new IRunnableWithProgress() {
//							public void run(IProgressMonitor monitor)
//									throws InvocationTargetException,
//									InterruptedException {
//
//								List<ITopic> list = rootTopic.getAllChildren();
//									monitor.beginTask(Const.MSG_CONFIG_CLEAR,
//										list.size());
//								for (ITopic child : list) {
//									rootTopic.remove(child);
//										monitor.worked(1);
//								}
//								monitor.done();
//							}
//						});
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
	
	@Override
	public void start() {
		
		if (!activeConfigLoadedCheck()) return;
		
		IWorkbook workbook = getCurrentWorkbook();
		if(workbook==null) return;
		
		ITopic topic = workbook.getPrimarySheet().getRootTopic();
		topic.addMarker(Const.MARKER_ROOT);
		selectTopic(topic);
		build();
		
	}
	
	@Override
	public void openPerspective() {
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			PlatformUI.getWorkbench().showPerspective(EditorPerspective.ID, window);
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}		
	}
	
	//SERVICE ************************************************************************************
	
	private void selectTopic(ITopic topic)
	{
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IEditorPart editor = page.getActiveEditor();
            if (editor != null && editor instanceof IGraphicalEditor) {
            	IGraphicalEditor ge = (IGraphicalEditor) editor;
            	IGraphicalEditorPage gp = ge.getActivePageInstance();                
            	StructuredSelection selection = new StructuredSelection(topic);
            	gp.getSelectionProvider().setSelection(selection);                
            }
        }		
	}
		
	private ITopic getSelectedTopic() {
		
		ITopic result = null;
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IEditorPart editor = page.getActiveEditor();
            if (editor != null && editor instanceof IGraphicalEditor) {
            	IGraphicalEditor ge = (IGraphicalEditor) editor;
            	IGraphicalEditorPage gp = ge.getActivePageInstance();                	
                StructuredSelection selection = (StructuredSelection) gp.getSelectionProvider().getSelection();
                if (selection != null)
					if (selection.getFirstElement() instanceof ITopic) {
						result = ((ITopic) selection.getFirstElement());
					}
            }
        }
        
		if (result == null) 
			MessageDialog.openInformation(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не выделено ни одной темы");
				
		return result;
	}
	
	private void setPageDurty(ITopic topic) {
		
		 IWorkbookRef wr = MindMapUI.getWorkbookRefManager().findRef(
                 topic.getOwnedWorkbook());
         if (wr != null) {
             ICommandStack cs = wr.getCommandStack();
             if (cs != null) {
                 Command cmd = new Command();
                 cs.execute(cmd);                 
             }
         }
	}

	private IWorkbook getCurrentWorkbook() {
		
		IWorkbook result = null;
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
		IEditorReference[] editorReferences = page.getEditorReferences();
		
		for (int i = editorReferences.length-1; i >= 0; i--) {			
			if (editorReferences[i].getId().equalsIgnoreCase("org.xmind.ui.MindMapEditor")) {
				IGraphicalEditor ge = (IGraphicalEditor) editorReferences[i].getEditor(true);
				page.activate(ge.getSite().getPart());
				IGraphicalEditorPage gp = ge.getActivePageInstance();                
            	ISheet sheet = (ISheet) gp.getViewer().getAdapter(ISheet.class);
    	        if (sheet == null) return null;
            	result = sheet.getOwnedWorkbook();
            	break;
			}
		}
		
//		if (page != null) {
//            IEditorPart editor = page.getActiveEditor();
//            if (editor != null && editor instanceof IGraphicalEditor) {
//            	IGraphicalEditor ge = (IGraphicalEditor) editor;
//            	IGraphicalEditorPage gp = ge.getActivePageInstance();                
//            	ISheet sheet = (ISheet) gp.getViewer().getAdapter(ISheet.class);
//    	        if (sheet == null) return null;
//            	result = sheet.getOwnedWorkbook();
//            }
//        }
        
		if (result == null) 
			MessageDialog.openInformation(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не найден текущий документ");
				
		return result;
	}
	
	//CHECKS ************************************************************************************
	
	private boolean searchEmptyCheck(String text)
	{
		if (text.isEmpty()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "На задана строка для поиска");
			return true;
		}
		
		return false;
	}
	
	private boolean buildEmptyCheck(List<BuildInfo> result)
	{
		if (result.isEmpty()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Поиск", "Ничего не найдено");
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean activeConfigLoadedCheck()
	{
		IDb db = dbManager.getActive(); 
		if(db==null){
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не установлена активная конфигурация");
			return false;
		}
		if (!db.isLoaded()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Активная конфигурация не загружена");
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean nonActiveConfigLoadedCheck()
	{
		IDb db = dbManager.getNonActive(); 
		if(db==null){			
			return false;
		}
		if (!db.isLoaded()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не загружена вторая конфигурация");
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean bothConfigLoadedCheck()
	{
		IDb db1 = dbManager.getActive(); 
		IDb db2 = dbManager.getNonActive();
		
		if (db1==null||db2==null)
			return false;
		
		if (!db1.isLoaded()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не загружена первая конфигурация");
			return false;
		}
		if (!db2.isLoaded()) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ошибка выполнения операции", "Не загружена вторая конфигурация");
			return false;
		}
		
		return true;
	}
	
	//CREATE TOPICS ************************************************************************************
	
	private void createTopicsFromList(ITopic topic, topicRole role, List<BuildInfo> list, IProgressMonitor monitor)
	{
		if (list.isEmpty())
			return;
		IWorkbook workbook = topic.getOwnedWorkbook();
		
		if(role==topicRole.root)
			topic.setFolded(true);

		HashMap<String, ITopic> group1 = new HashMap<String, ITopic>();
		HashMap<String, ITopic> group2 = new HashMap<String, ITopic>();
		HashMap<Integer, ITopic> module = new HashMap<Integer, ITopic>();
		HashMap<Integer, ITopic> proc = new HashMap<Integer, ITopic>();
		 
		ITopic t, t1;

		for (BuildInfo item : list)
		{
			String group2key = item.group1 + "." + item.group2;
			if (!group1.containsKey(item.group1)) {				
				if (role != topicRole.root)
					t = topic;
				else{
					t = workbook.createTopic();
					topic.add(t, ITopic.ATTACHED);					
					t.addMarker(Const.MARKER_OBJECT);
					t.setTitleText(item.group1);
					t.setFolded(true);
				}								
				if (role == topicRole.group1)
					t.setTitleText(item.group1);
					
				group1.put(item.group1, t);				
			}
			
			if (!group2.containsKey(group2key)) {				
				if (role != topicRole.root &&
						role != topicRole.group1)
					t = topic;
				else
				{
					t = workbook.createTopic();
					t.setTitleText(item.group2);
					t1 = group1.get(item.group1);
					t1.add(t, ITopic.ATTACHED);
				}
				if (role == topicRole.group2)
					t.setTitleText(item.group2);
				
				group2.put(group2key, t);
			}
			
			if ((item.module!=0)&&!module.containsKey(item.module)) {				
				if (role == topicRole.module)
					t = topic;
				else
				{
					t = workbook.createTopic();
					t.setTitleText(item.module_title);		
					t1 = group2.get(group2key);
					t1.add(t, ITopic.ATTACHED);					
					t.addMarker(Const.MARKER_MODULE);
					Utils.setStringExtension(t, item.buildExtension());
				}
				
				module.put(item.module, t);
				
			}
						
			if ((item.id!=0)&&!proc.containsKey(item.id)) {
				
				t = workbook.createTopic();
				
				t.setTitleText(item.title);				
				t1 = module.get(item.module);
				t1.add(t, ITopic.ATTACHED);
				t.addMarker(Const.MARKER_PROC);
				proc.put(item.id, t);
				Utils.setStringExtension(t, item.buildExtension());
				
				String color = colorManager.toTopicString(colorManager.getStandartProcedureColor(item.name));
				if (color != null)
				{
					IStyle style = Utils.getStyle(t, workbook);
					style.setProperty(Styles.TextColor, color);
					Utils.updateStyle(style, t);
				}
				
			}
			
			if (monitor != null)
			{
				if(monitor.isCanceled())
					break;					
				monitor.worked(1);
			}
		}
		
	}

	private void createTextTopicsFromList(ITopic topic, List<String> list) {
		if (list.isEmpty())
			return;
		IWorkbook workbook = topic.getOwnedWorkbook();
//		ITopic topic = workbook.createTopic();
//		topic.setTitleText(root_name);
//		topic.setFolded(false);
//		root.add(topic, ITopic.ATTACHED);

		ITopic t;

		for (String item : list) {
			t = workbook.createTopic();
			t.setTitleText(item);
			topic.add(t, ITopic.ATTACHED);
		}

	}

	//HIERARCHY ************************************************************************************

	@Override
	public List<BuildInfo> getCalled(BuildInfo data){
		
		List<BuildInfo> list = new ArrayList<BuildInfo>();
		
		if (!activeConfigLoadedCheck()) return list;
		
		dbManager.getActive().getCalled(list, data);
			
		return list;
	}

	@Override
	public List<BuildInfo> getCalls(final BuildInfo data, final boolean callsInObject) {
		
		final List<BuildInfo> list = new ArrayList<BuildInfo>();
		
		if (!activeConfigLoadedCheck()) return list;
		
		
		try {

			PlatformUI.getWorkbench().getProgressService().run(false, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
			try {
		
				dbManager.getActive().getCalls(list, data, callsInObject, monitor);
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				monitor.done();
			}}});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;

	}

	//EDITOR ************************************************************************************
	
	//DONE переход Печать(), распоряжение на разгрузку из формы в модуль
	//DONE показывать вызывающие только из текущего модуля, новый пункт
	@Override
	public void goToProcedure(String line, BuildInfo context) {
		
		List<BuildInfo> list = new ArrayList<BuildInfo>();
		
		if (!activeConfigLoadedCheck()) return;
		
		dbManager.getActive().getProcsInLine(list, line, context);
		
		switch (list.size()) {
		case 0:
			
			break;

		case 1:			
			factory.openEditor(list.get(0));
			break;
			
		default:
			//DONE переход к определению процедуры: вывод списка найденных процедур в outline
			Utils.openProcCallView(BuildInfo.toLineInfo(list, null));

			break;
		}				
		
	}
	
	@Override
	public void back() {
		
		BuildInfo data = history.getPrev();
		if (data!=null)
			factory.openEditorWithOutHistory(data);
		
	}

	@Override
	public void next() {
		BuildInfo data = history.getNext();
		if (data!=null)
			factory.openEditorWithOutHistory(data);
		
	}

	//BUILD ************************************************************************************
	
	@Override
	public void build() {
	
		if (!activeConfigLoadedCheck())
			return;
	
		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;
	
		if (topic.hasChildren(ITopic.ATTACHED)&&topic.isFolded())
		{
			topic.setFolded(false);
			return;
		}
		
		// SEARCH
		for (searchType type : searchType.values()) {
			searchTypeData data = searchTypeData(type);
			if (topic.hasMarker(data.markerId)) {
				setPageDurty(topic);
				search(type, topic.getTitleText(), topic);
				return;
			}
		}
	
		// ROOT
		if (topic.hasMarker(Const.MARKER_ROOT)) {
			setPageDurty(topic);
			build(topic, topicRole.root);
			return;
		}
				
		// OBJECT
		if (topic.hasMarker(Const.MARKER_OBJECT))
		{
			setPageDurty(topic);
			build(topic, topicRole.group1);
			return;
		}
		
		// MODULE		
		if (topic.hasMarker(Const.MARKER_MODULE))
		{
			setPageDurty(topic);
			build(topic, topicRole.module);
			return;
		}
		
		// PROCEDURE		
		if (topic.hasMarker(Const.MARKER_PROC))
		{
			build(topic, topicRole.proc);
			return;
		}
		
		// OBJECT 2
		ITopic parent = topic.getParent();
		if (parent!=null && parent.hasMarker(Const.MARKER_OBJECT))
		{
			setPageDurty(topic);
			build(topic, topicRole.group2);
			return;
		}
		
		setPageDurty(topic);
		topic.addMarker(Const.MARKER_SEARCH_META);
		search(searchType.meta, topic.getTitleText(), topic);
		
	}

	private void build(final ITopic topic, final topicRole role)
	{
		try {

			PlatformUI.getWorkbench().getProgressService().run(false, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
			try {
					
				
			monitor.beginTask(Const.MSG_CONFIG_QUERY, 0);

			List<BuildInfo> list = new ArrayList<BuildInfo>();
			topicRole _role = role;
			String _data = null;
			BuildInfo data = null;
			ITopic _topic = topic;
			switch (role) {
			case group1:

				_topic = clear(false);
				dbManager.getActive().buildObject(list, topic.getTitleText());
				break;

			case group2:

				_topic = clear(false);
				if (dbManager.getActive().buildObject(list,
						_topic.getParent().getTitleText(), _topic.getTitleText()))
					_role = topicRole.root;
				break;

			case module:

				// DONE: открываем модуль по связке объектов или по расширению
				// топика

				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data);	
				
				if (data==null) return;
				
				_topic = clear(false);
				dbManager.getActive().buildModule(list, data);		
				
				break;

			case proc:
				
				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data); 
				
				if (data==null) return;
				
				factory.openEditor(data);
				
				return;
				
			case hierarchy:
				
				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data); 
				
				if (data==null) return;
				
				_topic = clear(false);
				
				IWorkbook workbook = _topic.getOwnedWorkbook();
				ITopic new_topic;
				_role = topicRole.root;
				
				new_topic = workbook.createTopic();
				new_topic.setTitleText(Const.STRING_CALLS_LIST_NAME);
//				new_topic.setFolded(true);
				_topic.add(new_topic, ITopic.ATTACHED);
				
				List<BuildInfo> callsList = new ArrayList<BuildInfo>();
				dbManager.getActive().getCalls(callsList, data, false, monitor);
				
				monitor.beginTask(Const.MSG_CONFIG_BUILD, callsList.size());
				createTopicsFromList(new_topic, _role, callsList, monitor);
				
				new_topic = workbook.createTopic();
				new_topic.setTitleText(Const.STRING_CALLED_LIST_NAME);
//				new_topic.setFolded(true);
				_topic.add(new_topic, ITopic.ATTACHED);
				
				List<BuildInfo> calledList = new ArrayList<BuildInfo>();
				dbManager.getActive().getCalled(calledList, data);
							
				monitor.beginTask(Const.MSG_CONFIG_BUILD, calledList.size());
				createTopicsFromList(new_topic, _role, calledList, monitor);
				
				return;
			
			case param_list:
				
				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data); 
				
				if (data==null) return;
				
				_topic = clear(false);
				List<String> paramsList = new ArrayList<String>();
				dbManager.getActive().buildParamsList(paramsList, data);		
				
				createTextTopicsFromList(_topic, paramsList);
				
				return;
				
			default:
				_topic = clear(false);
				dbManager.getActive().buildObject(list);
				break;
			}

			if (buildEmptyCheck(list))
				return;

			 monitor.beginTask(Const.MSG_CONFIG_BUILD, list.size());

			 createTopicsFromList(_topic, _role, list, monitor);
			 
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				monitor.done();
			}}});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//COMPARE ************************************************************************************
	
	@Override
	public void compare() {
		
		if (!bothConfigLoadedCheck())
			return;

		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;

		setPageDurty(topic);

		//DONE сравнение конфигураций
		
		// ROOT
		if (topic.hasMarker(Const.MARKER_ROOT)) {
			compare(topic, topicRole.root);
			return;
		}

		// OBJECT
		if (topic.hasMarker(Const.MARKER_OBJECT)) {
			compare(topic, topicRole.group1);
			return;
		}

		ITopic parent = topic.getParent();
		if (parent != null && parent.hasMarker(Const.MARKER_OBJECT)) {
			compare(topic, topicRole.group2);
			return;
		}

		// MODULE
		if (topic.hasMarker(Const.MARKER_MODULE)) {
			compare(topic, topicRole.module);
			return;
		}

		// PROCEDURE
		if (topic.hasMarker(Const.MARKER_PROC)) {
			compare(topic, topicRole.proc);
			return;
		}

		topic.addMarker(Const.MARKER_ROOT);
		compare(topic, topicRole.root);
	}

	private void compare(final ITopic topic, final topicRole role) {
		try {

			PlatformUI.getWorkbench().getProgressService().run(false, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
				
			try {
			
			IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
				
			monitor.beginTask(Const.COMPARE_WORK, 0);
			monitor.subTask("");
			
			CompareResults compareResults = new CompareResults();
			topicRole _role = role;
			ITopic _topic = topic;
			String _data = null;
			BuildInfo data = null;
			switch (role) {
			case group1:

				_topic = clear(false);
				
				_role = topicRole.root;
				
				dbManager.getActive().compareObjects(dbManager.getNonActive(),
						compareResults, topic.getTitleText(), monitor);
				
				break;

			case group2:

				_topic = clear(false);

				//_role = topicRole.root;

				dbManager.getActive().compareObjects(dbManager.getNonActive(),
						compareResults, _topic.getParent().getTitleText(),
						_topic.getTitleText(), monitor);

				break;

			case module:

				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data);

				if (data == null)
					return;

				_topic = clear(false);

				dbManager.getActive().compareModules(dbManager.getNonActive(),
						compareResults, data, monitor);

				break;

			case proc:

				_data = Utils.getStringExtension(topic);
				data = BuildInfo.readExtension(_data);
				if (data == null)
					return;

				factory.closeEditors();
				
				data.onlyProc = true;
				data.compare = true;
//				Editor editorSource = (Editor)
				factory.openEditor(data);
				dbManager.setActive(dbManager.getNonActive().getId());
				data = new BuildInfo(data);
				data.compare = true;
				Editor editor = (Editor)factory.openEditor(data);
				editor.splitEditorArea(true);
				dbManager.setActive(dbManager.getNonActive().getId());
//				editorSource.setLink(editor);
				return;

			default:
				
				_topic = clear(false);
				
				dbManager.getActive().compareObjects(dbManager.getNonActive(), compareResults, monitor);
				
				break;
			}

			IWorkbook workbook = topic.getOwnedWorkbook();
			ITopic new_topic;

			monitor.subTask("");
			
			if (!store.getBoolean(PreferenceConstants.DONOT_SHOW_EQUALENT_IN_COMPARE) && !compareResults.equals.isEmpty()) {
				monitor.beginTask(Const.MSG_CONFIG_BUILD, compareResults.equals.size());
				new_topic = workbook.createTopic();
				_topic.add(new_topic, ITopic.ATTACHED);
				createTopicsFromList(new_topic, _role, compareResults.equals, monitor);
				new_topic.setTitleText(Const.COMPARE_EQUALS);
				new_topic.setFolded(true);
			}
			if (!compareResults.added.isEmpty()) {
				monitor.beginTask(Const.MSG_CONFIG_BUILD, compareResults.added.size());
				new_topic = workbook.createTopic();
				_topic.add(new_topic, ITopic.ATTACHED);
				createTopicsFromList(new_topic, _role, compareResults.added, monitor);
				new_topic.setTitleText(Const.COMPARE_ADDED);
				new_topic.setFolded(true);
			}
			if (!compareResults.removed.isEmpty()) {
				monitor.beginTask(Const.MSG_CONFIG_BUILD, compareResults.removed.size());
				new_topic = workbook.createTopic();
				_topic.add(new_topic, ITopic.ATTACHED);
				createTopicsFromList(new_topic, _role, compareResults.removed, monitor);
				new_topic.setTitleText(Const.COMPARE_REMOVED);
				new_topic.setFolded(true);				

			}
			if (!compareResults.changed.isEmpty()) {
				monitor.beginTask(Const.MSG_CONFIG_BUILD, compareResults.changed.size());
				new_topic = workbook.createTopic();
				_topic.add(new_topic, ITopic.ATTACHED);
				createTopicsFromList(new_topic, _role, compareResults.changed, monitor);
				new_topic.setTitleText(Const.COMPARE_CHANGED);
				new_topic.setFolded(true);

			}

			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				monitor.done();
			}}});
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		
	}

	@Override
	public void makeTopicWithProcLink(BuildInfo data) {
		
		IWorkbook workbook = getCurrentWorkbook();
		if(workbook==null) return;
		
		ITopic rootTopic = workbook.getPrimarySheet().getRootTopic();
		setPageDurty(rootTopic);
		
		ITopic topic = workbook.createTopic();
		topic.addMarker(Const.MARKER_PROC);
		topic.setTitleText(data.title);
		Utils.setStringExtension(topic, data.buildExtension());
		rootTopic.add(topic, ITopic.DETACHED);
		selectTopic(topic);
		
	}

	@Override
	public void toggleTopic() {
		final ITopic topic = getSelectedTopic();
		if (topic == null)
			return;
		
		topic.setFolded(!topic.isFolded());
		
	}

}
